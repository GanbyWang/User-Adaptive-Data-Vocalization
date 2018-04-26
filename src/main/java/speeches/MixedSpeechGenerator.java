package speeches;
import java.util.*;

import data_info.DataAnalysis;
import data_vocal.main_package.BitSetIterator;
import database_accessor.DbAccessor;

/**
 * Class to generate speech with combinations of columns
 * Subclass of SpeechGenerator
 */
public class MixedSpeechGenerator extends SpeechGenerator {
    protected List<MultiCorrelations> speeches;     // Speech list
    protected MultiCorrelations optimalSpeech;      // Optimal speech
    private int combinationNum;

    /**
     * Constructor function
     * Accept the same arguments of super class constructor
     * */
    public MixedSpeechGenerator(int targetColumn, DataAnalysis data,
                                int maxCol, double sampleFraction, String extraSQL) {

        // Initialize fields
        this.targetColumn = targetColumn;
        this.data = data;
        this.sampleFraction = sampleFraction;
        this.extraSQL = extraSQL;
        this.maxCol = maxCol;
        this.combinationNum = this.data.numericalNum * (this.data.numericalNum - 1) / 2;

        // Use greedy algorithm to generate the "optimal"
         greedyGeneration();
         printOptimal();
        
//        // Generate all speeches
//		printBasicInfo();
//        generateAllSpeeches(this.maxCol);
//        compete();
//        getOptimal();
//        printOptimal();
    }

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
     * Greedy algorithm to generate the speech
     * Every time add a new column (or pair) to the current optimal speech
     * */
    private void greedyGeneration() {
    	
    		printBasicInfo();
    	
    		// Generate all single column speech
    		generateAllSpeeches(1);
    		// Maintain a copy
    		List<MultiCorrelations> singleColSpeeches = new ArrayList<MultiCorrelations>(this.speeches);
    		// A list of index to use
    		// Would remove the selected index
    		List<Integer> indexSet = new ArrayList<Integer>();
    		for(int i = 0; i < singleColSpeeches.size(); i++) {
    			indexSet.add(i);
    		}
    		
    		// The first round
    		compete();
    		getOptimal();
    		
    		// Remove the chosen speech
    		indexSet.remove(speeches.indexOf(optimalSpeech));
    		
    		// Best speech to record the optimal one of all optimal speeches
    		MultiCorrelations bestSpeech = optimalSpeech;
    		
    		// Try multiple times
    		for(int i = 1; i < this.maxCol; i++) {
    			
    			// Empty the list of speeches
    			speeches.clear();
    			
    			// Record the set of columns to delete because of overlapping
    			List<Integer> deleteSet = new ArrayList<Integer>();
    			
    			// Generate the next speech list
    			// Append each single column to the current optimal one
    			for(Integer singleIndex : indexSet) {
    				
    				// Create the new speech
    				MultiCorrelations mergedSpeech = optimalSpeech.merge(singleColSpeeches.get(singleIndex));
    				
    				// Notice that the columns of a speech cannot overlap with each other
    				// If overlapped, delete the column as well
    				if (ifSetCompleteOverlap(mergedSpeech.correlatedCols) == false) {
					speeches.add(mergedSpeech);
				} else {
					deleteSet.add(singleIndex);
				}
    			}
    			
    			// Delete overlapped column
    			indexSet.removeAll(deleteSet);
    			
    			compete();
    			getOptimal();
    			
        		// Remove the chosen column
        		indexSet.remove(speeches.indexOf(optimalSpeech));
        		
        		// Update the best speech
        		if(optimalSpeech.score > bestSpeech.score) {
        			bestSpeech = optimalSpeech;
        		}
    		}
    		
    		optimalSpeech = bestSpeech;
    }
    
    /* Helper function to check if the given column set has partial overlapped columns */
    private boolean ifSetOverlap(List<List<Integer>> sets) {
        List<Integer> checkerTable = new LinkedList<Integer>();

        for(List<Integer> elementSet : sets) {
            for(Integer element : elementSet) {
                if(checkerTable.contains(element)) {
                    return true;
                } else {
                    checkerTable.add(element);
                }
            }
        }

        return false;
    }

    /* Helper function to check if the given column set has complete overlapped columns */
    private boolean ifSetCompleteOverlap(List<List<Integer>> sets) {
        List<Integer> singleSetTable = new LinkedList<Integer>();
        List<List<Integer>> multiSetList = new LinkedList<List<Integer>>();

        for(List<Integer> elementSet : sets) {
            if(elementSet.size() == 1) {
            		// Forbid duplicate sets
            		if(singleSetTable.containsAll(elementSet)) {
            			return true;
            		}
            		singleSetTable.add(elementSet.get(0));
            } else {
            		// Forbid duplicate sets
            		for(List<Integer> multiSet : multiSetList) {
            			if(multiSet.containsAll(elementSet)) {
            				return true;
            			}
            		}
            		multiSetList.add(elementSet);
            }
        }

        for(List<Integer> multiSet : multiSetList) {
            int overlapCount = 0;

            for(Integer element : multiSet) {
                if(singleSetTable.contains(element)) {
                    overlapCount++;
                }
            }

            if(overlapCount == multiSet.size()) {
                return true;
            }
        }

        return false;
    }

