package database_accessor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import data_info.DataAnalysis;

/** Used to access database */
public abstract class DbAccessor {
	
	// Indicate which table we are using
	protected String tableName;
	protected Connection connection;
	public static String dbType;
	public static boolean ifPercent;

    /** Connect to the table */
    public abstract Connection getConnected();

    /** Disconnect from the table */
    public void disconnected() {
        // Close the connection after getting the result
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Generate a sample together with a constraint query */
    public abstract List<List<Double>> getSample(double fraction, String constraint, DataAnalysis someData);
    
    /** Generate a sample together with a constraint query */
    public List<List<Double>> getSample(int tupleNum, String constraint, DataAnalysis someData) {
    		System.out.println("Shouldn't use tuple num to sample on PostgreSQL!");
    		System.exit(1);
    		return null;
    }
    
	  /**
	  * Extract a single tuple specified by a SQL command
	  * Return a double array
	  * */
	 public void extractSingleTuple(String SQL) {
	
	     PreparedStatement preparedStmt;
	     try {
	         preparedStmt = (PreparedStatement)connection.prepareStatement(SQL);
	         ResultSet resultSet = preparedStmt.executeQuery();
	
	         resultSet.next();
	
	         for(int i = 1; i <= 2; i++) {
	             System.out.println(resultSet.getString(i));
	         }
	     } catch (SQLException e) {
	         e.printStackTrace();
	     }
	 }
}
