#!/bin/bash
###############
# Step 3: Using repeating sentence model from Step 2, remove repeating sentences from regex-cleaned files, 
###############
echo "Step 3: Remove Repeating Sentences."

source ./classpath.sh
cd ..
#mvn -DskipTests clean package

#echo "$(cygpath -pw "$CLASSPATH")"

java -Xmx10g -Dfile.encoding=UTF-8 -classpath "$(cygpath -pw "$CLASSPATH")" cobra.textclean.batch.ReduceSentenceFiles \
    H:/data/Enron/output/regex \
    H:/data/Enron/output/sentences
