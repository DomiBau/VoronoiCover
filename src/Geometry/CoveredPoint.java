package Geometry;

public class CoveredPoint {

    private Point p;
    private Circle newC;
    private Circle oldC;
    private double newDistance;

    public CoveredPoint(Circle oldC, Point p, Circle newC, double newDistance){
        this.p = p;
        this.newC = newC;
        this.oldC = oldC;
        this.newDistance = newDistance;
    }

    public Point getP() {
        return p;
    }

    public Circle getNewC() {
        return newC;
    }

    public Circle getOldC() {
        return oldC;
    }

    public double getNewDistance() {
        return newDistance;
    }

    @Override
    public String toString() {
        //return "CoveredCircle{" + "p=" + p + ", c=" + c + '}';
        return oldC.toString() + ", " + p.toString() + " -> " + newC.toString();
    }

    public boolean pointEquals(CoveredPoint cP){
        return this.getP().equals(cP.getP());
    }

    public boolean equals(CoveredPoint o) {
        return this.getNewC().equals(o.getNewC()) && this.getP().equals(o.getP());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CoveredPoint)) return false;
        CoveredPoint that = (CoveredPoint) o;
        return equals(that);
    }

}
