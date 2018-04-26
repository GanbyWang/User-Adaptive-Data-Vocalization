package speeches.speech_generator_UCT;

import data_info.DataAnalysis;
import speeches.Speech;
import speeches.SpeechGenerator;
import speeches.SpeechRelation;

import java.util.ArrayList;
import java.util.List;

/**
 * Speech generator using UCT
 * Sub-class of SpeechGenerator
 * */
public class SpeechGeneratorUCT extends SpeechGenerator {

    // The bound of running MCTS algorithm
    private long runtimeBound;

    /** Constructor function */
    public SpeechGeneratorUCT(int targetColumn, DataAnalysis data,
                              int maxCol, double sampleFraction, long runtimeBound) {
        // Set fields
        this.targetColumn = targetColumn;
        this.data = data;
        this.maxCol = maxCol;
        this.sampleFraction = sampleFraction;
        this.runtimeBound = runtimeBound;
        this.optimalRelatedSpeech = null;

        // Run the algorithm
        runMCTS();
    }

    /* Function to run the MCTS algorithm */
    private void runMCTS() {

        // The root of the Monte Carlo Tree
        TreeNode root = new TreeNode(0);

        // Set the information needed
        TreeNode.setData(this.data);
        TreeNode.setAllColumns(generateAllSpeeches());

        int count = 0;

        // Run until exceed the bound
        long startTime = System.currentTimeMillis();
        long curTime = System.currentTimeMillis();
        while(curTime - startTime <= runtimeBound) {
            root.selectAction();
            curTime = System.currentTimeMillis();
            count++;
        }

        // Get the optimal speech
        this.optimalRelatedSpeech = root.getOptimal();
        this.outputSpeech = optimalRelatedSpeech.generateSpeech(data.columnNames, targetColumn);

        // Print information
        System.out.printf("MCTS algorithm ran %d ms with %d times\n", runtimeBound, count);
        this.optimalRelatedSpeech.printInfo(data.relatedCols);
        System.out.println(this.outputSpeech);
    }

    /* Function to generate all possible single-column speeches */
    private List<SpeechRelation> generateAllSpeeches() {
        List<SpeechRelation> result = new ArrayList<SpeechRelation>();

        // Add all single-column speech
        for(int i = 0; i < data.numericalNum; i++) {

            if(i != targetColumn) {
                // Note to add two possible relations (1 and -1)
                List<Integer> tmpList = new ArrayList<Integer>();
                tmpList.add(i);
                List<Integer> posTmpList = new ArrayList<Integer>();
                posTmpList.add(1);
                List<Integer> negTmpList = new ArrayList<Integer>();
                negTmpList.add(-1);
                result.add(new SpeechRelation(new Speech(tmpList), posTmpList));
                result.add(new SpeechRelation(new Speech(tmpList), negTmpList));
            }
        }

        return result;
    }
}
