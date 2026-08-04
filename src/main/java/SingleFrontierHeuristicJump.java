// Import necessary libraries
import java.util.*;

class SingleFrontierHeuristicJump{
    int expanded = 0;
    final int M;
    final int N;
    final boolean FORWARD = true;
    final boolean BACKWARD = false;
    Node answer = null;
    int limit = Integer.MAX_VALUE;
    int[][] initial;
    int[][] goal;
    Stack<Node> stack;

    public SingleFrontierHeuristicJump(int m, int n){
        M = m;
        N = n;
        createGoalArray();
    }

    private void createGoalArray() {
        this.goal = new int[M][N];
        int val = 1;
        for(int i = 0; i < M; i++){
            for(int j = 0; j< N; j++){
                this.goal[i][j] = val++;
            }
        }
        this.goal[M-1][N-1] = 0;
    }

    public SingleFrontierHeuristicJump(int n){
        this(n, n);
    }
    // State space tree node
    class Node{
        Node parent;
        boolean direction;
        int[][] start;
        int[][] end;
        int x1, y1;
        int x2, y2;
        final int cost;
        int level;
        int f;

        Node(int[][] a, int[][] b, int level, Node parent, int x1, int y1, int x2, int y2) {
            this.start = new int[M][N];
            this.end = new int[M][N];
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            for (int i = 0; i < M; i++){
                System.arraycopy(a[i], 0, this.start[i], 0, N);
                System.arraycopy(b[i], 0, this.end[i], 0, N);
            }
            this.level = level;
            this.parent = parent;
            this.direction = (this.parent == null) ? FORWARD : this.parent.direction;
            this.cost = calculateCost(start, end);
            this.f = cost + this.level;
        }
        @Override
        public int hashCode(){
            int hash = Arrays.deepHashCode(start) * 17;
            hash += Arrays.deepHashCode(end);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj == null){
                return false;
            }
            if (this == obj) return true;
            if (!(obj instanceof Node)) return false;
            Node other = (Node) obj;
            return Arrays.deepEquals(this.start, other.start) && Arrays.deepEquals(this.end, other.end);
        }

