// JDBC Example - printing a database's metadata
// Coded by Chen Li/Kirill Petrov Winter, 2005
// Slightly revised for ICS185 Spring 2005, by Norman Jacobson
	
	
//import java.sql.*;                              // Enable SQL processing
//
//public class JDBC1
//{
//	  public static void main(String[] arg) throws Exception
//      {
//13	
//14	               // Incorporate mySQL driver
//15	               Class.forName("com.mysql.jdbc.Driver").newInstance();
//16	
//17	                // Connect to the test database
//18	               Connection connection = DriverManager.getConnection("jdbc:mysql:///moviedb?autoReconnect=true&useSSL=false","mytestuser", "mypassword");
//19	
//20	               // Create an execute an SQL statement to select all of table"Stars" records
//21	               Statement select = connection.createStatement();
//22	               ResultSet result = select.executeQuery("Select * from stars");
//23	
//24	               // Get metatdata from stars; print # of attributes in table
//25	               System.out.println("The results of the query");
//26	               ResultSetMetaData metadata = result.getMetaData();
//27	               System.out.println("There are " + metadata.getColumnCount() + " columns");
//28	
//29	               // Print type of each attribute
//30	               for (int i = 1; i <= metadata.getColumnCount(); i++)
//31	                       System.out.println("Type of column "+ i + " is " + metadata.getColumnTypeName(i));
//32	
//33	               // print table's contents, field by field
//34	               while (result.next())
//35	               {
//36	                       System.out.println("Id = " + result.getInt(1));
//37	                       System.out.println("Name = " + result.getString(2) + result.getString(3));
//38	                       System.out.println("DOB = " + result.getString(4));
//39	                       System.out.println("photoURL = " + result.getString(5));
//40	                       System.out.println();
//41	               }
//42	       }
//43	}