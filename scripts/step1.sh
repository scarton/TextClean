#!/bin/bash
###############
# Performs regex cleanups on files, writing the output files to a target directory.
###############
echo "Step 1: Regex File Cleanup"
source ./classpath.sh
cd ..
mvn -DskipTests clean package

#echo "$(cygpath -pw "$CLASSPATH")"

#export MAX=100

java -Xmx10g -Dfile.encoding=UTF-8 -classpath $CLASSPATH cobra.textclean.batch.RegexFiles \
    /home/Sharing/ENRON/Export1/VOL000001/TEXT/TEXT000001 \
    /home/Sharing/ENRON/out/step1

#    D:/data/Enron/FromCORA \
#    H:/data/Enron/output/regex

    