package project1;

import java.util.Scanner;
import java.sql.*;

public class GamesConsole {
	final static String DATABASE = "gamedb";
	
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
	
	//Console options
	private static void displayConsoleCommands()
	{
		System.out.println("Known Console Commands:");
		System.out.println("pubgames, addgame, addcust, delcust, meta, query, logout\n");
	}
	
	private static void printGamesByPublisher(Connection conn, Scanner cmdline)
	{
		System.out.print("Enter name of publisher: ");
		String pubname = cmdline.nextLine();
		if (pubname.trim().compareTo("")==0) 
		{
			System.out.println("Publisher name cannot be empty.");
		} 
		else 
		{
			//print out games featuring a given publisher
			//1. games 2. publishers 3. publishers_of_games
			String searchPub = "SELECT id FROM publishers WHERE publisher = ?";
			String searchGames = "SELECT game_id FROM publishers_of_games WHERE publisher_id = ?";
			String listGames = "Select * FROM games WHERE id IN (" + searchGames + ")" + " ORDER BY games.name";
			try {
				PreparedStatement searchPubStatement = conn.prepareStatement(searchPub);
				PreparedStatement listGamesStatement = conn.prepareStatement(listGames);
				searchPubStatement.setString(1, pubname);
				
				ResultSet result = searchPubStatement.executeQuery();
				int game_id = -1;
				if(!result.next())
				{
					System.out.println("Cannot find publisher: " + pubname + " in the database");					
				}
				else //printout games from a given publisher existing in the database.
				{
					game_id = result.getInt(1);
					listGamesStatement.setInt(1,game_id);
					ResultSet gamesSet = listGamesStatement.executeQuery();
					printResult(gamesSet);	
				}
				
			} catch (SQLException error) {
				printCause(error);
			}
		}
	}
	
	//optional
	private static void printGamesByGenre(Connection conn)
	{
		
	}
	
	private static void addGame(Connection conn)
	{
		
	}
	
	//optional will do later
	private static void addPublisher(Connection conn)
	{
		
	}
	
	private static void addCustomer(Connection conn)
	{
		
	}
	
	private static void deleteCustomer(Connection conn)
	{
		
	}
	
	private static void displayMetadata(Connection conn)
	{
		
	}
	
