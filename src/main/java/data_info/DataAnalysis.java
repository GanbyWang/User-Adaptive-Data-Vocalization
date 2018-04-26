package data_info;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import database_accessor.DbAccessor;
import database_accessor.MonetDbAccessor;
import database_accessor.PostgreSQLDbAccessor;
import speeches.SpeechRelation;

import java.io.File;
import java.io.FileReader;
import java.util.Random;

/**
 * Class of basic information of a data set
 * Every time use a new data set must extend from this class
 * */
public abstract class DataAnalysis {

    // The fraction of sampling
    protected double sampleFraction;
    // Accessor to the database
    protected DbAccessor dbAccessor;
    // The constraint of sampling
    protected String extraSQL;
    // The target column
    protected int tarCol;
    // The table we operate on
    protected String relationName;
    // The distribution information of the data
    protected String distrInfo;
    public List<Integer> relatedCols;
    // The maximum limit of the sample
    protected int sampleSize;
    
    /** General information of the sample */
    public double sampleMean;
    public double sampleVar;
    public double sampleMax;
    public double sampleMin;
    /** Extracted list of the target column */
    public List<Double> singleColList;
    /** The number of numerical columns */
    public int numericalNum;
    
    /**
    * The column names of the data
    * Change the names a little to make them more natural to read out
    * */
    public String[] columnNames;

    /** The table column names of the data in the database */
    public String[] tableNames;

//    /** Used to store the file */
//    public List<tuple> originData = new ArrayList<tuple>();

    /** The sample */
    public List<List<Double>> sample = new ArrayList<List<Double>>();

//    /** Used to store every tuple of the data */
//    public class tuple {
//        // Only store numerical columns
//        public double columns[] = new double[8];
//
//        /**
//         * Helper function
//         * Used to print all fields
//         * */
//        public void printInfo() {
//            System.out.printf("Satisfaction level: %f\n"
//                            + "Last evaluation: %f\n"
//                            + "Number of project: %f\n"
//                            + "Average monthly hours: %f\n"
//                            + "Time spend in company: %f\n"
//                            + "Work accident: %f\n"
//                            + "Left: %f\n"
//                            + "Promotion in last 5 years: %f\n",
//                    columns[0], columns[1], columns[2], columns[3],
//                    columns[4], columns[5], columns[6], columns[7]);
//        }
//    }

//    /**
//     * Factor matrix of the data
//     * The matrix is get from the Kaggle
//     * The link is https://www.kaggle.io/svf/441884/647b8c07ae7a081c547af6d9324351c1/__results__.html#
//     * The matrix is 8*8
//     * Every column stands for:
//     *   "satisfaction_level", "last_evaluation", "number_project", "average_monthly_hours",
//     *   "time_spend_company", "Work_accident", "left", "promotion_last_5years"
//     * in order.
//     * */
//    public double[][] factorMatrix = {
//            {1.00000000, 0.105021214, -0.142969586, -0.020048113, -0.100866073, 0.058697241, -0.38837498, 0.025605186},
//            {0.10502121, 1.000000000, 0.349332589, 0.339741800, 0.131590722, -0.007104289, 0.00656712, -0.008683768},
//            {-0.14296959, 0.349332589, 1.000000000, 0.417210634, 0.196785891, -0.004740548, 0.02378719, -0.006063958},
//            {-0.02004811, 0.339741800, 0.417210634, 1.000000000, 0.127754910, -0.010142888, 0.07128718, -0.003544414},
//            {-0.10086607, 0.131590722, 0.196785891, 0.127754910, 1.000000000, 0.002120418, 0.14482217, 0.067432925},
//            {0.05869724, -0.007104289, -0.004740548, -0.010142888, 0.002120418, 1.000000000, -0.15462163, 0.039245435},
//            {-0.38837498, 0.006567120, 0.023787185, 0.071287179, 0.144822175, -0.154621634, 1.00000000, -0.061788107},
//            {0.02560519, -0.008683768, -0.006063958, -0.003544414, 0.067432925, 0.039245435, -0.06178811, 1.000000000}
//    };

