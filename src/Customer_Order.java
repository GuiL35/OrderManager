
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Properties;

/**
 * This project gives the relationship among products, customer, orderRecord
 * @author guilan && Victoria
 *
 */
public class Customer_Order {


	
	public static void main(String[] args) {
	    // the default framework is embedded
	    String protocol = "jdbc:derby:";
	    String dbName = "publication";
		String connStr = protocol + dbName+ ";create=true";

	    // tables created by this program
		String dbTables[] = {
			"Product", "InventoryRecord", "Customer", "Book", 
			"BookRecord"		// relation 
	    };
		
		// triggers created by this program
		String dbTriggers[] = {
			"DeleteBookRecord"
		};
		
		// functions created by this program
		String dbFunctions[] = {
			"isSKU"
		};

		
		Properties props = new Properties(); // connection properties
        // providing a user name and password is optional in the embedded
        // and derbyclient frameworks
        props.put("user", "user1");
        props.put("password", "user1");

		try (
	        // connect to the database using URL
			Connection conn = DriverManager.getConnection(connStr, props);
				
	        // statement is channel for sending commands thru connection 
	        Statement stmt = conn.createStatement();
		){
	        System.out.println("Connected to and created database " + dbName);

            // drop the database tables and recreate them below
            for (String tbl : dbTables) {
	            try {
	            		stmt.executeUpdate("drop table " + tbl);
	            		System.out.println("Dropped table " + tbl);
	            } catch (SQLException ex) {
	            		System.out.println("Did not drop table " + tbl);
	            }
            }
            
            // drop the database triggers and recreate them below
            for (String tgr : dbTriggers) {
	            try {
	            		stmt.executeUpdate("drop trigger " + tgr);
	            		System.out.println("Dropped trigger " + tgr);
	            } catch (SQLException ex) {
	            		System.out.println("Did not drop trigger " + tgr);
	            }
            }
            
            // drop the database procedures and recreate them below
            for (String proc : dbFunctions) {
	            try {
	            		stmt.executeUpdate("drop function " + proc);
	            		System.out.println("Dropped function " + proc);
	            } catch (SQLException ex) {
	            		System.out.println("Did not drop function " + proc);
	            }
            }
            
            // create item available procedure
            String createFunction_isSKU = 
            		"CREATE function isSKU(" + 
            		"	 sku varchar(64)" +
            		") returns boolean" + 
            		" PARAMETER STYLE JAVA" + 
            		" LANGUAGE JAVA" + 
            		" DETERMINISTIC" + 
            		" NO SQL" +
            		" EXTERNAL NAME " + 
            		"	'CheckSKU.isSKU'";
            stmt.executeUpdate(createFunction_isSKU);
            System.out.println("Created Function isSKU()");
            
            // create the Product table
            String createTable_Product =
            		  "create table Product ("
            		+ "  Name varchar(64) not null,"
            		+ "  Product_Description varchar(64) not null,"
            		+ "  SKU varchar(64) not null,"   // ???
            		+ "  check(isSKU(SKU)),"
            		+ "  primary key (SKU)"
            		+ ")";
            stmt.executeUpdate(createTable_Product);
            System.out.println("Created entity table Product");
            
            // inventory record table 
            String createTable_InventoryRecord =
          		  "create table InventoryRecord ("
          		+ "  number int not null check (number >= 0),"
          		+ "  ProductSKU varchar(64) not null,"
          		+ "	 price decimal(10, 2) not null check(price > 0),"
          		+ "  primary key (ProductSKU),"
          		+ "  foreign key (ProductSKU) references Product (SKU) on delete cascade"    //   ??
          		+ ")";
          stmt.executeUpdate(createTable_InventoryRecord);
          System.out.println("Created entity table InventoryRecord");
          
          // customer table 
          String createTable_Customer =
          		  "create table Customer ("
        		+ "  customerID int not null generated always as identity (start with 1, increment by 1),"
          		+ "  name varchar(64) not null,"
          		+ "	 address varchar(64) not null,"
          		+ "  city varchar(16) not null,"    // ?? add enumerate 
          		+ "  state varchar(16) not null,"
          		+ "  country varchar(16) not null,"
          		+ "  postcode int not null,"
          		+ "  primary key (customerID)"
          		+ ")";
          stmt.executeUpdate(createTable_Customer);
          System.out.println("Created entity table Customer");
          
          // Book table 
          String createTable_Book =
          		  "create table Book ("
          		+ "	 book_ID int not null generated always as identity (start with 1, increment by 1),"
          		+ "  book_Date date not null,"
          		+ "  shipDate date not null,"   // check
          		+ "  cID int not null,"
          		+ "	 primary key (book_ID),"
          		+ "  foreign key(cID) references Customer (customerID) on delete cascade"
          		+ ")";
          stmt.executeUpdate(createTable_Book);
          System.out.println("Created entity table Book");
          
          // book record table
          String createTable_BookRecord =
          		  "create table BookRecord ("
          		+ "  book_ID int not null,"
          		+ "  SKU varchar(64) not null,"
          		+ "	 numberOfUnits int not null,"
          		+ "  price float not null,"
          		+ "  primary key (book_ID, SKU),"
          		+ "  foreign key(book_ID) references Book (book_ID) on delete cascade,"
          		+ "  foreign key(SKU) references Product (SKU) on delete cascade"
          		+ ")";
          stmt.executeUpdate(createTable_BookRecord);
          System.out.println("Created entity table BookRecord");
          
          // create trigger for deleting a bookrecord that also deletes
          // Order for the customer and product for InvertoryRecord
          String createTrigger_DeleteBookRecord =
          		  "create trigger DeleteBookRecord"
          		+ " after delete on BookRecord"
          		+ " for each statement"
          		+ "   delete from Book where book_ID not in"
          		+ "     (select book_ID from BookRecord)";
          stmt.executeUpdate(createTrigger_DeleteBookRecord);
          System.out.println("Created trigger for deleting BookRecord");
          
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }
}

