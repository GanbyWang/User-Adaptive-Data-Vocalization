package speeches;

import java.util.*;

import data_info.DataAnalysis;
import data_vocal.main_package.BitSetIterator;
import database_accessor.DbAccessor;

/** 
 * Used to generate the optimal speech 
 * */
public class SpeechGenerator {
    // Store the target column
    protected int targetColumn;
    // An array of all possible speeches
    protected List<Speech> speeches;
    // An array of all possible speeches with relations
    protected List<SpeechRelation> relatedSpeeches;
    // The data to analyze
    protected DataAnalysis data;
    // The results
    protected Speech optimalSpeech;
    protected SpeechRelation optimalRelatedSpeech;
    protected String outputSpeech;
    // Accessor to the database
    protected DbAccessor dbAccessor;
    // Extra constraint on querying
    protected String extraSQL;
    // The general information of the target column (mean and variance)
    protected String targetColInfo;
    // The sample fraction (0-100)
    protected double sampleFraction;
    // The maximum of columns in the speech
    protected int maxCol;

    /**
     * Empty constructor for subclass
     * */
    public SpeechGenerator() {}

    /**
     * Constructor function
     * Two parameters: target column and the original data
     * */
    public SpeechGenerator(int targetColumn, DataAnalysis data,
                           int maxCol, double sampleFraction, String extraSQL) {
        this.targetColumn = targetColumn;
        this.data = data;
        this.extraSQL = extraSQL;
        this.sampleFraction = sampleFraction;

//        // Generate a new accessor to database
//        dbAccessor = new DbAccessor();

        // Generate all possible speeches
//        generateAllSpeeches();
//        printAllSpeeches();
        System.out.printf("Maximum Columns: %d\n", maxCol);
//        generateAllWithoutFactor(maxCol);
        generateAllWithBSI(maxCol);
//        printAllSpeeches();
//        printAllRelatedSpeeches();

        // Repeat enough times to find the optimal speech
//         compete(repeatTimes);
        competeWithDB();

        // Get the optimal speech
//         getOptimal();
        getRelatedOptimal();

//        dbAccessor.disconnected();
    }

//    /** Generate all speeches */
//    private void generateAllSpeeches() {
//        speeches = new ArrayList<Speech>();
//
//        /*
//        * Since the limit of characters is 300
//        * We only generate speeches with no more than 5 columns
//        * */
//        for(int i = 0; i < 8; i++) {
//            if(i == targetColumn) {
//                continue;
//            }
//
//            ArrayList<Integer> columns = new ArrayList<Integer>();
//            columns.add(i);
//            speeches.add(new Speech(columns));
//
//            for(int j = i + 1; j < 8; j++) {
//                if (j == targetColumn) {
//                    continue;
//                }
//
//                columns = new ArrayList<Integer>();
//                columns.add(i);
//                columns.add(j);
//                speeches.add(new Speech(columns));
//
//                for (int k = j + 1; k < 8; k++) {
//                    if(k == targetColumn) {
//                        continue;
//                    }
//
//                    columns = new ArrayList<Integer>();
//                    columns.add(i);
//                    columns.add(j);
//                    columns.add(k);
//                    speeches.add(new Speech(columns));
//
//                    for (int l = k + 1; l < 8; l++) {
//                        if(l == targetColumn) {
//                            continue;
//                        }
//
//                        columns = new ArrayList<Integer>();
//                        columns.add(i);
//                        columns.add(j);
//                        columns.add(k);
//                        columns.add(l);
//                        speeches.add(new Speech(columns));
//                    }
//                }
//            }
//        }
//    }

    /* Helper function to generate the basic information of this query */
    protected void printBasicInfo() {
        // Print basic information
        System.out.printf("Maximum Columns: %d\n", maxCol);
        System.out.printf("Target Column: %d\n", targetColumn);

        if(DbAccessor.ifPercent == true) {
            System.out.printf("Sampling Fraction: %f\n", sampleFraction);
        } else {
            System.out.printf("Sampling Tuples: %d\n", (int) sampleFraction);
        }
        System.out.printf("Repeated Times: %d\n", data.sample.size());
    }

