package Geometry;

import java.util.Objects;

public class CircleWithDistance {

    private Circle c;
    private double distance;

    public CircleWithDistance(Circle c, double distance){
        this.c = c;
        this.distance = distance;
    }

    public Circle getC() {
        return c;
    }

    public void setC(Circle c) {
        this.c = c;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public boolean equals(CircleWithDistance o) {
        if(o == null){
            return false;
        }
        return c.equals(o.getC());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CircleWithDistance)) return false;
        CircleWithDistance that = (CircleWithDistance) o;
        return equals(that);
    }

    @Override
    public int hashCode() {
        return Objects.hash(c);
    }

    @Override
    public String toString() {
        return "c=" + c.toString() +
                ", d=" + distance;
    }
}
