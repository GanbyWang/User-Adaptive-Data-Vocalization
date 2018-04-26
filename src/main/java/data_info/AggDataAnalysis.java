package data_info;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import database_accessor.DbAccessor;
import database_accessor.MonetDbAccessor;
import database_accessor.PostgreSQLDbAccessor;

/**
 * Class of the agg_match_stats relation
 * Extend DataAnalysis class
 * */
public class AggDataAnalysis extends DataAnalysis {
	
	/**
	 * Constructor function
	 * @param: the same as super-class
	 * */
	public AggDataAnalysis(double sampleFraction, String extraSQL,
						   int tarCol, List<Integer> relatedCols, int sampleSize) {
		// Call the super-class constructor first
		super(sampleFraction, extraSQL, tarCol, relatedCols, sampleSize);
		
		// Check the argument
		if(tarCol < 0 || tarCol > 10) {
			System.out.println("Illegal target column number");
			System.exit(1);
		}
		
		// Do initialization
		this.numericalNum = 10;
		this.relationName = "agg_match_stats";
        setNames();
        
        getSample();
	}
	
	/* Helper function to set the table names */
	protected void setNames() {
		columnNames = new String[10];
		// columnNames[0] = "date";
		columnNames[0] = "game size";
		// columnNames[2] = "match id";
		// columnNames[3] = "match mode";
		columnNames[1] = "party size";
		columnNames[2] = "number of assists";
		columnNames[3] = "number of knockdowns";
		columnNames[4] = "distance with vehicle";
		columnNames[5] = "distance on foot";
		columnNames[6] = "hitpoint";
		columnNames[7] = "number of kills";
		// columnNames[11] = "player name";
		columnNames[8] = "survive time";
		// columnNames[13] = "team id";
		columnNames[9] = "team rank";
		
		tableNames = new String[10];
		// tableNames[0] = "date";
		tableNames[0] = "game_size";
		// tableNames[2] = "match_id";
		// tableNames[3] = "match_mode";
		tableNames[1] = "party_size";
		tableNames[2] = "player_assists";
		tableNames[3] = "player_dbno";
		tableNames[4] = "player_dist_ride";
		tableNames[5] = "player_dist_walk";
		tableNames[6] = "player_dmg";
		tableNames[7] = "player_kills";
		// tableNames[11] = "player_name";
		tableNames[8] = "player_survive_time";
		// tableNames[13] = "team_id";
		tableNames[9] = "team_placement";

		setRelatedCols();
	}
	
	/** This function is used for reading in a tuple from database */
	@Override
	public List<Double> storeTuple(ResultSet resultSet) {
		List<Double> tmpTuple = new ArrayList<Double>();
		List<Double> result = new ArrayList<Double>();
		
		try {
	        for(int i = 0; i < 15; i++) {
				// Skip non-numerical columns
				if(i == 0 || i == 2 || i == 3 || i == 11 || i == 13) {
					continue;
				}
				tmpTuple.add(resultSet.getDouble(i + 1));
	        }
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		for(int i = 0; i < tmpTuple.size(); i++) {
			if(relatedCols.contains(i)) {
				result.add(tmpTuple.get(i));
			}
		}

		return result;
	}

}
