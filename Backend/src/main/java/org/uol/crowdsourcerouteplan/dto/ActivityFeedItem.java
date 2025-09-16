package org.uol.crowdsourcerouteplan.dto;

import lombok.AllArgsConstructor;

import java.time.Instant;

@AllArgsConstructor
public class ActivityFeedItem {

    private Integer id;
    private Integer userId;
    private String title;
    private String action;
    private Instant time;

    public ActivityFeedItem() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }


    public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
        this.time = time;
    }
}
