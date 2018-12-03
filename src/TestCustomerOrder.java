import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;

public class TestCustomerOrder {

	public static void main(String[] args) {
		 // the default framework is embedded
	    String protocol = "jdbc:derby:";
	    String dbName = "publication";    // where to put??
		String connStr = protocol + dbName+ ";create=true";

	    // tables tested by this program
		String dbTables[] = {
			"Product", "Customer", "InventoryRecord", "Book",		// relations
    	    	 	"BookRecord", 
    	    };

		// name of data file
		String fileName = "orderData.txt";

		Properties props = new Properties(); // connection properties
        // providing a user name and password is optional in the embedded
        // and derbyclient frameworks
        props.put("user", "user1");
        props.put("password", "user1");

        // result set for queries
        ResultSet rs = null;
		try (
			// open data file
			BufferedReader br = new BufferedReader(new FileReader(new File(fileName)));
			
			// connect to database
			Connection  conn = DriverManager.getConnection(connStr, props);
			Statement stmt = conn.createStatement();
			
			// insert prepared statements
			PreparedStatement insertRow_Product = conn.prepareStatement(
					"insert into Product values(?, ?, ?)");
			PreparedStatement insertRow_Customer = conn.prepareStatement(
					"insert into Customer(name, address, city, state, country, postcode) values(?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			PreparedStatement insertRow_InventoryRecord = conn.prepareStatement(
					"insert into InventoryRecord values(?, ?, ?)");
			PreparedStatement insertRow_Book = conn.prepareStatement(
					"insert into Book(book_Date, shipDate, cID) values(?, ?, ?)",Statement.RETURN_GENERATED_KEYS);
			PreparedStatement insertRow_BookRecord = conn.prepareStatement(
					"insert into BookRecord(book_ID, SKU, numberOfUnits, price) values(?, ?, ?, ?)");
		) {
			// connect to the database using URL
            System.out.println("Connected to database " + dbName);
            
            // clear data from tables
            for (String tbl : dbTables) {
	            try {
	            		stmt.executeUpdate("delete from " + tbl);
	            		System.out.println("Truncated table " + tbl);
	            } catch (SQLException ex) {
	            		System.out.println("Did not truncate table " + tbl);
	            }
            }
            System.out.println();
            System.out.println("Start insert table values:");
            
            // set up inventory
        	String line;
        	
			while ((line = br.readLine()) != null) {
				// split input line into fields at tab delimiter
				String[] data = line.split(",");
				if (data.length != 14) continue;
			
				// get fields from input data
				String productName = data[0];
				String productDesc = data[1];
				String sku = data[2];
				
				// add product if does not exist
				try {
					insertRow_Product.setString(1, productName);
					insertRow_Product.setString(2, productDesc);
					insertRow_Product.setString(3, sku);
					insertRow_Product.execute();
				} catch (SQLException ex) {
					System.out.println(ex.getMessage());
					
					// already exists
				}
				
				// add customer if does not exist
				String name = data[5], address = data[6];
				String city = data[7], state = data[8], country = data[9];
				int zip = Integer.parseInt(data[10]);
				int id = 0;
				try {
					insertRow_Customer.setString(1, name);
					insertRow_Customer.setString(2, address);
					insertRow_Customer.setString(3, city);
					insertRow_Customer.setString(4, state);
					insertRow_Customer.setString(5, country);
					insertRow_Customer.setInt(6, zip);
					insertRow_Customer.execute();
			            
			        rs = insertRow_Customer.getGeneratedKeys();
		            if(rs.next()){
		                id = rs.getInt(1);
		            }
		            rs.close();
				} catch (SQLException ex) {
					// already exists
				}
				
				// add inventory 
				int numberInventory = Integer.parseInt(data[3]);
				double price = Double.parseDouble(data[4]);
				try {
					insertRow_InventoryRecord.setInt(1, numberInventory);
					insertRow_InventoryRecord.setString(2, sku);
					insertRow_InventoryRecord.setDouble(3, price);
					insertRow_InventoryRecord.executeUpdate();
				} catch (SQLException ex) {
					// print stacktrace 
					System.out.print(ex.getMessage());
				}
				
				// add Book if does not exist
				int bookid = 0;
				try {
					conn.setAutoCommit(false);
					java.util.Date now = new java.util.Date();
					insertRow_Book.setDate(1, new java.sql.Date(now.getTime()));
					insertRow_Book.setTimestamp(2, new java.sql.Timestamp(now.getTime()));
					insertRow_Book.setInt(3, id);
			        insertRow_Book.executeUpdate();
			        
			        // get auto book_id
			        rs = insertRow_Book.getGeneratedKeys();
		            if(rs.next()){
		            	bookid = rs.getInt(1);
		            }
		            rs.close();
				} catch (SQLException ex) {
					// already exists
					System.out.println(ex.getMessage());
				} 
				
				// get InventoryRecord from input data
				int units = Integer.parseInt(data[13]);
				
				
				// set up the bookRecord, if units not available, roll back
				try {
					insertRow_BookRecord.setInt(1, bookid);
			 		insertRow_BookRecord.setString(2, sku);
					insertRow_BookRecord.setInt(3, units);
					insertRow_BookRecord.setDouble(4, price);
					insertRow_BookRecord.execute();
					
					// first update the number in inventory record
					// if product is out of stock, roll back (cancel the order)
					PreparedStatement update_inR = conn.prepareStatement(
							"UPDATE InventoryRecord SET number = ? WHERE ProductSKU = ?");
					update_inR.setInt(1, numberInventory - units);
					update_inR.setString(2, sku);
					update_inR.executeUpdate();
					
				} catch (SQLException ex) {
					// print stacktrace 
					System.out.printf("\nSorry, we do not have %s stock right now, but will be soon!\n", productName);
					
					System.out.println("Rolling back insertions.");
					conn.rollback();
					
					System.out.println("Committing transaction.\n");
					conn.commit();
				} 
				// restore auto-commit
				conn.setAutoCommit(true);
			}
			
			// print number of rows in tables
			for (String tbl : dbTables) {
				rs = stmt.executeQuery("select count(*) from " + tbl);
				if (rs.next()) {
					int count = rs.getInt(1);
					System.out.printf("Table %s : count: %d\n", tbl, count);
				}
			}
			rs.close();
			
			// print tables 
			System.out.println("\nLet's print tables!");
			PrintTable.printBook(conn);
			System.out.println();
			PrintTable.printCustomer(conn);
			System.out.println();
			PrintTable.printInventoryRecord(conn);
			System.out.println();
			PrintTable.printProduct(conn);
			System.out.println();
			PrintTable.printBookRecord(conn);
			
			// testing stored function SKU
			System.out.println("\nTesting stored function sku");
			
			// check null SKU
			try {
				rs = stmt.executeQuery("values isSKU(null)");
				rs.next();
				boolean isSKU = rs.getBoolean(1);
				System.out.printf("value of issn string 'null' is %b\n", isSKU);
			} catch (SQLException ex) {
				System.out.printf("value of issn string 'null': %s\n", ex.getMessage());
			}
			
			// check valid SKU
			try {
				rs = stmt.executeQuery("values isSKU('AB-000001-A0')");
				rs.next();
				boolean isSKU = rs.getBoolean(1);
				System.out.printf("value of issn string 'AB-000001-A0' is %b\n", isSKU);
			} catch (SQLException ex) {
				System.out.printf("value of issn string 'AB-000001-A0': %s\n", ex.getMessage());
			}
			
			// check invalid SKU
			try {
				rs = stmt.executeQuery("values isSKU('A0-000002-A8')");
				rs.next();
				boolean isSKU = rs.getBoolean(1);
				System.out.printf("value of issn string 'A0-000002-A8' is %b\n", isSKU);
			} catch (SQLException ex) {
				System.out.printf("value of issn string 'A0-000002-A8': %s\n", ex.getMessage());
			}
			
			// check invalid SKU
			try {
				rs = stmt.executeQuery("values isSKU('ZB-001290-./')");
				rs.next();
				boolean isSKU = rs.getBoolean(1);
				System.out.printf("value of issn string 'ZB-001290-./' is %b\n", isSKU);
			} catch (SQLException ex) {
				System.out.printf("value of issn string 'ZB-001290-./': %s\n", ex.getMessage());
			}
			
			// delete Cup from the Product
			System.out.println("\nDeleting cup from Product");
			stmt.executeUpdate("delete from Product where SKU = 'AB-000002-0D'");
			PrintTable.printProduct(conn);
			PrintTable.printInventoryRecord(conn);
			PrintTable.printBookRecord(conn);
			
			// delete customer from the Customer
			System.out.println("\nDeleting customer = 8");
			stmt.executeUpdate("delete from Customer where customerID = 8");
			PrintTable.printCustomer(conn);
			PrintTable.printBook(conn);
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }

}
