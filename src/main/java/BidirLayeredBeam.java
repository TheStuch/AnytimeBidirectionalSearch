import java.util.*;
import java.lang.*;

public class BidirLayeredBeam {
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


    public BidirLayeredBeam(int m, int n, int k){
        M = m;
        N = n;
        K = Math.max(k, 1); //k can't be set below 1
        heuristic = goByEarliest; //change this line to edit the jump policy
    }

    static JumpHeuristic alternating = (Node current, List<Node> forward, List<Node> backward)-> !current.directionTo;


    static JumpHeuristic bf = (Node current, List<Node> forward, List<Node> backward)->{ //branch factor
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
        return !current.directionTo;
    };

    static JumpHeuristic jis =  (Node current, List<Node> forward, List<Node> backward) -> {//jump if smaller
        if(forward == null || forward.size() <= 1){
            return FORWARD;
        }
        if(backward == null || backward.size() <= 1){
            return BACKWARD;
        }
        int fSum = 0;
        for(Node n : forward){
            fSum += n.cost;
        }
        double fAvg = fSum / (double) forward.size();
        int bSum = 0;
        for(Node n : backward){
            bSum += n.cost;
        }
        double bAvg = bSum / (double) backward.size();
        if(fAvg < bAvg){
            return FORWARD;
        }
        if(bAvg < fAvg){
            return BACKWARD;
        }
        return !current.directionTo;
    };

    static JumpHeuristic jil = (Node current, List<Node> forward, List<Node> backward) -> {//jump if larger
        if(forward == null || forward.size() <= 1){
            return FORWARD;
        }
        if(backward == null || backward.size() <= 1){
            return BACKWARD;
        }
        int fSum = 0;
        for(Node n : forward){
            fSum += n.f;
        }
        double fAvg = fSum / (double) forward.size();
        int bSum = 0;
        for(Node n : backward){
            bSum += n.f;
        }
        double bAvg = bSum / (double) backward.size();
        if(fAvg < bAvg){
            return BACKWARD;
        }
        if(bAvg < fAvg){
            return FORWARD;
        }
        return !current.directionTo;
    };

    static JumpHeuristic goByEarliest = (Node current, List<Node> forward, List<Node> backward) -> {//jump if by first larger
        if(forward == null || forward.size() <= 1){
            return FORWARD;
        }
        if(backward == null || backward.size() <= 1){
            return BACKWARD;
        }
        int fsize = forward.size();
        int bsize = backward.size();
        for(int i = 0; i < Math.min(fsize, bsize); i++){
            int f = forward.get(i).f;
            int b = backward.get(i).f;
            if(f < b){
                return  (i == 0 ? FORWARD: BACKWARD);
            }
            if(b < f) {
                return (i == 0 ? BACKWARD : FORWARD);
            }
        }
        return !current.directionTo;
    };
    static JumpHeuristic earliestModified = (Node current, List<Node> forward, List<Node> backward) -> {//follows the BiXDFBnB research's code
        if(forward == null || forward.isEmpty()){
            return FORWARD;
        }
        if(backward == null || backward.isEmpty()){
            return BACKWARD;
        }
        if(forward.get(0).f < backward.get(0).f){
            return FORWARD;
        }
        if(backward.get(0).f < forward.get(0).f){
            return BACKWARD;
        }
        int fSum = 0;
        for(Node n : forward){
            fSum += n.f;
        }
        double fAvg = fSum / (double) forward.size();
        int bSum = 0;
        for(Node n : backward){
            bSum += n.f;
        }
        double bAvg = bSum / (double) backward.size();
        if(fAvg < bAvg){
            return FORWARD;
        }
        if(bAvg < fAvg){
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
    static JumpHeuristic[] heuristics = new JumpHeuristic[]{alternating, bf, jis, jil, goByEarliest, earliestModified};

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
        Node otherParent = null;
        boolean directionTo;
        int[][] start;
        int[][] end;
        int x1, y1;
        int x2, y2;
        final int cost;
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
            this.cost = calculateCost();
            this.f = this.cost + this.level;
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
            path.add(root.start);
        } else {
            if(Arrays.deepEquals(root.start, path.get(0))){ //if starts the same, it was a back change
                path.add(root.end);
                System.out.print("backwards ");
            }else {     //change was done at the front
                path.add(0, root.start);
                System.out.print("forwards ");
            }
        }
        printPath(root.parent, path);
    }

    public void printAnswer(){
        if(answer == null){
            System.out.println("no answer found");
            return;
        }
        printPath(answer, null);
    }

    public void setJumpHeuristic(int n){
        if(n < 0 || n >= heuristics.length){
            System.out.println("no heuristic exists at spot " + n);
            return;
        }
        this.heuristic = heuristics[n];
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
        openList.add(root);
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

    private void findShortestPathToEnd(Node current) {
        if (current.f > limit || (seenNodes.containsKey(current) && seenNodes.get(current) <= current.f)) { //base case: prune if guaranteed suboptimal
            return;
        }
        if (current.cost == 0) { //base case: found solution
            answer = current;
            limit = current.level;
            return;
        }
        seenNodes.put(current, current.f);
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
                if (!seenNodes.containsKey(child)|| seenNodes.get(child) >= child.f) { //node hasn't been seen with a better value
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
                if (!seenNodes.containsKey(child) || seenNodes.get(child) >= child.f) { //node hasn't already been added or found better way
                    endChildren.add(child);
                }

            }
        }
        startChildren.sort(comp);
        endChildren.sort(comp);

        boolean direction;
        direction = heuristic.jumpDirection(current, startChildren, endChildren); //line that decides jump
        Stack<Node> others = new Stack<>();
        if(direction == FORWARD){
            //startChildren.sort(weighted);
            for(Node child : startChildren){
                if(child.f < limit){
                    child.directionTo = FORWARD;
                    if(openList.size() < K){
                        openList.add(child);
                    } else {
                        others.push(child);
                    }
                }
            }
        } else {
            for(Node child : endChildren){
                if(child.f < limit){
                    child.directionTo = BACKWARD;
                    if(openList.size() < K){
                        openList.add(child);
                    } else {
                        others.push(child);
                    }
                }
            }
        }
        while (!others.isEmpty()){
            extras.push(others.pop());
        }
    }

    public int getExpanded(){
        return expanded;
    }

    @FunctionalInterface
    static interface JumpHeuristic{
        boolean jumpDirection(Node current, List<Node> forward, List<Node> backward);
    }




    // Driver Code
    public static void main(String[] args) {
        // Initial configuration
        int m = 5;
        int n = 4;
        int k = 1000;
        long timeLimit = 60000000000L;
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
        BidirLayeredBeam solver = new BidirLayeredBeam(m, n, k);
        solver.setJumpHeuristic(4);
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