    /*
     * Constructor function
     * Accepts 3 arguments: sampling fraction, sampling constraint, target column
     * */
    protected DataAnalysis(double sampleFraction, String extraSQL, int tarCol,
                           List<Integer> relatedCols, int sampleSize) {
        this.sampleFraction = sampleFraction;
        this.extraSQL = extraSQL;
        this.tarCol = tarCol;
        this.relatedCols = new ArrayList<Integer>(relatedCols);
        this.sampleSize = sampleSize;
    }
    
    /* Function to calculate all general information of the target column */
    protected void getBasicInfo() {
    	
    	long startTime = System.currentTimeMillis();
    	
        singleColList = new ArrayList<Double>();
        singleColList.addAll(getSingleCol(sample, tarCol));

        double mean = getMean(singleColList);
        double var = getVariance(singleColList, mean);

//        System.out.printf("Mean: %f\n", mean);
//        System.out.printf("Var: %f\n", var);

        sampleMean = round(mean);
        sampleVar = round(var);
        sampleMax = getMax(singleColList);
        sampleMin = getMin(singleColList);
        
        long endTime = System.currentTimeMillis();
        System.out.printf("Getting the general information costs %d ms\n", endTime - startTime);
    }

    /*
     * Calculate the mean of the given array
     * One parameter: the array
     * */
    private double getMean(List<Double> list) {
        double sum = 0;
        for(Double each : list) {
            sum += each;
        }
        return sum / (double) list.size();
    }

    /*
     * Calculate the variance of the given array
     * Two parameter: the array and the mean of the array
     * */
    private double getVariance(List<Double> list, double mean) {
        double sum = 0;
        for(Double each : list) {
            sum += Math.pow(each - mean, 2);
        }
        return sum / (double) list.size();
    }

    /*
    * Helper function to round a double number
    * The result only have 2 effective bits
    * */
    private double round(double num) {

        if(num == 0) {
            return 0;
        }

        double cp1 = num;
        double cp2 = num;

        int posPower = 0;
        int negPower = 0;

        if(num > 1) {
            while (cp1 > 10) {
                posPower++;
                cp1 /= 10;
            }

            BigDecimal bd = new BigDecimal(cp1);
            return bd.setScale(1, BigDecimal.ROUND_HALF_EVEN).doubleValue() * Math.pow(10, posPower);
        } else {
            while(cp2 < 1) {
                negPower++;
                cp2 *= 10;
            }

            BigDecimal bd = new BigDecimal(cp2);
            return bd.setScale(1, BigDecimal.ROUND_HALF_EVEN).doubleValue() / Math.pow(10, negPower);
        }
    }

    /* Get the maximum of the given array */
    private Double getMax(List<Double> list) {
        double max = 0;
        for(Double element : list) {
            if(element > max) {
                max = element;
            }
        }

        return max;
    }

    /* Get the minimum of the given array */
    private Double getMin(List<Double> list) {
        double min = Double.MAX_VALUE;
        for(Double element : list) {
            if(element < min) {
                min = element;
            }
        }

        return min;
    }

    /* Extract every target column field from each tuple */
    private List<Double> getSingleCol(List<List<Double>> lists, int colNum) {
        List<Double> result = new ArrayList<Double>();

        for(List<Double> singleTuple : lists) {
            result.add(singleTuple.get(colNum));
        }

        return result;
    }

    /**
     * Helper function to print all general information of the target column
     * */
    public String colInfo() {
        String columnInfo = this.distrInfo;

        if(sampleMean >= 10) {
        	columnInfo += "with an average of " + (long) sampleMean;
        } else {
        	columnInfo += "with an average of " + sampleMean;
        }
        if(sampleVar >= 10) {
        	columnInfo += " and a variance of " + (long) sampleVar + ". ";
        } else {
        	columnInfo += " and a variance of " + sampleVar + ". ";
        }

        return columnInfo;
    }

    /** Different table has different way to store the data */
    public abstract List<Double> storeTuple(ResultSet resultSet);
    
    /** Different table has different number of columns and different column names */
    protected abstract void setNames();
    
    /** Helper function to get the table name */
    public String getRelationName() {
    		return this.relationName;
    }
    
    /** Setter function of distribution information */
    public void setDistrInfo(String distrInfo) {
    		this.distrInfo = distrInfo;
    }

