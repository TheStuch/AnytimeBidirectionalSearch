import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
public class GraphTests {

    @Test
    public void compareAlgorithmsOn3x3(){
        int N = 3;
        PuzzleMaker maker = new PuzzleMaker(N);
        int[][] input = maker.generatePuzzle();
        int[] zeroSpot = PuzzleMaker.findZeroLocation(input);
        int[][] goal = maker.generateGoal();
        StandardBnB normal = new StandardBnB(N);
        int stdResult = normal.solve(input, zeroSpot[0], zeroSpot[1], goal);
        if(stdResult == -1){
            System.out.println("Standard BnB couldn't solve it");
        } else {
            System.out.println("Standard BnB found path of length " + stdResult);
        }
        DFBnB anytime = new DFBnB(N);
        int anytimeResult = anytime.publicSolve(input, zeroSpot[0], zeroSpot[1], goal, 30000000000L);
        PuzzleMaker.printMatrix(input);

        if(anytimeResult == -1){
            System.out.println("Anytime DFBnB couldn't solve it");
        } else {
            System.out.println("Anytime DFBnB found path of length " + anytimeResult);
        }
    }
    @Test
    public void compareAlgorithmsOn3x4(){
        int M = 3;
        int N = 4;
        PuzzleMaker maker = new PuzzleMaker(M, N);
        int[][] input = maker.generatePuzzle();
        int[] zeroSpot = PuzzleMaker.findZeroLocation(input);
        PuzzleMaker.printMatrix(input);
        int[][] goal = maker.generateGoal();

        DFBnB anytime = new DFBnB(M, N);
        int anytimeResult = anytime.publicSolve(input, zeroSpot[0], zeroSpot[1], goal, 20000000000L);
        if(anytimeResult == -1){
            System.out.println("Anytime DFBnB couldn't solve it");
        } else {
            System.out.println("Anytime DFBnB found path of length " + anytimeResult);
        }

        StandardBnB normal = new StandardBnB(M, N);
        int stdResult = normal.solve(input, zeroSpot[0], zeroSpot[1], goal);
        if(stdResult == -1){
            System.out.println("Standard BnB couldn't solve it");
        } else {
            System.out.println("Standard BnB found path of length " + stdResult);
        }
    }

    @Test
    public void testingAnytimeDurations4x4(){
        int M = 4;
        int N = 4;
        PuzzleMaker maker = new PuzzleMaker(M, N);
        int[][] input = maker.generatePuzzle();
        int[] zeroSpot = PuzzleMaker.findZeroLocation(input);
        PuzzleMaker.printMatrix(input);
        int[][] goal = maker.generateGoal();

        long[] times = new long[]{10000000000L, 20000000000L, 40000000000L, 60000000000L};
        for(long time : times){
            DFBnB anytime = new DFBnB(M, N);
            int result = anytime.publicSolve(input, zeroSpot[0], zeroSpot[1], goal, time);
            System.out.print("In " + (time / 1000000000L) + " seconds, ");
            if(result == -1){
                System.out.println("no path was found");
            } else {
                System.out.println("the algorithm found a shortest path of " + result);
            }
        }
    }
}
