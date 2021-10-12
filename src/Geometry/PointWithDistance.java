package Geometry;

public class PointWithDistance {

    private Point p;
    private double distance;

    public PointWithDistance(Point p, double distance){
        this.p = p;
        this.distance = distance;
    }

    public Point getP() {
        return p;
    }

    public double getDistance() {
        return distance;
    }


    public String toString(){
        return p.getId() +" d:"+distance;
    }
}
