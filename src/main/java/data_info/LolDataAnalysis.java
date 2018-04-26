package data_info;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import database_accessor.DbAccessor;
import database_accessor.MonetDbAccessor;
import database_accessor.PostgreSQLDbAccessor;

/**
 * Sub-class of DataAnalysis
 * Describe the lol_match table
 * */
public class LolDataAnalysis extends DataAnalysis {
	
	/**
	 * Constructor function
	 * @param: the same as super-class
	 * */
	public LolDataAnalysis(double sampleFraction, String extraSQL,
						   int tarCol, List<Integer> relatedCols, int sampleSize) {
		// Call the super-class constructor first
		super(sampleFraction, extraSQL, tarCol, relatedCols, sampleSize);
		
		// Check the argument
		if(tarCol > 48 || tarCol < 0) {
			System.out.println("Illegal target column number");
			System.exit(1);
		}
		
		// Do initialization
		this.numericalNum = 48;
		this.relationName = "lol_match";
        setNames();
		
        getSample();
	}
			
	/* Helper function to set the table names */
	protected void setNames() {
		columnNames = new String[48];
		
//		columnNames[0] = "id";
		columnNames[0] = "if win";
//		columnNames[2] = "item1";
//		columnNames[3] = "item2";
//		columnNames[4] = "item3";
//		columnNames[5] = "item4";
//		columnNames[6] = "item5";
//		columnNames[7] = "item6";
//		columnNames[8] = "trinket";
		columnNames[1] = "kills";
		columnNames[2] = "deaths";
		columnNames[3] = "assists";
		columnNames[4] = "largest killing spree";
		columnNames[5] = "largest multi-kill";
		columnNames[6] = "killing sprees";
		columnNames[7] = "longest time spent living";
		columnNames[8] = "double kills";
		columnNames[9] = "triple kills";
		columnNames[10] = "quadra kills";
		columnNames[11] = "penta kills";
		columnNames[12] = "legendary kills";
		columnNames[13] = "total damage dealt";
		columnNames[14] = "magic damage dealt";
		columnNames[15] = "physical damage dealt";
		columnNames[16] = "true damage dealt";
		columnNames[17] = "largest crit";
		columnNames[18] = "total damage to champion";
		columnNames[19] = "magic damage to champion";
		columnNames[20] = "physical damage to champion";
		columnNames[21] = "true damage to champion";
		columnNames[22] = "total heal";
		columnNames[23] = "total units healed";
		columnNames[24] = "damage self mitigated";
		columnNames[25] = "damage to objectives";
		columnNames[26] = "damage to turrets";
		columnNames[27] = "vision score";
		columnNames[28] = "time CCing others";
		columnNames[29] = "total damage taken";
		columnNames[30] = "magic damage taken";
		columnNames[31] = "physical damage taken";
		columnNames[32] = "true damage taken";
		columnNames[33] = "gold earned";
		columnNames[34] = "gold spent";
		columnNames[35] = "turret kills";
		columnNames[36] = "inhibitors kills";
		columnNames[37] = "total minions killed";
		columnNames[38] = "neutral minions killed";
		columnNames[39] = "own jungle monsters killed";
		columnNames[40] = "enemy jungle monsters killed";
		columnNames[41] = "total CC time dealt";
		columnNames[42] = "champion level";
		columnNames[43] = "pinks bought";
		columnNames[44] = "wards bought";
		columnNames[45] = "wards placed";
		columnNames[46] = "wards killed";
		columnNames[47] = "first blood";
		
		tableNames = new String[48];
		
//		tableNames[0] = "id";
		tableNames[0] = "win";
//		tableNames[2] = "item1";
//		tableNames[3] = "item2";
//		tableNames[4] = "item3";
//		tableNames[5] = "item4";
//		tableNames[6] = "item5";
//		tableNames[7] = "item6";
//		tableNames[8] = "trinket";
		tableNames[1] = "kills";
		tableNames[2] = "deaths";
		tableNames[3] = "assists";
		tableNames[4] = "largestkillingspree";
		tableNames[5] = "largestmultikill";
		tableNames[6] = "killingsprees";
		tableNames[7] = "longesttimespentliving";
		tableNames[8] = "doublekills";
		tableNames[9] = "triplekills";
		tableNames[10] = "quadrakills";
		tableNames[11] = "pentakills";
		tableNames[12] = "legendarykills";
		tableNames[13] = "totdmgdealt";
		tableNames[14] = "magicdmgdealt";
		tableNames[15] = "physicaldmgdealt";
		tableNames[16] = "truedmgdealt";
		tableNames[17] = "largestcrit";
		tableNames[18] = "totdmgtochamp";
		tableNames[19] = "magicdmgtochamp";
		tableNames[20] = "physdmgtochamp";
		tableNames[21] = "truedmgtochamp";
		tableNames[22] = "totheal";
		tableNames[23] = "totunitshealed";
		tableNames[24] = "dmgselfmit";
		tableNames[25] = "dmgtoobj";
		tableNames[26] = "dmgtoturrets";
		tableNames[27] = "visionscore";
		tableNames[28] = "timecc";
		tableNames[29] = "totdmgtaken";
		tableNames[30] = "magicdmgtaken";
		tableNames[31] = "physdmgtaken";
		tableNames[32] = "truedmgtaken";
		tableNames[33] = "goldearned";
		tableNames[34] = "goldspent";
		tableNames[35] = "turretkills";
		tableNames[36] = "inhibkills";
		tableNames[37] = "totminionskilled";
		tableNames[38] = "neutralminionskilled";
		tableNames[39] = "ownjunglekills";
		tableNames[40] = "enemyjunglekills";
		tableNames[41] = "totcctimedealt";
		tableNames[42] = "champlvl";
		tableNames[43] = "pinksbought";
		tableNames[44] = "wardsbought";
		tableNames[45] = "wardsplaced";
		tableNames[46] = "wardskilled";
		tableNames[47] = "firstblood";

		setRelatedCols();
	}
	
	/**
	 * Overridden function to read in a single tuple from database
	 * */
	@Override
	public List<Double> storeTuple(ResultSet resultSet) {
		List<Double> tmpTuple = new ArrayList<Double>();
		List<Double> result = new ArrayList<Double>();
		
		try {
	        for(int i = 0; i < 56; i++) {
	        	// Skip useless fields
	        	if(i == 0 || (i >= 2 && i <= 8)) {
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
