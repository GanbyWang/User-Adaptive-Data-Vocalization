package data_vocal.main_package;

import java.util.List;

import data_info.DataAnalysis;
import database_accessor.DbAccessor;
import database_accessor.PostgreSQLDbAccessor;

/** Used to generate the optimal distribution */
public class DistributionGenerator {
    // Some basic data to the generator
    private DataAnalysis data;
    private int targetCol;
    private double sampleFraction;
    private String distributionInfo;
    // Accessor to the database
    private DbAccessor dbAccessor;

    /**
     * Constructor
     * Three parameters: basic data, the target column, the sample fraction
     * */
    public DistributionGenerator(DataAnalysis data, int targetCol, double sampleFraction) {
        this.data = data;
        this.targetCol = targetCol;
        this.sampleFraction = sampleFraction;

//        // Generate a new accessor to database
//        dbAccessor = new PostgreSQLDbAccessor(data.getRelationName());

        long startTime = System.currentTimeMillis();
        
        // Get the more possible distribution
        generateDistribution();
        
        long endTime = System.currentTimeMillis();
        System.out.printf("Getting the distribution costs %d ms\n", endTime - startTime);

//        dbAccessor.disconnected();
    }

//    /*
//     * Calculate the mean of the given array
//     * One parameter: the array
//     * */
//    private double getMean(List<Double> list) {
//        double sum = 0;
//        for(Double each : list) {
//            sum += each;
//        }
//        return sum / (double) list.size();
//    }
//
//    /*
//     * Calculate the variance of the given array
//     * Two parameter: the array and the mean of the array
//     * */
//    private double getVariance(List<Double> list, double mean) {
//        double sum = 0;
//        for(Double each : list) {
//            sum += Math.pow(each - mean, 2);
//        }
//        return sum / (double) list.size();
//    }
//
//    // Get the maximum of the given array
//    private Double getMax(List<Double> list) {
//        double max = 0;
//        for(Double element : list) {
//            if(element > max) {
//                max = element;
//            }
//        }
//
//        return max;
//    }
//
//    // Get the minimum of the given array
//    private Double getMin(List<Double> list) {
//        double min = Double.MAX_VALUE;
//        for(Double element : list) {
//            if(element < min) {
//                min = element;
//            }
//        }
//
//        return min;
//    }

    // Calculate the corresponding value of the given value
    private double getNormalValue(double number, double var, double avg) {
        double result = 1;
        double sigma = Math.sqrt(var);

        result *= 1.0/(Math.sqrt(2 * Math.PI) * sigma);
        double power = - (Math.pow(number - avg, 2)) / (2 * var);
        result *= Math.pow(Math.E, power);

        return result;
    }

    // Generate the more possible distribution
    private void generateDistribution() {
        // Generate a sample
//        List<Double> sample = dbAccessor.getSingleColSample(sampleFraction, data.tableNames[targetCol]);

        List<Double> sample = data.singleColList;

        double avg = data.sampleMean;
        double var = data.sampleVar;
        double max = data.sampleMax;
        double min = data.sampleMin;

        double normalScore = 0;
        double uniScore = 0;

        // Calculate the score
        for(Double each : sample) {
            normalScore += getNormalValue(each, var, avg);
            uniScore += 1 / (max - min);
        }

//        System.out.printf("Max: %f, Min: %f\n", max, min);
//        System.out.printf("Avg: %f, Var: %f\n", avg, var);
//        System.out.printf("Uni Score: %f, Nor Score: %f\n", uniScore, normalScore);

        if(normalScore > uniScore) {
            distributionInfo = "And it's more likely to be a normal distribution ";
        } else {
            distributionInfo = "And it's more likely to be a uniform distribution ";
        }

        // Store the information in data analysis
        data.setDistrInfo(distributionInfo);
//        System.out.printf("%s\n", distributionInfo);
    }

//    /** Read out the result */
//    public String readOutResult() {
//        return distributionInfo;
//    }
}