package org.platform.spidereddit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;


@SpringBootApplication
public class SpideredditApplication {

    public static void main(String[] args) throws IOException {
        SpringApplication.run(SpideredditApplication.class, args);
    }

}