    /*
    * Function to generate all speeches
    * Uses the BitSetIterator class to simplify execution
    * */
    private void generateAllSpeeches(int maxCol) {
        speeches = new ArrayList<MultiCorrelations>();

        /*
        * 28 possibilities:
        * 7 for single columns
        * 21 for column pairs
        * Overlapping target column is dealt with in another place
        * Spec: all columns after the target column is reduced by 1,
        *       to eliminate the gap
        * */
        BitSet set = new BitSet(this.combinationNum);
        set.set(0, this.combinationNum, true);

        // Enumerate all widths
        for(int k = 1; k <= maxCol; k++) {
            BitSetIterator bitSetIterator = new BitSetIterator(set, k);

            while(bitSetIterator.hasNext()) {
                BitSet answer = bitSetIterator.next();
                List<List<Integer>> tmpColPair = new ArrayList<List<Integer>>();

                for(int i = 0; i < this.combinationNum; i++) {
                    if(answer.get(i) == true) {
                        // Get the actual column set
                        tmpColPair.add(NumToPair(i, this.data.numericalNum - 1));
                    }
                }

                if(!ifSetCompleteOverlap(tmpColPair)) {
                    // Generate all possible relation combinations
                    addAllCorrelatedSpeeches(tmpColPair);
                }
            }
        }
    }

    /*
    * Helper function to generate all possible speech
    * Given a column set, generate all possible relation sets
    *   and create corresponding speeches
    * */
    private void addAllCorrelatedSpeeches(List<List<Integer>> colPairs) {
        int k = colPairs.size();

        // The combination width is decided by the column set size
        BitSet set = new BitSet(k);
        set.set(0, k, true);

        for (int i = 0; i <= k; i++) {
            BitSetIterator bitSetIterator = new BitSetIterator(set, i);

            while (bitSetIterator.hasNext()) {
                BitSet answer = bitSetIterator.next();
                List<Integer> correlations = new ArrayList<Integer>();

                // Generate a relation set based on the bit set
                for (int j = 0; j < k; j++) {
                    if (answer.get(j) == true) {
                        correlations.add(1);
                    } else {
                        correlations.add(-1);
                    }
                }

                // Add a new speech to the speech list
                speeches.add(new MultiCorrelations(colPairs, correlations));
            }
        }
    }

    /*
    * Function to compete among all speeches
    * Mostly the same as the ones in super class
    * */
    private void compete() {

        List<List<Double>> sample = data.sample;
        int sampleNum = sample.size();
        int repeatTimes = sampleNum;

        for(int i = 0; i < repeatTimes; i++) {
            // Generate two random tuples
            int index1 = new Random().nextInt(sampleNum);
            int index2 = new Random().nextInt(sampleNum);
            while(index2 == index1) {
                index2 = new Random().nextInt(sampleNum);
            }

            List<Double> tuple1 = sample.get(index1);
            List<Double> tuple2 = sample.get(index2);

            // Repeat for every speech
            for(MultiCorrelations correlatedSpeech : speeches) {
                int score1 = 0;
                int score2 = 0;

                // Calculate scores
                for(int j = 0; j < correlatedSpeech.correlatedCols.size(); j++) {
                    List<Integer> pair = correlatedSpeech.correlatedCols.get(j);

                    // If the factor is positive
                    if(correlatedSpeech.correlations.get(j) > 0) {
                        // Only every column is higher can we say it offers a predication
                        boolean oneWin = true;
                        for(int k = 0; k < pair.size(); k++) {
                            if(tuple1.get(pair.get(k)) <= tuple2.get(pair.get(k))) {
                                oneWin = false;
                                break;
                            }
                        }

                        if(oneWin == true) {
                            score1++;
                            continue;
                        }

                        boolean twoWin = true;
                        for(int k = 0; k < pair.size(); k++) {
                            if(tuple1.get(pair.get(k)) >= tuple2.get(pair.get(k))) {
                                twoWin = false;
                                break;
                            }
                        }

                        if(twoWin == true) {
                            score2++;
                        }

                        // If the factor is negative
                    } else {
                        boolean oneWin = true;
                        for(int k = 0; k < pair.size(); k++) {
                            if(tuple1.get(pair.get(k)) <= tuple2.get(pair.get(k))) {
                                oneWin = false;
                                break;
                            }
                        }

                        if(oneWin == true) {
                            score1--;
                            continue;
                        }

                        boolean twoWin = true;
                        for(int k = 0; k < pair.size(); k++) {
                            if(tuple1.get(pair.get(k)) >= tuple2.get(pair.get(k))) {
                                twoWin = false;
                                break;
                            }
                        }

                        if(twoWin == true) {
                            score2--;
                        }
                    }
                }

                // If the prediction is correct, this speech gets one point
                if((score1 > score2 && tuple1.get(targetColumn) > tuple2.get(targetColumn))
                        || (score1 < score2 && tuple1.get(targetColumn) < tuple2.get(targetColumn))) {
                    correlatedSpeech.score++;
                }
            }
        }
    }

    /*
    * Get the optimal speech
    * */
    private void getOptimal() {
        // Get the general and distribution of the target column
        targetColInfo = data.colInfo();

        // Find the speech with maximum score
        int maxScore = 0;
        for(MultiCorrelations correlatedSpeech : speeches) {
            if(correlatedSpeech.score > maxScore) {
                maxScore = correlatedSpeech.score;
                optimalSpeech = correlatedSpeech;
            }
        }
    }
    
    /* Helper function to generate the final output string */
    private void printOptimal() {
        // Print the information of the optimal speech
        optimalSpeech.printInfo();

        // Generate the output speech
//        outputSpeech = optimalSpeech.generateSpeech(data.columnNames, targetColumn);
        outputSpeech = optimalSpeech.generateCompressedSpeech(data.columnNames, targetColumn);

        System.out.printf("Optimal Speech: %s\n", outputSpeech);
        System.out.printf("General Information: %s\n", targetColInfo);

        outputSpeech = outputSpeech + targetColInfo;
    }
}
