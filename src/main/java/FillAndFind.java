import java.util.*;

public class FillAndFind {
    int expanded = 0;
    final int M;
    final int N;
    final int K;
    final static boolean FORWARD = true;
    final static boolean BACKWARD = false;
    Node answer = null;
    int limit = Integer.MAX_VALUE;
    long startTime = 0L;
    long timeLimit = Long.MAX_VALUE;
    Map<Node, Integer> seenNodes = new HashMap<>();
    Queue<Node> openList = new LinkedList<>();
    Stack<Node> extras = new Stack<>();
    JumpHeuristic heuristic;
    int[][] initial;
    int[][] goal;


    public FillAndFind(int m, int n, int k){
        M = m;
        N = n;
        K = Math.max(k, 1); //k can't be set below 1
        heuristic = goByEarliest; //change this line to edit the jump policy
    }


    static JumpHeuristic bf = (Node current, List<Node> forward, List<Node> backward)->{ //branch factor
        if(forward == null || forward.size() <= 1){ //if can't go one way or obviously better
            return FORWARD;
        }
        if(backward == null || backward.size() <= 1){
            return BACKWARD;
        }
        int fsize = forward.size() + (current.directionTo ? 1 : 0);
        int bsize = backward.size() + (current.directionTo ? 0 : 1);
        if(fsize < bsize){// go to smaller branch
            return FORWARD;
        }
        if(bsize < fsize){
            return BACKWARD;
        }
        return !current.directionTo;
    };

     boolean bf2(Node current, List<Node> forward, List<Node> backward){ //branch factor + grandkids branch factor
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
    }


    static JumpHeuristic goByEarliest = (Node current, List<Node> forward, List<Node> backward) -> {//jump if by first larger
        if(forward == null || forward.size() <= 1){
            return FORWARD;
        }
        if(backward == null || backward.size() <= 1){
            return BACKWARD;
        }
            int f = forward.get(0).f;
            int b = backward.get(0).f;
            if(f < b){
                return FORWARD;
            }
            if(b < f) {
                return BACKWARD;
            }
        return !current.directionTo;
    };


    // Custom comparator for priority queue
    Comparator<Node> comp = new Comparator<Node>() {
        public int compare(Node lhs, Node rhs) {
            return (lhs.f) - (rhs.f);
        }
    };
    // Custom comparator for priority queue
    static Comparator<Node> weighted = new Comparator<Node>() {
        public int compare(Node lhs, Node rhs) {
            return (lhs.f + lhs.cost) - (rhs.f + rhs.cost);

        }
    };
    // Bottom, left, top, right movement
    static int[] row = {1, 0, -1, 0};
    static int[] col = {0, -1, 0, 1};

    // Function to check if coordinates are valid
    boolean isSafe(int x, int y) {
        return (x >= 0 && x < M && y >= 0 && y < N);
    }
    boolean withinTimeLimit(){
        return (System.nanoTime() - startTime) < timeLimit;
    }

    class Node{
        Node parent;
        boolean directionTo;
        int[][] start;
        int[][] end;
        int[][] goalLoc = null;
        int x1, y1;
        int x2, y2;
        int cost;
        int level;
        int f;

        Node(int[][] a, int[][] b, int level, Node parent, int x1, int y1, int x2, int y2) {
            this.start = a;
            this.end = b;
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.level = level;
            this.parent = parent;
            this.directionTo = FORWARD;
            this.cost = Integer.MAX_VALUE;
            this.f = Integer.MAX_VALUE;
        }

