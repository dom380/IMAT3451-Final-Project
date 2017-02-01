package dmu.project.utils;

/**
 * Created by Dom on 20/01/2017.
 */

public class Vector2D {

    private Double x, y;

    public Vector2D(Double x, Double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2D(int x, int y) {
        this.x = (double)x;
        this.y = (double)y;
    }

    public Vector2D(Vector2D other) {
        this.x = other.x;
        this.y = other.y;
    }

    public Double dotProduct(Vector2D other) {
        return (x * other.x) + (y * other.y);
    }

    public Double magnitude() {
        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
    }

    @Override
    public boolean equals(Object ob) {
        if (ob == null) return false;
        if (ob.getClass() != getClass()) return false;
        Vector2D other = (Vector2D) ob;
        return (other.x.equals(x) && other.y.equals(y));
    }

    @Override
    public int hashCode() {
        return (int) (x * y);
    }

    public Vector2D add(Vector2D other) {
        return new Vector2D(x + other.x, y + other.y);
    }

    public Vector2D subtract(Vector2D other) {
        return new Vector2D(x - other.x, y - other.y);
    }

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }
}
