package data_info;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import database_accessor.DbAccessor;
import database_accessor.MonetDbAccessor;
import database_accessor.PostgreSQLDbAccessor;

/**
 * Sub-class of DataAnalysis
 * Describe the hr_comma_sep table
 * */
public class HrDataAnalysis extends DataAnalysis {
	
	/**
	 * Constructor function
	 * @param: the same as super-class
	 * */
	public HrDataAnalysis(double sampleFraction, String extraSQL,
						  int tarCol, List<Integer> relatedCols, int sampleSize) {
		// Call the super-class constructor first
		super(sampleFraction, extraSQL, tarCol, relatedCols, sampleSize);
		
		// Check the argument
		if(tarCol > 7 || tarCol < 0) {
			System.out.println("Illegal target column number");
			System.exit(1);
		}
		
		// Do initialization
		this.numericalNum = 8;
		this.relationName = "hr_comma_sep";
        setNames();
		
        getSample();
	}
	
	/* Helper function to set the table names */
	protected void setNames() {
		columnNames = new String[8];
		columnNames[0] = "satisfaction level";
		columnNames[1] = "last evaluation";
		columnNames[2] = "the number of projects";
		columnNames[3] = "average of monthly hours";
		columnNames[4] = "time spent in company";
		columnNames[5] = "work accidents";
		columnNames[6] = "left";
		columnNames[7] = "promotion in last 5 years";
//		columnNames[8] = "sales";
//		columnNames[9] = "salary";
		
		tableNames = new String[8];
		tableNames[0] = "satisfaction_level";
		tableNames[1] = "last_evaluation";
		tableNames[2] = "number_project";
		tableNames[3] = "average_montly_hours";
		tableNames[4] = "time_spend_company";
		tableNames[5] = "work_accident";
		tableNames[6] = "if_left";
		tableNames[7] = "promotion_last_5years";
//		tableNames[8] = "sales";
//		tableNames[9] = "salary";

		setRelatedCols();
	}
	
	/**
	 * Overridden function to read in a single tuple from database
	 * */
	@Override
	public List<Double> storeTuple(ResultSet resultSet) {
		List<Double> result = new ArrayList<Double>();
		
		try {
	        for(int i = 0; i < 8; i++) {
	        	if(relatedCols.contains(i)) {
					result.add(resultSet.getDouble(i + 1));
				}
	        }
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		return result;
	}
}