	private static void runQueryCommand(Connection conn)
	{
		
	}
	
	
	public static void queryMain (Connection conn) {
		Scanner cmdline = new Scanner(System.in);
		boolean running = true;
		while (running) {
			System.out.println("Enter query command. Known commands:\n"
					+"pubgames, addgame, addcust, delcust, meta\n"
					+"query, quit");
			String inp = cmdline.nextLine();
			String pubname = "";
			switch(inp.toLowerCase().trim()) {
			case "pubgames":
				System.out.print("Enter name of publisher: ");
				pubname = cmdline.nextLine();
				if (pubname.trim().compareTo("")==0) {
					System.out.println("Publisher name cannot be empty.");
				} else {
					//print out games featuring a given publisher
					//1. games 2. publishers 3. publishers_of_games
					
					//Select publisher from publishers where publisher= "pubname"
					//Select game_id from publishers_of_games where publisher_id= given id
					String searchPub = "SELECT id FROM publishers WHERE publisher = ?";
					String searchGames = "SELECT game_id FROM publishers_of_games WHERE publisher_id = ?";
					String listGames = "Select * FROM games WHERE id IN (" + searchGames + ")" + " ORDER BY games.name";
					try {
						PreparedStatement searchPubStatement = conn.prepareStatement(searchPub);
						PreparedStatement listGamesStatement = conn.prepareStatement(listGames);
						searchPubStatement.setString(1, pubname);
						
						ResultSet result = searchPubStatement.executeQuery();
						int game_id = -1;
						if(!result.next())
						{
							System.out.println("Cannot find publisher: " + pubname + " in the database");
							break;							
						}
						else //printout games from a given publisher existing in the database.
						{
							game_id = result.getInt(1);
							listGamesStatement.setInt(1,game_id);
							ResultSet gamesSet = listGamesStatement.executeQuery();
							printResult(gamesSet);	
						}
						
					} catch (SQLException error) {
						printCause(error);
					}
				}
				break;
			case "addgame":
				String name;
				System.out.println("Enter name of game.");
				name = cmdline.nextLine();
				name = name.trim();
				if (name.compareTo("")==0) {
					System.out.println("game name can not be empty.");
					break;
				} 
				String insert = "INSERT INTO games (name) VALUES ( ? )"; 
				try {					
					PreparedStatement statement = conn.prepareStatement(insert);
					statement.setString(1, name);
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
				String fname="";
				String lname="";
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
			System.out.print("Enter your username: ");
			String user = cmdline.nextLine();
			System.out.print("Enter password: ");
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
	
	
	//returns a connection on successful login; null otherwise
	private static Connection createLoginConnection(Scanner cmdline, int numAttempts) throws Exception
	{
		
		for(int i = 0;i < numAttempts; ++i)
		{
			System.out.print("Enter username: ");
			String user = cmdline.nextLine();
			System.out.print("Enter password: ");
			String password = cmdline.nextLine();
						
			try
			{
				Connection connection = DriverManager.getConnection("jdbc:mysql:///"
						+DATABASE+"?autoReconnect=true"
						+"&useSSL=false",user,password);
						return connection;
			}
			catch (SQLException error)
			{
				printCause(error);
			}
		}
		
		return null;
	}
	

	public static final String pubgames = "pubgames";
	public static final String addgame = "addgame";
	public static final String addcust = "addcust";
	private static final String delcust = "delcust";
	private static final String  meta= "meta";
	private static final String query = "query";
	private static final String quit = "quit";
	private static final String logout = "logout";
	private static final String help = "help";

	public static void main(String[] args) throws Exception {
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		boolean running = true;
		String inp = "";
		Scanner cmdline = new Scanner(System.in);
	
		//login menu logic		
		boolean loginMenuRunning = true;
		boolean consoleMenuRunning = false;
		System.out.println("Available commands are login, quit.");
		System.out.print("Enter your command: ");
		inp = cmdline.nextLine();
		Connection loginConnection = null;
	
		while(running)
		{
			//loginToConsole
			while(loginMenuRunning)
			{
				if(inp.equalsIgnoreCase("login"))
				{
					int numAttempts = 3;
					loginConnection = createLoginConnection(cmdline,numAttempts);
					if(loginConnection == null)
					{
						System.out.println(numAttempts + " attempts given. Login failed.");
						loginMenuRunning = false;
						running = false;
					}
					else
					{
						System.out.println("Login successful.");
						loginMenuRunning = false;
						consoleMenuRunning = true;
					}
				}
				else if(inp.equalsIgnoreCase("quit") || inp.equalsIgnoreCase("exit"))
				{
					loginMenuRunning = false;
					running = false;
				}
				else
				{
					System.out.println("Available commands are login, quit.");
					System.out.print("Enter your command: ");
					inp = cmdline.nextLine();
				}
			}
			
			while(consoleMenuRunning)
			{
				//queryMain(loginConnection);
				System.out.print("Enter your command (type 'help' to display known commands): ");
				inp = cmdline.nextLine();
				switch(inp)
				{
				case pubgames:
					GamesConsole.printGamesByPublisher(loginConnection, cmdline);
					break;
				case addgame:
					GamesConsole.addGame(loginConnection);
					break;
				case addcust:
					GamesConsole.addCustomer(loginConnection);
					break;
				case delcust:
					GamesConsole.deleteCustomer(loginConnection);
					break;
				case meta:
					GamesConsole.displayMetadata(loginConnection);
					break;
				case query:
					GamesConsole.runQueryCommand(loginConnection);
					break;
				case help:
					GamesConsole.displayConsoleCommands();
					break;
				case logout:
					consoleMenuRunning = false;
					loginMenuRunning = true;
					break;
				default:
					break;
				}
				
			}
		}
		
//		while (running) {
//			System.out.println("Enter your command.");
//			inp = cmdline.nextLine();
//			if (inp.compareToIgnoreCase("login")==0) {
//				login();
//			} else if (inp.compareToIgnoreCase("quit")==0 || inp.compareToIgnoreCase("exit")==0) {
//				running = false;
//			} else {
//				System.out.println("Available commands are login, quit.");
//			}
//		}
		
		cmdline.close();
	}

}


