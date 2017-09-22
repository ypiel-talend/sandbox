package org.ypiel.sandbox.verticacopystream;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.vertica.jdbc.VerticaConnection;
import com.vertica.jdbc.VerticaCopyStream;

public class TestVerticaCopyStream {

	public static void main(String[] args) {
		// Note: If running on Java 5, you need to call Class.forName
		// to manually load the JDBC driver.
		// Set up the properties of the connection
		Properties myProp = new Properties();
		myProp.put("user", "talend"); // Must be superuser
		myProp.put("password", "aze123#");
		// When performing bulk loads, you should always disable the
		// connection's AutoCommit property to ensure the loads happen as
		// efficiently as possible by reusing the same COPY command and
		// transaction.
		myProp.put("AutoCommit", "false");
		Connection conn;
		try {
			conn = DriverManager.getConnection("jdbc:vertica://192.168.50.133:5433/docker", myProp);
			Statement stmt = conn.createStatement();

			// Create a table to receive the data
			stmt.execute("DROP TABLE IF EXISTS customers;");
			stmt.execute("CREATE TABLE IF NOT EXISTS Public.customers (" //
					+ "  id integer PRIMARY KEY NOT NULL,"//
					+ "  name varchar(30) NOT NULL," //
					+ "  age integer NOT NULL,"//
					+ "  comment varchar(255)"//
					+ ");");//
			conn.commit();

			// Prepare the query to insert from a stream. This query must use
			// the COPY statement to load data from STDIN. Unlike copying from
			// a file on the host, you do not need superuser privileges to
			// copy a stream. All your user account needs is INSERT privileges
			// on the target table.
			String copyQuery = "COPY customers FROM STDIN " + "DELIMITER '|' DIRECT ENFORCELENGTH";

			// Create an instance of the stream class. Pass in the
			// connection and the query string.
			VerticaCopyStream stream = new VerticaCopyStream((VerticaConnection) conn, copyQuery);

			// Keep running count of the number of rejects
			int totalRejects = 0;

			// start() starts the stream process, and opens the COPY command.
			stream.start();

			// If you added streams to VerticaCopyStream before calling start(),
			// You should check for rejects here (see below). The start() method
			// calls execute() to send any pre-queued streams to the server
			// once the COPY statement has been created.

			// Simple for loop to load 5 text files named customers-1.txt to
			// customers-5.txt
			for (int loadNum = 0; loadNum <= 5; loadNum++) {
				// Prepare the input file stream. Read from a local file.
				String filename = "./resources/fill_vertica"+loadNum+".csv";
				System.out.println("\n\nLoading file: " + filename);
				File inputFile = new File(filename);
				FileInputStream inputStream = new FileInputStream(inputFile);

				// Add stream to the VerticaCopyStream
				stream.addStream(inputStream);

				// call execute() to load the newly added stream. You could
				// add many streams and call execute once to load them all.
				// Which method you choose depends mainly on whether you want
				// the ability to check the number of rejections as the load
				// progresses so you can stop if the number of rejects gets too
				// high. Also, high numbers of InputStreams could create a
				// resource issue on your client system.
				stream.execute();

				// Show any rejects from this execution of the stream load
				// getRejects() returns a List containing the
				// row numbers of rejected rows.
				List<Long> rejects = stream.getRejects();

				// The size of the list gives you the number of rejected rows.
				int numRejects = rejects.size();
				totalRejects += numRejects;
				System.out.println("Number of rows rejected in load #" + loadNum + ": " + numRejects);

				// List all of the rows that were rejected.
				Iterator<Long> rejit = rejects.iterator();
				long linecount = 0;
				while (rejit.hasNext()) {
					System.out.print("Rejected row #" + ++linecount);
					System.out.println(" is row " + rejit.next());
				}
			}
			// Finish closes the COPY command. It returns the number of
			// rows inserted.
			long results = stream.finish();
			System.out.println("Finish returned " + results);

			// If you added any streams that hadn't been executed(),
			// you should also check for rejects here, since finish()
			// calls execute() to

			// You can also get the number of rows inserted using
			// getRowCount().
			System.out.println("Number of rows accepted: " + stream.getRowCount());
			System.out.println("Total number of rows rejected: " + totalRejects);

			// Commit the loaded data
			conn.commit();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
