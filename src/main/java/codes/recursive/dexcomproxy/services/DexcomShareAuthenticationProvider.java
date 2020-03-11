package codes.recursive.dexcomproxy.services;

import codes.recursive.dexcomproxy.client.DexcomShareClient;
import codes.recursive.dexcomproxy.model.User;
import io.micronaut.security.authentication.*;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;

import javax.inject.Singleton;
import java.util.ArrayList;

@Singleton
public class DexcomShareAuthenticationProvider implements AuthenticationProvider {

    private final DataStore dataStore;
    private final DexcomShareClient dexcomShareClient;
    private final String noSessionToken = "00000000-0000-0000-0000-000000000000";

    public DexcomShareAuthenticationProvider(DataStore dataStore, DexcomShareClient dexcomShareClient) {
        this.dataStore = dataStore;
        this.dexcomShareClient = dexcomShareClient;
    }

    @Override
    public Publisher<AuthenticationResponse> authenticate(AuthenticationRequest authenticationRequest) {
        String username = (String) authenticationRequest.getIdentity();
        String password = (String) authenticationRequest.getSecret();
        User user = new User(username, password);
        String token = noSessionToken;
        try {
            token = dexcomShareClient.login(user).replace("\"", "");
        }
        catch(Exception e) {
            // fail silently
        }

        if( token != noSessionToken ) {
            dataStore.sessionIds.put(username, token);
            return Flowable.just(new UserDetails(username, new ArrayList<>()));
        }
        else {
            return Flowable.just(new AuthenticationFailed());
        }
    }
}
