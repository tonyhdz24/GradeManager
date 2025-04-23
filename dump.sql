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

-- Inserting Students
INSERT INTO Student (username, name) VALUES ('csteanson0', 'Chip Steanson');
INSERT INTO Student (username, name) VALUES ('rmaps1', 'Rebekkah Maps');
INSERT INTO Student (username, name) VALUES ('gwarrener2', 'Glynda Warrener');
INSERT INTO Student (username, name) VALUES ('tbayns3', 'Tremain Bayns');
INSERT INTO Student (username, name) VALUES ('bnardi4', 'Brannon Nardi');
INSERT INTO Student (username, name) VALUES ('lwalhedd5', 'Lindsay Walhedd');
INSERT INTO Student (username, name) VALUES ('lramstead6', 'Lesli Ramstead');
INSERT INTO Student (username, name) VALUES ('tvoyce7', 'Toiboid Voyce');
INSERT INTO Student (username, name) VALUES ('lchubb8', 'Lazaro Chubb');
INSERT INTO Student (username, name) VALUES ('rhodgets9', 'Rebeka Hodgets');

-- Inserting Classes
insert into Class (CourseNumber,ClassName, Term, SectionNumber, Description) values ('CS310',"Intro To DataBases" ,'Sp25', 3, 'Nullam sit amet turpis elementum ligula vehicula consequat. Morbi a ipsum. Integer a nibh. In quis justo. Maecenas rhoncus aliquam lacus. Morbi quis tortor id nulla ultrices aliquet. Maecenas leo odio, condimentum id, luctus nec, molestie sed, justo.');
insert into Class (CourseNumber,ClassName, Term, SectionNumber, Description) values ('MATH275','Multivariable & Vector Calc', 'Sp25', 1, 'Integer a nibh. In quis justo. Maecenas rhoncus aliquam lacus. Morbi quis tortor id nulla ultrices aliquet. Maecenas leo odio, condimentum id, luctus nec, molestie sed, justo. Pellentesque viverra pede ac diam. Cras pellentesque volutpat dui. Maecenas tristique, est et tempus semper, est quam pharetra magna, ac consequat metus sapien ut nunc. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Mauris viverra diam vitae quam. Suspendisse potenti.');
insert into Class (CourseNumber,ClassName, Term, SectionNumber, Description) values ('ECE330','Microprocessors', 'Fall24', 2, 'Aenean sit amet justo. Morbi ut odio. Cras mi pede, malesuada in, imperdiet et, commodo vulputate, justo. In blandit ultrices enim. Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Proin interdum mauris non ligula pellentesque ultrices. Phasellus id sapien in sapien iaculis congue. Vivamus metus arcu, adipiscing molestie, hendrerit at, vulputate vitae, nisl. Aenean lectus. Pellentesque eget nunc.');
insert into Class (CourseNumber,ClassName, Term, SectionNumber, Description) values ('CS402','Mobile App Development' ,'Fall24', 3, 'Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Proin interdum mauris non ligula pellentesque ultrices. Phasellus id sapien in sapien iaculis congue. Vivamus metus arcu, adipiscing molestie, hendrerit at, vulputate vitae, nisl. Aenean lectus. Pellentesque eget nunc. Donec quis orci eget orci vehicula condimentum. Curabitur in libero ut massa volutpat convallis. Morbi odio odio, elementum eu, interdum eu, tincidunt in, leo. Maecenas pulvinar lobortis est.');
insert into Class (CourseNumber,ClassName, Term, SectionNumber, Description) values ('CS421',"Algorithms", 'Sp25', 2, 'Vivamus tortor. Duis mattis egestas metus. Aenean fermentum. Donec ut mauris eget massa tempor convallis. Nulla neque libero, convallis eget, eleifend luctus, ultricies eu, nibh. Quisque id justo sit amet sapien dignissim vestibulum.');

-- Inserting Category
insert into Category (Name, classID, Weight) values ('Exam', 1, 0.5);
insert into Category (Name, classID, Weight) value('Homework', 1, 0.2);
insert into Category (Name, classID, Weight) values('Project', 1, 0.3);

-- Inserting Assignments
insert into Assignment (Name, PointValue, categoryID, classID, Description) values ('Homework-1', 10, 2, 1, 'Suspendisse potenti. Cras in purus eu magna vulputate luctus. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus.');
insert into Assignment (Name, PointValue, categoryID, classID, Description) values ('Homework-2', 10, 2, 1, 'Praesent blandit lacinia erat. Vestibulum sed magna at nunc commodo placerat. Praesent blandit. Nam nulla.');
insert into Assignment (Name, PointValue, categoryID, classID, Description) values ('Midterm-1', 30, 1, 1, 'Maecenas ut massa quis augue luctus tincidunt. Nulla mollis molestie lorem.');
insert into Assignment (Name, PointValue, categoryID, classID, Description) values ('Midterm-2', 30, 2, 1, 'Maecenas tristique, est et tempus semper, est quam pharetra magna, ac consequat metus sapien ut nunc.');
insert into Assignment (Name, PointValue, categoryID, classID, Description) values ('Final Project', 100, 3, 1, 'Vestibulum rutrum rutrum neque. Aenean auctor gravida sem. Praesent id massa id nisl venenatis lacinia.');

