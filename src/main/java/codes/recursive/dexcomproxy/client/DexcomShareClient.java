package codes.recursive.dexcomproxy.client;

import codes.recursive.dexcomproxy.model.GlucoseReading;
import codes.recursive.dexcomproxy.model.User;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.client.annotation.Client;

import java.util.List;

@Client(value = "${codes.recursive.dexcom-share-base-url}")
public abstract class DexcomShareClient {
    @Post("/ShareWebServices/Services/General/LoginPublisherAccountByName")
    public abstract String login(@Body User user);

    @Post("/ShareWebServices/Services/Publisher/ReadPublisherLatestGlucoseValues?sessionId={sessionId}&minutes={duration}&maxCount={max}")
    public abstract List<GlucoseReading> getReadings(@QueryValue String sessionId, @QueryValue int duration, @QueryValue int max);
}
