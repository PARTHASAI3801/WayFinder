package org.uol.crowdsourcerouteplan.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.sql.Timestamp;

@Entity
@Table(name = "v_user_activity_feed")
@Immutable
public class UserActivityFeedRow {

    @Id
    private Integer id;
    @Column(name = "userid")
    private Integer userId;
    @Column(name = "title")
    private String title;
    @Column(name = "action")
    private String action;
    @Column(name = "metadata")
    private String metadata;
    @Column(name = "time")
    private Timestamp time;

    public UserActivityFeedRow() {
    }

    public UserActivityFeedRow(Integer id, Integer userId, String title, String action, String metadata, Timestamp time) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.action = action;
        this.metadata = metadata;
        this.time = time;
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

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }
}
