package org.uol.crowdsourcerouteplan.dto;


import lombok.Data;

import java.util.List;

@Data
public class NlpParseResponse {
    private String status;
    private RouteIntent data;
    private String ask;
    private List<String> missingFields;
    private String error;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public RouteIntent getData() {
        return data;
    }

    public void setData(RouteIntent data) {
        this.data = data;
    }

    public String getAsk() {
        return ask;
    }

    public void setAsk(String ask) {
        this.ask = ask;
    }

    public List<String> getMissingFields() {
        return missingFields;
    }

    public void setMissingFields(List<String> missingFields) {
        this.missingFields = missingFields;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
