package org.scarter4work.wumpus2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class Wumpus2Application {

    public static void main(String[] args) {
        log.info("Starting Wumpus2 Application");
        SpringApplication.run(Wumpus2Application.class, args);
        log.info("Wumpus2 Application started successfully");
    }

}
