#!/bin/bash
###############
# Performs regex cleanups on files, writing the output files to a target directory.
###############
echo "Step 1: Regex File Cleanup"
source ./classpath.sh
cd ..
mvn -DskipTests clean package

#echo "$(cygpath -pw "$CLASSPATH")"

java -Xmx10g -Dfile.encoding=UTF-8 -classpath "$(cygpath -pw "$CLASSPATH")" cobra.textclean.batch.RegexFiles \
    "Z:\Data Sets\CORA_Analytics_ENRON_Set\Export1\VOL000001\TEXT\TEXT000001" \
    H:/data/Enron/output/regex-test

#    D:/data/Enron/FromCORA \
#    H:/data/Enron/output/regex

    