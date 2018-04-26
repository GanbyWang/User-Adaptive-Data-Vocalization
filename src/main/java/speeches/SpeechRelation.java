package speeches;

import java.util.ArrayList;
import java.util.List;
import static java.lang.System.exit;

/** 
 * Class to connect with Speech to record the relations of each column
 *  */
public class SpeechRelation {
    /** Which columns this speech is talking about */
    public Speech speech;
    /** Corresponding relations to columns */
    public List<Integer> relations;

    /**
     * Constructor function
     * Two parameter: speech and corresponding factors
     * */
    public SpeechRelation(Speech speech, List<Integer> relations) {
        // Exit if dimensions don't match
        if(speech.columns.size() != relations.size()) {
            System.out.print("Unmatched columns and relations!");
            exit(1);
        }

        this.speech = new Speech(speech);
        this.relations = relations;
    }

    /**
     * Constructor function
     * Merge a list of single-column speech into a complex
     * @param singleSpeeches: the list of single speeches
     * */
    public SpeechRelation(List<SpeechRelation> singleSpeeches) {
        List<Integer> columnList = new ArrayList<Integer>();
        relations = new ArrayList<Integer>();

        for(SpeechRelation singleSpeech : singleSpeeches) {
            columnList.add(singleSpeech.speech.columns.get(0));
            relations.add(singleSpeech.relations.get(0));
        }

        speech = new Speech(columnList);
    }

    /** Print information */
    public void printInfo() {
        speech.printInfo();
        System.out.println("Corresponding relations:");
        for(Integer relation : relations) {
            System.out.printf("%d ", relation);
        }
        System.out.printf("\n");
    }

    public void printInfo(List<Integer> relatedCols) {
        speech.printInfo(relatedCols);
        System.out.println("Corresponding relations:");
        for(Integer relation : relations) {
            System.out.printf("%d ", relation);
        }
        System.out.printf("\n");
    }

    /**
     * Generate a string output based on this speech
     * @param colNames: the name array of the table
     * @param targetCol: the column number of the target column
     * */
    public String generateSpeech(String[] colNames, int targetCol) {
        String output = "";

        List<Integer> posSet = new ArrayList<Integer>();
        List<Integer> negSet = new ArrayList<Integer>();

        for(int i = 0; i < relations.size(); i++) {
            if(relations.get(i) > 0) {
                posSet.add(speech.columns.get(i));
            } else {
                negSet.add(speech.columns.get(i));
            }
        }

        if(posSet.size() > 0) {
            for(int i = 0; i < posSet.size(); i++) {

                output += colNames[posSet.get(i)];

                if(i == posSet.size() - 2) {
                    output += " and ";
                } else if (i == posSet.size() - 1) {
                    if(posSet.size() > 1) {
                        output += " are positively correlated with " + colNames[targetCol] + ". ";
                    } else {
                        output += " is positively correlated with " + colNames[targetCol] + ". ";
                    }
                } else {
                    output += ", ";
                }
            }
        }

        if(negSet.size() > 0) {

            if(posSet.size() > 0) {
                output += "While ";
            }

            for(int i = 0; i < negSet.size(); i++) {
                output += colNames[negSet.get(i)];

                if(i == negSet.size() - 2) {
                    output += " and ";
                } else if (i == negSet.size() - 1) {
                    if(negSet.size() > 1) {
                        output += " are negatively correlated";
                    } else {
                        output += " is negatively correlated";
                    }

                    if(posSet.size() > 0) {
                        output += ". ";
                    } else {
                        output += " with " + colNames[targetCol] + ". ";
                    }
                } else {
                    output += ", ";
                }
            }
        }

        return output;
    }
}