package dmu.project.levelgen;

import dmu.project.utils.Vector2D;

/**
 * Created by Dom on 10/02/2017.
 */

public class Heuristics {
    private static double rootTwo = Math.sqrt(2);

    /**
     * Calculate the Diagonal  Distance between two positions.
     *
     * @param pos1 Two value array representing (x,y) coordinates.
     * @param pos2 Two value array representing (x,y) coordinates.
     * @return The Diagonal Distance.
     */
    public static double diagonalDist(Vector2D pos1, Vector2D pos2) {
        double dX = Math.abs(pos2.getX() - pos1.getX());
        double dY = Math.abs(pos2.getY() - pos1.getY());
        return (dX) + (rootTwo - 2) * Math.min(dX, dY);

    }

    public static double diagonalDist(int[] pos1, int[] pos2) {
        double dX = Math.abs(pos1[0] - pos2[0]);
        double dY = Math.abs(pos1[1] - pos2[1]);
        return (dX) + (rootTwo - 2) * Math.min(dX, dY);

    }

    public static double manhatDist(Vector2D pos1, Vector2D pos2) {
        double dX = Math.abs(pos1.getX() - pos2.getX());
        double dY = Math.abs(pos1.getY() - pos2.getY());
        return (dX + dY);
    }

    public static double manhatDist(int[] pos1, int[] pos2) {
        double dX = Math.abs(pos1[0] - pos2[0]);
        double dY = Math.abs(pos1[1] - pos2[1]);
        return (dX + dY);
    }

    public static double realDist(Vector2D pos1, Vector2D pos2) {
        double dX = Math.abs(pos2.getX() - pos1.getX());
        double dY = Math.abs(pos2.getY() - pos1.getY());
        return Math.sqrt(Math.pow(dX, 2) + Math.pow(dY, 2));
    }
}
