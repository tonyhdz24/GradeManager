#!/bin/bash

# Ensure the user passes the Java file and input file
if [ "$#" -ne 1 ]; then
    echo "Usage: ./run.sh"
    exit 1
fi

JAVA_FILE=$1

# Compile the Java file
echo "Compiling $JAVA_FILE.java..."
clear
javac "$JAVA_FILE.java"
if [ $? -ne 0 ]; then
    echo "Compilation failed."
    exit 1
fi

# Run the Java program with the input file
echo "Running $JAVA_FILE"
java "$JAVA_FILE" "$INPUT_FILE"
