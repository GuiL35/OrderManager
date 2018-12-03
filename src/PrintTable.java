
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * This class contains functions for printing product,
 * customer, bookInvertory, and InvertoryRocord in the database.
 * 
 * @author Haiyan, Guilan
 *
 */
public class PrintTable {

	/**
	 * Print product table.
	 * @param conn the connection
	 * @return number of authors
	 * @throws SQLException if a database operation fails
	 */
	static int printProduct(Connection conn) throws SQLException {
		try (
			Statement stmt = conn.createStatement();
			// list authors and their ORCIDs
			ResultSet rs = stmt.executeQuery(
					"select SKU, Name from Product order by SKU");
		) {
			System.out.println("Product:");
			int count = 0;
			while (rs.next()) {
				String SKU = rs.getString(1);
				String name = rs.getString(2);
				System.out.printf("sku: %s, product name: %s\n", SKU, name);
				count++;
			}
			return count;
		}
	}
	
	/**
	 * Print customer table.
	 * @param conn the connection
	 * @return number of articles
	 * @throws SQLException if a database operation fails
	 */
	static int printCustomer(Connection conn) throws SQLException {
		try (
			Statement stmt = conn.createStatement();
			// list authors and their ORCIDs
			ResultSet rs = stmt.executeQuery(
				"select customerID, name from Customer order by customerID"); //primary key
		) {
			System.out.println("Customer:");
			int count = 0;
			while (rs.next()) {
				int customerId = rs.getInt(1);
				String customerName = rs.getString(2);
				System.out.printf(" %d, (%s)\n",customerId, customerName); //primary Key
				count++;
			}
			return count;
		}		
	}
	
	/**
	 * Print BookRecord table.
	 * @param conn the connection
	 * @return number of journals
	 * @throws SQLException if a database operation fails
	 */
	static int printBook(Connection conn) throws SQLException {
		try (
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(
				"select * from Book");
		) {
			System.out.println("Book:");
			int count = 0;
			while (rs.next()) {
				int bookId = rs.getInt(1);
				Date bookDate = rs.getDate(2);
				Date shipDate = rs.getDate(3);
				int customer_id = rs.getInt(4);
				 
				DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
				String bD = df.format(bookDate);
				String sD = df.format(shipDate);
				
				System.out.printf("book_id: %d, book date: %s, ship date: %s, customer_id: %d\n",
						bookId, bD, sD, customer_id);
				count++;
			}
			return count;
		}		
	}
	
	/**
	 * Print invertoryRecord table.
	 * @param conn the connection
	 * @return number of publishers
	 * @throws SQLException if a database operation fails
	 */
	static int printInventoryRecord(Connection conn) throws SQLException {
		try (
			Statement stmt = conn.createStatement();
			// list authors and their ORCIDs
			ResultSet rs = stmt.executeQuery(
					"select ProductSKU, number from InventoryRecord");
		) {
			System.out.println("InvertoryRecord:");
			int count = 0;
			while (rs.next()) {
				String ProductSKU = rs.getString(1);
				int number = rs.getInt(2);
				System.out.printf("  %s, %d\n", ProductSKU, number);
				count++;
			}
			return count;
		}
	}
	static int printBookRecord(Connection conn) throws SQLException {
		try (
			Statement stmt = conn.createStatement();
			// list authors and their ORCIDs
			ResultSet rs = stmt.executeQuery(
					"select book_ID, SKU from BookRecord");
		) {
			System.out.println("BookRecord:");
			int count = 0;
			while (rs.next()) {
				int book_ID = rs.getInt(1);
				String sku = rs.getString(2);
				System.out.printf("  %d, %s\n", book_ID, sku);
				count++;
			}
			return count;
		}
	}
}