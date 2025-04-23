import java.io.*;
import java.sql.*;
import java.util.*;

public class GradeManager {
    public void showMenu() {
        System.out.println("Exit: Close GradeManager");
    }

    public static ResultSet newClass(String[] inputParameters, Connection con) throws SQLException {
        System.out.println("Adding a new class");
        /* TO INSERT INTO TABLES */
        String courseNumber = inputParameters[0];
        String term = inputParameters[1];
        String sectionNumber = inputParameters[2];
        String className = inputParameters[3];

        String insert = "INSERT INTO gradeManager.Class (CourseNumber, ClassName, Term, SectionNumber, Description) " +
                "VALUES ('" + courseNumber + "', '" + className + "', '" + term + "', '" + sectionNumber
                + "', 'No description')";

        Statement stmt = con.createStatement();
        int res = stmt.executeUpdate(insert);

        con.commit(); // transaction block ends

        System.out.println("Transaction done!");

        return stmt.executeQuery("select * from `" + "gradeManager" + "`.`Class`;");
    }

    public static void main(String[] args)
            throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {

        Connection con = null;
        Statement stmt = null, stmt2 = null;
        try {
            // Hardcoded Connection info
            int nRemotePort = 50939; // Remote port number of the database
            String strDbPassword = "db41825"; // Database login password
            String dbName = "gradeManager"; // Database name

            /*
             * STEP 1 and 2
             * LOAD the Database DRIVER and obtain a CONNECTION
             * 
             */
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println(
                    "jdbc:mysql://localhost:" + nRemotePort + "/test?verifyServerCertificate=false&useSSL=true");
            con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:" + nRemotePort
                            + "/test?verifyServerCertificate=false&useSSL=true&serverTimezone=UTC",
                    "msandbox",
                    strDbPassword);
            // Print welcome to terminal
            System.out.println("Database [test db] connection succeeded!");
            System.out.println("=======GradeManager=======");
            System.out.println("Welcome to GradeManager");

            // Read user input for command
            Scanner scanner = new Scanner(System.in); // Create a Scanner object
            boolean isRunning = true;
            ResultSet resultSet;
            // REPL
            while (isRunning) {
                System.out.println("Enter command: ");
                // Get user input and tokenize it
                String inputString = scanner.nextLine().toLowerCase(); // Read user input
                String[] inputTokenized = inputString.split(" ");
                String cmd = inputTokenized[0];

                // Store the rest of the input parametes in an array
                String[] inputParameters = Arrays.copyOfRange(inputTokenized, 1, inputTokenized.length);

                // Different Commands
                con.setAutoCommit(false);// transaction block starts
                stmt = con.createStatement();
                // ResultSet resultSet;
                switch (cmd) {
                    case "exit":
                        isRunning = false;
                        break;
                    case "new-class":
                        resultSet = newClass(inputParameters, con);

                        break;
                    default:
                        break;
                }
            }
            scanner.close();
            System.out.println();

            /*
             * STEP 3
             * EXECUTE STATEMENTS (by using Transactions)
             * 
             */

            con.setAutoCommit(false);// transaction block starts

            stmt = con.createStatement();

            /* TO EXECUTE A QUERY */

            // ResultSet resultSet = stmt.executeQuery("select * from `" + dbName +
            // "`.`Student`;");

            /* TO INSERT INTO TABLES */

            // String insert = "Insert into `"+dbName+"`.`Class` (Name, Code) Values
            // ('Databases','CS410')";
            // stmt2 = con.createStatement();
            // int res = stmt2.executeUpdate(insert);

            // String[] data = {"boise", "nampa"};
            // stmt2 = insertLocations(con,data);

            // con.commit(); //transaction block ends

            // System.out.println("Transaction done!");

            /*
             * STEP 4
             * Use result sets (tables) to navigate through the results
             * 
             */

            // ResultSetMetaData rsmd = resultSet.getMetaData();

            // int columnsNumber = rsmd.getColumnCount();
            // while (resultSet.next()) {
            // for (int i = 1; i <= columnsNumber; i++) {
            // if (i > 1)
            // System.out.print(", ");
            // String columnValue = resultSet.getString(i);
            // System.out.print(columnValue + " " + rsmd.getColumnName(i));
            // }
            // System.out.println(" ");
            // }

            System.out.println("Number of rows affected by the insert statement: ");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            con.rollback(); // In case of any exception, we roll back to the database state we had before
                            // starting this transaction
        } finally {

            /*
             * STEP 5
             * CLOSE CONNECTION AND SSH SESSION
             * 
             */

            if (stmt != null)
                stmt.close();

            if (stmt2 != null)
                stmt2.close();

            con.setAutoCommit(true); // restore dafault mode
            con.close();
        }

    }
}
