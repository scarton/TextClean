#!/bin/bash
###############
# Step 4: Second time, Performs regex cleanups on files, writing the output files to a target directory.
###############
echo "Step 4: 2nd Regex File Cleanup"
source ./classpath.sh
cd ..
mvn -DskipTests clean package

#echo "$(cygpath -pw "$CLASSPATH")"

java -Xmx10g -Dfile.encoding=UTF-8 -classpath "$(cygpath -pw "$CLASSPATH")" cobra.textclean.batch.RegexFiles \
    H:/data/Enron/output/sentences \
    H:/data/Enron/output/regex2
    
