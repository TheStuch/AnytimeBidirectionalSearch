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
        do {
            Collections.shuffle(list);
        } while (!isSolvable(list));
        int[][] matrix = new int[M][N];
        int index = 0;
        for(int i = 0; i < M; i++){
            for(int j = 0; j < N; j++){
                matrix[i][j] = list.get(index++);
            }
        }
        return matrix;
    }

    private boolean isSolvable(List<Integer> list) {
        int inversions = 0;
        int zeroLocation = -1;
        for(int i = 0; i < list.size(); i++){
            int curr = list.get(i);
            if(curr == 0){
                zeroLocation = i;
                continue;
            }
            for(int j = i + 1; j < list.size(); j++){
                if(list.get(j) != 0 && curr > j){
                    inversions++;
                }
            }
        }
        if(N % 2 == 1){ //if N is odd
            return inversions % 2 == 0;
        }
        if((M - 1 - (zeroLocation/N)) % 2 == 0){ //empty cell is even distance from bottom
            return inversions % 2 == 1;
        }
        return inversions % 2 == 0;
    }

}
