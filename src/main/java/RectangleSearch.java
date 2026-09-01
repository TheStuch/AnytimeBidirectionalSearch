// Import necessary libraries
import java.util.*;

class RectangleSearch{
    int expanded = 0;
    final int M;
    final int N;
    final int B;
    Layer root = null;
    Node answer = null;
    int limit = Integer.MAX_VALUE;
    long startTime = 0L;
    long timeLimit = Long.MAX_VALUE;
    Map<Node, Integer> seen = new HashMap<>();
    int[][] goal;

    public RectangleSearch(int m, int n, int b){
        M = m;
        N = n;
        B = Math.max(1, b);
    }

    public RectangleSearch(int m, int n){
        this(m, n, 1);
    }

    // State space tree node
    class Node{
        Node parent;
        int[][] mat;
        int level;
        int x, y;
        final int cost;

        Node(int[][] mat, int x, int y, Node parent) {
            this.mat = new int[M][N];
            for (int i = 0; i < M; i++)
                System.arraycopy(mat[i], 0, this.mat[i], 0, N);

            this.x = x;
            this.y = y;
            this.parent = parent;
            this.cost = calculateCost(mat);
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

    class Layer{
        final int level;
        PriorityQueue<Node> openList = new PriorityQueue<>(comp);
        Layer next = null;

        public Layer(int lv){
            this.level = lv;
        }

        Node remove(){
            assert !this.openList.isEmpty();
            return this.openList.remove();
        }

        public void add(Node node){
            this.openList.add(node);
        }

        public void createNextLayer(){
            this.next = new Layer(this.level + 1);
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

    // Print path from head node to destination node
    static void printPath(Node head) {
        if (head == null)
            return;
        printPath(head.parent);
        printMatrix(head.mat);
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
            return (lhs.cost) - (rhs.cost);
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
        return limit;
    }


    // Function to solve the puzzle using Branch and Bound
    private Node solve(int[][] initial, int x, int y, int[][] goal, long timeLimit) {
        this.timeLimit = timeLimit;
        this.startTime = System.nanoTime();
        this.goal = goal;
        this.root = new Layer(0);
        Node start = new Node(initial, x, y,  null);
        root.add(start);
        int iteration = 1;
        int repeats = 1;
        while (answer == null && System.nanoTime() - startTime < timeLimit){ //rectangle to find initial answer
            Layer current;
            for(current = root; answer == null && current != null && current.level < repeats; current = current.next){ //going through repeats once
                findShortestPathToEnd(current, 1);
            }
            for(int i = 0; i < B && answer == null; i++){
                findShortestPathToEnd(current, iteration);
                current = current.next;
            }
            iteration++;
            repeats += B;
        }
        while(System.nanoTime() - startTime < timeLimit && root != null){ //keep looking to find answers
            for(Layer current = root; current != null; current = current.next){
                findShortestPathToEnd(current, 1);
            }
        }
        return answer;
    }

    /**
     * pop a number of nodes from a specific layer
     * looks for answer. if none are found, puts children on next layer
     * @param layer the openList to remove from
     * @param amount amount of nodes to pop
     */
    private void findShortestPathToEnd(Layer layer, int amount) {
        int level = layer.level;
        for(int i = 0; i < amount && !layer.openList.isEmpty(); i++) { //amount of times to pop
            Node current = layer.remove();
            if (current.cost + level >= limit) {  //base case: nothing on the open list is valid
                layer.openList = new PriorityQueue<>(comp);
                continue;
            }
            if (seen.containsKey(current) && seen.get(current) < level) {//base case: prune
                continue;
            }
            if (Arrays.deepEquals(current.mat, goal)) { //base case: found solution
                limit = level;
                //System.out.println("Answer of length " + level + " found!");
                if(answer == null){
                    System.out.println("Initial answer of length " + level + " found after " + ((System.nanoTime() - startTime)/ 1000000000L) + " seconds");
                }
                answer = current;
                layer.next = null;
                layer.openList = new PriorityQueue<>(comp);
                return;
            }
            seen.put(current, level);
            expanded++;
            if(level + 1 >= limit) { //if the next layer should be delete
                layer.next = null;
            } else if (layer.next == null){ //if the next layer needs to be made
                layer.createNextLayer();
            }

            // Generate all possible child nodes
            if(layer.next != null){
                for (int j = 0; j < 4; j++) {
                    int newX = current.x + row[j], newY = current.y + col[j];
                    if (isSafe(newX, newY)) {
                        int[][] newMat = new int[M][N];
                        for (int k = 0; k < M; k++)
                            System.arraycopy(current.mat[k], 0, newMat[k], 0, N);

                        // Swap blank tile
                        newMat[current.x][current.y] = newMat[newX][newY];
                        newMat[newX][newY] = 0;

                        Node child = new Node(newMat, newX, newY, current);
                        if (!seen.containsKey(child) || seen.get(child) > level) { //node hasn't already been added or found better way
                            layer.next.add(child);
                        }
                    }
                }
            }
        }
        //if this is the root and is empty, then there can be no solution within that layer
        if(root.openList.isEmpty()){
            root = root.next;
        }
    }


    // Driver Code
    public static void main(String[] args) {
        // Initial configuration
        int m = 5;
        int n = 5;
        int b = 1;
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
        RectangleSearch solver = new RectangleSearch(m, n, b);

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
