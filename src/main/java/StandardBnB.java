// Import necessary libraries
import java.util.*;

class StandardBnB{
    int expanded = 0;
    final int M;
    final int N;

    public StandardBnB(int m, int n){
        M = m;
        N = n;
    }

    public StandardBnB(int n){
        this(n, n);
    }
    // State space tree node
    class Node{
        Node parent;
        int[][] mat;
        int x, y;
        int cost;
        int level;

        Node(int[][] mat, int x, int y, int level, Node parent) {
            this.mat = new int[M][N];
            for (int i = 0; i < M; i++)
                System.arraycopy(mat[i], 0, this.mat[i], 0, N);

            this.x = x;
            this.y = y;
            this.level = level;
            this.parent = parent;
            this.cost = Integer.MAX_VALUE;
        }

        @Override
        public int hashCode(){
            return Arrays.deepHashCode(mat);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Node)) return false;
            Node other = (Node) obj;
            return Arrays.deepEquals(this.mat, other.mat);
        }
    }

    // Function to print N x N matrix
    static void printMatrix(int[][] mat) {
        for (int[] row : mat) {
            for (int val : row) {
                System.out.print(val + " ");
            }
            System.out.println();
        }
    }

    // Bottom, left, top, right movement
    static int[] row = {1, 0, -1, 0};
    static int[] col = {0, -1, 0, 1};

    // Function to calculate misplaced tiles
    int calculateCost(int[][] initial, int[][] goal) {
        int count = 0;
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                if (initial[i][j] != 0 && initial[i][j] != goal[i][j])
                    count++;
        return count;
    }

    // Function to check if coordinates are valid
     boolean isSafe(int x, int y) {
        return (x >= 0 && x < M && y >= 0 && y < N);
    }

    // Print path from root node to destination node
    static int printPath(Node root) {
        if (root == null)
            return 0;
        int result = 1 + printPath(root.parent);
        printMatrix(root.mat);
        System.out.println();
        return result;
    }

    // Custom comparator for priority queue
     Comparator<Node> comp = new Comparator<Node>() {
        public int compare(Node lhs, Node rhs) {
            return (lhs.cost + lhs.level) - (rhs.cost + rhs.level);
        }
    };

    // Function to solve the puzzle using Branch and Bound
     int solve(int[][] initial, int x, int y, int[][] goal) {
         MinHeapImpl<Node> pq = new MinHeapImpl<Node>(comp);
        Node root = new Node(initial, x, y, 0, null);
        root.cost = calculateCost(initial, goal);
        pq.insert(root);

        while (!pq.isEmpty()) {
            expanded++;
            Node min = pq.remove();
           /* for (int[] row : min.mat){
                System.out.println(Arrays.toString(row));
            }
            System.out.println();

            */


            // If final state is reached, print the solution path
            if (min.cost == 0) {
                //printPath(min);
               return min.level;
            }

            // Generate all possible child nodes
            for (int i = 0; i < 4; i++) {
                int newX = min.x + row[i], newY = min.y + col[i];
                if (isSafe(newX, newY)) {
                    int[][] newMat = new int[M][N];
                    for (int j = 0; j < M; j++)
                        System.arraycopy(min.mat[j], 0, newMat[j], 0, N);

                    // Swap blank tile
                    newMat[min.x][min.y] = newMat[newX][newY];
                    newMat[newX][newY] = 0;

                    Node child = new Node(newMat, newX, newY, min.level + 1, min);
                    int index = pq.getArrayIndex(child);
                    if (index == Integer.MAX_VALUE) { //node hasn't already been added
                        child.cost = calculateCost(child.mat, goal);
                        pq.insert(child);
                    } else if (index != -1){
                        Node copy = pq.getCopy(index);
                        if(child.level < copy.level){
                            copy.level = child.level;
                            pq.reHeapify(index);
                        }
                    }
                }
            }
        }
        return -1;
    }

    // Driver Code
    public static void main(String[] args) {
        // Initial configuration
        int m = 3;
        int n = 3;
        PuzzleMaker pm = new PuzzleMaker(m, n);
        int[][] initial = pm.generatePuzzle();
       /* initial = new int[][]{
                {2, 0, 5},
                {1, 8, 7},
                {6, 4, 3}
        };
        */


        // Solvable Final configuration
        int[][] goal = new int[m][n];
        int val = 0;
        for(int i = 0; i < goal.length; i++){
            for(int j = 0; j < n; j++){
                goal[i][j] = val++;            }
        }

        // Blank tile coordinates in initial configuration
        int x = -1, y = -1;
        for(int i = 0; i < initial.length; i++){
            if(x != -1) break;
            for(int j = 0; j < initial[i].length; j++){
                if(initial[i][j] == 0){
                    x = i;
                    y = j;
                    break;
                }
            }
        }
        StandardBnB solver = new StandardBnB(m, n);

        int result = solver.solve(initial, x, y, goal);
        if(result == -1){
            System.out.println("solution couldn't be found for");
            System.out.println(Arrays.deepToString(initial));
            System.out.println("Nodes expanded: " + solver.expanded);
        } else{
            System.out.println("\nShortest Path length: " + result);
            System.out.println("Nodes expanded: " + solver.expanded);
        }
    }
}