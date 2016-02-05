#!/bin/bash
###############
# Step 2: gather repeating sentences from regex-cleaned files, 
#writing the output model to the parent directory of the source files.
###############
echo "Step 2: Gather Repeating Sentences and make/update model"

source ./classpath.sh
cd ..
#mvn -DskipTests clean package


java -Xmx10g -Dfile.encoding=UTF-8 -classpath $CLASSPATH cobra.textclean.batch.CreateRepeatingSentenceModel \
    /home/Sharing/Enron/out/step1