        private void print(){
            for(int i = 0; i < M; i++){
                for(int val1 : start[i]){
                    System.out.print(val1 + " ");
                }
                System.out.print("\t");
                for(int val2 : end[i]){
                    System.out.print(val2 + " ");
                }
                System.out.println();
            }
            //System.out.println();
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
    int calculateCost(int[][] mat, int[][] goal) {
        int dist = 0;
        int[][] goalLoc = new int[M * N][2];
        for(int i = 0; i < M; i++){
            for(int j = 0; j < N; j++){
                goalLoc[goal[i][j]] = new int[]{i, j};
            }
        }

        // For each tile in the current board
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < N; j++) {

                int val = mat[i][j];

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


    // Function to check if coordinates are valid
    boolean isSafe(int x, int y) {
        return (x >= 0 && x < M && y >= 0 && y < N);
    }

    // Print path from root node to destination node
    public static void printPath(Node root, List<int[][]> path) {
        if (root == null) {
            System.out.println();
            for (int[][] matrix : path) {
                printMatrix(matrix);
                System.out.println();
            }
            return;
        }
        String dir = "";
        if(path == null || path.isEmpty()){
            path = new ArrayList<>();
            path.add(root.start);
        } else {
            if(Arrays.deepEquals(root.start, path.get(0))){ //if starts the same, it was a back change
                path.add(root.end);
                dir = "backwards ";
            }else {     //change was done at the front
                path.add(0, root.start);
                dir = "forwards ";
            }
        }
        printPath(root.parent, path);
        System.out.print(dir);
    }

    // Custom comparator for priority queue
    Comparator<Node> comp = new Comparator<Node>() {
        public int compare(Node lhs, Node rhs) {
            return (lhs.cost) - (rhs.cost);
        }
    };

    // Function to solve the puzzle using Branch and Bound
    public int solve(int[][] initial) {
        int[] startZero = PuzzleMaker.findZeroLocation(initial);
        int[] endZero = PuzzleMaker.findZeroLocation(goal);
        this.initial = initial;
        Node root = new Node(initial, goal, 0,null, startZero[0], startZero[1], endZero[0], endZero[1]);
        this.limit = root.f - 1;
        while(this.answer == null){ // every try with new IDA* limit
            //System.out.println(limit);
            stack = new Stack<>();
            stack.push(root);
            this.limit++;
            while (!stack.isEmpty() && this.answer == null){
                Node current = stack.pop();
                findShortestPathToEnd(current);
            }
        }
        //System.out.println("shortest: " + this.limit);
        return this.limit;
    }

    //recursively find shortest path from current to goal
    private void findShortestPathToEnd(Node current) {
        if(current.f > limit){ //base case: prune
            return;
        }
        //current.print();
        if(Arrays.deepEquals(current.start, current.end)){ //base case: found solution
            answer = current;
            //printPath(answer, null);
            return;
        }
        expanded++;
        // Generate all possible child nodes
        List<Node> startChildren = new ArrayList<>();
        List<Node> endChildren = new ArrayList<>();
        for (int i = 0; i < 4; i++) {

            int newX = current.x1 + row[i], newY = current.y1 + col[i]; //child from start node
            if (isSafe(newX, newY)) {
                int[][] newMat = new int[M][N];
                for (int j = 0; j < M; j++)
                    System.arraycopy(current.start[j], 0, newMat[j], 0, N);

                // Swap blank tile
                newMat[current.x1][current.y1] = newMat[newX][newY];
                newMat[newX][newY] = 0;

                Node child = new Node(newMat, current.end, current.level + 1, current, newX, newY, current.x2, current.y2);
                if (!child.equals(current.parent)) { //node hasn't already been added or found better way
                    startChildren.add(child);
                }
            }

            newX = current.x2 + row[i];
            newY = current.y2 + col[i]; // child from end node
            if (isSafe(newX, newY)) {
                int[][] newMat = new int[M][N];
                for (int j = 0; j < M; j++)
                    System.arraycopy(current.end[j], 0, newMat[j], 0, N);

                // Swap blank tile
                newMat[current.x2][current.y2] = newMat[newX][newY];
                newMat[newX][newY] = 0;

                Node child = new Node(current.start, newMat, current.level+1, current, current.x1, current.y1, newX, newY);
                if (!child.equals(current.parent)) { //node hasn't already been added or found better way
                    endChildren.add(child);
                }
            }
        }
        startChildren.sort(comp);
        endChildren.sort(comp);
        double f_score = averageHeuristic(startChildren);
        double b_score = averageHeuristic(endChildren);
        // System.out.println("f children: " + startChildren.size() + ". b children: " + endChildren.size() +"\n");
        if(f_score > b_score){ //forward is higher
            current.direction = FORWARD;
            for (Node child : startChildren){
                child.direction = FORWARD;
                stack.add(child);
            }
        } else if(b_score > f_score){ // backwards is higher
            for (Node child : endChildren){
                child.direction = BACKWARD;
                stack.add(child);
            }
        } else if(current.direction == FORWARD){ //if equal branching factor, switch directions(so we benefit from both
            for (Node child : startChildren){
                child.direction = FORWARD;
                stack.add(child);
            }
        } else {
            for (Node child : endChildren){
                child.direction = BACKWARD;
                stack.add(child);
            }
        }
    }

    private double averageHeuristic(List<Node> children) {
        if(children == null || children.isEmpty()){
            return 0.0;
        }
        double size = (double) children.size();
        int sum = 0;
        for (Node c : children){
            sum += c.f;
        }
        return sum / size;
    }

    public int getExpanded(){
        return expanded;
    }

    public void printAnswer(){
        if(answer == null){
            System.out.println("no answer has been found yet");
            return;
        }
        printPath(answer, null);
    }

    // Driver Code
    public static void main(String[] args) {
        // Initial configuration
        int m = 4;
        int n = 4;
        PuzzleMaker pm = new PuzzleMaker(m, n);
        int[][] initial = pm.generatePuzzle();
       /*initial = new int[][]{
                {4, 0, 5},
                {2, 3, 1}
        };


        */
        printMatrix(initial);
        long startTime = System.nanoTime();
        SingleFrontierHeuristicJump solver = new SingleFrontierHeuristicJump(m, n);
        solver.solve(initial);
        solver.printPath(solver.answer, null);
        System.out.println("\nShortest Path length found: " + solver.limit);
        System.out.println("Nodes expanded: " + solver.expanded);
        long timeTaken = System.nanoTime() - startTime;
        System.out.println("time: " + (timeTaken / 1000000000L) + " seconds");
    }
}
