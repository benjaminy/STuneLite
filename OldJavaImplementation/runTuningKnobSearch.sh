#!/bin/bash

TOOLS_DIR=`tools_dir`
TK_DIR="${TOOLS_DIR}/TuningKnobs"

TKjar="${TK_DIR}/dist/TuningKnobs.jar"
JPjar="${TK_DIR}/JavaPlot-0.4.0/dist/JavaPlot.jar"
POIjar="${TK_DIR}/poi-3.2-FINAL/build/dist/poi-3.2-FINAL.jar"
Tuplejar="${TK_DIR}/tuple/out/tuple.jar"
cp="$TKjar:$JPjar:$POIjar:$Tuplejar"
cpArg="-cp $TKjar:$JPjar:$POIjar:$Tuplejar"
# cpArg="-classpath $TKjar:$JPjar:$POIjar:$Tuplejar"

# JAVA_CMD="/System/Library/Frameworks/JavaVM.framework/Versions/1.6.0/Commands/java"
JAVA_CMD="java"
# JAVA_CMD="jdb -Xdebug"

$JAVA_CMD ${cpArg} -Xmx1024M -enableassertions TuningKnobSearch $@
# $JAVA_CMD ${cpArg}  TuningKnobSearch $@
