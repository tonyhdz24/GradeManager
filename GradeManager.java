import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The GradeManager class provides functionality to manage classes, students,
 * assignments, and grades
 * in a grade management system. The class supports operations such as adding
 * new classes, listing
 * classes, managing students, assigning grades, and generating grade reports.
 * This class operates in
 * a command-line interface (CLI) mode, where the user can interact with the
 * program using various commands.
 * 
 * The program interacts with a MySQL database to store and retrieve data. It
 * includes commands for:
 * <ul>
 * <li>Class Management: Creating and selecting classes</li>
 * <li>Category and Assignment Management: Adding categories and
 * assignments</li>
 * <li>Student Management: Adding students, viewing students, and grading
 * assignments</li>
 * <li>Grade Reporting: Generating reports for individual students or the entire
 * class</li>
 * <li>Grade Calculation: Computing grades based on assignment categories</li>
 * </ul>
 * 
 * <p>
 * The GradeManager class is designed to be run from the command line, with the
 * user providing
 * commands for the system to execute. The commands are parsed, and
 * corresponding actions are performed
 * in the database to update or retrieve information.
 * </p>
 * 
 * <p>
 * All database interactions are executed within a transaction to ensure
 * consistency. If an error
 * occurs during any operation, the transaction is rolled back, and the user is
 * notified of the issue.
 * </p>
 * 
 * <p>
 * This class supports the following commands:
 * </p>
 * <ul>
 * <li><b>new-class</b>: Creates a new class</li>
 * <li><b>list-class</b>: Lists all available classes</li>
 * <li><b>select-class</b>: Selects a specific class</li>
 * <li><b>add-student</b>: Adds or enrolls a student</li>
 * <li><b>grade</b>: Assigns a grade for a student on a specific assignment</li>
 * <li><b>show-students</b>: Displays a list of students in the current
 * class</li>
 * <li><b>show-categories</b>: Displays the categories and weights for
 * assignments</li>
 * <li><b>student-grades</b>: Displays a student’s grades grouped by
 * category</li>
 * </ul>
 * 
 * @author Max Ma, Antonio Hernandez
 * @version 1.0
 */
public class GradeManager {
    private static Integer selectedClassID = null;

    /**
     * Displays the GradeManager menu to the user, listing all the available
     * commands and their descriptions.
     * This method provides an overview of the operations the user can perform in
     * the system.
     * 
     * <p>
     * The menu includes commands for managing classes, assignments, students,
     * grading, and viewing reports.
     * </p>
     * 
     * The available commands are:
     * <ul>
     * <li><b>1. Create a class:</b> Creates a new class with the specified
     * parameters.</li>
     * <li><b>2. List all classes:</b> Displays a list of all the classes in the
     * system.</li>
     * <li><b>3. Select a class:</b> Selects a class by specifying the class name,
     * term, and section.</li>
     * <li><b>4. Show current class:</b> Displays details of the current class.</li>
     * <li><b>5. Show categories:</b> Displays the available assignment
     * categories.</li>
     * <li><b>6. Add category:</b> Adds a new category with a specified name and
     * weight.</li>
     * <li><b>7. Show assignments:</b> Displays all the assignments in the current
     * class.</li>
     * <li><b>8. Add assignment:</b> Adds a new assignment with the specified
     * parameters.</li>
     * <li><b>9. Add student:</b> Adds a student to the system with the given
     * username, student ID, last name, and first name.</li>
     * <li><b>10. Show students:</b> Displays a list of all students in the current
     * class.</li>
     * <li><b>11. Grade student:</b> Assigns a grade to a student for a specified
     * assignment.</li>
     * <li><b>12. Show student grades:</b> Displays the grades for a specified
     * student.</li>
     * <li><b>13. Show gradebook:</b> Displays the complete gradebook for the
     * current class.</li>
     * <li><b>14. Exit:</b> Closes the GradeManager application.</li>
     * <li><b>15. Menu:</b> Displays the menu again.</li>
     * </ul>
     */
    public static void showMenu() {
        System.out.println("======= GradeManager Menu =======");
        System.out.println("1. Create a class: new-class <ClassName> <Term> <Section> <Subject>");
        System.out.println("2. List all classes: list-classes");
        System.out.println("3. Select a class: select-class <ClassName> <Term> <Section>");
        System.out.println("4. Show current class: show-class");
        System.out.println("5. Show categories: show-categories");
        System.out.println("6. Add category: add-category <Name> <Weight>");
        System.out.println("7. Show assignments: show-assignments");
        System.out.println("8. Add assignment: add-assignment <Name> <Category> <Description> <Points>");
        System.out.println("9. Add student: add-student <Username> <StudentId> <LastName> <FirstName>");
        System.out.println("10. Show students: show-students");
        System.out.println("11. Grade student: grade <AssignmentName> <Username> <Grade>");
        System.out.println("12. Show student grades: student-grades <Username>");
        System.out.println("13. Show gradebook: gradebook");
        System.out.println("14. Exit: Close GradeManager");
        System.out.println("15. Menu: Show Menu again");
        System.out.println("=================================");
    }

