CATALINA_OPTS="-Xmx512m"

# silence JDK restricted native access warning raised by GraalJS (Truffle); available since JDK 17
CATALINA_OPTS="$CATALINA_OPTS --enable-native-access=ALL-UNNAMED"

# silence JDK sun.misc.Unsafe deprecation warning raised by GraalJS (Truffle); flag only exists since JDK 23
JAVA_BIN="java"
if [ "x$JAVA_HOME" != "x" ]; then
  JAVA_BIN="$JAVA_HOME/bin/java"
fi
JAVA_VERSION=$(JAVA_TOOL_OPTIONS= "$JAVA_BIN" -version 2>&1 \
  | awk -F '"' '/version/ {print $2; exit}' \
  | sed '/^0\./s///' \
  | cut -d'.' -f1)
if [[ "$JAVA_VERSION" =~ ^[0-9]+$ ]] && [ "$JAVA_VERSION" -ge 23 ]; then
  CATALINA_OPTS="$CATALINA_OPTS --sun-misc-unsafe-memory-access=allow"
fi

export CATALINA_OPTS
