import java.util.*;
import java.lang.*;

public class BiBnBHeuristicJump {
    int expanded = 0;
    final int M;
    final int N;
    final static boolean FORWARD = true;
    final static boolean BACKWARD = false;
    Node answer = null;
    int limit = Integer.MAX_VALUE;
    long startTime = 0L;
    long timeLimit = Long.MAX_VALUE;
    Map<HashMatrix, Node> seenForward = new HashMap<>();
    Map<HashMatrix, Node> seenBackward = new HashMap<>();
    Set<Node> seenNodes = new HashSet<>();
    JumpHeuristic heuristic;
    int[][] initial;
    int[][] goal;
    Stack<Node> stack;


    public BiBnBHeuristicJump(int m, int n){
        M = m;
        N = n;
        heuristic = jis; //change this line to edit the jump policy
    }

    public BiBnBHeuristicJump(int n){
        this(n, n);
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
    static Comparator<Node> exponential = new Comparator<Node>() {
        public int compare(Node lhs, Node rhs) {
            return (lhs.modifiedCost) - (rhs.modifiedCost);
        }
    };

    static JumpHeuristic earliestExponential = (Node current, List<Node> forward, List<Node> backward) ->{
        if(forward == null || forward.isEmpty()){
            return FORWARD;
        }
        if(backward == null || backward.isEmpty()){
            return BACKWARD;
        }
        forward.sort(exponential);
        backward.sort(exponential);
        if(forward.get(0).modifiedCost < backward.get(0).modifiedCost){
            return FORWARD;
        }
        if(backward.get(0).modifiedCost < forward.get(0).modifiedCost){
            return BACKWARD;
        }
        int fSum = 0;
        for(Node n : forward){
            fSum += n.modifiedCost - n.flevel - n.blevel;
        }
        double fAvg = fSum / (double) forward.size();
        int bSum = 0;
        for(Node n : backward){
            bSum += n.modifiedCost - n.flevel - n.blevel;
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

    static JumpHeuristic earliestWeighted = (Node current, List<Node> forward, List<Node> backward) ->{
        if(forward == null || forward.isEmpty()){
            return FORWARD;
        }
        if(backward == null || backward.isEmpty()){
            return BACKWARD;
        }
        forward.sort(weighted);
        backward.sort(weighted);
        if(forward.get(0).cost < backward.get(0).cost){
            return FORWARD;
        }
        if(backward.get(0).cost < forward.get(0).cost){
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

    /*
    *  generates all grandchildren created by expanding both nodes once
    * adds the grandchildren to the stack in sorted order and empties the input lists so they aren't added
    * if any children are answers, set them as answer and return
     */
     JumpHeuristic simultaneous = (Node current, List<Node> forward, List<Node> backward) ->{
        List<Node> grandchildren = new ArrayList<>();
        for(Node fchild : forward){
            if(fchild.cost == 0){       //checking if child is a goal
                if(fchild.f < limit){
                   seenForward.put(fchild.start, fchild);
                   seenBackward.put(fchild.end, fchild);
                    limit = fchild.f;
                    answer = fchild;
                }
                return true;
            }
            for(Node bchild : backward){
                Node grandkid = new Node(fchild.start, bchild.end, fchild.flevel, bchild.blevel, fchild, fchild.x1, fchild.y1, bchild.x2, bchild.y2);
                grandkid.directionTo = BACKWARD;
                grandchildren.add(grandkid);
            }
        }
        forward.clear();
        backward.clear();
        grandchildren.sort(comp.reversed());
        for (Node n : grandchildren){
            if (n.f <= limit){
              seenForward.put(n.start, n);
              seenBackward.put(n.end, n);
              stack.add(n);
            }
        }
        return true;
    };

    static JumpHeuristic[] heuristics = new JumpHeuristic[]{alternating, bf, jis, jil, goByEarliest, earliestModified, earliestExponential, earliestWeighted};

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
        Node otherParent = null;
        boolean directionTo;
        HashMatrix start;
        HashMatrix end;
        int x1, y1;
        int x2, y2;
        final int cost;
        int modifiedCost = 0;
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
            this.modifiedCost = flevel + blevel;
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
                    int manhattan = Math.abs(i - goalX) + Math.abs(j - goalY);
                    dist += manhattan;
                    this.modifiedCost += manhattan * manhattan; //modified cost has sum of squared manhattan distance
                }
            }

            return dist;
        }
        @Override
        public int hashCode(){
            return Objects.hash(start, end);
        }

        @Override
        public boolean equals(Object obj){
            if(obj == null){
                return false;
            }
            if (this == obj) return true;
            if (!(obj instanceof Node)) return false;
            Node other = (Node) obj;
            return this.start.equals(other.start) && this.end.equals(other.end);
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

    public void printAnswer(){
        if(answer == null){
            System.out.println("no answer found");
            return;
        }
        printPath(answer);
    }

    static void printPath(Node curr){
        List<int[][]> path = new ArrayList<>();
        if(curr == null){
            return;
        }

        path.add(curr.start.matrix);
        curr = curr.parent;
        while(curr != null && curr.otherParent == null){ //while the tree has only one parent
            if(Arrays.deepEquals(curr.start.matrix, path.get(0))){ //if starts the same, it was a back change
                path.add(curr.end.matrix);
                //System.out.print("backwards ");
            }else {     //change was done at the front
                path.add(0, curr.start.matrix);
                //System.out.print("forwards ");
            }
            curr = curr.parent;
        }
        if(curr != null){
            Node front = curr.parent;
            Node back = curr.otherParent;
            while(front != null){
                if(!Arrays.deepEquals(front.start.matrix, path.get(0))){
                    path.add(0, front.start.matrix);
                }
                front = front.parent;
            }
            while(back != null){
                if(!Arrays.deepEquals(back.end.matrix, path.get(path.size()-1))){
                    path.add(back.end.matrix);
                }
                back = (back.otherParent != null ? back.otherParent : back.parent); //go up right side if it exists
            }
        }
        System.out.println();
        for (int[][] matrix : path) {
            PuzzleMaker.printMatrix(matrix);
            System.out.println();
        }
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
        seenForward.put(start, root);
        seenBackward.put(end, root);
        seenNodes.add(root);
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
        if (current.cost == 0) { //base case: found solution
            answer = current;
            limit = current.flevel + current.blevel;
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
                if (!seenForward.containsKey(child.start)|| seenForward.get(child.start).flevel >= child.flevel) { //node isn't the parent
                    startChildren.add(child);
                } else if(!seenNodes.contains(child)){ //if node could get to lower answer
                    child.f -= child.flevel;
                    child.modifiedCost -= child.flevel;
                    child.flevel = seenForward.get(child.start).flevel;
                    child.f += child.flevel;
                    child.modifiedCost += child.flevel;
                    child.otherParent = child.parent;
                    child.parent = seenForward.get(child.start);
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
                if (!seenBackward.containsKey(child.end) || seenBackward.get(child.end).blevel >= child.blevel) { //node hasn't already been added or found better way
                    endChildren.add(child);
                } else if(!seenNodes.contains(child)){ //if node could get to lower answer
                    child.f -= child.blevel;
                    child.modifiedCost -= child.blevel;
                    child.blevel = seenBackward.get(child.end).blevel;
                    child.f += child.blevel;
                    child.modifiedCost += child.blevel;
                    child.otherParent = seenBackward.get(child.end);
                    endChildren.add(child);
                }

            }
        }
        startChildren.sort(comp);
        endChildren.sort(comp);

        boolean direction = heuristic.jumpDirection(current, startChildren, endChildren); //line that decides jump
        if(direction == FORWARD){
            //startChildren.sort(weighted);
            for(int i = startChildren.size() - 1; i >= 0; i--){
                Node child = startChildren.get(i);
                if(child.f < limit){
                    seenForward.put(child.start, child);
                    seenNodes.add(child);
                    child.directionTo = FORWARD;
                    stack.add(child);
                }
            }
        } else {
            endChildren.sort(weighted);
            for(int i = endChildren.size() -1; i >= 0; i--){
                Node child = endChildren.get(i);
                if(child.f < limit){
                    seenBackward.put(child.end, child);
                    seenNodes.add(child);
                    child.directionTo = BACKWARD;
                    stack.add(child);
                }
            }
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
        int m = 4;
        int n = 4;
        long timeLimit = 40000000000L;
        PuzzleMaker pm = new PuzzleMaker(m, n);
        int[][] initial = pm.generatePuzzle();
       initial = new int[][]{
                {5, 2, 3, 4},
                {7, 1, 11, 8},
                {9, 0, 15, 10},
                {13, 14, 6, 12}
        };

        PuzzleMaker.printMatrix(initial);
        long startTime = System.nanoTime();
        BiBnBHeuristicJump solver = new BiBnBHeuristicJump(m, n);

        int result = solver.solve(initial, timeLimit);
       System.out.println(result);
        if(solver.answer != null && result < 80) {
            BiBnBHeuristicJump.printPath(solver.answer);
        }


        System.out.println("\nShortest Path length found: " + result);
        System.out.println("Nodes expanded: " + solver.expanded);
        long timeTaken = System.nanoTime() - startTime;
        System.out.println("time: " + (timeTaken / 1000000000L) + " seconds");
    }
}
