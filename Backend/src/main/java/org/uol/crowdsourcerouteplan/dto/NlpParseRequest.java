package org.uol.crowdsourcerouteplan.dto;


import lombok.Data;

@Data
public class NlpParseRequest {
    private String prompt;
    private ParseContext context;

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public ParseContext getContext() {
        return context;
    }

    public void setContext(ParseContext context) {
        this.context = context;
    }

    @Data
    public static class ParseContext{
        private String country = "UK";
        private String defaultMode = "driving-car";
        private Double userLat;
        private Double userLon;
        private Boolean allowCurrentLocation = true;

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getDefaultMode() {
            return defaultMode;
        }

        public void setDefaultMode(String defaultMode) {
            this.defaultMode = defaultMode;
        }

        public Double getUserLat() {
            return userLat;
        }

        public void setUserLat(Double userLat) {
            this.userLat = userLat;
        }

        public Double getUserLon() {
            return userLon;
        }

        public void setUserLon(Double userLon) {
            this.userLon = userLon;
        }

        public Boolean getAllowCurrentLocation() {
            return allowCurrentLocation;
        }

        public void setAllowCurrentLocation(Boolean allowCurrentLocation) {
            this.allowCurrentLocation = allowCurrentLocation;
        }
    }
}
