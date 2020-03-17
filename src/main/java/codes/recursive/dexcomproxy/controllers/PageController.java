package codes.recursive.dexcomproxy.controllers;

import codes.recursive.dexcomproxy.client.DexcomShareClient;
import codes.recursive.dexcomproxy.model.AuthRequest;
import codes.recursive.dexcomproxy.model.TokenRequest;
import codes.recursive.dexcomproxy.services.DataStore;
import codes.recursive.dexcomproxy.services.DexcomShareAuthenticationProvider;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.*;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.security.token.jwt.generator.AccessRefreshTokenGenerator;
import io.micronaut.security.token.jwt.render.AccessRefreshToken;
import io.micronaut.security.token.jwt.validator.JwtTokenValidator;
import io.micronaut.security.token.validator.TokenValidator;
import io.micronaut.views.ModelAndView;
import io.micronaut.views.View;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Secured(SecurityRule.IS_ANONYMOUS)
@Controller("/")
public class PageController {

    private final DexcomShareClient dexcomShareClient;
    private final DataStore dataStore;
    private final DexcomShareAuthenticationProvider authenticationProvider;
    private final AccessRefreshTokenGenerator accessRefreshTokenGenerator;
    private final TokenValidator tokenValidator;
    private final String clientId;
    private final String clientSecret;

    public PageController(
            DexcomShareClient dexcomShareClient,
            DataStore dataStore,
            DexcomShareAuthenticationProvider authenticationProvider,
            AccessRefreshTokenGenerator accessRefreshTokenGenerator,
            JwtTokenValidator tokenValidator,
            @Property(name = "codes.recursive.client-id") String clientId,
            @Property(name = "codes.recursive.client-secret") String clientSecret
    ) {
        this.dexcomShareClient = dexcomShareClient;
        this.dataStore = dataStore;
        this.authenticationProvider = authenticationProvider;
        this.accessRefreshTokenGenerator = accessRefreshTokenGenerator;
        this.tokenValidator = tokenValidator;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Get("/auth")
    public HttpResponse auth(
            @QueryValue(value = "redirect_uri") String redirectUri,
            @QueryValue(value = "client_id") String requestClientId,
            @QueryValue(value = "state") String state
    ) {
        if( !requestClientId.equals(clientId) ) {
            return HttpResponse.status(HttpStatus.UNAUTHORIZED).body(
                    new ModelAndView("unauthorized", Map.of())
            );
        }
        return HttpResponse.ok(
                new ModelAndView("auth", Map.of("redirect_uri", redirectUri, "state", state) )
        );
    }

    @Post("/auth")
    @View("/auth")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public HttpResponse authSubmit(@Body AuthRequest authRequest) throws IOException {

        UsernamePasswordCredentials usernamePasswordCredentials = new UsernamePasswordCredentials(authRequest.getAccountName(), authRequest.getPassword());
        Publisher<AuthenticationResponse> authenticationResponsePublisher = authenticationProvider.authenticate(usernamePasswordCredentials);
        AuthenticationResponse authenticationResponse = Flowable.fromPublisher(authenticationResponsePublisher).blockingFirst();

        if( authenticationResponse instanceof AuthenticationFailed ) {
            return HttpResponse.ok(
                    Map.of(
                            "redirect_uri", authRequest.getRedirectUri(),
                            "auth_failed", "failed",
                            "state", authRequest.getState()
                    )
            );
        }
        else {
            if (authenticationResponse.isAuthenticated()) {
                UserDetails userDetails = (UserDetails) authenticationResponse;
                Optional<AccessRefreshToken> token = accessRefreshTokenGenerator.generate(userDetails);
                if( token.isPresent() ) {
                    AccessRefreshToken accessRefreshToken = token.get();
                    String authCode = UUID.randomUUID().toString().replace("-", "");
                    Map<String, Object> store = Map.of("username", authRequest.getAccountName(), "token", accessRefreshToken);
                    dataStore.authCodes.put(authCode, store);
                    String url = authRequest.getRedirectUri()
                            + "?state=" + authRequest.getState()
                            + "&code="+ authCode;
                    return HttpResponse.redirect(URI.create(url));
                }
            }
        }
        return HttpResponse.ok(
                Map.of(
                        "redirect_uri", authRequest.getRedirectUri(),
                        "auth_failed", "failed",
                        "state", authRequest.getState()
                )
        );
    }

    @Post("/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse token(@Body TokenRequest tokenRequest) throws IOException {

        if( !clientId.equals(this.clientId) || !clientSecret.equals(this.clientSecret) ) {
            return HttpResponse.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("invalid_credentials", true)
            );
        }
        AccessRefreshToken token = new AccessRefreshToken();
        if( tokenRequest.getCode() != null ) {
            Map<String, Object> store = dataStore.authCodes.get(tokenRequest.getCode());
            token = (AccessRefreshToken) store.get("token");
        }
        if( tokenRequest.getRefreshToken() != null ) {
            Flowable<Authentication> authenticationFlowable = Flowable.fromPublisher(tokenValidator.validateToken(tokenRequest.getRefreshToken()));
            Authentication authentication = authenticationFlowable.blockingFirst();
            Optional<AccessRefreshToken> tokenOptional = accessRefreshTokenGenerator.generate(tokenRequest.getRefreshToken(), authentication.getAttributes());
            token = tokenOptional.get();
        }
        return HttpResponse.ok(
                Map.of(
                        "access_token", token.getAccessToken(),
                        "expires_in", token.getExpiresIn(),
                        "refresh_token", token.getRefreshToken(),
                        "token_type", "bearer"
                )
        );
    }
}
