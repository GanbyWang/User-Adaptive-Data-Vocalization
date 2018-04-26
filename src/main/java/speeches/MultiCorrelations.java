package speeches;

import java.util.*;

import static java.lang.System.exit;

/** Data structure for multiple columns in one correlation */
public class MultiCorrelations {
    /**
     * Correlated column sets
     * Right now we only only at most 2 columns as a pair
     *  */
    public List<List<Integer>> correlatedCols;
    /** Corresponding relations to each set */
    public List<Integer> correlations;
    /** Score of the speech */
    public int score;

    /**
     * Constructor
     * Two parameters: correlated column sets and corresponding relations
     * */
    public MultiCorrelations(List<List<Integer>> correlatedCols, List<Integer> correlations) {
        // Check if dimensions are matched
        if(correlatedCols.size() != correlations.size()) {
            System.out.print("Unmatched columns and relations!");
            exit(1);
        }

        // Initialize score
        score = 0;

        // Do deep copies
        this.correlatedCols = new ArrayList<List<Integer>>();
        for(List<Integer> item : correlatedCols) {
            List<Integer> singlePair = new ArrayList<Integer>();
            singlePair.addAll(item);
            this.correlatedCols.add(singlePair);
        }

        this.correlations = new ArrayList<Integer>();
        this.correlations.addAll(correlations);
    }

    /**
     * Merge the current speech with another speech
     * @return: a new merged speech
     * */
    public MultiCorrelations merge(MultiCorrelations anotherSpeech) {
    		List<List<Integer>> correlatedCols = new ArrayList<List<Integer>>(this.correlatedCols);
    		correlatedCols.addAll(anotherSpeech.correlatedCols);
    		List<Integer> correlations = new ArrayList<Integer>(this.correlations);
    		correlations.addAll(anotherSpeech.correlations);
    		
    		return new MultiCorrelations(correlatedCols, correlations);
    }
    
    /** Helper function to print all information of this speech */
    public void printInfo() {
        // Print columns
        System.out.printf("Selected column pairs:\n");
        for(List<Integer> pair : correlatedCols) {
            System.out.printf("{");
            for(int i = 0; i < pair.size() - 1; i++) {
                System.out.printf("%d, ", pair.get(i));
            }
            System.out.printf("%d} ", pair.get(pair.size() - 1));
        }
        System.out.printf("\n");

        // Print relations
        System.out.printf("Corresponding relations:\n");
        for(int i = 0; i < correlations.size(); i++) {
            System.out.printf("%d ", correlations.get(i));
        }
        System.out.printf("\n");

        // Print score
        System.out.printf("Score: %d\n", score);
    }

    private String setToString(List<Integer> indexSet, String[] colNames) {
        String output = "";
    		
        if(indexSet.size() > 1) {
			output += "the combination of ";
			output += colNames[indexSet.get(0)];

            for(int j = 1; j < indexSet.size() - 1; j++) {
                output += ", " + colNames[indexSet.get(j)];
            }

            output += " with " + colNames[indexSet.get(indexSet.size() - 1)];
        } else {
            output += colNames[indexSet.get(0)];
        }
    		
        return output;
    }
    
