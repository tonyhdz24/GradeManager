import java.io.*;
import java.sql.*;
import java.util.*;

public class GradeManager {
    private static Integer selectedClassID = null;

    public void showMenu() {
        System.out.println("Exit: Close GradeManager");
    }

    // public static ResultSet newClass(String[] inputParameters, Connection con)
    // throws SQLException {
    // System.out.println("Adding a new class");
    // /* TO INSERT INTO TABLES */
    // String courseNumber = inputParameters[0];
    // String term = inputParameters[1];
    // String sectionNumber = inputParameters[2];
    // String className = inputParameters[3];

    // String insert = "INSERT INTO gradeManager.Class (CourseNumber, ClassName,
    // Term, SectionNumber, Description) " +
    // "VALUES ('" + courseNumber + "', '" + className + "', '" + term + "', '" +
    // sectionNumber
    // + "', 'No description')";

    // Statement stmt = con.createStatement();
    // int res = stmt.executeUpdate(insert);

    // con.commit(); // transaction block ends

    // System.out.println("Transaction done!");

    // return stmt.executeQuery("select * from `" + "gradeManager" +
    // "`.`Student`;");
    // }

    public static void newClass(String[] params, Connection con) throws SQLException {
        if (params.length < 4) {
            System.out.println("Usage: new-class <courseNumber> <term> <sectionNumber> \"<className>\"");
            return;
        }
        String courseNumber = params[0];
        String term = params[1];
        int sectionNumber = Integer.parseInt(params[2]);
        String className = params[3].replace("\"", ""); // Remove quotes

        String sql = "INSERT INTO Class (CourseNumber, ClassName, Term, SectionNumber, Description) " +
                "VALUES (?, ?, ?, ?, 'No description')";
        PreparedStatement pstmt = con.prepareStatement(sql);
        pstmt.setString(1, courseNumber);
        pstmt.setString(2, className);
        pstmt.setString(3, term);
        pstmt.setInt(4, sectionNumber);
        int rows = pstmt.executeUpdate();
        System.out.println("Added class: " + rows + " row(s) affected");
        con.commit();
    }

    public static void listClasses(Connection con) throws SQLException {
        String sql = "SELECT c.*, COUNT(e.StudentID) as StudentCount " +
                "FROM Class c LEFT JOIN Enrolled e ON c.ClassID = e.ClassID " +
                "GROUP BY c.ClassID";
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            System.out.printf("%s %s %d (%s) - %d students%n",
                    rs.getString("CourseNumber"), rs.getString("Term"),
                    rs.getInt("SectionNumber"), rs.getString("ClassName"),
                    rs.getInt("StudentCount"));
        }
        con.commit();
    }

    public static void selectClass(String[] params, Connection con) throws SQLException {
        if (params.length < 1) {
            System.out.println("Usage: select-class <courseNumber> [<term> <sectionNumber>]");
            return;
        }
        String sql;
        PreparedStatement pstmt;
        if (params.length == 1) {
            sql = "SELECT ClassID FROM Class WHERE CourseNumber = ? " +
                    "ORDER BY Term DESC LIMIT 1";
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, params[0]);
        } else if (params.length == 3) {
            sql = "SELECT ClassID FROM Class WHERE CourseNumber = ? AND Term = ? AND SectionNumber = ?";
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, params[0]);
            pstmt.setString(2, params[1]);
            pstmt.setInt(3, Integer.parseInt(params[2]));
        } else {
            System.out.println("Invalid parameters");
            return;
        }
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            selectedClassID = rs.getInt("ClassID");
            System.out.println("Selected class ID: " + selectedClassID);
        } else {
            System.out.println("Class not found");
            selectedClassID = null;
        }
        con.commit();
    }

    public static void showClass(Connection con) throws SQLException {
        if (selectedClassID == null) {
            System.out.println("No class selected");
            return;
        }
        String sql = "SELECT * FROM Class WHERE ClassID = ?";
        PreparedStatement pstmt = con.prepareStatement(sql);
        pstmt.setInt(1, selectedClassID);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            System.out.printf("Course: %s, Term: %s, Section: %d, Name: %s, Desc: %s%n",
                    rs.getString("CourseNumber"), rs.getString("Term"),
                    rs.getInt("SectionNumber"), rs.getString("ClassName"),
                    rs.getString("Description"));
        }
        con.commit();
    }

    /**
     * List the categories with their weights for all classes
     * 
     * @param con - connection to the SQL database
     * @throws SQLException
     */
    public static ResultSet showCategories(Connection con) throws SQLException {
        // SQL Query to get all categories
        String showCategoriesSQL = "SELECT Name, Weight, ClassName FROM Category Left Join Class ON Category.classID = Class.ClassID";
        PreparedStatement pstmt = con.prepareStatement(showCategoriesSQL);
        return pstmt.executeQuery();
    }

    public static void main(String[] args)
            throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        // JDBC Variables
        Connection con = null;
        Statement stmt = null, stmt2 = null;
        ResultSet resultSet = null;

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
                            + "/" + dbName + "?verifyServerCertificate=false&useSSL=true&serverTimezone=UTC",
                    "msandbox",
                    strDbPassword);

            // Print welcome to terminal
            System.out.println("Database [test db] connection succeeded!");
            System.out.println("=======GradeManager=======");
            System.out.println("Welcome to GradeManager");

            // Read user input for command
            Scanner scanner = new Scanner(System.in); // Create a Scanner object
            boolean isRunning = true;

            GradeManager gm = new GradeManager();
            gm.showMenu();

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

                switch (cmd) {
                    case "exit":
                        isRunning = false;
                        break;
                    case "new-class":
                        newClass(inputParameters, con);

                        break;

                    case "list-class":
                        listClasses(con);
                        // resultSet=stmt.executeQuery("SELECT * FROM '" + dbName + "'.'Class':");
                        // ResultSetMetaData rsmd = resultSet.getMetaData();

                        // int columnsNumber = rsmd.getColumnCount();
                        // while (resultSet.next()) {
                        // for (int i = 1; i <= columnsNumber; i++) {
                        // if (i > 1) System.out.print(", ");
                        // String columnValue = resultSet.getString(i);
                        // System.out.print(columnValue + " " + rsmd.getColumnName(i));
                        // }
                        // System.out.println(" ");
                        // }

                        break;

                    case "select-class":
                        selectClass(inputParameters, con);
                        break;

                    case "show-class":
                        showClass(con);
                        break;

                    case "show-categories":
                        resultSet = showCategories(con);
                        break;

                    default:
                        break;
                }

                // Print out resultSet form executing queries
                ResultSetMetaData rsmd = resultSet.getMetaData();

                int columnsNumber = rsmd.getColumnCount();
                while (resultSet.next()) {
                    for (int i = 1; i <= columnsNumber; i++) {
                        if (i > 1)
                            System.out.print(", ");
                        String columnValue = resultSet.getString(i);
                        System.out.print(rsmd.getColumnName(i) + ": " + columnValue);
                    }
                    System.out.println(" ");
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