    /*
     * Function to generate all possible related speeches
     * Uses BitSetIterator class
     * */
    private void generateAllWithBSI(int maxCol) {
        // Limit the range of maximum columns
        if(maxCol >= 8) {
            maxCol = 7;
        }

        speeches = new ArrayList<Speech>();
        relatedSpeeches = new ArrayList<SpeechRelation>();

        BitSet speechSet = new BitSet(8);
        speechSet.set(0, 8, true);

        // Enumerate all possible speech widths
        for(int k = 1; k <= maxCol; k++) {
            BitSetIterator bitSetIterator = new BitSetIterator(speechSet, k);

            while(bitSetIterator.hasNext()) {
                BitSet answer = bitSetIterator.next();
                List<Integer> tmpCol = new ArrayList<Integer>();

                int i;
                for(i = 0; i < 8; i++) {
                    if(answer.get(i) == true) {
                        if(i == targetColumn) {
                            break;
                        } else {
                            tmpCol.add(i);
                        }
                    }
                }

                // Add a speech to speech list
                if(i == 8) {
                    speeches.add(new Speech(tmpCol));
                }
            }
        }

        // For each speech, generate all possible relation sets
        for(Speech speech : speeches) {
            // The width of relation set is determined by the speech size
            int k = speech.columns.size();
            BitSet relationSet = new BitSet(k);
            relationSet.set(0, k, true);

            for(int j = 0; j <= k; j++) {
                BitSetIterator bitSetIterator = new BitSetIterator(relationSet, j);

                while(bitSetIterator.hasNext()) {
                    BitSet answer = bitSetIterator.next();
                    List<Integer> relations = new ArrayList<Integer>();

                    for(int i = 0; i < k; i++) {
                        if(answer.get(i) == true) {
                            relations.add(1);
                        } else {
                            relations.add(-1);
                        }
                    }

                    relatedSpeeches.add(new SpeechRelation(speech, relations));
                }
            }
        }
    }

    /** Generate all speeches without factor matrix */
    private void generateAllWithoutFactor(int maxCol) {
        // Limit the range of maximum columns
        if(maxCol >= 8) {
            maxCol = 7;
        }

        speeches = new ArrayList<Speech>();
        generateByPermutation(0, new ArrayList<Integer>(), maxCol);

        relatedSpeeches = new ArrayList<SpeechRelation>();
        for(Speech speech : speeches) {
            permutateRelations(speech, 0, new ArrayList<Integer>());
        }
    }

    /* DFS to generate all speeches */
    private void generateByPermutation(int curCol, List<Integer> columns, int maxCol) {
        // Maintain a local copy
        List<Integer> tmpCol = new ArrayList<Integer>();
        tmpCol.addAll(columns);

        if(curCol == 7) {
            if(tmpCol.size() > 0) {
                speeches.add(new Speech(tmpCol));
            }
            // The column involved cannot be the target column
            if(tmpCol.size() < maxCol && targetColumn != curCol) {
                tmpCol.add(7);
                speeches.add(new Speech(tmpCol));
            }
            return;
        }

        generateByPermutation(curCol + 1, tmpCol, maxCol);

        if(tmpCol.size() < maxCol && targetColumn != curCol) {
            tmpCol.add(curCol);
            generateByPermutation(curCol + 1, tmpCol, maxCol);
        }
    }

    /* DFS to generate all speeches with relations */
    private void permutateRelations(Speech speech, int curLevel, List<Integer> relations) {
        // Maintain local copies
        List<Integer> tmpRelations1 = new ArrayList<Integer>();
        tmpRelations1.addAll(relations);
        List<Integer> tmpRelations2 = new ArrayList<Integer>();
        tmpRelations2.addAll(relations);

        if(curLevel == speech.columns.size() - 1) {
            tmpRelations1.add(1);
            relatedSpeeches.add(new SpeechRelation(speech, tmpRelations1));
            tmpRelations2.add(-1);
            relatedSpeeches.add(new SpeechRelation(speech, tmpRelations2));
            return;
        }

        tmpRelations1.add(1);
        permutateRelations(speech, curLevel + 1, tmpRelations1);
        tmpRelations2.add(-1);
        permutateRelations(speech, curLevel + 1,tmpRelations2);
    }

