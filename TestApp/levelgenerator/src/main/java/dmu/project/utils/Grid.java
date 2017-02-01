package dmu.project.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dmu.project.levelgen.Tile;
import dmu.project.levelgen.TileState;

/**
 * Created by Dom on 25/01/2017.
 */

public class Grid {

    private Node[][] grid;
    private int xMax, yMax;
    private boolean moveDiag;

    public Grid(int xMax, int yMax, boolean moveDiagonal) {
        grid = new Node[xMax][yMax];
        this.xMax = xMax;
        this.yMax = yMax;
        this.moveDiag = moveDiagonal;
    }

    public void addNode(int x, int y, Node node) {
        grid[x][y] = node;
    }

    public Node getNode(int x, int y) {
        return grid[x][y];
    }

    public boolean walkable(int x, int y, List<Tile> tilesSet) {
        if ((x < 0) || (x >= xMax)) {
            int check = 1;
            return false;
        } else if ((y < 0) || (y >= yMax)) {
            int check = 1;
            return false;
        } else if (!grid[x][y].walkable) {
            return false;
        }
        for (Tile tile : tilesSet) {
            if (tile.position[0] == x && tile.position[1] == y && tile.tileState == TileState.OBSTACLE) {
                return false;
            }
        }
        return true;
    }

    public void reset() {
        for (int i = 0; i < xMax; i++) {
            for (int j = 0; j < yMax; j++) {
                grid[i][j].fScore = -1;
                grid[i][j].gScore = -1;
                grid[i][j].hScore = -1;
            }
        }
    }


    public List<Node> getNeighbours(Node node, List<Tile> tilesSet) {
        List<Node> neighbours = new ArrayList<>();
        if (moveDiag) {
            for (Vector2D dir : eightDir) {
                double x = node.position.add(dir).getX();
                double y = node.position.add(dir).getY();
                if (walkable((int) x, (int) y, tilesSet))
                    neighbours.add(grid[(int) x][(int) y]);
            }
            return neighbours;
        } else {
            for (Vector2D dir : fourDir) {
                double x = node.position.add(dir).getX();
                double y = node.position.add(dir).getY();
                if (walkable((int) x, (int) y, tilesSet))
                    neighbours.add(grid[(int) x][(int) y]);
            }
            return neighbours;
        }
    }

    public List<Node> getNeighboursPrune(Node node, List<Tile> tilesSet) {
        if (node.parent == null) {
            return getNeighbours(node, tilesSet);
        } else {
            List<Node> neighbors = new ArrayList<>();
            int px, py, dx, dy, x, y;
            boolean walkX = false, walkY = false;
            x = node.position.getX().intValue();
            y = node.position.getY().intValue();
            px = node.parent.position.getX().intValue();
            py = node.parent.position.getY().intValue();
            //get the normalized direction of travel
            dx = (x - px) / Math.max(Math.abs(x - px), 1);
            dy = (y - py) / Math.max(Math.abs(y - py), 1);
            //search diagonally
            if (dx != 0 && dy != 0 && moveDiag) {
                if (walkable(x, y + dy, tilesSet)) {
                    neighbors.add(grid[x][(y + dy)]);
                    walkY = true;
                }
                if (walkable(x + dx, y, tilesSet)) {
                    neighbors.add(grid[x + dx][y]);
                    walkX = true;
                }
                if (walkX || walkY) {
                    if (walkable(x + dx, y + dy, tilesSet))
                        neighbors.add(grid[x + dx][y + dy]);
                }
                if (!walkable(x - dx, y, tilesSet) && walkY) {
                    neighbors.add(grid[x - dx][y + dy]);
                }
                if (!walkable(x, y - dy, tilesSet) && walkX) {
                    neighbors.add(grid[x + dx][y - dy]);
                }
            } else {
                if (dx == 0) {
                    if (walkable(x, y + dy, tilesSet)) {
                        neighbors.add(grid[x][y + dy]);
                        if (!walkable(x + 1, y, tilesSet)) {
                            if (walkable(x + 1, y + dy, tilesSet)) {
                                neighbors.add(grid[x + 1][y + dy]);
                            }
                        }
                        if (!walkable(x - 1, y, tilesSet)) {
                            if (walkable(x - 1, y + dy, tilesSet)) {
                                neighbors.add(grid[x - 1][y + dy]);
                            }
                        }
                    }
                    if (!moveDiag) {
                        if (walkable(x + 1, y, tilesSet)) {
                            neighbors.add(grid[x + 1][y]);
                        }
                        if (walkable(x - 1, y, tilesSet)) {
                            neighbors.add(grid[x - 1][y]);
                        }
                    }
                } else {
                    if (walkable(x + dx, y, tilesSet)) {
                        neighbors.add(grid[x + dx][y]);
                        if (!walkable(x, y + 1, tilesSet)) {
                            if (walkable(x + dx, y + 1, tilesSet))
                                neighbors.add(grid[x + dx][y + 1]);
                        }
                        if (!walkable(x, y - 1, tilesSet)) {
                            if (walkable(x + dx, y - 1, tilesSet))
                                neighbors.add(grid[x + dx][y - 1]);
                        }
                    }
                    if (!moveDiag) {
                        if (walkable(x, y + 1, tilesSet)) {
                            neighbors.add(grid[x][y + 1]);
                        }
                        if (walkable(x, y - 1, tilesSet)) {
                            neighbors.add(grid[x][y - 1]);
                        }
                    }
                }
            }
            return neighbors;
        }
    }

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

    private ArrayList<Vector2D> fourDir = new ArrayList<>(Arrays.asList(
            new Vector2D(-1.0, 0.0),
            new Vector2D(0.0, 1.0),
            new Vector2D(1.0, 0.0),
            new Vector2D(0.0, -1.0)
    ));

    public boolean isMoveDiag() {
        return moveDiag;
    }
}
