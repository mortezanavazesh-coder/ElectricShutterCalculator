#!/usr/bin/env bash

##############################################################################
#
# Gradle start up script for UN*X
#
##############################################################################

# Determine the directory of the script
APP_HOME=$(cd "$(dirname "$0")"; pwd)

# Set default JVM options
DEFAULT_JVM_OPTS=""

# Locate the wrapper JAR
WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

# Locate the wrapper properties
WRAPPER_PROPERTIES="$APP_HOME/gradle/wrapper/gradle-wrapper.properties"

# Execute the wrapper JAR
exec java $DEFAULT_JVM_OPTS -jar "$WRAPPER_JAR" "$@"