    /* Print the information of all speeches */
    private void printAllSpeeches() {
        System.out.printf("There're %d speeches in total.\n", speeches.size());

        for(Speech singleSpeech : speeches) {
            singleSpeech.printInfo();
        }
    }

    /* Print the information of all speeches with factors */
    private void printAllRelatedSpeeches() {
        System.out.printf("There're %d related speeches in total.\n", relatedSpeeches.size());

        for(SpeechRelation relation : relatedSpeeches) {
            relation.printInfo();
        }
    }

//    /**
//     * Used to distinguish speeches
//     * One parameter: the repeat time
//     * */
//    private void compete(int repeatTimes) {
//
//        System.out.printf("Target Column: %d\n", targetColumn);
//        System.out.printf("Repeated Times: %d\n", repeatTimes);
//
//        for(int i = 0; i < repeatTimes; i++) {
//            // Generate two random tuples
//            int index1 = new Random().nextInt(data.originData.size());
//            int index2 = new Random().nextInt(data.originData.size());
//            while(index2 == index1) {
//                index2 = new Random().nextInt(data.originData.size());
//            }
//
//            DataAnalysis.tuple tuple1 = data.originData.get(index1);
//            DataAnalysis.tuple tuple2 = data.originData.get(index2);
//
//            // Repeat for each single speech
//            for(Speech singleSpeech : speeches) {
//                // Initialize scores of tuples
//                int score1 = 0;
//                int score2 = 0;
//
//                // Calculate scores
//                for(Integer col : singleSpeech.columns) {
//                    if(data.factorMatrix[targetColumn][col] > 0) {
//                        if(tuple1.columns[col] > tuple2.columns[col]) {
//                            score1++;
//                        } else if(tuple1.columns[col] < tuple2.columns[col]) {
//                            score2++;
//                        }
//                    } else {
//                        if(tuple1.columns[col] > tuple2.columns[col]) {
//                            score1--;
//                        } else if(tuple1.columns[col] < tuple2.columns[col]) {
//                            score2--;
//                        }
//                    }
//                }
//
//                // If the prediction is correct, this speech gets one point
//                if((score1 > score2 && tuple1.columns[targetColumn] > tuple2.columns[targetColumn])
//                        || (score1 < score2 && tuple1.columns[targetColumn] < tuple2.columns[targetColumn])) {
//                    singleSpeech.score++;
//                }
//            }
//        }
//    }

