#!/bin/bash
###############
# Step 3: Using repeating sentence model from Step 2, remove repeating sentences from regex-cleaned files, 
###############
echo "Step 3: Remove Repeating Sentences."

source ./classpath.sh
cd ..
#mvn -DskipTests clean package


java -Xmx10g -Dfile.encoding=UTF-8 -classpath $CLASSPATH cobra.textclean.batch.ReduceSentenceFiles \
    /home/Sharing/ENRON/out/step1 \
    /home/Sharing/ENRON/out/step3

