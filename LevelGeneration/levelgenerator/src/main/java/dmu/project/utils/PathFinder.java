package dmu.project.utils;

import com.google.common.base.Stopwatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import dmu.project.levelgen.Heuristics;

/**
 * Created by Dom on 10/03/2017.
 * <p>
 * Utility class to perform Path Finding
 */

public class PathFinder {

    /**
     * Checks if a path exists between the two positions using Jump Point Search.
     *
     * @param start The start position as a 2 value array of ints.
     * @param goal  The goal position as a 2 value array of ints.
     * @param grid  The grid to search.
     * @return True if a path exists.
     */
    public static boolean checkPathExists(int[] start, int[] goal, Grid grid) {
        boolean goodPath = false;
        List<Node> cleaningList = new ArrayList<>();
        LIFOEntry.resetCount();
        final Queue<LIFOEntry<Node>> frontier = new PriorityQueue<>();
        Node startNode = grid.getNode(start[0], start[1]);
        Node goalNode = grid.getNode(goal[0], goal[1]);
        final Set<Node> closedSet = new HashSet<>();
        Stopwatch stopwatch = Stopwatch.createStarted();
        frontier.add(new LIFOEntry<Node>(startNode));
        cleaningList.add(startNode);
        boolean moveDiag = grid.isMoveDiag();
        //JPS
        while (!frontier.isEmpty()) {
            LIFOEntry<Node> entry = frontier.poll();
            Node current = entry.getEntry();
            if (current.position.equals(goalNode.position)) {
                stopwatch.stop();
                goodPath = true;
                break;
            }
            closedSet.add(current);
            List<Vector2D> neighbours = grid.getNeighboursPrune(current);
            for (Vector2D neighbourPos : neighbours) {
                Node jumpNode = jump(grid, neighbourPos, current, goalNode, moveDiag);
                if (jumpNode != null && !closedSet.contains(jumpNode)) {
                    Node neighbour = grid.getNode(neighbourPos.getX().intValue(), neighbourPos.getY().intValue());
                    double gScore = current.gScore != -1 ? (current.gScore + Heuristics.realDist(current.position, neighbour.position)) : (Heuristics.realDist(current.position, neighbour.position));
                    if (gScore >= neighbour.gScore && neighbour.gScore != -1)
                        continue; //This isn't a better path than on found before.
                    cleaningList.add(jumpNode);
                    if (moveDiag) {
                        jumpNode.updateScore(gScore, Heuristics.diagonalDist(jumpNode.position, goalNode.position), null);
                    } else {
                        jumpNode.updateScore(gScore, Heuristics.manhatDist(jumpNode.position, goalNode.position), current);
                    }
                    LIFOEntry<Node> jumpEntry = new LIFOEntry<Node>(jumpNode);
                    if (!frontier.contains(jumpEntry))
                        frontier.add(jumpEntry);
                }
            }
        }
        if (stopwatch.isRunning()) {
            long elapsed = stopwatch.elapsed(TimeUnit.MILLISECONDS);
            stopwatch.stop();
        }
        clearNodes(cleaningList);
        return goodPath;
    }

