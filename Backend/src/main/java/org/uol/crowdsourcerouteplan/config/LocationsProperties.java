package org.uol.crowdsourcerouteplan.config;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "locations")
public class LocationsProperties {
    private String baseUrl = "http://localhost:8080";
    public String getBaseUrl() {return baseUrl;}
    public void setBaseUrl(String baseUrl) {this.baseUrl = baseUrl;}
}
