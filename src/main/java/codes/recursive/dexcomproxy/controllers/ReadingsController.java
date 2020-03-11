package codes.recursive.dexcomproxy.controllers;

import codes.recursive.dexcomproxy.client.DexcomShareClient;
import codes.recursive.dexcomproxy.services.DataStore;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.security.annotation.Secured;

import java.security.Principal;

@Secured("isAuthenticated()")
@Controller("/api")
public class ReadingsController {

    private final DexcomShareClient dexcomShareClient;
    private final DataStore dataStore;

    public ReadingsController(DexcomShareClient dexcomShareClient, DataStore dataStore) {
        this.dexcomShareClient = dexcomShareClient;
        this.dataStore = dataStore;
    }

    @Get("/readings/{duration}/{max}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse getReadings(Principal principal, int duration, int max) {
        String token = dataStore.sessionIds.get(principal.getName());
        return HttpResponse.ok(
                dexcomShareClient.getReadings(token, duration, max)
        );
    }
}
