package project1;

import java.util.Scanner;
import java.sql.*;
import java.time.Year;
import java.time.format.DateTimeParseException;

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
			String typeName = meta.getColumnTypeName(i);
			String resString = "";
			switch(typeName.toUpperCase()) {
			case "YEAR":
				resString=result.getString(i).substring(0,4);
				break;
			}
			if (resString == "") {
				switch(type) {
				case Types.INTEGER:
					resString+=result.getInt(i);
					break;
				default:
					resString=result.getString(i);
					break;
				}
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
		System.out.println("pubgames, genregames, pubgenregames, addpub, addgame, addcust, delcust, meta, query, logout\n");
	}
	
	//TODO: List games by publisher name and genre name
	//if genre name is not specified, use publisher name instead.
	//if publisher name is not specified, use game name instead.
	//otherwise, print out error message if neither are specified.
	private static void printGamesByPublisherAndOrGenre(Connection conn, Scanner cmdline)
	{
		System.out.println("Enter publisher name and/or genre name separate by semicolon (;) ");
		String input = cmdline.nextLine();
		
		String[] contents = input.split(";");
		
		if(input.trim().isEmpty() || contents.length == 0)
		{
			System.out.println("Publisher name and genre name cannot be empty");
			return;
		}
	
		if(contents.length > 3)
		{
			System.out.println("Too many arguments passed");
		}
		else
		{
			String name = "";
			String pubname = "";
			String genrename = "";
			
			
			//verify that given publisher name is in the publisher's table
			String searchPub = "SELECT id FROM publishers WHERE publisher = ?";
			
			//verify that given genre name is in the genre's table
			String searchGenre = "SELECT id FROM genres WHERE genre = ?";
			
			if(contents.length == 1)
			{
				name = contents[0];
				
				try
				{
					PreparedStatement pubStatement = conn.prepareStatement(searchPub);
					PreparedStatement genreStatement = conn.prepareStatement(searchGenre);
					pubStatement.setString(1, name);
					genreStatement.setString(1, name);
					
					ResultSet s1 = pubStatement.executeQuery();
					
					//if name given cannot be found in the publisher's table, execute the query for the genre table
					if(!s1.next())
					{
						ResultSet s2 = genreStatement.executeQuery();
						
						//if name given cannot be found in the genre's table
						if(!s2.next())
						{
							System.out.println("Name: " + name + " was not found in the publishers and genres tables");
							return;
						}
						else
						{
							genrename = name;
							genrename.trim();
							int genre_id = s2.getInt(1);
							String query = "SELECT * FROM games g, genres_of_games e WHERE g.id=e.game_id AND e.genre_id = ? ORDER BY g.name";
							PreparedStatement listGames = conn.prepareStatement(query);
							listGames.setInt(1,genre_id);
							ResultSet results = listGames.executeQuery();
							printResult(results);
							
						}
					}
					else
					{
						pubname = name;
						pubname.trim();
						int pub_id = s1.getInt(1);
						String query = "SELECT * FROM games g, publishers_of_games p WHERE g.id=p.game_id AND p.publisher_id = ? ORDER BY g.name";
						PreparedStatement listGames = conn.prepareStatement(query);
						listGames.setInt(1,pub_id);
						listGames.executeQuery();
						ResultSet results = listGames.executeQuery();
						printResult(results);
					}
					
				}
				catch(SQLException e)
				{
					printCause(e);
				}
			}
			else //if both publisher name and genre name are supplied, run query to filter games by publisher and genre name.
			{
				pubname = contents[0];
				genrename = contents[1];
				pubname.trim();
				genrename.trim();
				String query = "SELECT id, rank, name, year, globalsales FROM games, publishers_of_games p NATURAL JOIN genres_of_games g WHERE games.id = p.game_id AND p.publisher_id = ? AND g.genre_id = ?";
				
				try
				{
					
					PreparedStatement pubStatement = conn.prepareStatement(searchPub);
					PreparedStatement genreStatement = conn.prepareStatement(searchGenre);
					pubStatement.setString(1, pubname);
					genreStatement.setString(1, genrename);
					
					ResultSet pubResult = pubStatement.executeQuery();
					if(!pubResult.next())
					{
						System.out.println("Cannot find Publisher: " + pubname + " in the database.");
						return;
					}
					
					ResultSet genreResult = genreStatement.executeQuery();
					if(!genreResult.next())
					{
						System.out.println("Cannot find Genre: " + genrename + " in the database.");
						return;
					}
					
					int pub_id = pubResult.getInt(1);
					int genre_id = genreResult.getInt(1);
					
					PreparedStatement statement = conn.prepareStatement(query);
					statement.setInt(1, pub_id);
					statement.setInt(2, genre_id);
					ResultSet searchResults = statement.executeQuery();
					printResult(searchResults);
					
				}
				catch(SQLException e)
				{
					printCause(e);
				}
			}
			
		}
	}
	
	private static void printGamesByPublisher(Connection conn, Scanner cmdline)
	{
		System.out.println("Enter publisher name or publisher id to filter by: ");
		String input = cmdline.nextLine();
		String pubname = "";
		Integer pubid = -1;
		
		if (input.trim().compareTo("")==0) 
		{
			System.out.println("Publisher name or id cannot be empty.");
		} 
		else 
		{
			try{
				pubid = Integer.parseInt(input);
			}catch(NumberFormatException e){
				pubname = input;
			}
			
			//print out games featuring a given publisher
			//1. games 2. publishers 3. publishers_of_games
			String searchPub = "SELECT id FROM publishers WHERE publisher = ?";
			String searchGames = "SELECT game_id FROM publishers_of_games WHERE publisher_id = ?";
			String listGames = "Select * FROM games WHERE id IN (" + searchGames + ")" + " ORDER BY games.name";
			try {
				
				if(pubid == -1){
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
				}
				else{
					
					String valsearchpub = "SELECT id FROM publishers WHERE id=?";
					PreparedStatement validation = conn.prepareStatement(valsearchpub);
					validation.setInt(1, pubid);
					ResultSet result_val = validation.executeQuery();
					
					if(!result_val.next()){
						System.out.println("Id you entered is invalid, it is not in database");
					}else{
						PreparedStatement listGameStatement = conn.prepareStatement(listGames);
						listGameStatement.setInt(1, pubid);
						ResultSet result = listGameStatement.executeQuery();
						printResult(result);	
					}
				}
				
			} catch (SQLException error) {
				printCause(error);
			}
		}
	}
	
	//TODO: List games by Genre name or genre id
	private static void printGamesByGenre(Connection conn, Scanner cmdline)
	{
		System.out.println("Enter game genre or genre id to filter by: ");
		String input = cmdline.nextLine();
		String genrename = "";
		Integer genreid = -1;
		
		if(input.trim().compareTo("")==0)
		{
			System.out.println("Input Cannot Be Empty.");			
		}else{
			
			try{
				genreid = Integer.parseInt(input);
			}catch(NumberFormatException e){
				genrename = input;
			}
		}
		
			String searchGenre = "SELECT id FROM genres WHERE genre = ?";
			String searchGames = "SELECT game_id FROM genres_of_games WHERE genre_id = ?";
			String listGames = "Select * FROM games WHERE id IN (" + searchGames + ")" + " ORDER BY games.name";
			
			try {
				
				if(genreid == -1){
					PreparedStatement searchGenreStatement = conn.prepareStatement(searchGenre);
					PreparedStatement listGamesStatement = conn.prepareStatement(listGames);
					searchGenreStatement.setString(1, genrename);
					
					ResultSet result = searchGenreStatement.executeQuery();
					int game_id = -1;
					if(!result.next())
					{
						System.out.println("Cannot find genre: " + genrename + " in the database");					
					}
				
					else //printout games from a given genre existing in the database.
					{
						game_id = result.getInt(1);
						listGamesStatement.setInt(1,game_id);
						ResultSet gamesSet = listGamesStatement.executeQuery();
						printResult(gamesSet);	
					}
				}else{
					
					String valsearchpub = "SELECT id FROM publishers WHERE id=?";
					PreparedStatement validation = conn.prepareStatement(valsearchpub);
					validation.setInt(1, genreid);
					ResultSet result_val = validation.executeQuery();
					
					if(!result_val.next()){
						System.out.println("Id you entered is invalid, it is not in database");
					}else{
						PreparedStatement listGameStatement = conn.prepareStatement(listGames);
						listGameStatement.setInt(1, genreid);
						ResultSet result = listGameStatement.executeQuery();
						printResult(result);	
					}
				}
			} catch (SQLException error) {
				printCause(error);
			}
		}
	
	
	
	//TODO: insert game by providing name and year and/or rank.
	//insert game by providing name (mandatory) and year (optional)
	//if year is not provided, game name will be inserted into the database without the year specified.
	//if  rank is not provided, game name will be inserted into the database without the rank specified.
	private static void addGame(Connection conn, Scanner cmdline)
	{
		System.out.println("Enter name of game OR Enter year and/or rank(Seperate the Game/Rank/Year by semicolons no spaces): ");
		
		String input = cmdline.nextLine();
		String name = "";
		String year = "";
		Integer rank = -1;
		
		
		input = input.trim();
		if (input.compareTo("")==0) {
			System.out.println("game name can not be empty.");
			return;
		}
		
		String[] words = input.split(";");
		
		if(words.length == 1){
			name = words[0];
			
			try {
				String insert = "INSERT INTO games (name) VALUES (?)"; 
				PreparedStatement statement = conn.prepareStatement(insert);
				statement.setString(1, name);
				int result = statement.executeUpdate();
				printRowChange(result,"updated");
			} catch (SQLException error) {
				printCause(error);
			}
		
		}
		else if(words.length == 2){
			Integer rank1 = -1;
			Year year_object = Year.now();
			name = words[0];
			year = words[1];
			
			
			try{
				
				year_object = Year.parse(year);
				
			}catch(DateTimeParseException e){
				
				rank1 = Integer.parseInt(year);
			}
			
			if(rank1 == -1){
				try {
					String insert = "INSERT INTO games (name,year) VALUES (?,?)"; 
					PreparedStatement statement = conn.prepareStatement(insert);
					
					
					
					
					statement.setString(1,name);
					statement.setString(2, year_object.toString());
					
					int result = statement.executeUpdate();
					printRowChange(result,"updated");
				} catch (SQLException error) {
					printCause(error);
				}
			}else{
				try {
					String insert = "INSERT INTO games (name,rank,year) VALUES (?,?,?)"; 
					PreparedStatement statement = conn.prepareStatement(insert);
					
					statement.setString(1,name);
					statement.setInt(2, rank1);
					statement.setString(3, year_object.toString());
					
					
					int result = statement.executeUpdate();
					printRowChange(result,"updated");
				} catch (SQLException error) {
					printCause(error);
				}
			}
		}
		else if(words.length == 3) {
			
			name = words[0];
			year = words[1];
			rank = Integer.parseInt(words[2]);
			
			try {
				String insert = "INSERT INTO games (name,year,rank) VALUES (?,?,?)"; 
				PreparedStatement statement = conn.prepareStatement(insert);
				
				
				statement.setString(1,name);
				statement.setString(2, year);
				statement.setInt(3, rank);
				
				int result = statement.executeUpdate();
				printRowChange(result,"updated");
			} catch (SQLException error) {
				printCause(error);
			}
			
		}
			
	}
	
	private static void addPublisher(Connection conn,Scanner cmdline)
	{
		System.out.println("Enter name of publisher: ");
		String name = cmdline.nextLine();
		name = name.trim();
		if(name.isEmpty())
		{
			System.out.println("Publisher name cannot be left empty");
			return;
		}
		
		try
		{
			String insert = "INSERT INTO publishers (name) VALUES (?)";
			PreparedStatement statement = conn.prepareStatement(insert);
			int result = statement.executeUpdate();
			printRowChange(result, "updated");
		}
		catch(SQLException error)
		{
			printCause(error);
		}
	}
	
	private static void addCustomer(Connection conn, Scanner cmdline)
	{
		System.out.println("Enter the name of the Customer to be added.");
		String name = cmdline.nextLine();
		name=name.trim();
		String fname="";
		String lname="";
		if (name.compareTo("")==0) {
			System.out.println("A name can not be empty.");
			return;
		} else if (name.split(" ").length > 2) {
			System.out.println("Name is too long for system.\n"
					+"First and/(or only) last name expected.");
			return;
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
				System.out.println("Cannot add " + fname + " " + lname + " into the database. Does not have a record in the creditcards table. ");
				return;
			}
			
			String cc_id = resultIDSet.getString(1);
			//insert the customer into the database
			String insertSQL = "INSERT INTO customers (first_name, last_name, cc_id, address, email, password) VALUES(?, ?, ?, ?, ?, ?)";	
			PreparedStatement insertStatement = conn.prepareStatement(insertSQL);
			
			String address = "";
			String email = "";
			String password = "";
			
			//QUESTION: should this be randomly generated or allow the user to input these?
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
		
	}
	
	private static void deleteCustomer(Connection conn, Scanner cmdline)
	{
		System.out.println("Enter the id of the Customer to be deleted: ");
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
	}
	
	private static void displayMetadata(Connection conn)
	{
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
	}
	
	private static void runQueryCommand(Connection conn, Scanner cmdline)
	{
		System.out.println("Type in your SELECT/UPDATE/INSERT/DELETE SQL statement: ");
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
	private static final String genregames = "genregames";
	public static final String addgame = "addgame";
	public static final String addpub = "addpub";
	public static final String addcust = "addcust";
	private static final String delcust = "delcust";
	private static final String  meta= "meta";
	private static final String query = "query";
	private static final String quit = "quit";
	private static final String logout = "logout";
	private static final String help = "help";
	private static final String pubgenregames = "pubgenregames";

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
					
					//authenticateConnection
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
				System.out.print("Enter your command (type 'help' to display known commands): ");
				inp = cmdline.nextLine();
				inp = inp.trim();
				switch(inp)
				{
				case pubgames:
					GamesConsole.printGamesByPublisher(loginConnection, cmdline);
					break;
				case genregames:
					GamesConsole.printGamesByGenre(loginConnection, cmdline);
					break;
				case addpub:
					GamesConsole.addPublisher(loginConnection, cmdline);
					break;
				case addgame:
					GamesConsole.addGame(loginConnection, cmdline);
					break;
				case addcust:
					GamesConsole.addCustomer(loginConnection, cmdline);
					break;
				case delcust:
					GamesConsole.deleteCustomer(loginConnection, cmdline);
					break;
				case meta:
					GamesConsole.displayMetadata(loginConnection);
					break;
				case query:
					GamesConsole.runQueryCommand(loginConnection, cmdline);
					break;
				case help:
					GamesConsole.displayConsoleCommands();
					break;
				case logout:
					consoleMenuRunning = false;
					loginMenuRunning = true;
					break;
				case pubgenregames:
					GamesConsole.printGamesByPublisherAndOrGenre(loginConnection, cmdline);
				default:
					break;
				}
				
			}
		}
		
		if(loginConnection != null)
			loginConnection.close();
		
		cmdline.close();
	}

}


