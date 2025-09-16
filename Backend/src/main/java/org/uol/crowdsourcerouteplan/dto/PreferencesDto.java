package org.uol.crowdsourcerouteplan.dto;

import lombok.Data;

@Data
public class PreferencesDto {
    private Boolean scenic = false;
    private Boolean avoidMotorways = false;
    private String leaveAfter;

    public Boolean getScenic() {
        return scenic;
    }

    public void setScenic(Boolean scenic) {
        this.scenic = scenic;
    }

    public Boolean getAvoidMotorways() {
        return avoidMotorways;
    }

    public void setAvoidMotorways(Boolean avoidMotorways) {
        this.avoidMotorways = avoidMotorways;
    }

    public String getLeaveAfter() {
        return leaveAfter;
    }

    public void setLeaveAfter(String leaveAfter) {
        this.leaveAfter = leaveAfter;
    }
}
