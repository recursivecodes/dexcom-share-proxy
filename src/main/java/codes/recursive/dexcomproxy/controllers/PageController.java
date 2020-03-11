package codes.recursive.dexcomproxy.controllers;

import codes.recursive.dexcomproxy.client.DexcomShareClient;
import codes.recursive.dexcomproxy.model.AuthResponse;
import codes.recursive.dexcomproxy.model.User;
import codes.recursive.dexcomproxy.services.DataStore;
import codes.recursive.dexcomproxy.services.DexcomShareAuthenticationProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.*;
import io.micronaut.security.event.LoginSuccessfulEvent;
import io.micronaut.security.handlers.LoginHandler;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.security.token.jwt.generator.AccessRefreshTokenGenerator;
import io.micronaut.security.token.jwt.render.AccessRefreshToken;
import io.micronaut.views.ModelAndView;
import io.micronaut.views.View;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;

import javax.annotation.Nullable;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
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
    private final String clientId;
    private final String clientSecret;

    public PageController(
            DexcomShareClient dexcomShareClient,
            DataStore dataStore,
            DexcomShareAuthenticationProvider authenticationProvider,
            AccessRefreshTokenGenerator accessRefreshTokenGenerator,
            @Property(name = "codes.recursive.client-id") String clientId,
            @Property(name = "codes.recursive.client-secret") String clientSecret
    ) {
        this.dexcomShareClient = dexcomShareClient;
        this.dataStore = dataStore;
        this.authenticationProvider = authenticationProvider;
        this.accessRefreshTokenGenerator = accessRefreshTokenGenerator;
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
    public HttpResponse authSubmit(String accountName, String password, String redirect_uri, String state) throws IOException {

        UsernamePasswordCredentials usernamePasswordCredentials = new UsernamePasswordCredentials(accountName, password);
        Publisher<AuthenticationResponse> authenticationResponsePublisher = authenticationProvider.authenticate(usernamePasswordCredentials);
        AuthenticationResponse authenticationResponse = Flowable.fromPublisher(authenticationResponsePublisher).blockingFirst();

        if( authenticationResponse instanceof AuthenticationFailed ) {
            return HttpResponse.ok(
                    Map.of(
                            "redirect_uri", redirect_uri,
                            "auth_failed", "failed",
                            "state", state
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
                    Map<String, Object> store = Map.of("username", accountName, "token", accessRefreshToken);
                    dataStore.authCodes.put(authCode, store);
                    String url = redirect_uri
                            + "?state=" + state
                            + "&code="+ authCode;
                    return HttpResponse.redirect(URI.create(url));
                }
            }
        }
        return HttpResponse.ok(
                Map.of(
                        "redirect_uri", redirect_uri,
                        "auth_failed", "failed",
                        "state", state
                )
        );
    }

    @Post("/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse token(
            @Nullable String code,
            String grant_type,
            String client_id,
            String client_secret,
            @Nullable String refresh_token) throws IOException {

        if( !client_id.equals(this.clientId) || !client_secret.equals(this.clientSecret) ) {
            return HttpResponse.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("invalid_credentials", true)
            );
        }

        Map<String, Object> store = dataStore.authCodes.get(code);
        AccessRefreshToken token = (AccessRefreshToken) store.get("token");
        String username = (String) store.get("username");
        String sessionId = dataStore.sessionIds.get(username);

        if( refresh_token != null ) {
            Optional<AccessRefreshToken> tokenOptional = accessRefreshTokenGenerator.generate(refresh_token, new HashMap<String, Object>());
            token = tokenOptional.get();
            store.put("token", token);
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
