CREATE DATABASE IF NOT EXISTS gradeManager;
USE gradeManager;

CREATE TABLE Student (
    StudentID INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE Class (
    ClassID INT PRIMARY KEY AUTO_INCREMENT,
    CourseNumber VARCHAR(10) NOT NULL,
    ClassName VARCHAR(50) NOT NULL,
    Term VARCHAR(20) NOT NULL,
    SectionNumber INT NOT NULL,
    Description TEXT
);

CREATE TABLE Category (
    CategoryID INT PRIMARY KEY AUTO_INCREMENT,
    Name VARCHAR(50) NOT NULL,
    classID INT NOT NULL,
    Weight DECIMAL(5,2) NOT NULL,
    FOREIGN KEY (classID) REFERENCES Class(ClassID)
);

CREATE TABLE Assignment (
    AssignmentID INT PRIMARY KEY AUTO_INCREMENT,
    Name VARCHAR(100) NOT NULL UNIQUE,
    PointValue INT NOT NULL,
    categoryID INT NOT NULL,
    classID INT NOT NULL,
    Description TEXT,
    FOREIGN KEY (categoryID) REFERENCES Category(CategoryID),
    FOREIGN KEY (classID) REFERENCES Class(ClassID),
    UNIQUE (classID, Name)
);

CREATE TABLE Enrolled (
    StudentID INT NOT NULL,
    ClassID INT NOT NULL,
    PRIMARY KEY (StudentID, ClassID),
    FOREIGN KEY (StudentID) REFERENCES Student(StudentID),
    FOREIGN KEY (ClassID) REFERENCES Class(ClassID)
);

CREATE TABLE Completed (
    StudentID INT NOT NULL,
    AssignmentID INT NOT NULL,
    Grade DECIMAL(5,2),
    PRIMARY KEY (StudentID, AssignmentID),
    FOREIGN KEY (StudentID) REFERENCES Student(StudentID),
    FOREIGN KEY (AssignmentID) REFERENCES Assignment(AssignmentID)
);
