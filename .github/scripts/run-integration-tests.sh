#!/usr/bin/env bash

EXECUTE_BUILD=true
EXECUTE_TEST=true
TEST_SUITE="engine"
DATABASE="h2"
DISTRO="tomcat"
VALID_TEST_SUITES=("engine" "webapps" "instance-migration" "rolling-update" "old-engine")
VALID_DISTROS=("tomcat" "wildfly")
VALID_DATABASES=("h2" "postgresql")

##########################################################################
check_valid_values() {
  local param_name=$1
  local value=$2
  shift 2
  local array=("$@")
  for item in "${array[@]}"; do
    if [[ "$value" == "$item" ]]; then
      return 0
    fi
  done
  echo "Error: Argument '$param_name' must be one of: [${array[*]}], but was '$value'"
  exit 1
}

##########################################################################
parse_args() {
  while [ "$#" -gt 0 ]; do
    case "$1" in
      --testsuite=*)
        TEST_SUITE="${1#*=}"
        ;;
      --distro=*)
        DISTRO="${1#*=}"
        ;;
      --db=*)
        DATABASE="${1#*=}"
        ;;
      --no-build)
        EXECUTE_BUILD=false
        ;;
      --no-test)
        EXECUTE_TEST=false
        ;;
    esac
    shift
  done

  check_valid_values "testsuite" "$TEST_SUITE" "${VALID_TEST_SUITES[@]}"
  # --distro and --db are ignored for migration suites but still validated when provided
  if [[ "$TEST_SUITE" != "instance-migration" && "$TEST_SUITE" != "rolling-update" && "$TEST_SUITE" != "old-engine" ]]; then
    check_valid_values "distro" "$DISTRO" "${VALID_DISTROS[@]}"
    check_valid_values "db" "$DATABASE" "${VALID_DATABASES[@]}"
  fi
}

run_build () {
  # Migration suites don't deploy to a server; only engine artifacts need to be installed
  if [[ "$TEST_SUITE" == "instance-migration" || "$TEST_SUITE" == "rolling-update" || "$TEST_SUITE" == "old-engine" ]]; then
    echo "ℹ️ Installing engine artifacts for $TEST_SUITE tests"
    echo "./mvnw -DskipTests -Pcheck-engine clean install"
    ./mvnw -DskipTests -Pcheck-engine clean install
    if [[ $? -ne 0 ]]; then
      echo "❌ Error: Build failed"
      popd > /dev/null
      exit 1
    fi
    return
  fi

  PROFILES=(distro distro-webjar h2-in-memory)

  if [[ "$DISTRO" == "eximeebpms" ]]; then
    PROFILES+=(distro-run integration-test-eximeebpms-run)
  fi
  if [[ "$DISTRO" == "tomcat" ]]; then
    PROFILES+=(tomcat distro-tomcat)
  fi
  if [[ "$DISTRO" == "wildfly" ]]; then
    PROFILES+=(wildfly distro-wildfly)
  fi

  echo "ℹ️ Building $TEST_SUITE integration tests for distro $DISTRO with $DATABASE database using profiles: [${PROFILES[*]}]"
  echo "./mvnw -U -DskipTests -Dcargo.maven.skip=true -Pdistro-ce,$(IFS=,; echo "${PROFILES[*]}") clean install"
  ./mvnw -U -DskipTests -Dcargo.maven.skip=true -Pdistro-ce,$(IFS=,; echo "${PROFILES[*]}") clean install
  if [[ $? -ne 0 ]]; then
    echo "❌ Error: Build failed"
    popd > /dev/null
    exit 1
  fi
}

##########################################################################
run_tests () {
  # Migration suites run directly against h2; no server distro needed
  case "$TEST_SUITE" in
    instance-migration|rolling-update|old-engine)
      echo "ℹ️ Running $TEST_SUITE tests with h2"
      echo "./mvnw -P${TEST_SUITE},h2 clean verify -f qa"
      ./mvnw -P${TEST_SUITE},h2 clean verify -f qa
      if [[ $? -ne 0 ]]; then
        echo "❌ Error: Tests failed"
        popd > /dev/null
        exit 1
      fi
      return
      ;;
  esac

  PROFILES=()

  case "$TEST_SUITE" in
    engine)
      PROFILES+=(engine-integration-jakarta)
      ;;
    webapps)
      PROFILES+=(webapps-integration)
      ;;
  esac

  case "$DISTRO" in
    eximeebpms)
      PROFILES+=(integration-test-eximeebpms-run)
      QA_DIR=distro/run/qa
      ;;
    tomcat)
      PROFILES+=(tomcat)
      QA_DIR=qa
      ;;
    wildfly)
      PROFILES+=(wildfly)
      QA_DIR=qa
      ;;
  esac

  DB_ARGS=()
  case "$DATABASE" in
    h2)
      PROFILES+=(h2)
      ;;
    postgresql)
      PROFILES+=(postgresql)
      DB_ARGS=(
        -Ddatabase.url="${DATABASE_URL:-jdbc:postgresql://localhost:5432/process-engine}"
        -Ddatabase.username="${DATABASE_USERNAME:-eximeebpms}"
        -Ddatabase.password="${DATABASE_PASSWORD:-eximeebpms}"
      )
      ;;
  esac

  echo "ℹ️ Running $TEST_SUITE integration tests for distro $DISTRO with $DATABASE database using profiles: [${PROFILES[*]}]"
  echo "./mvnw -U -Pdistro-ce,$(IFS=,; echo "${PROFILES[*]}") clean verify -f $QA_DIR ${DB_ARGS[*]}"
  ./mvnw -U -Pdistro-ce,$(IFS=,; echo "${PROFILES[*]}") clean verify -f $QA_DIR "${DB_ARGS[@]}"
  if [[ $? -ne 0 ]]; then
    echo "❌ Error: Build failed"
    popd > /dev/null
    exit 1
  fi
}

##########################################################################
# main script
parse_args "$@"

pushd $(pwd) > /dev/null
cd $(git rev-parse --show-toplevel) || exit 1

if [[ "$EXECUTE_BUILD" == true ]]; then
  run_build
else
  echo "ℹ️ Skipping build"
fi

if [[ "$EXECUTE_TEST" == true ]]; then
  run_tests
else
  echo "ℹ️ Skipping tests"
fi

popd > /dev/null
