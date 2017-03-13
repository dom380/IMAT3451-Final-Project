package dmu.project.utils;

/**
 * Created by Dom on 20/01/2017.
 * Class representing a 2D vector.
 */

public class Vector2D {

    private Double x, y;

    /**
     * Constructor.
     *
     * @param x The X component.
     * @param y The Y component.
     */
    public Vector2D(Double x, Double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Constructor.
     *
     * @param x The X component.
     * @param y The Y component.
     */
    public Vector2D(int x, int y) {
        this.x = (double) x;
        this.y = (double) y;
    }

    /**
     * Deep Copy constructor
     *
     * @param other The vector2D to copy from.
     */
    public Vector2D(Vector2D other) {
        this.x = other.x;
        this.y = other.y;
    }

    /**
     * Performs the Dot Product with the specified vector
     *
     * @param other The vector to dot product with.
     * @return The dot product.
     */
    public Double dotProduct(Vector2D other) {
        return (x * other.x) + (y * other.y);
    }

    /**
     * @return The magnitude of the vector.
     */
    public Double magnitude() {
        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
    }

    /**
     * Equals override.
     * Performs component-wise equals if specified object is a Vector2D
     * otherwise returns false.
     *
     * @param ob The object to compare to.
     * @return True if vectors are equal.
     */
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

    /**
     * Add two vectors.
     * @param other The vector to add.
     * @return The resultant vector
     */
    public Vector2D add(Vector2D other) {
        return new Vector2D(x + other.x, y + other.y);
    }

    /**
     * Add subtract vectors.
     * @param other The vector to subtract.
     * @return The resultant vector
     */
    public Vector2D subtract(Vector2D other) {
        return new Vector2D(x - other.x, y - other.y);
    }

    //Getters and Setters.
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

    public void setXY(Double x, Double y) {
        this.x = x;
        this.y = y;
    }

    public void setXY(int x, int y) {
        this.x = (double) x;
        this.y = (double) y;
    }
}
