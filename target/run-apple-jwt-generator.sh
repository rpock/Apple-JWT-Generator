#!/bin/bash

# Get the directory of this script
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Run the JAR file
java -jar "$DIR/applejwtgenerator-1.0-SNAPSHOT.jar"