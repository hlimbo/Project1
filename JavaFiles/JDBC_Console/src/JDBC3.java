//// JDBC Example - updating a DB via SQL template and value groups
//2	// Coded by Chen Li/Kirill Petrov Winter, 2005
//3	// Slightly revised for ICS185 Spring 2005, by Norman Jacobson
//4	
//5	import java.sql.*;                              // Enable SQL processing
//6	
//7	public class JDBC3
//8	{
//9	    public static void main(String[] arg) throws Exception
//10	    {
//11	                // Incorporate MySQL driver
//12	                 Class.forName("com.mysql.jdbc.Driver").newInstance();
//13	       
//14	                // Connect to the test database
//15	                Connection connection =
//16	                    DriverManager.getConnection("jdbc:mysql:///moviedb?autoReconnect=true&useSSL=false", "mytestuser", "mypassword");
//17	       
//18	                // prepare SQL statement template that's to be repeatedly excuted
//19	                String updateString = "update stars set first_name = ? where id = ?";
//20	                PreparedStatement updateStars = connection.prepareStatement(updateString);
//21	       
//22	                // values for first and second "?" wildcard in statement template
//23	                int [] ids = {755011, 755017};
//24	                String [] firstNames = {"New Arnold", "New Eddie"};
//25	       
//26	                // for each record in table, update to new values given
//27	                for(int i = 0; i < ids.length; i++)
//28	                    {
//29	                        updateStars.setString(1, firstNames[i]);
//30	                        updateStars.setInt(2, ids[i]);
//31	                        updateStars.executeUpdate();
//32	                    }
//33	    }
//34	}