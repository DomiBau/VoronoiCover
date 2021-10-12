package Geometry;

public class CircleWithIndex {
    private Circle c;
    private int index;

    public CircleWithIndex(Circle c, int index){
        this.c = c;
        this.index = index;
    }

    public Circle getC() {
        return c;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        //return "PointWithIndex{" + "p=" + p + ", index=" + index + '}';
        return c.toString();
    }

    public boolean equals(CircleWithIndex o) {
        if(o == null){
            return false;
        }
        return c.equals(o.getC());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CircleWithIndex)) return false;
        CircleWithIndex that = (CircleWithIndex) o;
        return equals(that);
    }
}