    /**
     * Checks if a path exists between the two positions using Jump Point Search.
     *
     * @param start The start position as a 2 value array of ints.
     * @param goal  The goal position as a 2 value array of ints.
     * @param grid  The grid to search.
     * @return The path if found. Null if no path found.
     */
    public static List<Node> findPathJPS(int[] start, int[] goal, Grid grid) {
        List<Node> cleaningList = new ArrayList<>();
        LIFOEntry.resetCount();
        final Queue<LIFOEntry<Node>> frontier = new PriorityQueue<>();
        Node startNode = grid.getNode(start[0], start[1]);
        Node goalNode = grid.getNode(goal[0], goal[1]);
        final Set<Node> closedSet = new HashSet<>();
        Stopwatch stopwatch = Stopwatch.createStarted();
        frontier.add(new LIFOEntry<Node>(startNode));
        cleaningList.add(startNode);
        boolean moveDiag = grid.isMoveDiag();
        //JPS
        while (!frontier.isEmpty()) {
            LIFOEntry<Node> entry = frontier.poll();
            Node current = entry.getEntry();
            if (current.position.equals(goalNode.position)) {
                stopwatch.stop();
                List<Node> path = rebuildPathJPS(current, startNode);
                clearNodes(cleaningList);
                return path;
            }
            closedSet.add(current);
            List<Vector2D> neighbours = grid.getNeighboursPrune(current);
            for (Vector2D neighbourPos : neighbours) {
                Node jumpNode = jump(grid, neighbourPos, current, goalNode, moveDiag);
                if (jumpNode != null && !closedSet.contains(jumpNode)) {
                    Node neighbour = grid.getNode(neighbourPos.getX().intValue(), neighbourPos.getY().intValue());
                    double gScore = current.gScore != -1 ? (current.gScore + Heuristics.realDist(current.position, neighbour.position)) : (Heuristics.realDist(current.position, neighbour.position));
                    if (gScore >= neighbour.gScore && neighbour.gScore != -1)
                        continue; //This isn't a better path than on found before.
                    cleaningList.add(jumpNode);
                    if (moveDiag) {
                        jumpNode.updateScore(gScore, Heuristics.diagonalDist(jumpNode.position, goalNode.position), null);
                    } else {
                        jumpNode.updateScore(gScore, Heuristics.manhatDist(jumpNode.position, goalNode.position), current);
                    }
                    LIFOEntry<Node> jumpEntry = new LIFOEntry<Node>(jumpNode);
                    if (!frontier.contains(jumpEntry))
                        frontier.add(jumpEntry);
                }
            }
        }
        if (stopwatch.isRunning()) {
            long elapsed = stopwatch.elapsed(TimeUnit.MILLISECONDS);
            stopwatch.stop();
        }
        clearNodes(cleaningList);
        return null;
    }

    /**
     * Checks if a path exists between the two positions using A*
     *
     * @param start The start position as a 2 value array of ints.
     * @param goal  The goal position as a 2 value array of ints.
     * @param grid  The grid to search.
     * @return The path if found. Null if no path found.
     */
    public static List<Node> findPathAStar(int[] start, int[] goal, Grid grid) {
        //A* Search
        Map<Node, Node> cameFrom = new HashMap<>();
        List<Node> cleaningList = new ArrayList<>();
        LIFOEntry.resetCount();
        final Queue<LIFOEntry<Node>> frontier = new PriorityQueue<>();
        Node startNode = grid.getNode(start[0], start[1]);
        Node goalNode = grid.getNode(goal[0], goal[1]);
        final Set<Node> closedSet = new HashSet<>();
        Stopwatch stopwatch = Stopwatch.createStarted();
        frontier.add(new LIFOEntry<Node>(startNode));
        cleaningList.add(startNode);
        frontier.add(new LIFOEntry<Node>(startNode));
        startNode.fScore = Heuristics.diagonalDist(start, goal);
        while (!frontier.isEmpty()) {
            LIFOEntry entry = frontier.poll();
            Node current = (Node) entry.getEntry();
            if (current.position.equals(goalNode.position)) {
                long elapsed = stopwatch.elapsed(TimeUnit.MILLISECONDS);
                stopwatch.stop();
                List<Node> path = rebuildPathAStar(cameFrom, current, startNode);
                clearNodes(cleaningList);
                return path;
            }
            closedSet.add(current);
            List<Vector2D> neighbours = grid.getNeighbours(current, false);
            for (Vector2D neighbourPos : neighbours) {
                Node neighbour = grid.getNode(neighbourPos.getX().intValue(), neighbourPos.getY().intValue());
                if (closedSet.contains(neighbour)) {
                    continue;
                }
                double gScore = current.gScore != -1 ? (current.gScore + Heuristics.realDist(current.position, neighbour.position)) : (Heuristics.realDist(current.position, neighbour.position));
                if (gScore >= neighbour.gScore && neighbour.gScore != -1)
                    continue; //This isn't a better path than one found before.
                cleaningList.add(current);
                neighbour.updateScore(gScore, Heuristics.manhatDist(neighbour.position, goalNode.position), current);
                if (!frontier.contains(neighbour)) {
                    frontier.add(new LIFOEntry<Node>(neighbour));
                }
                cameFrom.put(neighbour, current);

            }
        }
        long elapsed = stopwatch.elapsed(TimeUnit.MILLISECONDS);
        stopwatch.stop();
        clearNodes(cleaningList);
        return null;
    }
    /////////////////////////////
    //Private Utility Methods //
    ////////////////////////////


    /**
     * Rebuilds the path from a Jump point search.
     *
     * @param current The goal node.
     * @param start   The start node.
     * @return A list of nodes that make up the path
     */
    private static List<Node> rebuildPathJPS(Node current, Node start) {
        List<Node> path = new ArrayList<>();
        Vector2D position = start.position;
        while (true) {
            if (current.parent != null && !current.parent.position.equals(position)) {
                current = current.parent;
                path.add(current);
            } else {
                path.add(start);
                Collections.reverse(path);
                return path;
            }
        }
    }