    /**
     * Used to distinguish speeches with factors
     * One parameter: the repeat time
     * */
    private void competeWithDB() {
        System.out.printf("Target Column: %d\n", targetColumn);
//        System.out.printf("Repeated Times: %d\n", repeatTimes);

//        // Create a new database accessor
//        int tupleNum = dbAccessor.getTupleNum();

//        List<List<Double>> sample1 = dbAccessor.getSample(sampleFraction);
//        List<List<Double>> sample2 = dbAccessor.getSample(sampleFraction);

//        List<List<Double>> sample1 = dbAccessor.getSample(sampleFraction, extraSQL);
//        List<List<Double>> sample2 = dbAccessor.getSample(sampleFraction, extraSQL);
        List<List<Double>> sample = data.sample;
        int sampleNum = sample.size();
        int repeatTimes = sampleNum;

//        int repeatTimes = Math.min(sample1.size(), sample2.size());

        System.out.printf("Sampling Fraction: %d\n", sampleFraction);
        System.out.printf("Repeated Times: %d\n", repeatTimes);
//        System.out.printf("Size of sample1: %d\n", sample1.size());
//        System.out.printf("Size of sample2: %d\n", sample2.size());

        for(int i = 0; i < repeatTimes; i++) {
            // Generate two random tuples
            int index1 = new Random().nextInt(sampleNum);
            int index2 = new Random().nextInt(sampleNum);
            while(index2 == index1) {
                index2 = new Random().nextInt(sampleNum);
            }

            List<Double> tuple1 = sample.get(index1);
            List<Double> tuple2 = sample.get(index2);

//            List<Double> tuple1 = sample1.get(i);
//            List<Double> tuple2 = sample2.get(repeatTimes - 1 - i);

            // Repeat for every speech
            for(SpeechRelation relatedSpeech : relatedSpeeches) {
                int score1 = 0;
                int score2 = 0;

                // Calculate scores
                for(int j = 0; j < relatedSpeech.relations.size(); j++) {
                    int col = relatedSpeech.speech.columns.get(j);

                    // If the factor is positive
                    if(relatedSpeech.relations.get(j) > 0) {
                        if(tuple1.get(col) > tuple2.get(col)) {
                            score1++;
                        } else if(tuple1.get(col) < tuple2.get(col)) {
                            score2++;
                        }
                    // If the factor is negative
                    } else {
                        if(tuple1.get(col) > tuple2.get(col)) {
                            score1--;
                        } else if(tuple1.get(col) < tuple2.get(col)) {
                            score2--;
                        }
                    }
                }

                // If the prediction is correct, this speech gets one point
                if((score1 > score2 && tuple1.get(targetColumn) > tuple2.get(targetColumn))
                        || (score1 < score2 && tuple1.get(targetColumn) < tuple2.get(targetColumn))) {
                    relatedSpeech.speech.score++;
                }
            }
        }
    }

//    /** Get the optimal speech */
//    private void getOptimal() {
//        // Sort the speech
//        Collections.sort(speeches, new Comparator<Speech>() {
//            @Override
//            public int compare(Speech o1, Speech o2) {
//                if(o1.score < o2.score) {
//                    return -1;
//                } else if(o1.score == o2.score) {
//                    return 0;
//                } else {
//                    return 1;
//                }
//            }
//        });
//
//        // Get the speech with the highest score
//        optimalSpeech = speeches.get(speeches.size() - 1);
//
//        optimalSpeech.printInfo();
//
//        // Change the result into a string
//        outputSpeech = "";
//
//        for(int col : optimalSpeech.columns) {
//            outputSpeech += "Column ";
//            outputSpeech += data.columnNames[col];
//            outputSpeech += " is ";
//
//            if(data.factorMatrix[targetColumn][col] < 0) {
//                outputSpeech += "negatively ";
//            } else {
//                outputSpeech += "positively ";
//            }
//
//            outputSpeech += "correlated with column ";
//            outputSpeech += data.columnNames[targetColumn];
//            outputSpeech += ". ";
//        }
//
//        System.out.printf("Optimal Speech: %s\n", outputSpeech);
//    }

    /** Get the optimal speech with factors */
    public void getRelatedOptimal() {
//        // Sort the speech
//        Collections.sort(relatedSpeeches, new Comparator<SpeechRelation>() {
//            @Override
//            public int compare(SpeechRelation o1, SpeechRelation o2) {
//                if(o1.speech.score < o2.speech.score) {
//                    return -1;
//                } else if(o1.speech.score == o2.speech.score) {
//                    return 0;
//                } else {
//                    return 1;
//                }
//            }
//        });
//
//        // Get the speech with the highest score
//        optimalRelatedSpeech = relatedSpeeches.get(relatedSpeeches.s

//        targetColInfo = dbAccessor.colInfo(data.tableNames[targetColumn],
//                sampleFraction, data.columnNames[targetColumn]);

        targetColInfo = data.colInfo();

        // Loop to find the maximum score
        int maxScore = 0;
        for(SpeechRelation relatedSpeech : relatedSpeeches) {
            if(relatedSpeech.speech.score > maxScore) {
                maxScore = relatedSpeech.speech.score;
                optimalRelatedSpeech = relatedSpeech;
            }
        }

        optimalRelatedSpeech.printInfo();

//        generateSpeechNaive();
        // Generate compressed speech
        generateSpeechCompressed();

        System.out.printf("Optimal Speech: %s\n", outputSpeech);
        System.out.printf("General Information: %s\n", targetColInfo);

        outputSpeech = outputSpeech + targetColInfo;
    }

//    private void generateSpeechNaive() {
//        // Change the result into a string
//        outputSpeech = "";
//
//        for(int i = 0; i < optimalRelatedSpeech.relations.size(); i++) {
//            int col = optimalRelatedSpeech.speech.columns.get(i);
//
//            outputSpeech += "Column ";
//            outputSpeech += data.columnNames[col];
//            outputSpeech += " is ";
//
//            if(optimalRelatedSpeech.relations.get(i) < 0) {
//                outputSpeech += "negatively ";
//            } else {
//                outputSpeech += "positively ";
//            }
//
//            outputSpeech += "correlated with column ";
//            outputSpeech += data.columnNames[targetColumn];
//            outputSpeech += ". ";
//        }
//    }

