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
    public static boolean showCategories(Connection con) throws SQLException {
        if (selectedClassID == null) {
            System.out.println("No class selected");
            return false;
        }
        // SQL Query to get all categories
        String showCategoriesSQL = "SELECT Name, Weight FROM Category WHERE Category.classID = ?";
        PreparedStatement pstmt = con.prepareStatement(showCategoriesSQL);
        pstmt.setInt(1, selectedClassID);

        ResultSet resultSet = pstmt.executeQuery();

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

        return true;
    }

    public static boolean addCategories(String[] inputParams, Connection con) throws SQLException {
        if (selectedClassID == null) {
            System.out.println("No class selected");
            return false;
        }
        if (inputParams.length != 2) {
            System.out.println("Invalid parameters!");
            System.out.println("Usage: add-category <Name> <weight>");
            return false;
        }

        // Parse input
        String name = inputParams[0];
        float weight = Float.parseFloat(inputParams[1]);

        // Example
        // insert into Category (Name, classID, Weight) values ('Exam', 1, 0.5);
        String addCategorySQL = "INSERT INTO Category (Name, classID, Weight) " +
                "VALUES (?, ?, ?)";

        PreparedStatement pstmt = con.prepareStatement(addCategorySQL);
        pstmt.setString(1, name);
        pstmt.setInt(2, selectedClassID);
        pstmt.setFloat(3, weight);
        int rows = pstmt.executeUpdate();
        System.out.println("Added class: " + rows + " row(s) affected");
        con.commit();
        return true;
    }

    public static boolean showAssignmentsByCategories(Connection con) throws SQLException {
        if (selectedClassID == null) {
            System.out.println("No class selected");
            return false;
        }

        // SQL Query to get all assignments by category for a class
        String sql = "SELECT Assignment.Name, Category.Name, Assignment.PointValue FROM Assignment LEFT JOIN Category ON Assignment.categoryID = Category.CategoryID WHERE Assignment.classID = ?;";
        PreparedStatement pstmt = con.prepareStatement(sql);
        pstmt.setInt(1, selectedClassID);

        ResultSet resultSet = pstmt.executeQuery();

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

        return true;
    }

    public static int getCategoryID(String category, int classId, Connection con) throws SQLException {
        String sql = "SELECT Category.CategoryID FROM Category WHERE Category.classID = ? AND Category.Name = ?;";
        PreparedStatement pstmt = con.prepareStatement(sql);
        pstmt.setInt(1, classId);
        pstmt.setString(2, category);

        ResultSet resultSet = pstmt.executeQuery();
        if (resultSet.next()) { // Move to first row
            String retId = resultSet.getString(1); // 1, not 0
            return Integer.parseInt(retId);
        } else {
            throw new SQLException("No category found for name: " + category + " and classID: " + classId);
        }
    }

    /**
     * Adds a new assignment to the database for the currently selected class.
     * This method validates the input parameters, retrieves the corresponding
     * category ID based on the provided category name, and inserts a new record
     * into the Assignment table with the specified details.
     *
     * @param parameters An array of Strings representing assignment details in the
     *                   order:
     *                   [assignment name, category name, description, points].
     * @param con        The active database connection to use for executing the
     *                   insert operation.
     * @return true if the assignment was successfully added; false
     *         if no class is selected or the input parameters are invalid.
     * @throws SQLException If a database access error occurs during the insertion.
     */
    public static boolean addAssignment(String[] parameters, Connection con) throws SQLException {

        if (selectedClassID == null) {
            System.out.println("No class selected");
            return false;
        }

        // Validate input parameters
        System.out.println(parameters.length);
        if (parameters.length != 4) {
            System.out.println("Invalid parameters!");
            System.out.println("Usage: add- <assignment name> <Category> <Description> <points>");
            return false;
        }

        // Parse input
        // add- assignment name Category Description points
        String assignmentName = parameters[0];
        String category = parameters[1]; // category name
        String description = parameters[2];
        int points = Integer.parseInt(parameters[3]);

        // Getting category id from input category name
        System.out.println("Before");
        int categoryID = getCategoryID(category, selectedClassID, con);
        System.out.println("Category: " + categoryID);

        // Example

        // insert into Assignment (Name, PointValue, categoryID, classID, Description)
        // values ('Homework-1', 10, 2, 1, 'Suspendisse potenti. Cras in purus eu magna
        // vulputate luctus. Cum sociis natoque penatibus et magnis dis parturient
        // montes, nascetur ridiculus mus.');
        String addAssignmentSQL = "INSERT INTO Assignment (Name, PointValue, categoryID, classID, Description) " +
                "VALUES (?, ?, ?, ? ,?)";

        PreparedStatement pstmt = con.prepareStatement(addAssignmentSQL);

        pstmt.setString(1, assignmentName);
        pstmt.setInt(2, points);
        pstmt.setInt(3, categoryID);
        pstmt.setInt(4, selectedClassID);
        pstmt.setString(5, description);

        // Execute command
        pstmt.executeUpdate();
        con.commit();

        return true;
    }

    public static boolean addStudent(String[] parameters, Connection con) {
        // Validate class has been selected
        if (selectedClassID == null) {
            System.out.println("No class selected");
            return false;
        }
        // Validate parameters
        System.out.println(parameters.length);
        if (parameters.length != 4) {
            System.out.println("Invalid parameters!");
            System.out.println("Usage: add-student <username> <studentid> <Last> <First>");
            return false;
        }

        // parse inputs
        String username = parameters[0];
        int studentid = Integer.parseInt(parameters[1]);
        String last = parameters[2];
        String first = parameters[3];

        return false;
    }

    public static void main(String[] args)
            throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        // JDBC Variables
        Connection con = null;
        // Hardcoded Connection info
        int nRemotePort = 50939; // Remote port number of the database
        String strDbPassword = "db41825"; // Database login password
        String dbName = "gradeManager"; // Database name
        Statement stmt = null;
        try {
            // **LOAD the Database DRIVER and obtain a CONNECTION
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

            // ====Transaction block starts====
            con.setAutoCommit(false);
            stmt = con.createStatement(); // responsible for executing queries

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
                switch (cmd) {
                    case "exit":
                        isRunning = false;
                        break;
                    case "new-class":
                        newClass(inputParameters, con);

                        break;

                    case "list-class":
                        listClasses(con);
                        break;

                    case "select-class":
                        selectClass(inputParameters, con);
                        break;

                    case "show-class":
                        showClass(con);
                        break;

                    case "show-categories":
                        showCategories(con);

                        break;
                    case "add-category":
                        addCategories(inputParameters, con);
                        break;

                    case "show-assignment":
                        showAssignmentsByCategories(con);
                        break;
                    case "add-":
                        addAssignment(inputParameters, con);
                        break;
                    case "add-student":
                        addStudent(inputParameters, con);
                        break;
                    default:
                        break;
                }
                // After Query execution commit changes
                con.commit();

            }
            // ====Transaction block starts====
            con.setAutoCommit(false);
            // Close connection
            con.close();
            scanner.close();
            System.out.println();

            System.out.println("Exiting Grademanager thank you! ");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            con.rollback(); // In case of any exception, we roll back to the database state we had before
                            // starting this transaction
        }

    }
}
