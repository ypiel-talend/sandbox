package org.ypiel.sandbox.verticacopylocal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class TestVerticaCopyLocal {

	public static void main(String[] args) {
		// Note: If using Java 5, you must call Class.forName to load the
		// JDBC driver.
		Properties myProp = new Properties();
		myProp.put("user", "talend"); // Do not need to superuser
		myProp.put("password", "aze123#");
		Connection conn;
		try {
			conn = DriverManager.getConnection("jdbc:vertica://192.168.50.133:5433/docker", myProp);
			// Disable AutoCommit
			conn.setAutoCommit(false);
			Statement stmt = conn.createStatement();
			// Create a table to hold data.
			stmt.execute("DROP TABLE IF EXISTS customers;");
			stmt.execute("CREATE TABLE IF NOT EXISTS Public.customers (" //
					+ "  id integer PRIMARY KEY NOT NULL,"//
					+ "  name varchar(30) NOT NULL," //
					+ "  age integer NOT NULL,"//
					+ "  comment varchar(255)"//
					+ ");");//
			conn.commit();

			// Use the COPY command to load data. Load directly into ROS, since
			// this load could be over 100MB. Use ENFORCELENGTH to reject
			// strings too wide for their columns.

			System.out.println("COPY LOCAL with all success");
			boolean result = stmt.execute(
					"COPY customers FROM LOCAL " + " './resources/fill_vertica_1100.csv.bz2' BZIP"); // DIRECT
																														// ENFORCELENGTH");

			// Determine if execution returned a count value, or a full result
			// set.
			if (result) {
				System.out.println("** Got result set");
			} else {
				// Count will usually return the count of rows inserted.
				System.out.println("Got count");
				int rowCount = stmt.getUpdateCount();
				System.out.println("Number of accepted rows = " + rowCount);
			}

			System.out.println("** COPY LOCAL with two rejects");
			result = stmt.execute("COPY customers FROM LOCAL './resources/fill_vertica_1000.csv'"); // DIRECT
																													// ENFORCELENGTH");

			// Determine if execution returned a count value, or a full result
			// set.
			if (result) {
				System.out.println("Got result set");
			} else {
				// Count will usually return the count of rows inserted.
				System.out.println("Got count");
				int rowCount = stmt.getUpdateCount();
				System.out.println("Number of accepted rows = " + rowCount);
			}

			conn.close();
		} catch (SQLException e) {
			System.out.print("Error: ");
			System.out.println(e.toString());
		}
	}

}