    /**
     * Rebuilds the path from an A* search.
     *
     * @param current The goal node.
     * @param start   The start node.
     * @return A list of nodes that make up the path
     */
    private static List<Node> rebuildPathAStar(Map<Node, Node> cameFrom, Node current, Node start) {
        List<Node> path = new ArrayList<>();
        path.add(current);
        while (!cameFrom.get(current).equals(start)) {
            current = cameFrom.get(current);
            path.add(current);
        }
        Collections.reverse(path);
        return path;
    }

    /**
     * Utility method to perform the recursive jumps in Jump Point Search.
     *
     * @param grid     The grid to search.
     * @param nodePos  The current nodes position.
     * @param parent   The parent node.
     * @param goal     The goal node.
     * @param moveDiag True if can move diagonally.
     * @return The jump point node or null if no jump point found.
     */
    private static Node jump(Grid grid, Vector2D nodePos, Node parent, Node goal, boolean moveDiag) {
        int x = nodePos.getX().intValue(), y = nodePos.getY().intValue();
        if (!grid.walkable(x, y)) { //If space isn't walkable return null
            return null;
        }
        Node node = grid.getNode(x, y);
        node.parent = parent;
        if (node.position.equals(goal.position)) { //If end point, return it. Search over.
            return node;
        }
        //get the normalized direction of travel
        int px = parent.position.getX().intValue(), py = parent.position.getY().intValue();
        int dx = (x - px) / Math.max(Math.abs(x - px), 1);
        int dy = (y - py) / Math.max(Math.abs(y - py), 1);

        if (dx != 0 && dy != 0) { //If x and y have changed we're moving diagonally. Check for forced neighbours
            if ((grid.walkable(x - dx, y + dy) && !grid.walkable(x - dx, y)) || //we are moving diagonally, don't check the parent, or our next diagonal step, but the other diagonals
                    (grid.walkable(x + dx, y - dy) && !grid.walkable(x, y - dy))) {  //if we find a forced neighbor here, we are on a jump point, and we return the current position
                return node;
            }
            //Moving diagonally so have to check for vertical and horizontal jump points
            if (jump(grid, new Vector2D(x + dx, y), node, goal, moveDiag) != null || jump(grid, new Vector2D(x, y + dy), node, goal, moveDiag) != null) {
                return node;
            }
        } else { //Check horizontal and vertical
            if (dx != 0) { //Moving in X
                if (moveDiag) { //And we allow diagonal movement
                    if ((grid.walkable(x + dx, y + 1) && !grid.walkable(x, y + 1)) || //check the diagonal nodes for forced neighbours
                            (grid.walkable(x + dx, y - 1) && !grid.walkable(x, y - 1))) {
                        return node;
                    }
                } else { //Diagonal moves not allowed.
                    if (grid.walkable(x + 1, y) || grid.walkable(x - 1, y)) { // if left or right free
                        return node;                                                            // return node as we're on a jump point
                    }
                }
            } else { //Moving in Y
                if (moveDiag) { //If diagonal movement allowed.
                    if ((grid.walkable(x + 1, y + dy) && !grid.walkable(x + 1, y)) ||
                            (grid.walkable(x - 1, y + dy) && !grid.walkable(x - 1, y))) {
                        return node;
                    }
                } else {
                    if (grid.walkable(x, y + 1) || grid.walkable(x, y - 1)) {
                        return node;
                    }
                }
            }
        }
        if (moveDiag) {
            if (grid.walkable(x + dx, y) || grid.walkable(x, y + dy)) {
                return jump(grid, new Vector2D(x + dx, y + dy), node, goal, true); //Haven't found a forced neighbour or goal yet, jump to next diagonal in current direction.
            } else { //Blocked from going diagonally
                return null;
            }
        }
        return null;//Couldn't jump anywhere.
    }


    /**
     * Utility method to reset the list of nodes.
     *
     * @param nodes List of nodes to reset.
     */
    public static void clearNodes(List<Node> nodes) {
        if (nodes.isEmpty()) return;
        for (Node node : nodes) {
            node.fScore = -1;
            node.gScore = -1;
            node.hScore = -1;
            node.parent = null;
            node.walkable = true;
        }
        nodes.clear();
    }
}