-- Inserting Enrolled
-- Enroll all students into ClassID 1
INSERT INTO Enrolled (StudentID, ClassID) VALUES (1, 1);
INSERT INTO Enrolled (StudentID, ClassID) VALUES (2, 1);
INSERT INTO Enrolled (StudentID, ClassID) VALUES (3, 1);
INSERT INTO Enrolled (StudentID, ClassID) VALUES (4, 1);
INSERT INTO Enrolled (StudentID, ClassID) VALUES (5, 1);
INSERT INTO Enrolled (StudentID, ClassID) VALUES (6, 1);
INSERT INTO Enrolled (StudentID, ClassID) VALUES (7, 1);
INSERT INTO Enrolled (StudentID, ClassID) VALUES (8, 1);
INSERT INTO Enrolled (StudentID, ClassID) VALUES (9, 1);
INSERT INTO Enrolled (StudentID, ClassID) VALUES (10, 1);

-- Inserting Completed: All students completed all assignments
insert into Completed (StudentID, AssignmentID, Grade) values (1, 1, 85);
insert into Completed (StudentID, AssignmentID, Grade) values (1, 2, 87);
insert into Completed (StudentID, AssignmentID, Grade) values (1, 3, 78);
insert into Completed (StudentID, AssignmentID, Grade) values (1, 4, 82);
insert into Completed (StudentID, AssignmentID, Grade) values (1, 5, 90);

insert into Completed (StudentID, AssignmentID, Grade) values (2, 1, 88);
insert into Completed (StudentID, AssignmentID, Grade) values (2, 2, 79);
insert into Completed (StudentID, AssignmentID, Grade) values (2, 3, 84);
insert into Completed (StudentID, AssignmentID, Grade) values (2, 4, 81);
insert into Completed (StudentID, AssignmentID, Grade) values (2, 5, 93);

insert into Completed (StudentID, AssignmentID, Grade) values (3, 1, 76);
insert into Completed (StudentID, AssignmentID, Grade) values (3, 2, 80);
insert into Completed (StudentID, AssignmentID, Grade) values (3, 3, 77);
insert into Completed (StudentID, AssignmentID, Grade) values (3, 4, 85);
insert into Completed (StudentID, AssignmentID, Grade) values (3, 5, 89);

insert into Completed (StudentID, AssignmentID, Grade) values (4, 1, 92);
insert into Completed (StudentID, AssignmentID, Grade) values (4, 2, 90);
insert into Completed (StudentID, AssignmentID, Grade) values (4, 3, 88);
insert into Completed (StudentID, AssignmentID, Grade) values (4, 4, 84);
insert into Completed (StudentID, AssignmentID, Grade) values (4, 5, 95);

insert into Completed (StudentID, AssignmentID, Grade) values (5, 1, 70);
insert into Completed (StudentID, AssignmentID, Grade) values (5, 2, 73);
insert into Completed (StudentID, AssignmentID, Grade) values (5, 3, 68);
insert into Completed (StudentID, AssignmentID, Grade) values (5, 4, 75);
insert into Completed (StudentID, AssignmentID, Grade) values (5, 5, 78);

insert into Completed (StudentID, AssignmentID, Grade) values (6, 1, 95);
insert into Completed (StudentID, AssignmentID, Grade) values (6, 2, 92);
insert into Completed (StudentID, AssignmentID, Grade) values (6, 3, 91);
insert into Completed (StudentID, AssignmentID, Grade) values (6, 4, 89);
insert into Completed (StudentID, AssignmentID, Grade) values (6, 5, 94);

insert into Completed (StudentID, AssignmentID, Grade) values (7, 1, 83);
insert into Completed (StudentID, AssignmentID, Grade) values (7, 2, 85);
insert into Completed (StudentID, AssignmentID, Grade) values (7, 3, 79);
insert into Completed (StudentID, AssignmentID, Grade) values (7, 4, 82);
insert into Completed (StudentID, AssignmentID, Grade) values (7, 5, 87);

insert into Completed (StudentID, AssignmentID, Grade) values (8, 1, 86);
insert into Completed (StudentID, AssignmentID, Grade) values (8, 2, 89);
insert into Completed (StudentID, AssignmentID, Grade) values (8, 3, 84);
insert into Completed (StudentID, AssignmentID, Grade) values (8, 4, 90);
insert into Completed (StudentID, AssignmentID, Grade) values (8, 5, 92);

insert into Completed (StudentID, AssignmentID, Grade) values (9, 1, 77);
insert into Completed (StudentID, AssignmentID, Grade) values (9, 2, 80);
insert into Completed (StudentID, AssignmentID, Grade) values (9, 3, 72);
insert into Completed (StudentID, AssignmentID, Grade) values (9, 4, 79);
insert into Completed (StudentID, AssignmentID, Grade) values (9, 5, 85);

insert into Completed (StudentID, AssignmentID, Grade) values (10, 1, 91);
insert into Completed (StudentID, AssignmentID, Grade) values (10, 2, 88);
insert into Completed (StudentID, AssignmentID, Grade) values (10, 3, 87);
insert into Completed (StudentID, AssignmentID, Grade) values (10, 4, 90);
insert into Completed (StudentID, AssignmentID, Grade) values (10, 5, 96);