    /**
     * Creates a new class in the database with the given course details.
     * 
     * This method takes the course number, term, section number, and class name as
     * parameters,
     * validates that enough parameters are provided, and then inserts the new class
     * record into
     * the database. If successful, a confirmation message is printed with the
     * number of rows affected.
     * 
     * 
     * @param params An array of strings containing the course number, term, section
     *               number, and class name.
     * @param con    A connection object to the database.
     * @throws SQLException If there is an issue with the SQL query or database
     *                      connection.
     */
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

    /**
     * Lists all classes in the database along with the number of students enrolled
     * in each class.
     * 
     * 
     * This method retrieves all classes from the database, along with the count of
     * students
     * enrolled in each class. It uses a SQL `LEFT JOIN` query to join the `Class`
     * and `Enrolled`
     * tables and groups the results by class ID. The results are then printed to
     * the console in
     * a readable format.
     * 
     * 
     * @param con A connection object to the database.
     * @throws SQLException If there is an issue with the SQL query or database
     *                      connection.
     */
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

    /**
     * Selects a class based on the provided course number, and optionally term and
     * section number.
     * 
     * This method allows the user to select a class based on the course number. If
     * a term
     * and section number are provided, the method will select the class that
     * matches all three parameters.
     * If only the course number is provided, the method will return the most recent
     * class based on the term.
     * 
     * 
     * The selected class's ID is stored in the `selectedClassID` variable for
     * further use.
     * 
     * 
     * @param params An array of parameters where the first element is the course
     *               number,
     *               the second element is the optional term, and the third is the
     *               optional section number.
     * @param con    A connection object to the database.
     * @throws SQLException If there is an issue with the SQL query or database
     *                      connection.
     */
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

    /**
     * Displays the details of the currently selected class.
     * 
     * 
     * This method retrieves and displays the course number, term, section number,
     * class name,
     * and description of the class that is currently selected (based on the
     * `selectedClassID` variable).
     * If no class is selected, a message is printed indicating this.
     * 
     * 
     * @param con A connection object to the database.
     * @throws SQLException If there is an issue with the SQL query or database
     *                      connection.
     */
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

    /**
     * Adds a new category to the currently selected class.
     * 
     * <p>
     * This method adds a category (such as "Exam", "Homework", etc.) to the
     * selected class with a specified
     * weight. The weight represents the relative importance of the category in the
     * grading system. If no class
     * is selected or the parameters are invalid, the method will print an error
     * message and return false.
     * </p>
     * 
     * @param inputParams An array of strings containing the category name and
     *                    weight. The array should contain
     *                    two elements: the name of the category and the weight (a
     *                    float value).
     * @param con         A connection object to the database.
     * @return True if the category was successfully added, otherwise false.
     * @throws SQLException If there is an issue with the SQL query or database
     *                      connection.
     */
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

    /**
     * Displays all assignments for the currently selected class, grouped by their
     * respective categories.
     * 
     * <p>
     * This method retrieves and displays assignments within the currently selected
     * class, showing each
     * assignment's name, associated category, and point value. If no class is
     * selected, an error message is printed.
     * </p>
     * 
     * @param con A connection object to the database.
     * @return True if assignments are displayed successfully, otherwise false.
     * @throws SQLException If there is an issue with the SQL query or database
     *                      connection.
     */
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

