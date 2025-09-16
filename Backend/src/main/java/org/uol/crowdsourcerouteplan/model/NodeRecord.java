package org.uol.crowdsourcerouteplan.model;


import lombok.Data;

import java.util.Objects;

@Data
public class NodeRecord {
    private Location location;
    private double gScore;
    private double hScore;
    private double fScore;
    private NodeRecord cameFrom;



    public NodeRecord(Location location, double gScore, double hScore, double fScore, NodeRecord cameFrom) {
        this.location = location;
        this.gScore = gScore;
        this.hScore = hScore;
        this.fScore = fScore;
        this.cameFrom = cameFrom;
    }

    public NodeRecord() {
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NodeRecord)) return false;
        NodeRecord that = (NodeRecord) o;
        return location.getId() == that.location.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(location.getId());
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public double getgScore() {
        return gScore;
    }

    public void setgScore(double gScore) {
        this.gScore = gScore;
    }

    public double gethScore() {
        return hScore;
    }

    public void sethScore(double hScore) {
        this.hScore = hScore;
    }

    public double getfScore() {
        return fScore;
    }

    public void setfScore(double fScore) {
        this.fScore = fScore;
    }

    public NodeRecord getCameFrom() {
        return cameFrom;
    }

    public void setCameFrom(NodeRecord cameFrom) {
        this.cameFrom = cameFrom;
    }
}
