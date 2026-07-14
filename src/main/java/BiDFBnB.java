import java.util.*;

public class BiDFBnB {
    int expanded = 0;
    final int M;
    final int N;
    final boolean FORWARD = true;
    final boolean BACKWARD = false;
    Node answer = null;
    int limit = Integer.MAX_VALUE;
    long startTime = 0L;
    long timeLimit = Long.MAX_VALUE;
    Map<HashMatrix, Integer> seenForward = new HashMap<>();
    Map<HashMatrix, Integer> seenBackward = new HashMap<>();

    int[][] initial;
    int[][] goal;
    Stack<Node> stack;

    public BiDFBnB(int m, int n){
        M = m;
        N = n;
    }

    public BiDFBnB(int n){
        this(n, n);
    }

    // Bottom, left, top, right movement
    static int[] row = {1, 0, -1, 0};
    static int[] col = {0, -1, 0, 1};

    // Function to check if coordinates are valid
    boolean isSafe(int x, int y) {
        return (x >= 0 && x < M && y >= 0 && y < N);
    }
    boolean withinTimeLimit(){
        return System.nanoTime() - startTime < timeLimit;
    }

    class HashMatrix{ //wrapper class for 2D matrix so they can go in hashmap
        int[][] matrix;

        private HashMatrix(int[][] input){
            matrix = new int[M][N];
            for (int i = 0; i < M; i++){
                System.arraycopy(input[i], 0, this.matrix[i], 0, N);
            }
        }

        @Override
        public int hashCode(){
            return Arrays.deepHashCode(matrix);
        }

        @Override
        public boolean equals(Object obj){
            if(obj == null){
                return false;
            }
            if (this == obj) return true;
            if (!(obj instanceof HashMatrix)) return false;
            HashMatrix other = (HashMatrix) obj;
            return Arrays.deepEquals(this.matrix, other.matrix);
        }
    }

    class Node{
        Node parent;
        boolean directionTo;
        HashMatrix start;
        HashMatrix end;
        int x1, y1;
        int x2, y2;
        final int cost;
        int flevel, blevel;
        int f;

        Node(HashMatrix a, HashMatrix b, int flevel, int blevel, Node parent, int x1, int y1, int x2, int y2) {
            this.start = a;
            this.end = b;
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;

            this.flevel = flevel;
            this.blevel = blevel;
            this.parent = parent;
            this.directionTo = FORWARD;
            this.cost = calculateCost();
            this.f = cost + this.flevel + this.blevel;
        }

        private int calculateCost() {
            int dist = 0;
            int[][] goalLoc = new int[M * N][2];
            for(int i = 0; i < M; i++){
                for(int j = 0; j < N; j++){
                    goalLoc[end.matrix[i][j]] = new int[]{i, j};
                }
            }

            // For each tile in the current board
            for (int i = 0; i < M; i++) {
                for (int j = 0; j < N; j++) {

                    int val = start.matrix[i][j];

                    // Skip the blank tile
                    if (val == 0) continue;

                    // Compute the tile's goal position
                    int goalX = goalLoc[val][0];
                    int goalY = goalLoc[val][1];

                    // Add Manhattan distance
                    dist += Math.abs(i - goalX) + Math.abs(j - goalY);
                }
            }

            return dist;
        }
    }
    // Print path from root node to destination node
    static void printPath(Node root, List<int[][]> path) {
        if (root == null) {
            System.out.println();
            for (int[][] matrix : path) {
                PuzzleMaker.printMatrix(matrix);
                System.out.println();
            }
            return;
        }
        if(path == null || path.isEmpty()){
            path = new ArrayList<>();
            path.add(root.start.matrix);
        } else {
            if(Arrays.deepEquals(root.start.matrix, path.get(0))){ //if starts the same, it was a back change
                path.add(root.end.matrix);
                System.out.print("backwards ");
            }else {     //change was done at the front
                path.add(0, root.start.matrix);
                System.out.print("forwards ");
            }
        }
        printPath(root.parent, path);
    }

