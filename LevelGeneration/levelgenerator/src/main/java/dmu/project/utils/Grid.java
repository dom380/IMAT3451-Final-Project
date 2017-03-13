package dmu.project.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Dom on 25/01/2017.
 * Class representing the map as a grid for path finding.
 */

public class Grid {

    private Node[][] grid;
    private int xMax, yMax;
    private boolean moveDiag;

    /**
     * Constructor.
     *
     * @param xMax         Number of nodes in the X direction.
     * @param yMax         Number of nodes in the Y direction.
     * @param moveDiagonal True if the grid allows diagonal movement.
     */
    public Grid(int xMax, int yMax, boolean moveDiagonal) {
        grid = new Node[xMax][yMax];
        this.xMax = xMax;
        this.yMax = yMax;
        this.moveDiag = moveDiagonal;
    }

    /**
     * Adds a node to the grid at the specified position.
     *
     * @param x    The X coordinate of the node.
     * @param y    The Y coordinate of the node.
     * @param node The node to add.
     */
    public void addNode(int x, int y, Node node) {
        grid[x][y] = node;
    }

    /**
     * Retrieves the node at the specified coordinate.
     *
     * @param x The X coordinate of the node.
     * @param y The Y coordinate of the node.
     * @return The node at that position.
     */
    public Node getNode(int x, int y) {
        return grid[x][y];
    }

    /**
     * Checks if that node is free to move to.
     *
     * @param x The X coordinate of the node.
     * @param y The Y coordinate of the node.
     * @return True if the node can be moved to.
     */
    public boolean walkable(int x, int y) {
        if ((x < 0) || (x >= xMax)) {
            int check = 1;
            return false;
        } else if ((y < 0) || (y >= yMax)) {
            int check = 1;
            return false;
        }
        return grid[x][y].walkable;
    }

    /**
     * Resets every node in the grid.
     */
    public void reset() {
        for (int i = 0; i < xMax; i++) {
            for (int j = 0; j < yMax; j++) {
                grid[i][j].fScore = -1;
                grid[i][j].gScore = -1;
                grid[i][j].hScore = -1;
                grid[i][j].parent = null;
            }
        }
    }

    /**
     * Returns a list of the positions of the free neighbours of the specified node.
     *
     * @param node The node to retrieve the neighbours of.
     * @return A list of the positions of the free neighbours of the specified node.
     */
    public List<Vector2D> getNeighbours(Node node) {
        return getNeighbours(node, moveDiag);
    }

    /**
     * Returns a list of the positions of the free neighbours of the specified node
     *
     * @param node     The node to retrieve the neighbours of.
     * @param moveDiag If true will also check the corner neighbours.
     * @return A list of the positions of the free neighbours of the specified node.
     */
    public List<Vector2D> getNeighbours(Node node, boolean moveDiag) {
        List<Vector2D> neighbours = new ArrayList<>();
        if (moveDiag) {
            for (Vector2D dir : eightDir) {
                double x = node.position.add(dir).getX();
                double y = node.position.add(dir).getY();
                if (walkable((int) x, (int) y))
                    neighbours.add(new Vector2D(x, y));
            }
            return neighbours;
        } else {
            for (Vector2D dir : fourDir) {
                double x = node.position.add(dir).getX();
                double y = node.position.add(dir).getY();
                if (walkable((int) x, (int) y))
                    neighbours.add(new Vector2D(x, y));
            }
            return neighbours;
        }
    }

    /**
     * Used for Jump Point Search. Returns all free neighbours in the current direction of travel
     * relative to the node's parent. If the node has no parent return all free nodes.
     *
     * @param node The node to retrieve the neighbours of.
     * @return A list of the positions of the free neighbours of the specified node.
     */
    public List<Vector2D> getNeighboursPrune(Node node) {
        if (node.parent == null) {
            return getNeighbours(node);
        } else {
            List<Vector2D> neighbors = new ArrayList<>();
            int px, py, dx, dy, x, y;
            x = node.position.getX().intValue();
            y = node.position.getY().intValue();
            px = node.parent.position.getX().intValue();
            py = node.parent.position.getY().intValue();
            //get the normalized direction of travel
            dx = (x - px) / Math.max(Math.abs(x - px), 1);
            dy = (y - py) / Math.max(Math.abs(y - py), 1);
            //search diagonally
            if (dx != 0 && dy != 0 && moveDiag) {
                if (walkable(x, y + dy)) {
                    neighbors.add(new Vector2D(x, (y + dy)));
                }
                if (walkable(x + dx, y)) {
                    neighbors.add(new Vector2D(x + dx, y));
                }
                if (walkable(x + dx, y + dy)) {
                    neighbors.add(new Vector2D(x + dx, y + dy));
                }
                if (!walkable(x - dx, y)) { //Check for forced neighbour
                    neighbors.add(new Vector2D(x - dx, y + dy));
                }
                if (!walkable(x, y - dy)) { //Check for forced neighbour
                    neighbors.add(new Vector2D(x + dx, y - dy));
                }
            } else { //Moving Horz/vert
                if (dx == 0) { //If moving in x
                    if (walkable(x, y + dy)) {
                        neighbors.add(new Vector2D(x, y + dy));
                    }
                    if (!walkable(x + 1, y)) {
                        neighbors.add(new Vector2D(x + 1, y + dy));
                    }
                    if (!walkable(x - 1, y)) {
                        neighbors.add(new Vector2D(x - 1, y + dy));
                    }

                } else { //moving in y
                    if (walkable(x + dx, y)) {
                        neighbors.add(new Vector2D(x + dx, y));
                    }
                    if (!walkable(x, y + 1)) {
                        neighbors.add(new Vector2D(x + dx, y + 1));
                    }
                    if (!walkable(x, y - 1)) {
                        neighbors.add(new Vector2D(x + dx, y - 1));
                    }
                }
            }
            return neighbors;
        }
    }

    /////////////////////////////
    //Private Utility Methods //
    ////////////////////////////

    /**
     * Array of Vector2Ds for 8 directions.
     */
    private ArrayList<Vector2D> eightDir = new ArrayList<>(Arrays.asList(
            new Vector2D(-1.0, 0.0),
            new Vector2D(-1.0, 1.0),
            new Vector2D(0.0, 1.0),
            new Vector2D(1.0, 1.0),
            new Vector2D(1.0, 0.0),
            new Vector2D(1.0, -1.0),
            new Vector2D(0.0, -1.0),
            new Vector2D(-1.0, -1.0)
    ));

    /**
     * Array of Vector2Ds for 4 directions.
     */
    private ArrayList<Vector2D> fourDir = new ArrayList<>(Arrays.asList(
            new Vector2D(-1.0, 0.0),
            new Vector2D(0.0, 1.0),
            new Vector2D(1.0, 0.0),
            new Vector2D(0.0, -1.0)
    ));

    /**
     * @return True if diagonal movement enabled.
     */
    public boolean isMoveDiag() {
        return moveDiag;
    }

    /**
     * Returns the node at the specified position.
     *
     * @param x The X coordinate of the node.
     * @param y The Y coordinate of the node.
     * @return The node at the requested coordinates/
     */
    public Node getNode(Double x, Double y) {
        return getNode(x.intValue(), y.intValue());
    }
}
