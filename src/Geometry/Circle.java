package Geometry;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class Circle {
    private double x;
    private double y;
    private int gridX;
    private int gridY;
    private double radius;
    private HashSet<PointWithDistance> pointsInside;
    private int id;
    private List<PointWithDistance> allPointsInside;
    private boolean currentlyInCover;
    private boolean gotAHeight;
    private double height;

    public Circle(double x, double y, double radius, HashSet<PointWithDistance> pointsInside, int id){
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.pointsInside = pointsInside;
        this.id = id;
        allPointsInside = new LinkedList<>();
        currentlyInCover = true;
    }

    public int getId(){
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

    public double getRadius() {
        return radius;
    }

    public void updateRadius(double radius){
        this.radius = radius;
    }

    public HashSet<PointWithDistance> getPointsInside() {
        return pointsInside;
    }

    public void addPointInside(PointWithDistance point) {
        pointsInside.add(point);
    }

    public void removePointInside(PointWithDistance point){
        pointsInside.remove(point);
    }

    public void resetPointsInside(){
        pointsInside = new HashSet<>();
    }

    public void addToAllPointInside(PointWithDistance pWD) {
        allPointsInside.add(pWD);
    }

    public void sortAllPointsInside(){
        Collections.sort(allPointsInside, (o1, o2) -> {
            if(o1.getDistance()-o2.getDistance()<0.0){
                return -1;
            }
            return 1;
        });
    }

    public List<PointWithDistance> getAllPointsInside() {
        return allPointsInside;
    }

    public boolean isInside(Point point){
        return pointsInside.contains(point);
    }

    public boolean isCurrentlyInCover() {
        return currentlyInCover;
    }

    public void setCurrentlyInCover(boolean currentlyInCover) {
        this.currentlyInCover = currentlyInCover;
    }

    @Override
    public String toString() {
        //return "Circle{" +"x=" + x +", y=" + y +", radius=" + radius +'}';
        return "c" + id + "(x=" + x + ", y="+y + ")" /*+ " pointsInside: " + pointsInside.toString()*/;
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

    public boolean equals(Circle o) {
        return this.getId() == o.getId();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Circle)) return false;
        Circle that = (Circle) o;
        return equals(that);
    }
}
