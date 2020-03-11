package codes.recursive.dexcomproxy;

import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.client.annotation.Client;

@Client("/")
public interface AppClient {

    @Get("/")
    String home(@Header String authorization);
}
