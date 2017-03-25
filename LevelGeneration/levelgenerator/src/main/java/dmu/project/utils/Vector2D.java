package dmu.project.utils;

/**
 * Created by Dom on 20/01/2017.
 * Class representing a 2D vector.
 */

public class Vector2D {

    private Double mX, mY;

    /**
     * Constructor.
     *
     * @param mX The X component.
     * @param mY The Y component.
     */
    public Vector2D(Double mX, Double mY) {
        this.mX = mX;
        this.mY = mY;
    }

    /**
     * Constructor.
     *
     * @param mX The X component.
     * @param mY The Y component.
     */
    public Vector2D(int mX, int mY) {
        this.mX = (double) mX;
        this.mY = (double) mY;
    }

    /**
     * Deep Copy constructor
     *
     * @param other The vector2D to copy from.
     */
    public Vector2D(Vector2D other) {
        this.mX = other.mX;
        this.mY = other.mY;
    }

    /**
     * Performs the Dot Product with the specified vector
     *
     * @param other The vector to dot product with.
     * @return The dot product.
     */
    public Double dotProduct(Vector2D other) {
        return (mX * other.mX) + (mY * other.mY);
    }

    /**
     * @return The magnitude of the vector.
     */
    public Double magnitude() {
        return Math.sqrt(Math.pow(mX, 2) + Math.pow(mY, 2));
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
        return (other.mX.equals(mX) && other.mY.equals(mY));
    }

    @Override
    public int hashCode() {
        return (int) (mX * mY);
    }

    /**
     * Add two vectors.
     *
     * @param other The vector to add.
     * @return The resultant vector
     */
    public Vector2D add(Vector2D other) {
        return new Vector2D(mX + other.mX, mY + other.mY);
    }

    /**
     * Add subtract vectors.
     *
     * @param other The vector to subtract.
     * @return The resultant vector
     */
    public Vector2D subtract(Vector2D other) {
        return new Vector2D(mX - other.mX, mY - other.mY);
    }

    //Getters and Setters.
    public Double getX() {
        return mX;
    }

    public void setX(Double x) {
        this.mX = x;
    }

    public Double getY() {
        return mY;
    }

    public void setY(Double y) {
        this.mY = y;
    }

    public void setXY(Double x, Double y) {
        this.mX = x;
        this.mY = y;
    }

    public void setXY(int x, int y) {
        this.mX = (double) x;
        this.mY = (double) y;
    }
}