    /** Setter function of related columns */
    public void setRelatedCols() {

        String[] newColumnNames = new String[relatedCols.size()];
        String[] newTableNames = new String[relatedCols.size()];

        for(int i = 0; i < newColumnNames.length; i++) {
            newColumnNames[i] = columnNames[relatedCols.get(i)];
            newTableNames[i] = tableNames[relatedCols.get(i)];
            // Reset the target column
            if(relatedCols.get(i) == tarCol) {
                tarCol = i;
            }
        }

        // Reset the arrays and the number of columns
        columnNames = newColumnNames;
        tableNames = newTableNames;
        numericalNum = relatedCols.size();
    }

    protected void getSample() {
        // Get the sample using SQL query
        if(DbAccessor.dbType == "Postgre") {
            dbAccessor = new PostgreSQLDbAccessor(this.relationName);
            // Set the size limit of the sample
            ((PostgreSQLDbAccessor) dbAccessor).setMaxSize(sampleSize);
        } else if (DbAccessor.dbType == "Monet") {
            dbAccessor = new MonetDbAccessor(this.relationName);
        }

        if(DbAccessor.ifPercent == true) {
            sample = dbAccessor.getSample((double) this.sampleFraction, this.extraSQL, this);
        } else {
            sample = dbAccessor.getSample(this.sampleFraction, this.extraSQL, this);
        }

        dbAccessor.disconnected();

        // Calculate the general information of the sample
        getBasicInfo();
    }

    /** Getter function of target column */
    public int getResetTarCol() {
        return this.tarCol;
    }

    /**
     * Public function to do the competing on the sample in the data
     * @param repeatTime: the repeat time
     * @param relatedSpeech: the speech to test on
     * @return the number of correct prediction
     * */
    public int compete(int repeatTime, SpeechRelation relatedSpeech) {
        int score = 0;

        if(relatedSpeech == null || relatedSpeech.relations.size() == 0) {
            return 0;
        }

        // It's bad to copy and paste the code...
        for(int i = 0; i < repeatTime; i++) {
            // Generate two random tuples
            int index1 = new Random().nextInt(sample.size());
            int index2 = new Random().nextInt(sample.size());
            while (index2 == index1) {
                index2 = new Random().nextInt(sample.size());
            }

            List<Double> tuple1 = sample.get(index1);
            List<Double> tuple2 = sample.get(index2);

            int score1 = 0;
            int score2 = 0;

            // Calculate scores
            for (int j = 0; j < relatedSpeech.relations.size(); j++) {
                int col = relatedSpeech.speech.columns.get(j);

                // If the factor is positive
                if (relatedSpeech.relations.get(j) > 0) {
                    if (tuple1.get(col) > tuple2.get(col)) {
                        score1++;
                    } else if (tuple1.get(col) < tuple2.get(col)) {
                        score2++;
                    }
                    // If the factor is negative
                } else {
                    if (tuple1.get(col) > tuple2.get(col)) {
                        score1--;
                    } else if (tuple1.get(col) < tuple2.get(col)) {
                        score2--;
                    }
                }
            }

            // If the prediction is correct, this speech gets one point
            if ((score1 > score2 && tuple1.get(tarCol) > tuple2.get(tarCol))
                    || (score1 < score2 && tuple1.get(tarCol) < tuple2.get(tarCol))) {
                score++;
            }
        }

        return score;
    }


//    /** This function reads in the CSV file and store all data into the array */
//    public void readFile() throws Exception {
//
//        // Read in the file
//        // The file is put in the res documentary
//        File file = new File("src/res/HR_comma_sep.csv");
//        FileReader fReader = new FileReader(file);
//        CSVReader csvReader = new CSVReader(fReader);
//
//        // Store the column names first
//        String[] colNames = csvReader.readNext();
//
//        // allData stores all information
//        List<String[]> allData = csvReader.readAll();
//        for(String[] singleData : allData) {
//            tuple tmp = new tuple();
//
//            for(int i = 0; i < 8; i++) {
//                tmp.columns[i] = Double.valueOf(singleData[i]);
//            }
//
//            // Add to the array
//            originData.add(tmp);
//        }
//        csvReader.close();
//    }
}
