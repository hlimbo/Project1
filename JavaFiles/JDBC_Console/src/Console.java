import java.sql.*;
import java.util.Scanner;


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
	
	private static void printMoviesGivenFeaturedStar(Connection connection) throws Exception
	{
		Scanner scan = new Scanner(System.in);
		
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
	
	public static void main(String[] args) throws Exception
	{	
		System.out.println("Welcome to JDBC Console!");
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		
		Connection connection = DriverManager.getConnection("jdbc:mysql:///moviedb?autoReconnect=true&useSSL=false","root", "password");
		
		Scanner scan = new Scanner(System.in);
		
		boolean done = false;
		
		
		while(!done)
		{
			displayMenuOptions();
			System.out.print("Enter numeric choice (1-8): ");
			Integer choice = scan.nextInt();
			switch(choice)
			{
			case 1:
				printMoviesGivenFeaturedStar(connection);
				break;
			case 2:
				break;
			case 3:
				break;
			case 4:
				break;
			case 5:
				break;
			case 6:
				break;
			case 7:
				done = true;
				break;
			case 8:
				done = true;
				break;
			default:
				System.out.println("Invalid choice... program will now exit");
				done = true;
			}
		}
		
	}
 
}