package com.poultryprophet.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Bound from {@code poultry.security.*}. */
@ConfigurationProperties(prefix = "poultry.security")
public class SecurityProperties {

    private String jwtSecret;
    private long jwtExpirationMs = 86_400_000L;

    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public long getJwtExpirationMs() {
        return jwtExpirationMs;
    }

    public void setJwtExpirationMs(long jwtExpirationMs) {
        this.jwtExpirationMs = jwtExpirationMs;
    }
}
