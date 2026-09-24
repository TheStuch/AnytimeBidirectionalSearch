// Import necessary libraries
import java.util.*;

class BidirRectangle{
    int expanded = 0;
     int M = 1;
     int N = 1;
    final int B;
    final static boolean FORWARD = true;
    final static boolean BACKWARD = false;
    Layer root = null;
    Node answer = null;
    int limit = Integer.MAX_VALUE;
    long startTime = 0L;
    long timeLimit = Long.MAX_VALUE;
    Map<Node, Integer> seen = new HashMap<>();
    int[][] goal;

    public BidirRectangle(int m, int n, int b){
        M = m;
        N = n;
        B = Math.max(1, b);
    }

    public BidirRectangle(int m, int n){
        this(m, n, 1);
    }


    // State space tree node
    class Node{
        Node parent;
        boolean directionTo;
        int[][] start;
        int[][] end;
        int x1, y1;
        int x2, y2;
        final int cost;

        Node(int[][] a, int[][] b, Node parent, int x1, int y1, int x2, int y2) {
            this.start = a;
            this.end = b;
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.parent = parent;
            this.directionTo = FORWARD;
            this.cost = calculateCost();
        }

        private int calculateCost() {
            int dist = 0;
            int[][] goalLoc = new int[M * N][2];
            for(int i = 0; i < M; i++){
                for(int j = 0; j < N; j++){
                    goalLoc[end[i][j]] = new int[]{i, j};
                }
            }

            // For each tile in the current board
            for (int i = 0; i < M; i++) {
                for (int j = 0; j < N; j++) {

                    int val = start[i][j];

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
        @Override
        public int hashCode(){
            return (Arrays.deepHashCode(start) * 37) + Arrays.deepHashCode(end);
        }

        @Override
        public boolean equals(Object obj){
            if(obj == null){
                return false;
            }
            if (this == obj) return true;
            if (!(obj instanceof Node)) return false;
            Node other = (Node) obj;
            return Arrays.deepEquals(this.start, other.start) && Arrays.deepEquals(this.end, other.end);
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

    // Custom comparator for priority queue
    Comparator<Node> comp = new Comparator<Node>() {
        public int compare(Node lhs, Node rhs) {
            return (lhs.cost) - (rhs.cost);
        }
    };

    JumpHeuristic bf = (Node current, List<Node> forward, List<Node> backward) -> { //branch factor + grandkids branch factor
        if(forward == null){ //if can't go one way
            return FORWARD;
        }
        if(backward == null){
            return BACKWARD;
        }
        if(forward.size() < backward.size()){// go to smaller branch
            return FORWARD;
        }
        if(backward.size() < forward.size()){
            return BACKWARD;
        }
        int f = 0;
        int b = 0;
        for(Node child : forward){ //count how many children are in edges
            if(child.x1 == 0 || child.x1 == M-1){
                f++;
            }
            if(child.y1 == 0 || child.y1 == N-1){
                f++;
            }
        }
        for(Node child : backward){
            if(child.x2 == 0 || child.x2 == M-1){
                b++;
            }
            if(child.y2 == 0 || child.y2 == N-1){
                b++;
            }
        }
        if(f > b){
            return FORWARD;
        }
        if(b > f){
            return BACKWARD;
        }
        return !current.directionTo;
    };

    JumpHeuristic goByEarliest = (Node current, List<Node> forward, List<Node> backward) -> {//jump if by first larger
        if(forward == null || forward.size() <= 1){
            return FORWARD;
        }
        if(backward == null || backward.size() <= 1){
            return BACKWARD;
        }
        forward.sort(comp);
        backward.sort(comp);
        int fsize = forward.size();
        int bsize = backward.size();
        for(int i = 0; i < Math.min(fsize, bsize); i++){
            int f = forward.get(i).cost;
            int b = backward.get(i).cost;
            if(f < b){
                return  FORWARD;
            }
            if(b < f) {
                return BACKWARD;
            }
        }
        if(fsize != bsize){
            return (fsize < bsize); // returns Forward if forward is smaller
        }
        return !current.directionTo;
    };


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
    static void printPath(Node root, List<int[][]> path) {
        if (root == null) {
            System.out.println();
            for (int[][] matrix : path) {
                PuzzleMaker.printMatrix(matrix);
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

    public void printAnswer(){
        if(answer == null){
            System.out.println("no answer found");
            return;
        }
        printPath(answer, null);
    }

    public int getExpanded(){
        return expanded;
    }

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
        Node start = new Node(initial, goal, null, x, y,  M-1, N-1);
        root.add(start);
        int iteration = 1;
        int repeats = 1;
        while (answer == null && System.nanoTime() - startTime < timeLimit){ //rectangle to find initial answer
            Layer current;
            for(current = root; answer == null && current.level < repeats; current = current.next){ //going through repeats once
                findShortestPathToEnd(current, 1);
            }
            for(int i = 0; i < B && answer == null && current != null; i++){
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
        JumpHeuristic jh = bf;
        if(amount == 1 && !layer.openList.isEmpty() && layer.openList.peek().cost < layer.level){ // when to change heuristic, if set that way
            jh = goByEarliest;
        }
        for(int i = 0; i < amount && !layer.openList.isEmpty(); i++) { //amount of times to pop
            Node current = layer.remove();
            if (current.cost + level >= limit) {  //base case: nothing on the open list is valid
                layer.openList = new PriorityQueue<>(comp);
                continue;
            }
            if (seen.containsKey(current) && seen.get(current) < level) {//base case: prune
                continue;
            }
            if (current.cost == 0) { //base case: found solution
                limit = level;
                //System.out.println("Answer of length " + level + " found!");
                if(answer == null){
                    //System.out.println("Initial answer of length " + level + " found after " + ((System.nanoTime() - startTime)/ 1000000000L) + " seconds");
                }
                answer = current;
                layer.next = null;
                layer.openList = new PriorityQueue<>(comp);
                return;
            }
            seen.put(current, level);
            expanded++;
            if(level + 1 >= limit) { //if the next layer should be deleted
                layer.next = null;
            } else if (layer.next == null){ //if the next layer needs to be made
                layer.createNextLayer();
            }

            // Generate all possible child nodes
            if(layer.next != null){
                List<Node> startChildren = new ArrayList<>();
                List<Node> endChildren = new ArrayList<>();
                for (int j = 0; j < 4; j++) {

                    int newX = current.x1 + row[j], newY = current.y1 + col[j]; //child from start node
                    if (isSafe(newX, newY)) {
                        int[][] newMat = new int[M][N];
                        for (int k = 0; k < M; k++)
                            System.arraycopy(current.start[k], 0, newMat[k], 0, N);

                        // Swap blank tile
                        newMat[current.x1][current.y1] = newMat[newX][newY];
                        newMat[newX][newY] = 0;

                        Node child = new Node(newMat, current.end, current, newX, newY, current.x2, current.y2);
                        child.directionTo = FORWARD;
                        if (!seen.containsKey(child) || seen.get(child) > level) { //node hasn't been seen with a better value
                            startChildren.add(child);
                        }

                    }

                    newX = current.x2 + row[j];
                    newY = current.y2 + col[j]; // child from end node
                    if (isSafe(newX, newY)) {
                        int[][] newMat = new int[M][N];
                        for (int k = 0; k < M; k++)
                            System.arraycopy(current.end[k], 0, newMat[k], 0, N);

                        // Swap blank tile
                        newMat[current.x2][current.y2] = newMat[newX][newY];
                        newMat[newX][newY] = 0;

                        Node child = new Node(current.start, newMat, current, current.x1, current.y1, newX, newY);
                        child.directionTo = BACKWARD;
                        if (!seen.containsKey(child) || seen.get(child) > level) { //node hasn't already been added or found better way
                            endChildren.add(child);
                        }

                    }
                }
                boolean dir = jh.jumpDirection(current, startChildren, endChildren);
                if(dir == FORWARD){        //forward
                    for(Node child : startChildren){
                        layer.next.add(child);
                    }
                }
                if(dir == BACKWARD){ //backward
                    for(Node child : endChildren){
                        layer.next.add(child);
                    }
                }
            }
        }
        //if this is the root and is empty, then there can be no solution within that layer
        if(root.openList.isEmpty()){
            root = root.next;
        }
    }
    
    @FunctionalInterface
    interface JumpHeuristic{
        boolean jumpDirection(Node current, List<Node> forward, List<Node> backward);
    }
    // Driver Code
    public static void main(String[] args) {
        // Initial configuration
        int m = 6;
        int n = 6;
        int b = 1;
        long time = 30000000000L;
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
        BidirRectangle solver = new BidirRectangle(m, n, b);

        int result = solver.solve(initial, time);
        if(result == -1){
            System.out.println("solution couldn't be found for");
            System.out.println(Arrays.deepToString(initial));
        } else{
            System.out.println("\nShortest Path length found: " + solver.limit);
            //solver.printAnswer();
            System.out.println("\nShortest Path length found: " + solver.limit);
        }
        System.out.println("Nodes expanded: " + solver.expanded);
    }
}