    public String generateCompressedSpeech(String[] colNames, int targetCol) {
    		String output = "";
    		
    		List<List<Integer>> posSet = new ArrayList<List<Integer>>();
    		List<List<Integer>> negSet = new ArrayList<List<Integer>>();
    		
    		for(int i = 0; i < correlations.size(); i++) {
    			if(correlations.get(i) == 1) {
    				posSet.add(correlatedCols.get(i));
    			} else {
    				negSet.add(correlatedCols.get(i));
    			}
    		}
    		
    		if(posSet.size() > 0) {
    			for(int i = 0; i < posSet.size(); i++) {
    				
    				output += setToString(posSet.get(i), colNames);
    				
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
    				
    				output += setToString(negSet.get(i), colNames);
    				
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
    
    /**
     * Helper function to generate a string based on the speech
     * */
    public String generateSpeech(String[] colNames, int targetCol) {
        String output = "";

        List<Integer> posSingleSet = new ArrayList<Integer>();
        List<Integer> negSingleSet = new ArrayList<Integer>();
        
        boolean firstFlag = true;

        // Enumerate every column set
        for(int i = 0; i < correlatedCols.size(); i++) {
            String relation = correlations.get(i) > 0 ? "positively" : "negatively";
            List<Integer> curSet = correlatedCols.get(i);

            if(curSet.size() > 1) {
                output += "The combination of ";
            } else {
                /*
                * If there's only one column in this set
                * Then add it to single sets to compress and skip this enumeration
                * */
                if(correlations.get(i) > 0) {
                    posSingleSet.add(curSet.get(0));
                } else {
                    negSingleSet.add(curSet.get(0));
                }
                continue;
            }

            output += colNames[curSet.get(0)];

            for(int j = 1; j < curSet.size() - 1; j++) {
                output += ", " + colNames[curSet.get(j)];
            }

            output += " and " + colNames[curSet.get(curSet.size() - 1)];
            output += " is ";

            if(firstFlag == true) {
            		firstFlag = false;
            		output += relation + " correlated with " + colNames[targetCol] + ". ";
            } else {
            		output += relation + " correlated with it. ";
            }
        }

        // Add compressed sentences to output as well
        output += compressedSentences(posSingleSet, negSingleSet, colNames, targetCol, firstFlag);

//        System.out.println(output);
        return output;
    }
    
    /* Helper function to generate compressed sentence */
    private String compressedSentences(List<Integer> positiveCols, List<Integer> negativeCols,
                                       String[] columnNames, int targetColumn, boolean firstFlag) {
        String outputSpeech = "";

        if(positiveCols.size() > 1) {
            for(int i = 0; i < positiveCols.size() - 1; i++) {
                outputSpeech += columnNames[positiveCols.get(i)];
                outputSpeech += ", ";
            }
            outputSpeech += "and ";
            outputSpeech += columnNames[positiveCols.get(positiveCols.size() - 1)];
            outputSpeech += " are positively correlated with ";
            
            if(firstFlag == true) {
            		firstFlag = false;
            		outputSpeech += columnNames[targetColumn];
            		outputSpeech += ". ";
            } else {
            		outputSpeech += "it. ";
            }
        } else if(positiveCols.size() == 1) {
            outputSpeech += columnNames[positiveCols.get(0)];
            outputSpeech += " is positively correlated with ";
            
            if(firstFlag == true) {
	        		firstFlag = false;
	        		outputSpeech += columnNames[targetColumn];
	        		outputSpeech += ". ";
	        } else {
	        		outputSpeech += "it. ";
	        }
        }

        if(negativeCols.size() > 1) {
            for(int i = 0; i < negativeCols.size() - 1; i++) {
                outputSpeech += columnNames[negativeCols.get(i)];
                outputSpeech += ", ";
            }
            outputSpeech += "and ";
            outputSpeech += columnNames[negativeCols.get(negativeCols.size() - 1)];
            outputSpeech += " are negatively correlated with ";
            
            if(firstFlag == true) {
	        		firstFlag = false;
	        		outputSpeech += columnNames[targetColumn];
	        		outputSpeech += ". ";
	        } else {
	        		outputSpeech += "it. ";
	        }
        } else if(negativeCols.size() == 1) {
            outputSpeech += columnNames[negativeCols.get(0)];
            outputSpeech += " is negatively correlated with ";
            
            if(firstFlag == true) {
	        		firstFlag = false;
	        		outputSpeech += columnNames[targetColumn];
	        		outputSpeech += ". ";
	        } else {
	        		outputSpeech += "it. ";
	        }
        }

        return outputSpeech;
    }

    /**
     * Overridden equals to check if the two speeches are identical
     * */
    @Override
    public boolean equals(Object object) {

        // If the classes don't match
        if(object.getClass() != MultiCorrelations.class) {
            return false;
        }

        MultiCorrelations anotherSpeech = (MultiCorrelations) object;

        // If the sizes don't match
        if(this.correlatedCols.size() != anotherSpeech.correlatedCols.size()) {
            return false;
        }

        for(int i = 0; i < this.correlatedCols.size(); i++) {

            // If cannot find a given column
            if(!anotherSpeech.correlatedCols.contains(this.correlatedCols.get(i))) {
                return false;
            }

            // If the corresponding relations are not identical
            int j = anotherSpeech.correlatedCols.indexOf(this.correlatedCols.get(i));
            if(this.correlations.get(i) != anotherSpeech.correlations.get(j)) {
                return false;
            }
        }

        return true;
    }
}

