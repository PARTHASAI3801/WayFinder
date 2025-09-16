package org.uol.crowdsourcerouteplan.model;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "rp_user_activity_log")
@Data
public class UserActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "user_id")
    private int userId;
    @Column(name = "action")
    private String action;
    @Column(name = "message")
    private String message;
    @Column(name = "ref_table")
    private String refTable;
    @Column(name = "details")
    private String details;
    @Column(name = "ref_id")
    private Integer refId;
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public UserActivityLog(Integer id, int userId, String action, String message, String refTable, String details, Integer refId, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.action = action;
        this.message = message;
        this.refTable = refTable;
        this.details = details;
        this.refId = refId;
        this.createdAt = createdAt;
    }

    public UserActivityLog() {

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRefTable() {
        return refTable;
    }

    public void setRefTable(String refTable) {
        this.refTable = refTable;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Integer getRefId() {
        return refId;
    }

    public void setRefId(Integer refId) {
        this.refId = refId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
