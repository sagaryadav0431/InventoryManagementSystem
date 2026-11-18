import java.sql.*;
import java.util.Scanner;

public class InventoryManagement {
    private static final String URL = "jdbc:mysql://localhost:3306/retail_db";
    private static final String USER = "root";
    private static final String PASSWORD = "root123";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Scanner scanner = new Scanner(System.in)) {

            while (true) {
                System.out.println("\n--- Inventory Management System ---");
                System.out.println("1. (a) Insert Product Records");
                System.out.println("2. (b) Conditional Retrieval and Sorting");
                System.out.println("3. (c) Update Product Stock");
                System.out.println("4. (d) Search and Modify Product Details");
                System.out.println("5. (e) Aggregate Query and Conditional Deletion");
                System.out.println("6. Exit");
                System.out.print("Enter your choice: ");

                int choice = scanner.nextInt();
                switch (choice) {
                    case 1: insertProducts(conn, scanner); break;
                    case 2: retrieveProducts(conn, scanner); break;
                    case 3: updateStock(conn, scanner); break;
                    case 4: modifyPrice(conn, scanner); break;
                    case 5: aggregateAndDelete(conn, scanner); break;
                    case 6:
                        System.out.println("Exiting...");
                        return;
                    default:
                        System.out.println("Invalid choice. Try again.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void insertProducts(Connection conn, Scanner scanner) {
        String sql = "INSERT INTO Product (product_id, product_name, category, quantity, price) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            System.out.print("How many products to insert? ");
            int n = scanner.nextInt();
            for (int i = 0; i < n; i++) {
                System.out.print("Product ID: "); int id = scanner.nextInt();
                scanner.nextLine();
                System.out.print("Product Name: "); String name = scanner.nextLine();
                System.out.print("Category: "); String cat = scanner.nextLine();
                System.out.print("Quantity: "); int qty = scanner.nextInt();
                System.out.print("Price: "); double price = scanner.nextDouble();

                pstmt.setInt(1, id); pstmt.setString(2, name); pstmt.setString(3, cat);
                pstmt.setInt(4, qty); pstmt.setDouble(5, price);
                try {
                    pstmt.executeUpdate();
                    System.out.println("Product inserted successfully.");
                } catch (SQLException e) {
                    System.out.println("Error: Duplicate ID or invalid data handled.");
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private static void retrieveProducts(Connection conn, Scanner scanner) {
        scanner.nextLine();
        System.out.print("Enter category to search: ");
        String cat = scanner.nextLine();
        String sql = "SELECT * FROM Product WHERE category = ? AND quantity < 20 ORDER BY price DESC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cat);
            ResultSet rs = pstmt.executeQuery();
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("ID: %d | Name: %s | Qty: %d | Price: %.2f\n",
                        rs.getInt("product_id"), rs.getString("product_name"),
                        rs.getInt("quantity"), rs.getDouble("price"));
            }
            if (!found) System.out.println("No matching products found.");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private static void updateStock(Connection conn, Scanner scanner) {
        System.out.print("Enter Product ID: "); int id = scanner.nextInt();
        System.out.print("Enter quantity to add: "); int add = scanner.nextInt();
        String sql = "UPDATE Product SET quantity = quantity + ? WHERE product_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, add); pstmt.setInt(2, id);
            if (pstmt.executeUpdate() > 0) System.out.println("Stock updated.");
            else System.out.println("Product does not exist.");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private static void modifyPrice(Connection conn, Scanner scanner) {
        System.out.print("Enter Product ID: "); int id = scanner.nextInt();
        System.out.print("Enter new price: "); double price = scanner.nextDouble();
        if (price <= 0) { System.out.println("Price must be > 0."); return; }

        String checkSql = "SELECT * FROM Product WHERE product_id = ?";
        String updateSql = "UPDATE Product SET price = ? WHERE product_id = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql);
             PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
            checkStmt.setInt(1, id);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                updateStmt.setDouble(1, price); updateStmt.setInt(2, id);
                updateStmt.executeUpdate();
                System.out.println("Price updated successfully.");
            } else { System.out.println("Product does not exist."); }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private static void aggregateAndDelete(Connection conn, Scanner scanner) {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT SUM(quantity * price) AS total FROM Product")) {
            if (rs.next()) System.out.printf("Total Inventory Value: %.2f\n", rs.getDouble("total"));
        } catch (SQLException e) { e.printStackTrace(); }

        System.out.print("Enter Product ID to delete (must have 0 qty): "); int id = scanner.nextInt();
        String checkSql = "SELECT quantity FROM Product WHERE product_id = ?";
        String deleteSql = "DELETE FROM Product WHERE product_id = ? AND quantity = 0";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql);
             PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
            checkStmt.setInt(1, id);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                if (rs.getInt("quantity") == 0) {
                    deleteStmt.setInt(1, id); deleteStmt.executeUpdate();
                    System.out.println("Product deleted successfully.");
                } else { System.out.println("Cannot delete: quantity is not zero."); }
            } else { System.out.println("Product does not exist."); }
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
