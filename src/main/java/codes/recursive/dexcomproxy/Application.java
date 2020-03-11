package codes.recursive.dexcomproxy;

import io.micronaut.runtime.Micronaut;

public class Application {

    public static void main(String[] args) {
        Micronaut
                .build(new String[]{})
                .mainClass(Application.class)
                .environmentPropertySource(false)
                .start();
    }
}