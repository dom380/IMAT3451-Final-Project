package dmu.project.utils;

/**
 * Created by Dom on 20/01/2017.
 * Class that represents a graph node in the path finding grid.
 */

public class Node implements Comparable<Node> {
    public double gScore;
    public double fScore;
    public double hScore;
    public Vector2D position;
    public Node parent;
    public boolean walkable;

    /**
     * Constructor.
     *
     * @param pos      The Vector2D position of the node.
     * @param g_Score  It's global score.
     * @param walkable If true, the node can be navigated to.
     */
    public Node(Vector2D pos, double g_Score, boolean walkable) {
        position = pos;
        gScore = g_Score;
        parent = null;
        this.walkable = walkable;
        fScore = 99999999;
        hScore = -1;
    }

    /**
     * Constructor.
     *
     * @param pos      The Vector2D position of the node.
     * @param g_Score  It's global score.
     * @param walkable If true, the node can be navigated to.
     * @param parent   This node's parent node.
     */
    public Node(Vector2D pos, double g_Score, boolean walkable, Node parent) {
        position = pos;
        gScore = g_Score;
        this.parent = parent;
        this.walkable = walkable;
        fScore = 99999999;
        hScore = -1;
    }

    /**
     * Update the nodes path finding score.
     *
     * @param g_Score The new global score.
     * @param h_score The new heuristic score.
     * @param parent  Optional, the new parent of this node.
     */
    public void updateScore(double g_Score, double h_score, Node parent) {
        gScore = g_Score;
        hScore = h_score;
        fScore = gScore + hScore;
        this.parent = parent != null ? parent : this.parent;
    }

    /**
     * Equals override.
     * Objects are equal if their position, gScore, fScore and hScore are equal.
     *
     * @param o The object to check against.
     * @return True if the objects are equal.
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o.getClass() != getClass()) return false;
        Node other = (Node) o;
        if (other.fScore != fScore && other.gScore != gScore && other.hScore != hScore)
            return false;
        return position.equals(other.position);
    }

    /**
     * CompareTO override.
     *
     * @param o The node to compare to.
     * @return -1 if other node has a higher fScore. 1 If this has a higher fScore. 0 if fScores are equal.
     */
    @Override
    public int compareTo(Node o) {
        if (o.fScore > fScore) return -1;
        if (o.fScore < fScore) return 1;
        return 0;
    }


}
