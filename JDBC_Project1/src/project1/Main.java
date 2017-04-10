package project1;

import java.util.Scanner;
import java.sql.*;

public class Main {
	final static String DATABASE = "moviedb";
	
	public static void printCause (SQLException error) {
		boolean exit=false;
		while (error!=null && !exit) {
			int code = error.getErrorCode();
			String state = error.getSQLState();
			switch (code) {
			case 1045:
				System.out.println("Either user or password incorrect.");
				exit = true;
				break;
			case 1049:
				System.out.println(error.getMessage());
				exit = true;
				break;
			default:
				Throwable t = error.getCause();
				if (t==null) {
					System.out.println("Error Code: "+error.getErrorCode());
					System.out.println("State: "+error.getSQLState());
					System.out.println(error.getMessage());
				}
				error = (SQLException) t;
				break;
			}
		}
	}
	
	public static void printRow (ResultSet result) throws SQLException {
		ResultSetMetaData meta = result.getMetaData();
		for (int i=1;i<=meta.getColumnCount();++i) {
			int type = meta.getColumnType(i);
			String resString = "";
			switch(type) {
			case Types.INTEGER:
				resString+=result.getInt(i);
				break;
			default:
				resString=result.getString(i);
				break;
			}
			System.out.print(resString);
			if (i<meta.getColumnCount()) {
				System.out.print(", ");
			}
		}
	}

	public static void printAttributes (ResultSet result) throws SQLException {
		ResultSetMetaData meta = result.getMetaData();
		for (int i=1;i<=meta.getColumnCount();++i) {
			System.out.print(meta.getColumnLabel(i)+" "+meta.getColumnTypeName(i));
			if (i<meta.getColumnCount()) {
				System.out.print(", ");
			}
		}
		System.out.println();
	}
	
	public static void printResult (ResultSet result) throws SQLException {
		ResultSetMetaData meta = result.getMetaData();
		for (int i=1;i<=meta.getColumnCount();++i) {
			System.out.print(meta.getColumnLabel(i));
			if (i<meta.getColumnCount()) {
				System.out.print(", ");
			}
		}
		System.out.println();
		while (result.next()) {
			printRow(result);
			System.out.println();
		}
	}
	
	public static void printRowChange (int count, String change) {
		if (count > 1) {
			System.out.println(count+" rows "+change+".");
		} else if (count == 1) {
			System.out.println(count+" row "+change+".");
		} else {
			System.out.println("No rows were "+change+".");
		}
	}
	
	