        private void calculateCost() {
            int dist = 0;
            if(this.directionTo == FORWARD){
                this.goalLoc = this.parent.goalLoc;
            }
            if(this.goalLoc == null){
                goalLoc = new int[M * N][2];
                for(int i = 0; i < M; i++){
                    for(int j = 0; j < N; j++){
                        goalLoc[end[i][j]] = new int[]{i, j};
                    }
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
            this.cost = dist;
            this.f = this.cost + this.level;
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

    // Function to solve the puzzle using Branch and Bound
    private Node solve(int[][] initial, int x, int y, int[][] goal, long timeLimit) {
        this.timeLimit = timeLimit;
        this.startTime = System.nanoTime();
        this.initial = initial;
        this.goal = goal;
        Node root = new Node(initial, goal, 0, null, x, y, M - 1, N - 1);
        root.directionTo = BACKWARD;
        root.calculateCost();
        seenNodes.put(root, 0);
        if(x != 0 && x != M - 1 && y != 0 && y != N - 1){ //start in the middle
            putForwardChildrenInList(root);
        } else {
            openList.add(root);
        }
        while ((!openList.isEmpty() || !extras.isEmpty()) && withinTimeLimit()){
            Node current = null;
            if(!openList.isEmpty()){
                current = openList.remove();
            } else {
                current = extras.pop();
            }
            findShortestPathToEnd(current);
        }
        return answer;
    }

    /**
     * expands node in the forward direction and puts all valid children on the open list
     * @param current node that's forward children are being expanded (should only be used on the root node)
     */
    private void putForwardChildrenInList(Node current) {
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
                child.directionTo = FORWARD;
                child.calculateCost();
                seenNodes.put(child, child.level);
                openList.add(child);
            }
        }
    }

    private void findShortestPathToEnd(Node current) {
        if (current.f >= limit || (seenNodes.containsKey(current) && seenNodes.get(current) < current.level)) { //base case: prune if guaranteed suboptimal
            return;
        }
        if (current.cost == 0) { //base case: found solution
            /*
            long seconds = (System.nanoTime() - startTime) / 1000000000L;
            System.out.println("Answer of " + current.level + " found at " + seconds + "sec and " + expanded + " expansions");
           */
            answer = current;
            limit = current.level;
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
                    System.arraycopy(current.start[j], 0, newMat[j], 0, N);

                // Swap blank tile
                newMat[current.x1][current.y1] = newMat[newX][newY];
                newMat[newX][newY] = 0;

                Node child = new Node(newMat, current.end, current.level + 1, current, newX, newY, current.x2, current.y2);
                child.directionTo = FORWARD;
                if (!seenNodes.containsKey(child)|| seenNodes.get(child) >= child.level) { //node hasn't been seen with a better value
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

                Node child = new Node(current.start, newMat,current.level + 1, current, current.x1, current.y1, newX, newY);
                child.directionTo = BACKWARD;
                if (!seenNodes.containsKey(child) || seenNodes.get(child) >= child.level) { //node hasn't already been added or found better way
                    endChildren.add(child);
                }

            }
        }

        boolean direction;
        if(openList.size() < K - 2){
            direction = bf2(current, startChildren, endChildren);
            if(direction == FORWARD){
                setCostsAndSort(startChildren);
            } else {
                setCostsAndSort(endChildren);
            }
        } else{
            setCostsAndSort(startChildren);
            setCostsAndSort(endChildren);
            direction = heuristic.jumpDirection(current, startChildren, endChildren); //line that decides jump
        }
        Stack<Node> others = new Stack<>();
        if(direction == FORWARD){
            for(Node child : startChildren){
                if(child.f < limit){
                    if(openList.size() < K){
                        seenNodes.put(child, child.level);
                        openList.add(child);
                    } else {
                        others.push(child);
                    }
                }
            }
        } else {
            for(Node child : endChildren){
                if(child.f < limit){
                    if(openList.size() < K){
                        seenNodes.put(child, child.level);
                        openList.add(child);
                    } else {
                        others.push(child);
                    }
                }
            }
        }
        while (!others.isEmpty()){
            Node child = others.pop();
            seenNodes.put(child, child.level);
            extras.push(child);
        }
    }

    private void setCostsAndSort(List<Node> children){
        for(Node child : children){
            if(child.cost == Integer.MAX_VALUE){
                child.calculateCost();
            }
        }
        children.sort(comp);
    }

    public int getExpanded(){
        return expanded;
    }

    @FunctionalInterface
     interface JumpHeuristic{
        boolean jumpDirection(Node current, List<Node> forward, List<Node> backward);
    }




    // Driver Code
    public static void main(String[] args) {
        // Initial configuration
        int m = 5;
        int n = 4;
        int k = 5000;
        long timeLimit = 30000000000L;
        PuzzleMaker pm = new PuzzleMaker(m, n);
        int[][] initial = pm.generatePuzzle();
        /*initial = new int[][]{
                {5, 3, 9, 0},
                {1, 10, 11, 15},
                {8, 7, 13, 14},
                {4, 6, 2, 12}
        };

         */


        PuzzleMaker.printMatrix(initial);
        long startTime = System.nanoTime();
        FillAndFind solver = new FillAndFind(m, n, k);
        int result = solver.solve(initial, timeLimit);
        System.out.println(result);
        if(solver.answer != null && result < 80) {
            solver.printAnswer();
        }


        System.out.println("\nShortest Path length found: " + result);
        System.out.println("Nodes expanded: " + solver.expanded);
        long timeTaken = System.nanoTime() - startTime;
        System.out.println("time: " + (timeTaken / 1000000000L) + " seconds");
    }
}
