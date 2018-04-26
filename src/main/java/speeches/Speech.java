package speeches;

import java.util.ArrayList;
import java.util.List;

/** 
 * Used to store basic information of a speech 
 * */
public class Speech {
    /** Which columns this speech is talking about */
    public List<Integer> columns;
    /** Score is used to compare which speech is better */
    public int score;

    /**
     * Constructor function
     * One parameter: which columns are in this speech
     * */
    public Speech(List<Integer> columns) {
        this.columns = new ArrayList<Integer>();
        this.columns.addAll(columns);
        score = 0;
    }

    /**
     * Constructor function
     * One parameter: do a complete copy from an existing speech
     * */
    public Speech(Speech speech) {
        this.columns = new ArrayList<Integer>();
        this.columns.addAll(speech.columns);
        this.score = 0;
    }

    /** Print information */
    public void printInfo() {
        System.out.printf("Selected Columns: ");
        for(Integer col : columns) {
            System.out.printf("%d ", col);
        }
        System.out.printf("\nScore: %d\n", score);
    }

    public void printInfo(List<Integer> relatedCols) {
        System.out.printf("Selected Columns: ");
        for(Integer col : columns) {
            System.out.printf("%d ", relatedCols.get(col));
        }
        System.out.println();
//        System.out.printf("\nScore: %d\n", score);
    }
}