    /**
     * Retrieves the CategoryID for a specific category in a given class.
     * 
     * <p>
     * This method queries the database for a category by name within a specified
     * class and returns its corresponding CategoryID.
     * If the category does not exist in the given class, an exception is thrown.
     * </p>
     * 
     * @param category The name of the category.
     * @param classId  The ID of the class to which the category belongs.
     * @param con      A connection object to the database.
     * @return The CategoryID corresponding to the specified category and class.
     * @throws SQLException If there is an issue with the SQL query or if the
     *                      category is not found.
     */
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
     * Looks up a student by their StudentID in the database.
     * 
     * <p>
     * This method checks if a student with the given StudentID exists in the
     * Student table. If found, it prints a message indicating the student was found
     * and returns true.
     * If no student is found, it prints a message indicating that no student was
     * found and that a new student will be added, returning false.
     * </p>
     * 
     * @param studentid The StudentID of the student to be looked up.
     * @param con       The connection object to the database.
     * @return true if the student is found, false if the student is not found.
     * @throws SQLException If there is an issue with the SQL query or the database
     *                      connection.
     */
    public static boolean studentLookUp(int studentid, Connection con) throws SQLException {
        String sql = "SELECT * FROM Student WHERE EXISTS (select Student.StudentID FROM Student WHERE Student.StudentID = ?);";
        PreparedStatement pstmt = con.prepareStatement(sql);

        pstmt.setInt(1, studentid);

        ResultSet resultSet = pstmt.executeQuery();
        if (resultSet.next()) { // Move to first row
            String retId = resultSet.getString(1); // 1, not 0
            System.out.println("Student with ID: " + retId + " Found");
            return true;

        } else {
            // Student was not found need to create new student and add them
            System.out.println("No student found for studentID: " + studentid + "\n Adding...");

            return false;
        }
    }

