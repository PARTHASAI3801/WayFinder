package org.uol.crowdsourcerouteplan.dto;


import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RouteIntent {
    private String from;
    private String to;
    private List<String> via = new ArrayList<String>();
    private String mode;
    private PreferencesDto preferences = new PreferencesDto();
    private Boolean useCurrentLocation = false;


    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public List<String> getVia() {
        return via;
    }

    public void setVia(List<String> via) {
        this.via = via;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public PreferencesDto getPreferences() {
        return preferences;
    }

    public void setPreferences(PreferencesDto preferences) {
        this.preferences = preferences;
    }

    public Boolean getUseCurrentLocation() {
        return useCurrentLocation;
    }

    public void setUseCurrentLocation(Boolean useCurrentLocation) {
        this.useCurrentLocation = useCurrentLocation;
    }
}
