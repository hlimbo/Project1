//	//JDBC Example - deleting a record
//2	// Coded by Checn Li/Kirill Petrov Winter, 2005
//3	// Slightly revised for ICS185 Spring 2005, by Norman Jacobson
//4	       
//5	
//6	import java.sql.*;              // Enable SQL processing
//7	
//8	public class JDBC2
//9	{
//10	    public static void main(String[] arg) throws Exception
//11	    {
//12	        // Incorporate mySQL driver
//13	        Class.forName("com.mysql.jdbc.Driver").newInstance();
//14	
//15	        // Connect to the test database
//16	        Connection connection = DriverManager.getConnection("jdbc:mysql:///moviedb?autoReconnect=true&useSSL=false","mytestuser", "mypassword");
//17	
//18	        // create update DB statement -- deleting second record of table; return status
//19	        Statement update = connection.createStatement();
//20	        int retID = update.executeUpdate("delete from stars where id = 755011");
//21	        System.out.println("retID = " + retID);
//22	    }
//23	}