package data_vocal.main_package;

import data_info.DataAnalysis;
import data_info.AggDataAnalysis;
import data_info.HrDataAnalysis;
import data_info.LolDataAnalysis;
import database_accessor.DbAccessor;
import database_accessor.MonetDbAccessor;
import speeches.MixedSpeechGenerator;
import speeches.SpeechGenerator;
import speeches.speech_generator_UCT.SpeechGeneratorUCT;

import java.io.InputStreamReader;
import java.util.*;

/**
 * The class of the program
 * */
public class App {
    public static void main( String[] args) {

        // Use PosgreSQL with percentage sampling
		DbAccessor.dbType = "Postgre";
		DbAccessor.ifPercent = true;

		Scanner scanner = new Scanner(new InputStreamReader(System.in));

		// Read in the data set name
//		System.out.println("Enter data set name:");
//		String dataSetName = scanner.next();
        String dataSetName = "agg_match_stats";

		// Read in the target column as well as related columns
		System.out.println("Enter target column:");
		int targetCol = scanner.nextInt();
//        System.out.println("Enter related columns:");
//
//        // Remove the new line
//        scanner.nextLine();
//        String[] tmpStrs = scanner.nextLine().split(" ");
//
//        int[] tmpInts = new int[tmpStrs.length];
//        for(int i = 0; i < tmpInts.length; i++) {
//            tmpInts[i] = Integer.valueOf(tmpStrs[i]);
//        }
//        List<Integer> relatedCols = new ArrayList<Integer>();
//        for(Integer tmpInt : tmpInts) {
//            relatedCols.add(tmpInt);
//        }
//        if(!relatedCols.contains(targetCol)) {
//            relatedCols.add(targetCol);
//        }
//        Collections.sort(relatedCols);

        List<Integer> relatedCols = new ArrayList<Integer>();
        for(int i = 0; i < 10; i++) {
            relatedCols.add(i);
        }

		// The maximum number of columns in a speech is fixed
		int maxCol = 3;

        // Read in the sample fraction
//		System.out.println("Enter sampling fraction (0.0 - 100.0): " );
//		double sampleFraction = scanner.nextDouble();
        double sampleFraction = 100;

		// Get the maximum size of the sample
//		System.out.println("Enter the maximum size of the sample: ");
//		int sampleSize = scanner.nextInt();
        int sampleSize = 10000;

		// Read in the predicate
        // Right now only support equality predicate
//		System.out.println("Enter predicate: ");
//		// Remove the new line
//		scanner.nextLine();
//		String extraSQL = scanner.nextLine();
        String extraSQL = "NULL";

		// If there's no predicate
        if(extraSQL.equals("NULL")) {
            extraSQL = "SELECT * FROM " + dataSetName + ";";
        }

//    	// Target column and maximum column number are distinguished as integers
//        int targetCol = Integer.valueOf(args[0]);
//        int maxCol = Integer.valueOf(args[1]);
//        double sampleFraction = Double.valueOf(args[2]);
//        String extraSQL = args[3];
//        System.out.printf("Extra query constraint is: \"%s\"\n", extraSQL);
        
//        long startTime = System.currentTimeMillis();
        
        // Read the basic information in
        DataAnalysis data = null;
        if(dataSetName.equals("hr_comma_sep")) {
            data = new HrDataAnalysis(sampleFraction, extraSQL, targetCol, relatedCols, sampleSize);
        } else if(dataSetName.equals("agg_match_stats")) {
            data = new AggDataAnalysis(sampleFraction, extraSQL, targetCol, relatedCols, sampleSize);
        } else {
            new LolDataAnalysis(sampleFraction, extraSQL, targetCol, relatedCols,sampleSize);
        }

        targetCol = data.getResetTarCol();
//        System.out.println(targetCol);
        
//        long endTime = System.currentTimeMillis();
//        // Calculate the time of reading in the sample
//        long readInDataTime = endTime - startTime;
        
//        try {
//            data.readFile();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        long startTime = System.currentTimeMillis();
//
//        // Generate the output speech
//        SpeechGenerator generator = new SpeechGenerator(targetCol, data,
//                maxCol, sampleFraction, extraSQL);
//
//        long endTime = System.currentTimeMillis();
//
//        System.out.printf("Single-col implementation costs %d ms\n", endTime - startTime);

        DistributionGenerator disGenerator = new DistributionGenerator(data, targetCol, sampleFraction);

//        // Generate the output speech
//        MixedSpeechGenerator mixedGenerator = new MixedSpeechGenerator(targetCol, data,
//                maxCol, sampleFraction, extraSQL);

        // The time limit of generating the speech
        long runTimeLimit = 500;
        SpeechGeneratorUCT speechGeneratorUCT = new SpeechGeneratorUCT(targetCol, data, maxCol,
                sampleFraction, runTimeLimit);

//        System.out.printf("Reading in the sample costs %d ms\n", readInDataTime);
//        System.out.printf("Generating the speech costs %d ms\n", generatingTime);

        // Read out the result
//        Speaker speaker = new Speaker();
//        speaker.speak(mixedGenerator.readOutResult());
    }
}