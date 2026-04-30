package com.cloud.jml.utils.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret = "gOOKoiLpQ/XDAAo9I85L/2Bh3Y9KWvDFyRf7vqqGP38="; // valor por defecto si no esta en el YAML (src/main/resources/application.yml)
    private long expiration = Duration.ofHours(1).toMillis(); // valor por defecto si no esta en el YAML (src/main/resources/application.yml)
    private long refreshExpirationMs = Duration.ofHours(3).toMillis(); // valor por defecto si no esta en el YAML (src/main/resources/application.yml)
}
