import java.sql.*;
import java.util.Scanner;
import java.util.ArrayList;

public class Console {

	private static void displayMenuOptions()
	{
		System.out.println("1. Print out the movies featuring a given star (ID or first and last name required)");
		System.out.println("2. Insert a new star into the database");
		System.out.println("3. Insert a new customer into the database");
		System.out.println("4. Delete a customer from the database");
		System.out.println("5. Provide the metadata of the database");
		System.out.println("6. Enter a SQL Command");
		System.out.println("7. exit the menu (will be implemented later)");
		System.out.println("8. Exit the program");
	}
	
	//todo input validation
	private static void printMoviesGivenFeaturedStar(Connection connection,Scanner scan) throws Exception
	{
		System.out.print("Enter the first and last name or id of the star to display the movies they are featured in: ");
		String response = scan.nextLine();
		Integer star_id = -1;
		String first_name = "";
		String last_name = "";
		
		String id_query = "";
		
		try
		{
			star_id = Integer.parseInt(response, 10);
		}
		catch(NumberFormatException e)
		{
			System.out.println("Invalid id number... try to parse as first name and last name instead");
			String[] arr = response.split(" ");
			first_name = arr[0];
			last_name = arr[1];		
			
			id_query = "SELECT id FROM stars WHERE first_name=\"" + first_name + "\" and last_name=\"" + last_name + "\"";
			System.out.println(id_query);
			Statement select = connection.createStatement();
			ResultSet idResult = select.executeQuery(id_query);
			idResult.next();
			star_id = idResult.getInt(1);
			System.out.println(response + " id: " + star_id);
		}
			
		String movieQuery = "SELECT * FROM movies WHERE id in (SELECT movie_id FROM stars_in_movies where star_id=(SELECT id FROM stars WHERE id=" + star_id + "))";
		
		Statement select = connection.createStatement();
		ResultSet result = select.executeQuery(movieQuery);
		
		ResultSetMetaData metadata = result.getMetaData();
		for(int i = 1;i <= metadata.getColumnCount();++i)
			System.out.print(metadata.getColumnLabel(i) + "|\t");
		System.out.println();
		
		while(result.next())
		{
			System.out.print(result.getInt(1) + " \t");
			System.out.print(result.getString(2) + " \t");
			System.out.print(result.getInt(3) + " \t");
			System.out.print(result.getString(4) + " \t");
			System.out.print(result.getString(5) + " \t");
			System.out.println(result.getString(6) + " \t");
		}
	}
	
	private static void insertNewStar(Connection connection,Scanner scan) throws Exception
	{
		
		System.out.print("Enter new star name to insert into database: ");
		String response = scan.nextLine();
		String[] name = response.split(" ");
		String first_name = name[0];
		String last_name = name[1];
		String insertString = "INSERT INTO stars (first_name, last_name) VALUES(" + "\"" + first_name + "\"," +" \"" + last_name + "\""  + ")";
		Statement update = connection.createStatement();
		int retID = update.executeUpdate(insertString);
		System.out.println("retID = " + retID);
	}
	
	private static void insertNewCustomer(Connection connection, Scanner scan) throws Exception
	{
		System.out.print("Enter new customer full name: ");
		String response = scan.nextLine();
		
		String[] name = response.split(" ");
		String first_name = null;
		String last_name = null;
		
		if(name.length == 2)
		{
			first_name = name[0];
			last_name = name[1];
		}
		else if(name.length == 1)
		{
			last_name = name[0];
			first_name = "";
		}
		
		///Find creditcard holder's id given creditcard holder's first name and last name.
		//Select id from creditcards where first_name="first" and last_name="last
		String ccQuery = "SELECT id FROM creditcards WHERE first_name = ? and last_name= ?";
		PreparedStatement ccStatement = connection.prepareStatement(ccQuery);
		ccStatement.setString(1, first_name);
		ccStatement.setString(2, last_name);
		ResultSet resultIDSet = ccStatement.executeQuery();
		
		//if no credit card ids are associated with the first and last name provided.
		if(!resultIDSet.next())
		{
			System.out.println("Cannot add " + first_name + " " + last_name + " into the database ");
			return;
		}
		
		//insert the customer into the database
		String insertSQL = "INSERT INTO customers (first_name, last_name, address, email, password) VALUES(?, ?, ?, ?, ?)";	
		PreparedStatement insertStatement = connection.prepareStatement(insertSQL);
		insertStatement.setString(1, first_name);
		insertStatement.setString(2, last_name);
		//todo: have user probably insert address, email, and password.
		insertStatement.setString(3, "123 fake street");
		insertStatement.setString(4, "fake@email.net");
		insertStatement.setString(5, "patheticpassword");		
		int retID = insertStatement.executeUpdate();
		
		System.out.println("Successfully inserted: " + first_name + " " + last_name + " into the customers table");
	}
	
	private static void deleteCustomer(Connection connection, Scanner scan) throws Exception
	{
		
	}
	
	private static void displayMetadata(Connection connection, Scanner scan) throws Exception
	{				
		DatabaseMetaData metadataDB = connection.getMetaData();
		ResultSet tables = metadataDB.getTables(connection.getCatalog(), null, "%", null);
		
		while(tables.next())
		{
			String tableName = tables.getString("TABLE_NAME");
			System.out.println(tableName);
			ResultSet tableColumns = metadataDB.getColumns(connection.getCatalog(),null,tableName, "%");
			
			while(tableColumns.next())
			{
				String columnName = tableColumns.getString("COLUMN_NAME");
				String columnType = tableColumns.getString("TYPE_NAME");
				System.out.print(columnName + ":" +  columnType + " |");
			}
			System.out.println();
		}
		
		
		
	}
	
	private static void enterSQLCommand(Connection connection, Scanner scan) throws Exception
	{
		
	}
	
	//needs to logout user from the database..
	private static boolean exitMenu(Connection connection, Scanner scan) throws Exception
	{
		return true;
	}
	
	private static boolean exitProgram(Connection connection, Scanner scan) throws Exception
	{
		return true;
	}
	
	private static int parseChoice(Scanner scan)
	{
		while(scan.hasNext())
		{
			if(scan.hasNextInt())
				return scan.nextInt();
			else
				scan.next();
		}
		
		return -1;
	}
	
	public static void main(String[] args) throws Exception
	{	
		System.out.println("Welcome to JDBC Console!");
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		
		Connection connection = DriverManager.getConnection("jdbc:mysql:///moviedb?autoReconnect=true&useSSL=false","root", "password");
		
		
		//todo scanning for input breaks after entering string as input.
		Scanner scan = new Scanner(System.in);
		
		boolean done = false;
		
		while(!done)
		{
			displayMenuOptions();
			System.out.print("Enter numeric choice (1-8): ");
			Integer choice = parseChoice(scan);
			scan.nextLine();
			switch(choice)
			{
			case 1:
				printMoviesGivenFeaturedStar(connection,scan);
				break;
			case 2:
				insertNewStar(connection, scan);
				break;
			case 3:
				insertNewCustomer(connection,scan);
				break;
			case 4:
				deleteCustomer(connection,scan);
				break;
			case 5:
				displayMetadata(connection,scan);
				break;
			case 6:
				enterSQLCommand(connection,scan);
				break;
			case 7:
				done = exitMenu(connection,scan);
				break;
			case 8:
				done = exitProgram(connection,scan);
				break;
			default:
				System.out.println("Invalid choice... program will now exit");
				done = true;
			}
		}
		
		scan.close();
	}
 
}
