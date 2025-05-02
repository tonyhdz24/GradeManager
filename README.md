# **GradeManager**

GradeManager is a Java-based command-line application that allows users to manage grades for students in a database. The application supports various actions such as adding new classes, managing assignments, grading students, and viewing grade reports.

## **Features**

* **Class Management**: Add new classes, list available classes, and select a class to manage.

* **Category Management**: Add and view categories for each class (e.g., Homework, Exams).

* **Assignment Management**: Add assignments to categories and view assignments by category.

* **Student Management**: Add students, list students, and assign grades.

* **Gradebook**: View the overall gradebook for a class and grade reports for individual students.

* **Menu System**: Command-line menu interface to interact with the application.

* **Transactional Support**: Changes are committed to the database, with rollback support in case of errors.

## **Requirements**

* Java 8 or higher

* MySQL Database

## **Database Setup**

1. **Database**: You need a MySQL database with the `gradeManager` schema. You can import the required tables from an existing schema or set it up manually. A dump.sql file is provided to generate a starter database.

2. **JDBC Driver**: Ensure you have the MySQL JDBC driver (`mysql-connector-java`) in your project classpath.

## **Configuration**

Before running the application, you need to configure the database connection details in the `main` method of the `GradeManager` class:

* `nRemotePort`: The port number of your MySQL database.

* `strDbPassword`: The password for the database user.

* `dbName`: The name of the database (e.g., `gradeManager`).

```java
// Example configuration
int nRemotePort = 50939; // Remote port number of the database  
String strDbPassword = "db41825"; // Database login password  
String dbName = "gradeManager"; // Database name
```
## **Usage**

Once the application is set up and the database is running, you can start the `GradeManager` application. When executed, the program will:

1. Connect to the database.

2. Display a welcome banner and the main menu.

3. Accept commands from the user.

### **Command List**

* **exit**: Exit the application.

* **new-class**: Create a new class.

* **list-classes**: List available classes.

* **select-class**: Select a class to manage.

* **show-class**: Show information about the selected class.

* **show-categories**: Show categories for the selected class.

* **add-category**: Add a new category to the selected class.

* **show-assignment**: Show assignments by categories for the selected class.

* **add-student**: Add a new student.

* **show-students**: List all students in the selected class.

* **grade**: Assign grades to students.

* **student-grades**: View grades for a specific student.

* **gradebook**: View the gradebook for the selected class.

* **menu**: Display the command menu.

### **Example Commands**

```sh  
new-class "Math 101"  
list-classes  
select-class "Math 101"  
add-category "Homework"  
show-assignment  
grade "student1" "homework1" 90  
student-grades "student1"  
gradebook
```
## **Error Handling**

* The application ensures that changes to the database are done within transactions. If an error occurs during any operation, the changes will be rolled back to ensure that the database remains in a consistent state.

## **Running the Application**

To run the GradeManager application, follow these steps:

1. Clone or download the repository.
```bash
git clone https://github.com/tonyhdz24/GradeManager
cd Grademanager
```

2. Compile the Java code:

```bash  
javac GradeManager.java
```  
3. Run the application:

```bash    
   java GradeManager
```