package database_accessor;

import data_info.DataAnalysis;

import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Used to access data in MonetDB
 * */
public class MonetDbAccessor extends DbAccessor {
	
	/**
	 * Constructor
	 * @param: the name of the table
	 * */
	public MonetDbAccessor(String tableName) {
		this.tableName = tableName;
		this.connection = getConnected();
	}
	
	/**
	 * Overridden function to get connected to the database
	 * */
	@Override
	public Connection getConnected() {
		
		Connection connection = null;
		
		try {
            // make a connection to the MonetDB server using JDBC URL starting with: jdbc:monetdb:// 
            connection = DriverManager.getConnection("jdbc:monetdb://localhost:50000/voc?so_timeout=10000",
            		"monetdb", "monetdb");
        } catch (SQLException se) {
            System.out.println(se.getMessage());
        }
		
		return connection;
    }
	
	  /**
	  * Extract a single tuple specified by a SQL command
	  * Return a double array
	  * */
	 public void extractSingleTuple(String SQL) {
	
	     Statement stmt;
	     try {
	         stmt = connection.createStatement();
	         ResultSet resultSet = stmt.executeQuery(SQL);
	
	         resultSet.next();
	
	         for(int i = 1; i <= 2; i++) {
	             System.out.println(resultSet.getString(i));
	         }
	     } catch (SQLException e) {
	         e.printStackTrace();
	     }
	 }
	
    /** 
     * Overridden getSample function
     * Generate a sample together with a constraint query 
     * @param: the sample fraction, constraint SQL and specific data
     * @return: the list of tuples
     * */
	@Override
    public List<List<Double>> getSample(double fraction, String constraint, DataAnalysis someData) {
		
		long startTime = System.currentTimeMillis();
		
    	List<List<Double>> sample = new ArrayList<List<Double>>();
    		
        /*
         * Reconstruct the SQL query
         * Can only put TABLESAMPLE in the inner query
         * */
    		String sampleSQL = "";
    		if(DbAccessor.ifPercent == true) {
    			sampleSQL = "(SELECT * FROM " + this.tableName 
    					+ " SAMPLE " + fraction + ") AS tmp";
    		} else {
    			sampleSQL = "(SELECT * FROM " + this.tableName 
    					+ " SAMPLE " + (int) fraction + ") AS tmp";
    		}
        String preSQL = constraint.substring(0, constraint.indexOf("FROM")) + "FROM ";
        String postSQL = "";

        if(constraint.indexOf("WHERE") > 0) {
            postSQL += " ";
            postSQL += constraint.substring(constraint.indexOf("WHERE"));
        } else {
            postSQL += ";";
        }

        String SQL = preSQL + sampleSQL + postSQL;
        System.out.println(SQL);

        Statement stmt;
        try {
            stmt = (Statement) connection.createStatement();
            ResultSet resultSet = stmt.executeQuery(SQL);

            while(resultSet.next()) {
                sample.add(someData.storeTuple(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        System.out.printf("Sampling costs %d ms\n", endTime - startTime);
        
        return sample;
    }
	
    /** 
     * Overridden getSample function
     * Generate a sample together with a constraint query 
     * @param: the number of tuples of sampling, constraint SQL and specific data
     * @return: the list of tuples
     * */
	@Override
    public List<List<Double>> getSample(int tupleNum, String constraint, DataAnalysis someData) {
		
		long startTime = System.currentTimeMillis();
		
    	List<List<Double>> sample = new ArrayList<List<Double>>();
    		
        /*
         * Reconstruct the SQL query
         * Can only put TABLESAMPLE in the inner query
         * */
        String sampleSQL = "(SELECT * FROM " + this.tableName 
        		+ " SAMPLE " + tupleNum + ") AS tmp";
        String preSQL = constraint.substring(0, constraint.indexOf("FROM")) + "FROM ";
        String postSQL = "";

        if(constraint.indexOf("WHERE") > 0) {
            postSQL += " ";
            postSQL += constraint.substring(constraint.indexOf("WHERE"));
        } else {
            postSQL += ";";
        }

        String SQL = preSQL + sampleSQL + postSQL;
        System.out.println(SQL);

        Statement stmt;
        try {
            stmt = (Statement) connection.createStatement();
            ResultSet resultSet = stmt.executeQuery(SQL);

            while(resultSet.next()) {
                sample.add(someData.storeTuple(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        System.out.printf("Sampling costs %d ms\n", endTime - startTime);
        
        return sample;
    }
}
