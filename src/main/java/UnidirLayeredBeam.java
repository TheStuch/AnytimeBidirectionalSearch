// Import necessary libraries
import java.util.*;

class UnidirLayeredBeam{
    int expanded = 0;
    final int M;
    final int N;
    final int K;
    Node answer = null;
    int limit = Integer.MAX_VALUE;
    long startTime = 0L;
    long timeLimit = Long.MAX_VALUE;
    Map<Node, Integer> seen = new HashMap<>();
    Queue<Node> openList = new LinkedList<>();
    Stack<Node> extras = new Stack<>();
    int[][] goal;

    public UnidirLayeredBeam(int m, int n, int k){
        M = m;
        N = n;
        K = Math.max(1, k);
    }

    // State space tree node
    class Node{
        Node parent;
        int[][] mat;
        int x, y;
        final int cost;
        int level;
        int f;

        Node(int[][] mat, int x, int y, int level, Node parent) {
            this.mat = new int[M][N];
            for (int i = 0; i < M; i++)
                System.arraycopy(mat[i], 0, this.mat[i], 0, N);

            this.x = x;
            this.y = y;
            this.level = level;
            this.parent = parent;
            this.cost = calculateCost(mat);
            this.f = cost + this.level;
        }

        @Override
        public int hashCode(){
            return Arrays.deepHashCode(mat);
        }

        @Override
        public boolean equals(Object obj) {
            if(obj == null){
                return false;
            }
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
    int calculateCost(int[][] mat) {
        int dist = 0;

        // For each tile in the current board
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < N; j++) {

                int val = mat[i][j] - 1;

                // Skip the blank tile
                if (val == -1) continue;

                // Compute the tile's goal position
                int goalX = val / N;
                int goalY = val % N;

                // Add Manhattan distance
                dist += Math.abs(i - goalX) + Math.abs(j - goalY);
            }
        }

        return dist;
    }


    // Function to check if coordinates are valid
    boolean isSafe(int x, int y) {
        return (x >= 0 && x < M && y >= 0 && y < N);
    }

    // Print path from root node to destination node
    static void printPath(Node root) {
        if (root == null)
            return;
        printPath(root.parent);
        printMatrix(root.mat);
        System.out.println();
    }

    public void printPath(){
        if(answer == null){
            System.out.println("No answer was found");
            return;
        }
        printPath(answer);
    }

    public int getExpanded(){
        return expanded;
    }

    // Custom comparator for priority queue
    Comparator<Node> comp = new Comparator<Node>() {
        public int compare(Node lhs, Node rhs) {
            return (lhs.cost + lhs.level) - (rhs.cost + rhs.level);
        }
    };

    //solve for optimality without time limit
    public int solve(int[][] initial){
        return solve(initial, Long.MAX_VALUE);
    }

    //find best answer within time limit
    public int solve(int[][] initial, long timeLimit){
        int[] zeroLoc = PuzzleMaker.findZeroLocation(initial);
        Node ans = solve(initial, zeroLoc[0], zeroLoc[1], PuzzleMaker.generateGoal(M, N), timeLimit);
        if(ans == null){
            return -1;
        }
        return ans.level;
    }
    public int publicSolve(int[][] initial, int x, int y, int[][] goal, long timeLimit){
        Node ans = solve(initial, x, y, goal, timeLimit);
        if(ans == null){
            return -1;
        }
        return ans.level;
    }

    // Function to solve the puzzle using Branch and Bound
    private Node solve(int[][] initial, int x, int y, int[][] goal, long timeLimit) {
        this.timeLimit = timeLimit;
        this.startTime = System.nanoTime();
        this.goal = goal;
        Node root = new Node(initial, x, y, 0, null);
        openList.add(root);
        while ((!openList.isEmpty() || !extras.isEmpty()) && System.nanoTime() - startTime < timeLimit){
            Node current = null;
            if(!openList.isEmpty()){
                current = openList.remove();
            }else{
                current = extras.pop();
            }
            findShortestPathToEnd(current);
        }
        return answer;
    }

    //recursively find shortest path from current to goal
    private void findShortestPathToEnd(Node current) {
        if(current.f >= limit ||(seen.containsKey(current) && seen.get(current) <= current.f)){ //base case: prune
            return;
        }
        if(Arrays.deepEquals(current.mat, goal)){ //base case: found solution
            limit = current.level;
            answer = current;
            return;
        }
        seen.put(current, current.f);
        expanded++;
        // Generate all possible child nodes
        List<Node> children = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            int newX = current.x + row[i], newY = current.y + col[i];
            if (isSafe(newX, newY)) {
                int[][] newMat = new int[M][N];
                for (int j = 0; j < M; j++)
                    System.arraycopy(current.mat[j], 0, newMat[j], 0, N);

                // Swap blank tile
                newMat[current.x][current.y] = newMat[newX][newY];
                newMat[newX][newY] = 0;

                Node child = new Node(newMat, newX, newY, current.level + 1, current);
                if (!seen.containsKey(child) || seen.get(child) > child.f) { //node hasn't already been added or found better way
                    //seen.put(child, child);
                    children.add(child);
                }
            }
        }
        children.sort(comp);
        Stack<Node> leftovers = new Stack<>();
        for(Node c : children){
            if(c.f < limit){
                if(openList.size() < K){
                    openList.add(c);
                } else {
                    leftovers.add(c);
                }
            }
        }
        while (!leftovers.isEmpty()){
            extras.push(leftovers.pop());
        }
    }


    // Driver Code
    public static void main(String[] args) {
        // Initial configuration
        int m = 4;
        int n = 4;
        int k = 1000;
        long time = 60000000000L;
        PuzzleMaker pm = new PuzzleMaker(m, n);
        int[][] initial = pm.generatePuzzle();
        /*
        initial = new int[][]{
                {2, 0, 5},
                {1, 8, 7},
                {6, 4, 3}
        };
         */


        // Solvable Final configuration
        int[][] goal = pm.generateGoal();

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
        UnidirLayeredBeam solver = new UnidirLayeredBeam(m, n, k);

        int result = solver.solve(initial, time);
        if(result == -1){
            System.out.println("solution couldn't be found for");
            System.out.println(Arrays.deepToString(initial));
        } else{
            System.out.println("\nShortest Path length found: " + solver.limit);
            printPath(solver.answer);
            System.out.println("\nShortest Path length found: " + solver.limit);
        }
        System.out.println("Nodes expanded: " + solver.expanded);
    }
}
