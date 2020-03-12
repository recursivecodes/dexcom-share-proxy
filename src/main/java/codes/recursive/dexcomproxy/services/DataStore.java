package codes.recursive.dexcomproxy.services;

import io.micronaut.security.token.jwt.render.AccessRefreshToken;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Singleton
public class DataStore {

    public Map<String, String> sessionIds = new HashMap<String, String>();
    public Map<String, Map<String, Object>> authCodes = new HashMap<String, Map<String, Object>>();

    public DataStore() {
    }
}
