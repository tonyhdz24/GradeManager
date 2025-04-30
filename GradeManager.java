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
        selectedClassID = 2;
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
        selectedClassID = 1;
        // Validate class has been selected
        // if (selectedClassID == null) {
        // System.out.println("No class selected");
        // return false;
        // } else if (inputParameters.length != 3) {
        // System.out.println("Invalid parameters\nUsage: grade <assignmentname>
        // <username> <grade>");
        // return false;
        // }

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

        // DEBUG
        System.out.println("Assignment ID " + assignmentID);
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

        // SQL query
        // EXAMPLE: insert into Completed (StudentID, AssignmentID, Grade) values (1,
        // 1,
        // 85);
        String gradeSQL = "INSERT INTO Completed (StudentID, AssignmentID, Grade) VALUES (?, ?, ?)";

        PreparedStatement pstmt = con.prepareStatement(gradeSQL);

        // Setting query parameters
        pstmt.setInt(1, studentID);
        pstmt.setInt(2, assignmentID);
        pstmt.setInt(3, selectedClassID);

        // Executing query
        pstmt.executeUpdate();
        con.commit();

        return true;
    }

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
                System.out.println("\nEnter command: ");
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
                    default:
                        System.out.println("Invalid command: " + cmd);
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