    // Custom comparator for priority queue
    Comparator<Node> comp = new Comparator<Node>() {
        public int compare(Node lhs, Node rhs) {
            return (lhs.f) - (rhs.f);
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
        return ans.flevel + ans.blevel;
    }

    // Function to solve the puzzle using Branch and Bound
    private Node solve(int[][] initial, int x, int y, int[][] goal, long timeLimit) {
        this.timeLimit = timeLimit;
        this.startTime = System.nanoTime();
        this.goal = goal;
        HashMatrix start = new HashMatrix(initial);
        HashMatrix end = new HashMatrix(goal);
        Node root = new Node(start, end, 0, 0, null, x, y, M - 1, N - 1);
        stack = new Stack<>();
        stack.push(root);
        seenForward.put(start, 0);
        seenBackward.put(end, 0);
        while (!stack.isEmpty() && withinTimeLimit()){
            Node current = stack.pop();
            findShortestPathToEnd(current);
        }
        return answer;
    }

    private void findShortestPathToEnd(Node current) {
        if (current.f > limit) { //base case: prune
            return;
        }
        if (Arrays.deepEquals(current.start.matrix, current.end.matrix)) { //base case: found solution
            answer = current;
            limit = current.flevel + current.blevel;
            //printPath(answer, null);
            return;
        }
        expanded++;

        List<Node> startChildren = new ArrayList<>();
        List<Node> endChildren = new ArrayList<>();
        for (int i = 0; i < 4; i++) {

            int newX = current.x1 + row[i], newY = current.y1 + col[i]; //child from start node
            if (isSafe(newX, newY)) {
                int[][] newMat = new int[M][N];
                for (int j = 0; j < M; j++)
                    System.arraycopy(current.start.matrix[j], 0, newMat[j], 0, N);

                // Swap blank tile
                newMat[current.x1][current.y1] = newMat[newX][newY];
                newMat[newX][newY] = 0;

                HashMatrix copy = new HashMatrix(newMat);

                Node child = new Node(copy, current.end, current.flevel + 1, current.blevel, current, newX, newY, current.x2, current.y2);
                if (!seenForward.containsKey(child.start) || seenForward.get(child.start) >= child.flevel) { //node hasn't already been added or found better way
                    startChildren.add(child);
                }
            }

            newX = current.x2 + row[i];
            newY = current.y2 + col[i]; // child from end node
            if (isSafe(newX, newY)) {
                int[][] newMat = new int[M][N];
                for (int j = 0; j < M; j++)
                    System.arraycopy(current.end.matrix[j], 0, newMat[j], 0, N);

                // Swap blank tile
                newMat[current.x2][current.y2] = newMat[newX][newY];
                newMat[newX][newY] = 0;

                HashMatrix copy = new HashMatrix(newMat);

                Node child = new Node(current.start, copy, current.flevel, current.blevel + 1, current, current.x1, current.y1, newX, newY);
                if (!seenBackward.containsKey(child.end) || seenBackward.get(child.end) >= child.blevel) { //node hasn't already been added or found better way
                    endChildren.add(child);
                }
            }
        }
        startChildren.sort(comp.reversed());
        endChildren.sort(comp.reversed());
        boolean direction = !current.directionTo; //line that decides jump
        if(direction == FORWARD){
            for(Node child : startChildren){
                if(child.f < limit){
                    seenForward.put(child.start, child.flevel);
                    child.directionTo = FORWARD;
                    stack.add(child);
                }
            }
        } else {
            for(Node child : endChildren){
                if(child.f < limit){
                    seenBackward.put(child.end, child.blevel);
                    child.directionTo = BACKWARD;
                    stack.add(child);
                }
            }
        }
    }

    public int getExpanded(){
        return expanded;
    }

    // Driver Code
    public static void main(String[] args) {
        // Initial configuration
        int m = 3;
        int n = 4;
        long timeLimit = 90000000000L;
        PuzzleMaker pm = new PuzzleMaker(m, n);
        int[][] initial = pm.generatePuzzle();
       /*initial = new int[][]{
                {4, 0, 5},
                {2, 3, 1}
        };


        */
        PuzzleMaker.printMatrix(initial);
        long startTime = System.nanoTime();
        BiDFBnB solver = new BiDFBnB(m, n);

        int result = solver.solve(initial, timeLimit);
        System.out.println("\nShortest Path length found: " + result);
       /* if(solver.answer != null) {
            BiDFBnB.printPath(solver.answer, null);
        }
        */
        System.out.println("Nodes expanded: " + solver.expanded);
        long timeTaken = System.nanoTime() - startTime;
        System.out.println("time: " + (timeTaken / 1000000000L) + " seconds");
    }
}
