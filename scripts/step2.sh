#!/bin/bash
###############
# Step 2: gather repeating sentences from regex-cleaned files, 
#writing the output model to the parent directory of the source files.
###############
echo "Step 2: Gather Repeating Sentences and make/update model"

source ./classpath.sh
cd ..
#mvn -DskipTests clean package

#echo "$(cygpath -pw "$CLASSPATH")"

java -Xmx10g -Dfile.encoding=UTF-8 -classpath "$(cygpath -pw "$CLASSPATH")" cobra.textclean.batch.CreateRepeatingSentenceModel \
    H:/data/Enron/output/regex
