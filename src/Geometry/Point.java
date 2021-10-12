package Geometry;

import java.util.*;


public class Point {
    private final double x;
    private final double y;
    private int gridX;
    private int gridY;
    private PriorityQueue<CircleWithDistance> nearbyCircles;
    private HashSet<CircleWithDistance> allCirclesInRange;
    private final int id;
    private boolean gotAHeight;
    private double height;

    public Point(double x, double y, int id){
        this.x = x;
        this.y = y;
        this.id = id;
        gotAHeight = false;
        allCirclesInRange = new HashSet<>();
        nearbyCircles = new PriorityQueue<>((o1, o2) -> {
            if (o1.getDistance() - o2.getDistance() < 0) {
                return -1;
            }
            return 1;
        });
    }

    public int getId() {
        return id;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getGridX() {
        return gridX;
    }

    public void setGridX(int gridX) {
        this.gridX = gridX;
    }

    public int getGridY() {
        return gridY;
    }

    public void setGridY(int gridY) {
        this.gridY = gridY;
    }

    public PriorityQueue<CircleWithDistance> getNearbyCircles() {
        return nearbyCircles;
    }

    public CircleWithDistance getClosestPoint(){
        return nearbyCircles.peek();
    }

    public void removeFromNearbyCircles(CircleWithDistance cWD){
        nearbyCircles.remove(cWD);
    }

    public void addToNearbyCircles(CircleWithDistance cWD){
        nearbyCircles.add(cWD);
    }

    public HashSet<CircleWithDistance> getAllCirclesInRange() {
        return allCirclesInRange;
    }

    public void addToAllCirclesInRange(CircleWithDistance c){
        allCirclesInRange.add(c);
    }

    @Override
    public String toString() {
        return "p"+ id + "(x = " + x +", y = " + y +')';
        //return "p" + id + ": covering " + this.inCircles;
    }

    public boolean gotAHeight(){
        return gotAHeight;
    }

    public void setHeight(double height){
        this.height = height;
        gotAHeight = true;
    }

    public double getHeight() {
        return height;
    }

    public boolean equals(Point o) {
        return this.getId() == o.getId();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Point)) return false;
        Point that = (Point) o;
        return equals(that);
    }
}
