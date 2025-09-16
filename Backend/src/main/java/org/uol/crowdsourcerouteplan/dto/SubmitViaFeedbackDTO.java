package org.uol.crowdsourcerouteplan.dto;

import lombok.Data;


@Data
public class SubmitViaFeedbackDTO {
    private int routeId; // required for via
    private int userId;
    private int rating;
    private String comment;

    public SubmitViaFeedbackDTO() {
    }

    public SubmitViaFeedbackDTO(int routeId, int userId, int rating, String comment) {
        this.routeId = routeId;
        this.userId = userId;
        this.rating = rating;
        this.comment = comment;
    }


    public int getRouteId() {
        return routeId;
    }

    public void setRouteId(int routeId) {
        this.routeId = routeId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
