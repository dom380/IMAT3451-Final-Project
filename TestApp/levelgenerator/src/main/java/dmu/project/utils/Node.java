package dmu.project.utils;

/**
 * Created by Dom on 20/01/2017.
 */

public class Node implements Comparable<Node> {

    public Node(Vector2D pos, double g_Score, boolean walkable) {
        position = pos;
        gScore = g_Score;
        parent = null;
        this.walkable = walkable;
        fScore = 99999999;
        hScore = -1;
    }

    public Node(Vector2D pos, double g_Score, boolean walkable, Node parent) {
        position = pos;
        gScore = g_Score;
        this.parent = parent;
        this.walkable = walkable;
        fScore = 99999999;
        hScore = -1;
    }

    public void updateScore(double g_Score, double h_score, Node parent) {
        gScore = g_Score;
        hScore = h_score;
        fScore = gScore + hScore;
        this.parent = parent != null ? parent : this.parent;;
    }

    public double gScore;
    public double fScore;
    public double hScore;
    public Vector2D position;
    public Node parent;
    public boolean walkable;

    @Override
    public boolean equals(Object o) {
        if(o == null) return false;
        if(o.getClass() != getClass()) return false;
        Node other = (Node) o;
        if(other.fScore!=fScore && other.gScore!=gScore && other.hScore!=hScore) return false;
        return position.equals(other.position);
    }

    @Override
    public int compareTo(Node o) {
        if (o.fScore > fScore) return -1;
        if (o.fScore < fScore) return 1;
        return 0;
    }


}