	public static void queryMain (Connection conn) {
		Scanner cmdline = new Scanner(System.in);
		boolean running = true;
		while (running) {
			System.out.println("Enter query command. Known commands:\n"
					+"starmovies, addstar, addcust, delcust, meta\n"
					+"query, quit");
			String inp = cmdline.nextLine();
			String fname = "";
			String lname = "";
			String name = "";
			switch(inp.toLowerCase().trim()) {
			case "starmovies":
				System.out.println("Enter first name of movie star.");
				fname = cmdline.nextLine();
				System.out.println("Enter last name of movie star.");
				lname = cmdline.nextLine();
				if (fname.trim().compareTo("")==0 && lname.trim().compareTo("")==0) {
					System.out.println("Both first and last name empty. At least one required.");
				} else {
					String search = "SELECT * FROM movies, stars, stars_in_movies sm WHERE";
					search+=" sm.star_id=stars.id AND sm.movie_id=movies.id";
					if (fname.trim().compareTo("")!=0) {
						search+=" AND stars.first_name = '"+fname+"'";
					}
					if (lname.trim().compareTo("")!=0) {
						search+=" AND stars.last_name = '"+lname+"'";
					}
					search+=" ORDER BY movies.title";
					try {
						Statement statement = conn.createStatement();
						ResultSet result  = statement.executeQuery(search);
						printResult(result);
					} catch (SQLException error) {
						printCause(error);
					}
				}
				break;
			case "addstar":
				System.out.println("Enter name of movie star.");
				name = cmdline.nextLine();
				name = name.trim();
				fname = "";
				lname = "";
				if (name.compareTo("")==0) {
					System.out.println("A name can not be empty.");
					break;
				} else if (name.split(" ").length > 2) {
					System.out.println("Name is too long for system.\n"
							+"First and/(or only) last name expected.");
					break;
				} else if (name.split(" ").length == 1) {
					lname=name;
				} else {
					fname = name.split(" ")[0];
					lname = name.split(" ")[1];
				}
				String insert = "INSERT INTO stars (first_name, last_name) VALUES ('"
						+fname+"', '"+lname+"')"; 
				try {
					Statement statement = conn.createStatement();
					int result = statement.executeUpdate(insert);
					printRowChange(result,"updated");
				} catch (SQLException error) {
					printCause(error);
				}
				break;
			case "addcust":
				System.out.println("Enter the name of the Customer to be added.");
				name = cmdline.nextLine();
				name=name.trim();
				fname="";
				lname="";
				if (name.compareTo("")==0) {
					System.out.println("A name can not be empty.");
					break;
				} else if (name.split(" ").length > 2) {
					System.out.println("Name is too long for system.\n"
							+"First and/(or only) last name expected.");
					break;
				} else if (name.split(" ").length == 1) {
					lname=name;
				} else {
					fname = name.split(" ")[0];
					lname = name.split(" ")[1];
				}
				
				//TODO
				try
				{
					///Find creditcard holder's id given creditcard holder's first name and last name.
					//Select id from creditcards where first_name="first" and last_name="last
					String ccQuery = "SELECT id FROM creditcards WHERE first_name = ? and last_name = ?";
					PreparedStatement ccStatement = conn.prepareStatement(ccQuery);
					ccStatement.setString(1, fname);
					ccStatement.setString(2, lname);
					ResultSet resultIDSet = ccStatement.executeQuery();
					
					//if no credit card ids are associated with the first and last name provided.
					if(!resultIDSet.next())
					{
						System.out.println("Cannot add " + fname + " " + lname + " into the database ");
						return;
					}
					
					String cc_id = resultIDSet.getString(1);
					//insert the customer into the database
					String insertSQL = "INSERT INTO customers (first_name, last_name, cc_id, address, email, password) VALUES(?, ?, ?, ?, ?, ?)";	
					PreparedStatement insertStatement = conn.prepareStatement(insertSQL);
					
					String address = "";
					String email = "";
					String password = "";
					
					System.out.print("Enter address: ");
					address = cmdline.nextLine();
					System.out.print("Enter email: ");
					email = cmdline.nextLine();
					System.out.print("Enter password: ");
					password = cmdline.nextLine();
					
					insertStatement.setString(1, fname);
					insertStatement.setString(2, lname);
					insertStatement.setString(3, cc_id);
					insertStatement.setString(4, address);
					insertStatement.setString(5, email);
					insertStatement.setString(6,  password);
					
					insertStatement.executeUpdate();
					System.out.println("Successfully inserted: " + name + " into the customers table");
				}
				catch(SQLException e)
				{
					printCause(e);
				}
				
				break;
			case "delcust":
				System.out.println("Enter the id of the Customer to be deleted.");
				String id = cmdline.nextLine();
				id=id.trim();
				String del = " DELETE FROM customers WHERE customers.id="+id;
				try {
					Statement statement = conn.createStatement();
					int result = statement.executeUpdate(del);
					printRowChange(result,"deleted");
				} catch (SQLException error) {
					printCause(error);
				}
				break;
			case "meta":
				try {
//					Statement statement = conn.createStatement();
//					ResultSet result = statement.executeQuery("SHOW TABLES");
//					System.out.println("Tables of "+DATABASE+":");
//					while (result.next()) {
//						printRow(result);
//						System.out.print(", ");
					
					DatabaseMetaData metadataDB = conn.getMetaData();
					ResultSet tables = metadataDB.getTables(conn.getCatalog(), null, "%", null);
					
					while(tables.next())
					{
						String tableName = tables.getString("TABLE_NAME");
						System.out.println(tableName);
						ResultSet tableColumns = metadataDB.getColumns(conn.getCatalog(),null,tableName, "%");
						
						
						tableColumns.next();
						String columnName = tableColumns.getString("COLUMN_NAME");
						String columnType = tableColumns.getString("TYPE_NAME");
						System.out.print(columnName + ":" +  columnType);
						
						while(tableColumns.next())
						{
							columnName = tableColumns.getString("COLUMN_NAME");
							columnType = tableColumns.getString("TYPE_NAME");
							System.out.print(" | " + columnName + ":" +  columnType);
						}
						System.out.println();
					}
					
					System.out.println();
				} catch (SQLException error) {
					printCause(error);
				}
				break;
			case "query":
				System.out.println("Type in your SELECT/UPDATE/INSERT/DELETE SQL statement.");
				String sql = cmdline.nextLine();
				sql = sql.trim();
				try {
					Statement statement = conn.createStatement();
					ResultSet result;
					int rowCount;
					if (sql.toUpperCase().startsWith("SELECT")) {
						result = statement.executeQuery(sql);
						printResult(result);
					} else if (sql.toUpperCase().startsWith("UPDATE")) {
						rowCount = statement.executeUpdate(sql);
						printRowChange(rowCount,"updated");
					} else if (sql.toUpperCase().startsWith("INSERT")) {
						rowCount = statement.executeUpdate(sql);
						printRowChange(rowCount,"inserted");
					} else if (sql.toUpperCase().startsWith("DELETE")) {
						rowCount = statement.executeUpdate(sql);
						printRowChange(rowCount,"deleted");
					} else {
						System.out.println("Query not of type SELECT, UPDATE, INSERT, or DELETE");
					}
				} catch (SQLException error) {
					printCause(error);
				}
				break;
			case "exit":
			case "quit":
				System.out.println("Exiting.");
				running=false;
				break;
			default:
			}
		}
	}
	
	public static void login() throws Exception {
		Scanner cmdline = new Scanner(System.in);
		for (int i=0;i<3;++i) {
			System.out.println("Enter your user name: ");
			String user = cmdline.nextLine();
			System.out.println("Enter password: ");
			String password = cmdline.nextLine();
			try {
				Connection connection = DriverManager.getConnection("jdbc:mysql:///"
				+DATABASE+"?autoReconnect=true"
				+"&useSSL=false",user,password);
				System.out.println("Login successful.");
				queryMain(connection);
				return;
			} catch (SQLException error)  {
				printCause(error);
				
			}
		}
		System.out.println("Three attempts given. Login failed.");
	}

	public static void main(String[] args) throws Exception {
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		boolean running = true;
		String inp = "";
		Scanner cmdline = new Scanner(System.in);
		//login();
		while (running) {
			System.out.println("Enter your command.");
			inp = cmdline.nextLine();
			if (inp.compareToIgnoreCase("login")==0) {
				login();
			} else if (inp.compareToIgnoreCase("quit")==0 || inp.compareToIgnoreCase("exit")==0) {
				running = false;
			} else {
				System.out.println("Available commands are login, quit.");
			}
		}
	}

}