    /* Helper function to compress speech */
    private void generateSpeechCompressed() {
        outputSpeech = "";

        List<Integer> positiveCols = new ArrayList<Integer>();
        List<Integer> negativeCols = new ArrayList<Integer>();

        List<Integer> resultCols = optimalRelatedSpeech.speech.columns;
        List<Integer> resultRelations = optimalRelatedSpeech.relations;

        for(int i = 0; i < resultCols.size(); i++) {
            if(resultRelations.get(i) > 0) {
                positiveCols.add(resultCols.get(i));
            } else if(resultRelations.get(i) < 0) {
                negativeCols.add(resultCols.get(i));
            }
        }

        if(positiveCols.size() > 1) {
            outputSpeech += "Columns ";
            for(int i = 0; i < positiveCols.size() - 1; i++) {
                outputSpeech += data.columnNames[positiveCols.get(i)];
                outputSpeech += ", ";
            }
            outputSpeech += "and ";
            outputSpeech += data.columnNames[positiveCols.get(positiveCols.size() - 1)];
            outputSpeech += " are positively correlated with ";
            outputSpeech += data.columnNames[targetColumn];
            outputSpeech += ". ";
        } else if(positiveCols.size() == 1) {
            outputSpeech += "Column ";
            outputSpeech += data.columnNames[positiveCols.get(0)];
            outputSpeech += " is positively correlated with ";
            outputSpeech += data.columnNames[targetColumn];
            outputSpeech += ". ";
        }

        if(negativeCols.size() > 1) {
            outputSpeech += "Columns ";
            for(int i = 0; i < negativeCols.size() - 1; i++) {
                outputSpeech += data.columnNames[negativeCols.get(i)];
                outputSpeech += ", ";
            }
            outputSpeech += "and ";
            outputSpeech += data.columnNames[negativeCols.get(negativeCols.size() - 1)];
            outputSpeech += " are negatively correlated with ";
            outputSpeech += data.columnNames[targetColumn];
            outputSpeech += ". ";
        } else if(negativeCols.size() == 1) {
            outputSpeech += "Column ";
            outputSpeech += data.columnNames[negativeCols.get(0)];
            outputSpeech += " is negatively correlated with ";
            outputSpeech += data.columnNames[targetColumn];
            outputSpeech += ". ";
        }
    }

    /**
     * Read out the optimal speech
     * */
    public String readOutResult() {
        return outputSpeech;
    }

    /*
     * Helper function to convert an integer to a column number or a column pair
     * Called by generateAllSpeeches
     * */
    protected List<Integer> NumToPair(int x, int n) {
        List<Integer> result = new ArrayList<Integer>();

        int sum = n + n * (n - 1) / 2;

        // Check illegal arguments
        if(x < 0 || x >= sum) {
            System.out.println("Invalid convert!");
            System.exit(1);
        }

        int current = n - 1;

        while(x >= n) {
            x -= current;
            current--;
        }

        // Get the first column
        if(current < n - 1) {
            result.add(n - 2 - current);
        }
        // Get the second column
        result.add(x);

//        // Data-based transformation (7 single columns and 21 pairs)
//        if(x <= 6) {
//            result.add(x);
//        } else if(x <= 12) {
//            result.add(0);
//            result.add(x - 6);
//        } else if (x > 12 && x <= 17) {
//            result.add(1);
//            result.add(x - 11);
//        } else if (x > 17 && x <= 21) {
//            result.add(2);
//            result.add(x - 15);
//        } else if (x > 21 && x <= 24) {
//            result.add(3);
//            result.add(x - 18);
//        } else if (x > 24 && x <= 26) {
//            result.add(4);
//            result.add(x - 20);
//        } else {
//            result.add(5);
//            result.add(6);
//        }

        // Deal with overlapping with target column
        for(int i = 0; i < result.size(); i++) {
            if(result.get(i) >= targetColumn) {
                result.set(i, result.get(i) + 1);
            }
        }

        return result;
    }
}