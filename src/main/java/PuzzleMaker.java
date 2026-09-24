import java.util.*;
public class PuzzleMaker {
    final int M;
    final int N;
    
    public PuzzleMaker(int m, int n){
        this.M =  m;
        this.N = n;
    }
    
    public PuzzleMaker(int n) {
        this(n, n);
    }

    /**
     * 
     * @return M x N matrix representing a random solvable puzzle state
     */
    public int[][] generatePuzzle(){
        List<Integer> list = new ArrayList<>();
        for(int i = 0; i < M*N; i++){
            list.add(i);
        }
        int[][] matrix = null;
        while(matrix == null){
            Collections.shuffle(list);
            matrix = makeSolvable(list);
        }
        int index = 0;
        for(int i = 0; i < M; i++){
            for(int j = 0; j < N; j++){
                matrix[i][j] = list.get(index++);
            }
        }
        return matrix;
    }

    public int[][] generateGoal(){
        int[][] matrix = new int[M][N];
        int val = 1;
        for(int i = 0; i < M; i++){
            for(int j = 0; j < N; j++){
                matrix[i][j] = val++;            }
        }
        matrix[M-1][N-1] = 0;
        return matrix;
    }
    public static int[][] generateGoal(int m, int n){
        int[][] matrix = new int[m][n];
        int val = 1;
        for(int i = 0; i < m; i++){
            for(int j = 0; j < n; j++){
                matrix[i][j] = val++;            }
        }
        matrix[m-1][n-1] = 0;
        return matrix;
    }


    //returns the list as a matrix, if solvable
    //if unsolvable, returns null
    private int[][] makeSolvable(List<Integer> list) {
        int inversions = 0;
        int zeroLocation = 0;
        for(int i = 0; i < list.size(); i++){
            int curr = list.get(i);
            if(curr == 0){
                continue;
            }
            for(int j = i + 1; j < list.size(); j++){
                if(list.get(j) != 0 && curr > list.get(j)){
                    inversions++;
                }
            }
        }
        int index = 0;
        int[][] matrix = new int[M][N];
        for (int i = 0; i < M; i++){
            for(int j = 0; j < N; j++){
                matrix[i][j] = list.get(index++);
                if(matrix[i][j] == 0){
                    zeroLocation = M - i;
                }
            }
        }
        //System.out.println("list version " + list);
        //System.out.println("Matrix version: " + Arrays.deepToString(matrix));
        if(N % 2 == 1){ //if N is odd
            if(inversions % 2 == 0){
                //System.out.println("N is odd and hase even inversions");
                return matrix;
            }
            return null;
        }
        if(zeroLocation % 2 == 1 && inversions % 2 == 0){ //empty cell is odd distance from bottom
            //System.out.println("N is even, 0 on odd row, and has even inversions");
            return matrix;
        }
        if(zeroLocation % 2 == 0 && inversions % 2 == 1){
            //System.out.println("N is even, 0 on even row, and has odd inversions");
            return matrix;
        }
        return null;
    }

    public static int[] findZeroLocation(int[][] matrix){
        for(int i = 0; i < matrix.length; i++){
            for(int j = 0; j < matrix[i].length; j++){
                if(matrix[i][j] == 0){
                    return new int[]{i, j};
                }
            }
        }
        throw new IllegalArgumentException("matrix has no zero");
        //return null;
    }
    public static void printMatrix(int[][] mat) {
        for (int[] row : mat) {
            for (int val : row) {
                System.out.print(val + " ");
            }
            System.out.println();
        }
    }

    public static int calculateCost(int[][] mat) {
        if(mat == null || mat.length == 0 || mat[0].length == 0){
            return -1;
        }
        int dist = 0;
        int M = mat.length;
        int N = mat[0].length;
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

}
