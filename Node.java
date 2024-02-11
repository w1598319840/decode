package tree.haffumanCoding;

public class Node implements Comparable<Node> {
    private char data;//数据具体值
    private int weight;//权值
    private Node left;
    private Node right;

    public char getData() {
        return data;
    }

    public int getWeight() {
        return weight;
    }

    public Node getLeft() {
        return left;
    }

    public void setLeft(Node left) {
        this.left = left;
    }

    public Node getRight() {
        return right;
    }

    public void setRight(Node right) {
        this.right = right;
    }

    public Node(char data, int weight) {
        this.data = data;
        this.weight = weight;
    }

    @Override
    public int compareTo(Node node) {
        return this.weight - node.weight;
    }

    @Override
    public String toString() {
        return "Node{" +
                "data=" + data +
                ", weight=" + weight +
                '}';
    }

    private void preOrder(Node node) {
        if (node == null) {
            return;
        }
        System.out.print(node.data + " ");
        preOrder(node.getLeft());
        preOrder(node.getRight());
    }

    public void preOrder() {
        preOrder(this);
    }
}
