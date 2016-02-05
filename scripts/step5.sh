#!/bin/bash
###############
# Step 5: Grab word tokens from cleaned text. 
###############
echo "Step 5: tokenize."

source ./classpath.sh
cd ..
mvn -DskipTests clean package

#echo "$(cygpath -pw "$CLASSPATH")"

java -Xmx10g -Dfile.encoding=UTF-8 -classpath "$(cygpath -pw "$CLASSPATH")" cobra.textclean.batch.TokenizeFiles \
    H:/data/Enron/output/regex2 \
    H:/data/Enron/output/tokenized