    /**
     * Adds a new student to the database.
     * 
     * <p>
     * This method inserts a new student record into the Student table in the
     * database with the provided StudentID, username, and name.
     * </p>
     * 
     * @param username  The username of the student to be added.
     * @param name      The full name of the student to be added.
     * @param studentid The unique ID of the student to be added.
     * @param con       The connection object to the database.
     * @return true if the student was successfully added.
     * @throws SQLException If there is an issue with the SQL query or the database
     *                      connection.
     */
    public static boolean addStudent(String username, String name, int studentid, Connection con) throws SQLException {
        // String sql = "INSERT INTO Student (StudentID,username, name) VALUES
        // (111,'rhodgets9', 'Rebeka Hodgets');;";
        String sql = "INSERT INTO Student (StudentID,username, name) VALUES (? ,? ,?);";
        PreparedStatement pstmt = con.prepareStatement(sql);
        // insert into Category (Name, classID, Weight) values ('Exam', 1, 0.5);
        pstmt.setInt(1, studentid);
        pstmt.setString(2, username);
        pstmt.setString(3, name);
        int rows = pstmt.executeUpdate();
        System.out.println("Student " + username + " Added!!");
        con.commit();

        return true;
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

    /**
     * Returns a students ID given their username
     * 
     * 
     * @param username - Student username
     * @param con      - Connection to database
     * @return StudentID or if no student was found returns -1
     * @throws SQLException
     */
    public static int getStudentID(String username, Connection con) throws SQLException {
        String sql = "select Student.StudentID FROM Student WHERE Student.username = ?;";
        PreparedStatement pstmt = con.prepareStatement(sql);

        pstmt.setString(1, username);

        ResultSet resultSet = pstmt.executeQuery();
        if (resultSet.next()) { // Move to first row
            String retId = resultSet.getString(1); // 1, not 0
            System.out.println("Student with ID: " + retId + " Found");
            return Integer.parseInt(retId);

        } else {
            // Student was not found need to create new student and add them
            System.out.println("No student found for student user name: " + username + "\n Try again...");

            return -1;
        }
    }

    /**
     * Adds a student to a class based on their username.
     * 
     * <p>
     * This method retrieves the student ID associated with the provided username
     * and adds the student to the enrolled list of the selected class.
     * </p>
     * 
     * @param parameters An array of strings containing the username of the student
     *                   to be added to the class.
     * @param con        The connection object to the database.
     * @return true if the student was successfully added to the class; false if no
     *         student was found with the provided username.
     * @throws SQLException If there is an issue with the SQL query or the database
     *                      connection.
     */
    public static boolean addStudentUsername(String[] parameters, Connection con) throws SQLException {
        String userName = parameters[0];
        // Based on username get studentID
        // IF there is no student with that username Throw an error
        int studentID = getStudentID(userName, con);
        if (studentID == -1) {
            return false;
        }

        // SQL query
        // EXAMPLE: INSERT INTO Enrolled (StudentID, ClassID) VALUES (1, 1);
        String addStudentSQL = "INSERT INTO Enrolled (StudentID, ClassID) VALUES (?, ?)";

        PreparedStatement pstmt = con.prepareStatement(addStudentSQL);

        // Setting query parameters
        pstmt.setInt(1, studentID);
        pstmt.setInt(2, selectedClassID);

        // Executing query
        pstmt.executeUpdate();
        con.commit();
        return true;

    }

    /**
     * Shows all students enrolled in the current class in which the student's
     * name contains a string
     * 
     * @param inputString - The string we want to look for in the student's name
     * @param con         - Connection to the Database
     * @return boolean where transaction was successful
     * @throws SQLException
     */
    public static boolean showStudentsContainsString(String[] inpuString, Connection con) throws SQLException {
        String lookingForStr = inpuString[0];
        // SQL query to get all students in a class
        String sql = "SELECT Student.name FROM gradeManager.Enrolled JOIN gradeManager.Student ON Enrolled.StudentID = Student.StudentID WHERE Enrolled.ClassID = ? AND (LOWER(Student.name) LIKE '%"
                + lookingForStr + "%' OR LOWER(Student.username) LIKE '%" + lookingForStr + "%');";
        PreparedStatement pstmt = con.prepareStatement(sql);
        pstmt.setInt(1, selectedClassID);

        // Result set of all students enrolled in selected class
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

    /**
     * Shows all students enrolled in the current class
     * 
     * @param con - Connection to the Database
     * @return boolean where transaction was successful
     * @throws SQLException
     */
    public static boolean showStudents(String[] inputParameters, Connection con) throws SQLException {
        // Validate class has been selected
        if (selectedClassID == null) {
            System.out.println("No class selected");
            return false;
        }
        if (inputParameters.length == 1) {
            showStudentsContainsString(inputParameters, con);
            return true;
        }
        // SQL query to get all students in a class
        String sql = "SELECT Student.name FROM gradeManager.Enrolled join gradeManager.Student on Enrolled.StudentID = Student.StudentID WHERE Enrolled.ClassID = ?;";
        PreparedStatement pstmt = con.prepareStatement(sql);
        pstmt.setInt(1, selectedClassID);

        // Result set of all students enrolled in selected class
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

    /**
     * Adds a student to the selected class.
     * 
     * <p>
     * This method checks if the class has been selected, validates the provided
     * parameters, and either looks up an existing student or creates a new student
     * record. It then adds the student to the specified class by inserting an
     * enrollment record into the database.
     * </p>
     * 
     * @param parameters An array of strings containing the student information. If
     *                   only the username is provided, the student will be added
     *                   based on the username. Otherwise, the array must contain
     *                   the following elements: username, student ID, last name,
     *                   and first name.
     * @param con        The connection object to the database.
     * @return true if the student was successfully added to the class; false if
     *         there were any issues or invalid parameters.
     * @throws SQLException If there is an issue with the SQL query or the database
     *                      connection.
     */
    public static boolean addStudent(String[] parameters, Connection con) throws SQLException {
        // Validate class has been selected
        if (selectedClassID == null) {
            System.out.println("No class selected");
            return false;
        }
        // Validate parameters
        System.out.println(parameters.length);
        if (parameters.length == 1) {
            // Entered only username
            addStudentUsername(parameters, con);
            return true;
        } else if (parameters.length != 4) {
            System.out.println("Invalid parameters!");
            System.out.println("Usage: add-student <username> <studentid> <Last> <First>");
            return false;
        }

        // parse inputs
        String username = parameters[0];
        int studentid = Integer.parseInt(parameters[1]);
        String last = parameters[2];
        String first = parameters[3];

        // SQL query
        // EXAMPLE: INSERT INTO Enrolled (StudentID, ClassID) VALUES (1, 1);
        String addStudentSQL = "INSERT INTO Enrolled (StudentID, ClassID) VALUES (?, ?)";

        PreparedStatement pstmt = con.prepareStatement(addStudentSQL);

        // If student does not exist create a new student based on StudentID
        boolean studentExist = studentLookUp(studentid, con);
        if (!studentExist) {
            // Create new student
            String name = first + " " + last;
            addStudent(username, name, studentid, con);
        }
        // Setting query parameters
        pstmt.setInt(1, studentid);
        pstmt.setInt(2, selectedClassID);

        // Executing query
        pstmt.executeUpdate();
        con.commit();

        return true;
    }

    /**
     * Displays the grades of a student in the selected class, including details
     * about individual assignments, categories, and the overall grade.
     * 
     * <p>
     * This method fetches the student's ID based on their username, checks if they
     * are enrolled in the selected class, and then retrieves the student's grades
     * for assignments in various categories. The results are grouped by category,
     * showing the grade for each assignment, the earned and possible points for
     * each category, and the overall grade based on the weighted categories.
     * </p>
     * 
     * @param params An array of strings containing the student's username. The
     *               array must contain exactly one element.
     * @param con    The connection object to the database.
     * @throws SQLException If there is an issue with the SQL queries or the
     *                      database connection.
     */
    public static void studentGrades(String[] params, Connection con) throws SQLException {
        if (selectedClassID == null) {
            System.out.println("No class selected");
            return;
        }
        if (params.length < 1) {
            System.out.println("Usage: student-grades <username>");
            return;
        }
        String username = params[0];

        // Get StudentID from username
        String studentSql = "SELECT StudentID FROM Student WHERE username = ?";
        PreparedStatement studentPstmt = con.prepareStatement(studentSql);
        studentPstmt.setString(1, username);
        ResultSet studentRs = studentPstmt.executeQuery();
        if (!studentRs.next()) {
            System.out.println("Student not found");
            return;
        }
        int studentID = studentRs.getInt("StudentID");

        // Check enrollment
        String enrollSql = "SELECT * FROM Enrolled WHERE StudentID = ? AND ClassID = ?";
        PreparedStatement enrollPstmt = con.prepareStatement(enrollSql);
        enrollPstmt.setInt(1, studentID);
        enrollPstmt.setInt(2, selectedClassID);
        ResultSet enrollRs = enrollPstmt.executeQuery();
        if (!enrollRs.next()) {
            System.out.println("Student not enrolled in this class");
            return;
        }

        // Fetch assignments, categories, and grades
        String sql = "SELECT c.Name AS CategoryName, a.Name AS AssignmentName, a.PointValue, comp.Grade " +
                "FROM Assignment a " +
                "JOIN Category c ON a.categoryID = c.CategoryID " +
                "LEFT JOIN Completed comp ON a.AssignmentID = comp.AssignmentID AND comp.StudentID = ? " +
                "WHERE a.classID = ? " +
                "ORDER BY c.Name, a.Name";
        PreparedStatement pstmt = con.prepareStatement(sql);
        pstmt.setInt(1, studentID);
        pstmt.setInt(2, selectedClassID);
        ResultSet rs = pstmt.executeQuery();

        // Group by category
        Map<String, List<String>> categoryAssignments = new LinkedHashMap<>();
        Map<String, Double> categoryEarned = new HashMap<>();
        Map<String, Double> categoryPossible = new HashMap<>();
        String currentCategory = null;

        while (rs.next()) {
            String category = rs.getString("CategoryName");
            String assignment = rs.getString("AssignmentName");
            int pointValue = rs.getInt("PointValue");
            Double grade = rs.getObject("Grade") != null ? rs.getDouble("Grade") : null;

            if (!category.equals(currentCategory)) {
                currentCategory = category;
                categoryAssignments.put(category, new ArrayList<>());
                categoryEarned.put(category, 0.0);
                categoryPossible.put(category, 0.0);
            }

            String display = assignment + " (" + pointValue + " pts): " + (grade != null ? grade : "N/A");
            categoryAssignments.get(category).add(display);

            if (grade != null) {
                categoryEarned.put(category, categoryEarned.get(category) + grade);
            }
            categoryPossible.put(category, categoryPossible.get(category) + pointValue);
        }

        // Calculate and display results
        double overallGrade = 0.0;
        for (String category : categoryAssignments.keySet()) {
            double earned = categoryEarned.get(category);
            double possible = categoryPossible.get(category);
            double categoryWeight = getCategoryWeight(category, con);
            double categoryScore = (possible > 0) ? (earned / possible) * categoryWeight : 0;
            overallGrade += categoryScore;

            System.out.println("Category: " + category);
            for (String assignment : categoryAssignments.get(category)) {
                System.out.println("  " + assignment);
            }
            System.out.printf("  Subtotal: %.2f / %.2f (%.2f%% of total)%n", earned, possible, categoryWeight);
        }

        System.out.printf("Overall Grade: %.2f%%", overallGrade);
        con.commit();
    }

    /**
     * Displays the gradebook for all students enrolled in the selected class,
     * showing their username, student ID, full name, and total grade.
     * 
     * <p>
     * This method fetches the list of students enrolled in the selected class,
     * calculates their total grades using the `calculateTotalGrade` method, and
     * prints the results in a formatted way.
     * </p>
     * 
     * @param con The connection object to the database.
     * @throws SQLException If there is an issue with the SQL queries or the
     *                      database connection.
     */
    public static void gradebook(Connection con) throws SQLException {
        if (selectedClassID == null) {
            System.out.println("No class selected");
            return;
        }

        // Fetch enrolled students
        String studentSql = "SELECT s.StudentID, s.username, s.name " +
                "FROM Student s JOIN Enrolled e ON s.StudentID = e.StudentID " +
                "WHERE e.ClassID = ?";
        PreparedStatement studentPstmt = con.prepareStatement(studentSql);
        studentPstmt.setInt(1, selectedClassID);
        ResultSet studentRs = studentPstmt.executeQuery();

        while (studentRs.next()) {
            int studentID = studentRs.getInt("StudentID");
            String username = studentRs.getString("username");
            String name = studentRs.getString("name");
            double totalGrade = calculateTotalGrade(studentID, con);

            System.out.printf("%s (%s): %s - %.2f%%%n", username, studentID, name, totalGrade);
        }
        con.commit();
    }

    /**
     * Retrieves the weight of a specific category for the selected class.
     * 
     * <p>
     * This method queries the database to find the weight of a given category in
     * the selected class. If the category is found, it returns the weight;
     * otherwise, it returns 0.0.
     * </p>
     * 
     * @param categoryName The name of the category whose weight is to be retrieved.
     * @param con          The connection object to the database.
     * @return The weight of the specified category in the selected class.
     * @throws SQLException If there is an issue with the SQL query or the database
     *                      connection.
     */
    private static double getCategoryWeight(String categoryName, Connection con) throws SQLException {
        String sql = "SELECT Weight FROM Category WHERE Name = ? AND classID = ?";
        PreparedStatement pstmt = con.prepareStatement(sql);
        pstmt.setString(1, categoryName);
        pstmt.setInt(2, selectedClassID);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            return rs.getDouble("Weight");
        }
        return 0.0;
    }

    /**
     * Calculates the total grade for a student based on their performance in each
     * category of assignments for a specific class.
     * 
     * <p>
     * This method retrieves the earned points, possible points, and category weight
     * for each category in the class. It calculates the total grade by multiplying
     * the earned percentage for each category by its weight.
     * </p>
     * 
     * @param studentID The ID of the student whose total grade is being calculated.
     * @param con       The connection object to the database.
     * @return The total grade as a percentage for the student in the selected
     *         class.
     * @throws SQLException If there is an issue with the SQL query or the database
     *                      connection.
     */
    private static double calculateTotalGrade(int studentID, Connection con) throws SQLException {
        String sql = "SELECT c.Name AS CategoryName, SUM(COALESCE(comp.Grade, 0)) AS Earned, " +
                "SUM(a.PointValue) AS Possible, c.Weight " +
                "FROM Assignment a " +
                "JOIN Category c ON a.categoryID = c.CategoryID " +
                "LEFT JOIN Completed comp ON a.AssignmentID = comp.AssignmentID AND comp.StudentID = ? " +
                "WHERE a.classID = ? " +
                "GROUP BY c.CategoryID, c.Name, c.Weight";
        PreparedStatement pstmt = con.prepareStatement(sql);
        pstmt.setInt(1, studentID);
        pstmt.setInt(2, selectedClassID);
        ResultSet rs = pstmt.executeQuery();

        double totalGrade = 0.0;
        while (rs.next()) {
            double earned = rs.getDouble("Earned");
            double possible = rs.getDouble("Possible");
            double weight = rs.getDouble("Weight");
            if (possible > 0) {
                totalGrade += (earned / possible) * weight;
            }
        }
        return totalGrade;
    }

    /**
     * Assign a grade for a student for an assignment
     * If student already has a grade for said assgnemtn update it. Grade can not
     * excede the assignment total points
     * 
     * @param inputParameters - String array containing assignment name, student
     *                        username
     *                        and the assignment grade
     * @param con             - Connection to the database
     * @return
     * @throws SQLException
     */
    public static boolean grade(String[] inputParameters, Connection con) throws SQLException {
        // Validate class has been selected
        if (selectedClassID == null) {
            System.out.println("No class selected");
            return false;
        } else if (inputParameters.length != 3) {
            System.out.println("Invalid parameters\nUsage: grade <assignmentname> <username> <grade>");
            return false;
        }

        // parsing input array
        String assignmentName = "Homework-1";
        String username = "csteanson0";
        int grade = 9;
        // String assignmentName = inputParameters[0];
        // String username = inputParameters[1];
        // int grade = Integer.parseInt(inputParameters[2]);
        // 1. Check that the assignment exists
        // IF is doesnt return false
        // then get its id
        int assignmentID = getAssignmentID(assignmentName, con);

        if (assignmentID == -1) {
            System.out.println("No assignment found with name " + assignmentName);
            return false;
        }

        // 2. Get student id based on input username
        int studentID = getStudentID(username, con);
        if (studentID == -1) {
            System.out.println("No student found with username " + username);
            return false;
        }

        // 3. Make sure that the grade doesnt exceeds the points value for the
        // assignment
        // print warning and stop
        int assignmentPointValue = getAssignmentPoints(assignmentID, con);

        if (grade > assignmentPointValue) {
            System.out.println(
                    "WARNING input grade " + grade + " greater than max point value of " + assignmentPointValue);
            return false;
        }
        // check if there is already a grade for the assignment
        // 4. Check if a grade already exists
        String checkSQL = "SELECT Grade FROM Completed WHERE StudentID = ? AND AssignmentID = ?";
        PreparedStatement checkStmt = con.prepareStatement(checkSQL);
        checkStmt.setInt(1, studentID);
        checkStmt.setInt(2, assignmentID);
        ResultSet rs = checkStmt.executeQuery();
        if (rs.next()) {
            // 5a. Update existing grade
            String updateSQL = "UPDATE Completed SET Grade = ? WHERE StudentID = ? AND AssignmentID = ?";
            PreparedStatement updateStmt = con.prepareStatement(updateSQL);
            updateStmt.setInt(1, grade);
            updateStmt.setInt(2, studentID);
            updateStmt.setInt(3, assignmentID);
            updateStmt.executeUpdate();
            con.commit();
            System.out.println("Updated grade for " + username + " on " + assignmentName);
        } else {
            // 5b. Insert new grade
            String insertSQL = "INSERT INTO Completed (StudentID, AssignmentID, Grade) VALUES (?, ?, ?)";
            PreparedStatement insertStmt = con.prepareStatement(insertSQL);
            insertStmt.setInt(1, studentID);
            insertStmt.setInt(2, assignmentID);
            insertStmt.setInt(3, grade);
            insertStmt.executeUpdate();
            con.commit();
            System.out.println("Inserted grade for " + username + " on " + assignmentName);
        }

        return true;
    }

    /**
     * Retrieves the AssignmentID for a given assignment name.
     * 
     * <p>
     * This method queries the database to find the AssignmentID for the assignment
     * that matches the given assignment name.
     * </p>
     * 
     * @param assignmentName The name of the assignment whose ID is to be retrieved.
     * @param con            The connection object to the database.
     * @return The AssignmentID if the assignment is found, otherwise -1 if no
     *         matching assignment is found.
     * @throws SQLException If there is an issue with the SQL query or the database
     *                      connection.
     */
    public static int getAssignmentID(String assignmentName, Connection con) throws SQLException {
        String sql = "SELECT Assignment.AssignmentID FROM gradeManager.Assignment where Assignment.Name = \""
                + assignmentName + "\";";
        PreparedStatement pstmt = con.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            return rs.getInt("AssignmentID");
        }
        return -1;
    }

    /**
     * Retrieves the point value for a given assignment ID.
     * 
     * <p>
     * This method queries the database to retrieve the point value for the
     * assignment associated with the given assignment ID.
     * </p>
     * 
     * @param assignmentID The ID of the assignment whose point value is to be
     *                     retrieved.
     * @param con          The connection object to the database.
     * @return The point value for the assignment if found, otherwise -1 if no
     *         matching assignment is found.
     * @throws SQLException If there is an issue with the SQL query or the database
     *                      connection.
     */
    public static int getAssignmentPoints(int assignmentID, Connection con) throws SQLException {
        String sql = "SELECT Assignment.PointValue FROM gradeManager.Assignment where Assignment.AssignmentID = ?;";
        PreparedStatement pstmt = con.prepareStatement(sql);
        pstmt.setInt(1, assignmentID);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            return rs.getInt("PointValue");
        }
        return -1;

    }

