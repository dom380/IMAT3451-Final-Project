package dmu.project.levelgen;

import dmu.project.utils.Vector2D;

/**
 * Created by Dom on 10/02/2017.
 * Utility class holding a number of static methods for heuristics.
 */

public class Heuristics {
    private final static double ROOT_TWO = Math.sqrt(2);

    /**
     * Calculate the Diagonal  Distance between two positions.
     *
     * @param pos1 A Vector2D representing (x,y) coordinates.
     * @param pos2 A Vector2D representing (x,y) coordinates.
     * @return The Diagonal (Octile) Distance.
     */
    public static double diagonalDist(Vector2D pos1, Vector2D pos2) {
        double dX = Math.abs(pos2.getX() - pos1.getX());
        double dY = Math.abs(pos2.getY() - pos1.getY());
        double F = ROOT_TWO - 1;
        return (dX < dY) ? F * dX + dY : F * dY + dX;

    }

    /**
     * Calculate the Diagonal  Distance between two positions.
     *
     * @param pos1 Two value array representing (x,y) coordinates.
     * @param pos2 Two value array representing (x,y) coordinates.
     * @return The Diagonal (Octile) Distance.
     */
    public static double diagonalDist(int[] pos1, int[] pos2) {
        double dX = Math.abs(pos1[0] - pos2[0]);
        double dY = Math.abs(pos1[1] - pos2[1]);
        double F = ROOT_TWO - 1;
        return (dX < dY) ? F * dX + dY : F * dY + dX;
    }

    /**
     * Calculate the Manhattan (orthogonal) distance between two positions.
     *
     * @param pos1 The first position as a Vector2D.
     * @param pos2 The second position as a Vector2D.
     * @return The distance between them.
     */
    public static double manhatDist(Vector2D pos1, Vector2D pos2) {
        double dX = Math.abs(pos1.getX() - pos2.getX());
        double dY = Math.abs(pos1.getY() - pos2.getY());
        return (dX + dY);
    }


    /**
     * Calculate the Manhattan (orthogonal) distance between two positions.
     *
     * @param x1 The X coordinate of the first position.
     * @param y1 The Y coordinate of the first position.
     * @param x2 The X coordinate of the second position.
     * @param y2 The Y coordinate of the second position.
     * @return The distance between them.
     */
    public static double manhatDist(double x1, double y1, double x2, double y2) {
        double dX = Math.abs(x1 - x2);
        double dY = Math.abs(y1 - y2);
        return (dX + dY);
    }

    /**
     * Calculate the Manhattan (orthogonal) distance between two positions.
     *
     * @param pos1 The first position as a two value array.
     * @param pos2 The second position as a two value array.
     * @return The distance between them.
     */
    public static double manhatDist(int[] pos1, int[] pos2) {
        double dX = Math.abs(pos1[0] - pos2[0]);
        double dY = Math.abs(pos1[1] - pos2[1]);
        return (dX + dY);
    }

    /**
     * Calculates the actual distance between two positions.
     *
     * @param pos1 The first position as a Vector2D.
     * @param pos2 The second position as a Vector2D.
     * @return The distance between them.
     */
    public static double realDist(Vector2D pos1, Vector2D pos2) {
        double dX = Math.abs(pos2.getX() - pos1.getX());
        double dY = Math.abs(pos2.getY() - pos1.getY());
        return Math.sqrt(Math.pow(dX, 2) + Math.pow(dY, 2));
    }
}
