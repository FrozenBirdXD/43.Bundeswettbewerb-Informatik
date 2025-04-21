import java.util.ArrayList;
import java.util.List;

// Node for Huffman tree
public class Node implements Comparable<Node> {
    Character character; // null for internal nodes
    long frequency;
    List<Node> children;
    boolean isLeaf;

    // For leaf nodes
    public Node(Character character, long frequency) {
        this.character = character;
        this.frequency = frequency;
        this.children = new ArrayList<>();
        this.isLeaf = true;
    }

    // For internal nodes
    public Node(List<Node> children, long frequency) {
        this.character = null;
        this.frequency = frequency;
        this.children = children;
        this.isLeaf = false;
    }

    @Override
    public int compareTo(Node other) {
        // Compare based on frequency, lower frequency first
        return Long.compare(this.frequency, other.frequency);
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public long getFrequency() {
        return frequency;
    }
}