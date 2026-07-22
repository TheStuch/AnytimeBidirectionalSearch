import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;
public class GraphTests {

    @Test
    public void compareAlgorithmsOn3x3(){
        int N = 3;
        PuzzleMaker maker = new PuzzleMaker(N);
        int[][] input = maker.generatePuzzle();
        //System.out.println(Arrays.deepToString(input));
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
        int anytimeResult = anytime.solve(input, 30000000000L);
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

    @Test
    public void testingSFBDSOn4x4(){
        int N = 4;
        PuzzleMaker pm = new PuzzleMaker(N);
        int[][] initial = pm.generatePuzzle();
        PuzzleMaker.printMatrix(initial);
        long startTime = System.nanoTime();
        SFBDSIDA solver = new SFBDSIDA(N);
        int result = solver.solve(initial);
        long time = (System.nanoTime() - startTime) / 1000000000L;
        solver.printAnswer();
        System.out.println("Shortest path length found: " + result);
        System.out.println("Nodes expanded: " + solver.getExpanded());
        System.out.println("Time taken: " + time + " seconds");
    }

    @Test
    public void testingJIL1On4x4(){
        int N = 4;
        PuzzleMaker pm = new PuzzleMaker(N);
        int[][] initial = pm.generatePuzzle();
        PuzzleMaker.printMatrix(initial);
        long startTime = System.nanoTime();
        SingleFrontierHeuristicJump solver = new SingleFrontierHeuristicJump(N);
        int result = solver.solve(initial);
        long time = (System.nanoTime() - startTime) / 1000000000L;
        solver.printAnswer();
        System.out.println("Shortest path length found: " + result);
        System.out.println("Nodes expanded: " + solver.getExpanded());
        System.out.println("Time taken: " + time + " seconds");
    }

    @Test
    public void testingJIL1On3x3(){
        int N = 3;
        PuzzleMaker pm = new PuzzleMaker(N);
        int[][] initial = pm.generatePuzzle();
        PuzzleMaker.printMatrix(initial);
        long startTime = System.nanoTime();
        SingleFrontierHeuristicJump solver = new SingleFrontierHeuristicJump(N);
        int result = solver.solve(initial);
        long time = (System.nanoTime() - startTime) / 1000000000L;
        solver.printAnswer();
        System.out.println("Shortest path length found: " + result);
        System.out.println("Nodes expanded: " + solver.getExpanded());
        System.out.println("Time taken: " + time + " seconds");
    }

    @Test
    public void compareSFBDSHeuristics3x3(){
        int N = 3;
        for (int i = 0; i < 5; i++){
            PuzzleMaker pm = new PuzzleMaker(N);
            int[][] initial = pm.generatePuzzle();
            PuzzleMaker.printMatrix(initial);
            SFBDSIDA bf = new SFBDSIDA(N);
            int result1 = bf.solve(initial);
            SingleFrontierHeuristicJump jil = new SingleFrontierHeuristicJump(N);
            int result2 = jil.solve(initial);
            assertEquals(result1, result2);
            System.out.println();
        }
    }

    @Test
    public void compareSFBDSHeuristics4x4(){
        int N = 4;
        PuzzleMaker pm = new PuzzleMaker(N);
        int[][] initial = pm.generatePuzzle();
        PuzzleMaker.printMatrix(initial);
        long startTime = System.nanoTime();
        SFBDSIDA bf = new SFBDSIDA(N);
        int result1 = bf.solve(initial);
        long time1 = (System.nanoTime() - startTime) / 1000000000L;
        System.out.println("Branching factor results:");
        System.out.println("Shortest path length found: " + result1);
        System.out.println("Nodes expanded: " + bf.getExpanded());
        System.out.println("Time taken: " + time1 + " seconds");
        startTime = System.nanoTime();
        SingleFrontierHeuristicJump jil = new SingleFrontierHeuristicJump(N);
        int result2 = jil.solve(initial);
        long time2 = (System.nanoTime() - startTime) / 1000000000L;
        System.out.println("\nJIL(1) results:");
        System.out.println("Shortest path length found: " + result2);
        System.out.println("Nodes expanded: " + jil.getExpanded());
        System.out.println("Time taken: " + time2 + " seconds");
    }

    @Test
    public void anytimeOptimalityTest3x3(){
        int M = 3;
        int N = 3;
        int runs = 15;
        for(int i = 0; i < runs; i++){
            PuzzleMaker pm = new PuzzleMaker(M, N);
            int[][] initial = pm.generatePuzzle();
            SingleFrontierHeuristicJump jil = new SingleFrontierHeuristicJump(M, N);
            int expected = jil.solve(initial);
            DFBnB bnb = new DFBnB(M, N);
            int actual = bnb.solve(initial);
            assertEquals(expected, actual);
        }
    }

    @Test
    public void anytimeOptimalityTest3x4(){
        int M = 3;
        int N = 4;
            PuzzleMaker pm = new PuzzleMaker(M, N);
            int[][] initial = pm.generatePuzzle();
            PuzzleMaker.printMatrix(initial);
            SingleFrontierHeuristicJump jil = new SingleFrontierHeuristicJump(M, N);
            int expected = jil.solve(initial);
            System.out.println("JIL got " + expected);
            DFBnB bnb = new DFBnB(M, N);
            int actual = bnb.solve(initial);
            assertEquals(expected, actual);
    }

    @Test
    public void testingJIL1On5x5(){
        int N = 5;
        PuzzleMaker pm = new PuzzleMaker(N);
        int[][] initial = pm.generatePuzzle();
        PuzzleMaker.printMatrix(initial);
        long startTime = System.nanoTime();
        SingleFrontierHeuristicJump solver = new SingleFrontierHeuristicJump(N);
        int result = solver.solve(initial);
        long time = (System.nanoTime() - startTime) / 1000000000L;
        solver.printAnswer();
        System.out.println("Shortest path length found: " + result);
        System.out.println("Nodes expanded: " + solver.getExpanded());
        System.out.println("Time taken: " + time + " seconds");
    }

    @Test
    public void BiDFBnBOptimalityTest(){
        int N = 3;
        int runs = 10;
        for(int i = 0; i < runs; i++){
            PuzzleMaker pm = new PuzzleMaker(N);
            int[][] initial = pm.generatePuzzle();
            SingleFrontierHeuristicJump jil = new SingleFrontierHeuristicJump(N);
            int expected = jil.solve(initial);
            BiBnBHeuristicJump bidir = new BiBnBHeuristicJump(N);
            int actual = bidir.solve(initial);
            assertEquals(expected, actual);
            System.out.println("success");
        }
    }

    @Test
    public void comparingAnytimeDurations(){
        int M = 4;
        int N = 4;
        PuzzleMaker maker = new PuzzleMaker(M, N);
        int[][] input = maker.generatePuzzle();
        int[] zeroSpot = PuzzleMaker.findZeroLocation(input);
        PuzzleMaker.printMatrix(input);
        int[][] goal = maker.generateGoal();

        long[] times = new long[]{10000000000L, 30000000000L, 60000000000L};
        for(long time : times){
            BiBnBHeuristicJump bidir = new BiBnBHeuristicJump(M, N);
            int result1 = bidir.solve(input, time);
            System.out.print("In " + (time / 1000000000L) + " seconds, ");
            if(result1 == -1){
                System.out.println("Bidirectional Heuristic found nothing");
            } else {
                System.out.println("Bidirectional Heuristic found " + result1);
            }

            DFBnB unidir = new DFBnB(M, N);
            int result2 = unidir.solve(input, time);
            if(result2 == -1){
                System.out.println("Unidirectional found nothing");
            } else {
                System.out.println("Unidirectional found " + result2);
            }
        }
    }

    @Test
    public void bidirectionalRelativeSuccessCount(){
        int M = 4;
        int N = 4;
        int runs = 5;
        long time = 60000000000L;
        int success = 0;
        int failure = 0;
        int mid = 0;
        int bad = 0;
        for(int i = 0; i < runs; i++){
            PuzzleMaker pm = new PuzzleMaker(M, N);
            int[][] initial = pm.generatePuzzle();
            //PuzzleMaker.printMatrix(initial);
            BiBnBHeuristicJump bidir = new BiBnBHeuristicJump(M, N);
            int result = bidir.solve(initial, time);
            if(result == -1){
                System.out.println("fail");
                failure++;
            } else if (result <= 150){
                System.out.println("Success! " + result);
                success++;
            } else if(result <= 1000){
                System.out.println("decent: " + result);
                mid++;
            }else{
                System.out.println("bad answer: " + result);
                bad++;
            }
        }
        System.out.println("Final results:");
        System.out.println("Successes: " + success);
        System.out.println("Decent: " + mid);
        System.out.println("High answers: " + bad);
        System.out.println("Failures: " + failure);
    }

    @Test
    public void compareAllBidirectionalHeuristics(){
        int M = 4;
        int N = 4;
        long time = 60000000000L;
        String[] heuristics = new String[]{"alternating", "bf", "jis", "jil", "goByEarliest", "earliestModified", "earliestExponential", "earliestWeighted"};
        PuzzleMaker pm = new PuzzleMaker(M, N);
        int[][] initial = pm.generatePuzzle();
        PuzzleMaker.printMatrix(initial);
        DFBnB unidir = new DFBnB(M, N);
        int result2 = unidir.solve(initial, time);
        if(result2 == -1){
            System.out.println("Unidirectional found nothing");
        } else {
            System.out.println("Unidirectional found " + result2);
        }
        for(int i = 0; i < heuristics.length; i++){
            BiBnBHeuristicJump bidir = new BiBnBHeuristicJump(M, N);
            bidir.setJumpHeuristic(i);
            int result = bidir.solve(initial, time);
            if(result == -1){
                System.out.println(heuristics[i] + " got nothing");
            } else {
                System.out.println(heuristics[i] + " got shortest path of " + result);
            }
        }
    }

    @Test
    public void unidirectionalLayeredBeamOptimalityTest(){
        int M = 3;
        int N = 3;
        int runs = 10;
        for(int i = 0; i < runs; i++){
            PuzzleMaker pm = new PuzzleMaker(M, N);
            int[][] initial = pm.generatePuzzle();
            SingleFrontierHeuristicJump jil = new SingleFrontierHeuristicJump(N);
            int expected = jil.solve(initial);
            UnidirLayeredBeam ulb = new UnidirLayeredBeam(M, N, 3);
            int actual = ulb.solve(initial);
            assertEquals(expected, actual);
            System.out.println("success");
        }
    }

    @Test
    public void comparingLayeredBeamSizes(){
        int M = 4;
        int N = 4;
        long time = 60000000000L;
        PuzzleMaker pm = new PuzzleMaker(M, N);
        int[][] initial = pm.generatePuzzle();
        PuzzleMaker.printMatrix(initial);
        for(int k = 1; k <= 16; k <<= 1){
            UnidirLayeredBeam ulb = new UnidirLayeredBeam(M, N, k);
            int result = ulb.solve(initial, time);
            System.out.println("For k = " + k + ", result was " + result);
        }
    }
    @Test
    public void bidirectionalLayeredBeamOptimalityTest(){
        int M = 3;
        int N = 3;
        int runs = 10;
        for(int i = 0; i < runs; i++){
            PuzzleMaker pm = new PuzzleMaker(M, N);
            int[][] initial = pm.generatePuzzle();
            SingleFrontierHeuristicJump jil = new SingleFrontierHeuristicJump(N);
            int expected = jil.solve(initial);
            BidirLayeredBeam blb = new BidirLayeredBeam(M, N, 10);
            int actual = blb.solve(initial);
            assertEquals(expected, actual);
            System.out.println("success");
        }
    }

    @Test
    public void comparingBidirectionalLayeredBeamSizes(){
        int M = 4;
        int N = 4;
        long time = 60000000000L;
        int runs = 2;
        for(int i = 0; i < runs; i++){
            PuzzleMaker pm = new PuzzleMaker(M, N);
            int[][] initial = pm.generatePuzzle();
            PuzzleMaker.printMatrix(initial);
            for(int k = 0; k <= 250; k += 50){
                UnidirLayeredBeam ulb = new UnidirLayeredBeam(M, N, k);
                int result1 = ulb.solve(initial, time);
                System.out.println("For k = " + k + ",");
                System.out.print("unidirectional got " + result1);
                BidirLayeredBeam bilb = new BidirLayeredBeam(M, N, k);
                int result2 = bilb.solve(initial, time);
                System.out.println(". Bidirectional got " + result2);
            }
        }
    }
    @Test
    public void comparingDirectionsOfLayeredBeams(){
        int M = 4;
        int N = 4;
        int K = 1000;
        long time = 30000000000L;
        int runs = 10;
        for(int i = 0; i < runs; i++){
            PuzzleMaker pm = new PuzzleMaker(M, N);
            int[][] initial = pm.generatePuzzle();
            PuzzleMaker.printMatrix(initial);
            UnidirLayeredBeam ulb = new UnidirLayeredBeam(M, N, K);
            int result1 = ulb.solve(initial, time);
            System.out.println("Unidirectional got " + result1);
            BidirLayeredBeam bilb = new BidirLayeredBeam(M, N, K);
            int result2 = bilb.solve(initial, time);
            System.out.println("Bidirectional got " + result2);
        }
    }

    @Test
    public void compareAllBidirectionalHeuristicsLayeredBeam4x4(){
        int M = 4;
        int N = 4;
        int K = 1000;
        long time = 60000000000L;
        String[] heuristics = new String[]{"alternating", "bf", "jis", "jil", "goByEarliest", "earliestModified", "earliestWeighted"};
        PuzzleMaker pm = new PuzzleMaker(M, N);
        int[][] initial = pm.generatePuzzle();
        PuzzleMaker.printMatrix(initial);
        UnidirLayeredBeam unidir = new UnidirLayeredBeam(M, N, K);
        int result2 = unidir.solve(initial, time);
        if(result2 == -1){
            System.out.println("Unidirectional found nothing");
        } else {
            System.out.println("Unidirectional found " + result2);
        }
        for(int i = 0; i < heuristics.length; i++){
            BidirLayeredBeam bidir = new BidirLayeredBeam(M, N, K);
            bidir.setJumpHeuristic(i);
            int result = bidir.solve(initial, time);
            if(result == -1){
                System.out.println(heuristics[i] + " got nothing");
            } else {
                System.out.println(heuristics[i] + " got shortest path of " + result);
            }
        }
    }

    @Test
    public void comparingDirectionsOfLayeredBeams5x4(){
        int M = 5;
        int N = 4;
        int K = 1000;
        long time = 60000000000L;
        int runs = 5;
        for(int i = 0; i < runs; i++){
            PuzzleMaker pm = new PuzzleMaker(M, N);
            int[][] initial = pm.generatePuzzle();
            PuzzleMaker.printMatrix(initial);
            UnidirLayeredBeam ulb = new UnidirLayeredBeam(M, N, K);
            int result1 = ulb.solve(initial, time);
            System.out.println("Unidirectional got " + result1);
            BidirLayeredBeam bilb = new BidirLayeredBeam(M, N, K);
            int result2 = bilb.solve(initial, time);
            System.out.println("Bidirectional got " + result2);
        }
    }

    @Test
    public void compareAllBidirectionalHeuristicsLayeredBeam5x4(){
        int M = 5;
        int N = 4;
        int K = 1000;
        long time = 60000000000L;
        String[] heuristics = new String[]{"alternating", "bf", "jis", "jil", "goByEarliest", "earliestModified"};
        PuzzleMaker pm = new PuzzleMaker(M, N);
        int[][] initial = pm.generatePuzzle();
        PuzzleMaker.printMatrix(initial);
        UnidirLayeredBeam unidir = new UnidirLayeredBeam(M, N, K);
        int result2 = unidir.solve(initial, time);
        if(result2 == -1){
            System.out.println("Unidirectional found nothing");
        } else {
            System.out.println("Unidirectional found " + result2);
        }
        for(int i = 1; i < heuristics.length; i++){
            BidirLayeredBeam bidir = new BidirLayeredBeam(M, N, K);
            bidir.setJumpHeuristic(i);
            int result = bidir.solve(initial, time);
            if(result == -1){
                System.out.println(heuristics[i] + " got nothing");
            } else {
                System.out.println(heuristics[i] + " got shortest path of " + result);
            }
        }
    }

    @Test
    public void comparingBestLayeredBeamAlgorithms5x4(){
        int M = 5;
        int N = 4;
        int K = 1000;
        long time = 60000000000L;
        int runs = 5;
        for(int i = 0; i < runs; i++){
            PuzzleMaker pm = new PuzzleMaker(M, N);
            int[][] initial = pm.generatePuzzle();
            PuzzleMaker.printMatrix(initial);
            UnidirLayeredBeam ulb = new UnidirLayeredBeam(M, N, K);
            int result1 = ulb.solve(initial, time);
            System.out.println("Unidirectional got " + result1);
            BidirLayeredBeam bf = new BidirLayeredBeam(M, N, K);
            bf.setJumpHeuristic(1);
            int result2 = bf.solve(initial, time);
            System.out.println("Branch factor got " + result2);
            BidirLayeredBeam fAndF = new BidirLayeredBeam(M, N, K);
            bf.setJumpHeuristic(4); //go by earliest
            int result3 = bf.solve(initial, time);
            System.out.println("Fill and Find got " + result3);
        }
    }

}