    /**
     * Parses the input string into an array of tokens, handling both quoted and
     * unquoted strings.
     * 
     * <p>
     * This method splits the input string into tokens, where each token is either a
     * word or a quoted string.
     * Quoted strings are treated as a single token, and spaces are used to separate
     * unquoted tokens.
     * </p>
     * 
     * @param input The input string to be parsed into tokens.
     * @return An array of strings, where each string is a token extracted from the
     *         input.
     */
    public static String[] parseInput(String input) {
        // Regular expression that captures strings (with or without quotes) and treats
        // them as tokens
        String regex = "\"([^\"]*)\"|([^\\s\"]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        List<String> tokens = new ArrayList<>();
        while (matcher.find()) {
            if (matcher.group(1) != null) {
                // Add quoted string as a single token
                tokens.add(matcher.group(1));
            } else {
                // Add unquoted token
                tokens.add(matcher.group(2));
            }
        }

        return tokens.toArray(new String[0]); // Convert list to array
    }

    /**
     * The main method that starts the GradeManager application.
     * 
     * <p>
     * This method establishes a connection to a MySQL database, displays a welcome
     * banner, and enters a read-eval-print loop (REPL) to accept user commands. The
     * user can perform various actions like adding classes, adding categories,
     * managing assignments, grading students, and viewing the gradebook.
     * </p>
     * 
     * <p>
     * The method handles different commands entered by the user, performs the
     * corresponding actions, and commits the changes to the database. In case of
     * errors, the method ensures that the transaction is rolled back to maintain
     * database integrity.
     * </p>
     * 
     * @param args The command-line arguments (unused in this case).
     * @throws ClassNotFoundException If the JDBC driver class is not found.
     * @throws SQLException           If a database error occurs.
     * @throws InstantiationException If the JDBC driver cannot be instantiated.
     * @throws IllegalAccessException If the JDBC driver class cannot be accessed.
     */
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
            // System.out.println("=======GradeManager=======");
            String banner = "   ______               __     __  ___                                 \n" +
                    "  / ____/________ _____/ /__  /  |/  /___ _____  ____ _____ ____  _____\n" +
                    " / / __/ ___/ __ `/ __  / _ \\/ /|_/ / __ `/ __ \\/ __ `/ __ `/ _ \\/ ___/\n" +
                    "/ /_/ / /  / /_/ / /_/ /  __/ /  / / /_/ / / / / /_/ / /_/ /  __/ /    \n" +
                    "\\____/_/   \\__,_/\\__,_/\\___/_/  /_/\\__,_/_/ /_/\\__,_/\\__, /\\___/_/     \n" +
                    "                                                    /____/              \n";

            System.out.println(banner);
            System.out.println("Welcome!");

            // Read user input for command
            Scanner scanner = new Scanner(System.in); // Create a Scanner object
            boolean isRunning = true;

            GradeManager gm = new GradeManager();
            GradeManager.showMenu();

            // ====Transaction block starts====
            con.setAutoCommit(false);
            stmt = con.createStatement(); // responsible for executing queries

            // REPL
            while (isRunning) {
                System.out.println("\nEnter command: ");

                // Get user input and tokenize it
                String inputString = scanner.nextLine().toLowerCase(); // Read user input
                String[] inputTokenized = parseInput(inputString);

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

                    case "list-classes":
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
                    case "show-students":
                        showStudents(inputParameters, con);
                        break;
                    case "grade":
                        grade(inputParameters, con);
                        break;
                    case "student-grades":
                        studentGrades(inputParameters, con);
                        break;
                    case "gradebook":
                        gradebook(con);
                        break;
                    case "menu":
                        showMenu();
                        break;
                    default:
                        System.out.println("Invalid command: " + cmd);
                        showMenu();
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
