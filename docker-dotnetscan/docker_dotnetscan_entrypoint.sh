#!/usr/bin/env bash

get_help() {
  # Display Help
  echo "Run code analysis of dotnet codebase using Sonarqube."
  echo
  if [[ $(basename "$0") == "entrypoint.sh" ]]; then
    get_help_docker
  else
    get_help_script
  fi
  echo
}

get_help_scanning_options() {
  echo "Required:"
  echo -e "\t-t|--token\tSonarqube global or project analysis auth token."
  echo -e "or\t-l|--login\tSonarqube user auth token.\n"
  echo "Optional:"
  echo -e "\t-p|--project\tProject key (default: ecoCode-csharp-test-project)"
  echo -e "\t-u|--url\tUrl to Sonarqube instance (default: http://localhost:9000)"
  echo -e "\t-h|--help\tPrint this help\n"
  echo "Arguments:"
  echo -e "\tSpecify <build-commands> to dotnet, if needed (default: \"build\")"
}

get_help_script() {
  echo "Usage: $(basename "$0") -t|-l|--token|-login <auth_token> [-p|--project <project_key>] [-u|--url <sonarqube_url>] [-h|--help] [-- [<build-commands>]]"
  echo
  echo -e "Options:\n--------"
  get_help_scanning_options
}

get_help_docker() {
  echo "Usage: docker run --rm -it <container-params> scanner <analysis-params>"
  echo
  echo -e "container-params:\n-----------------"
  echo -e "\t--mount type=bind,src=\"<path_to_codebase>\",target=/src [--network <name>]"
  echo "Required:"
  echo -e "\t--mount\t\tVolume mounting options, specify <path_to_codebase> with the root path to codebase to analyze.\n"
  echo "Optional:"
  echo -e "\t--network\tSpecify docker network to use."
  echo
  echo -e "analysis-params:\n----------------"
  echo -e "\t-t|-l|--token|-login <auth_token> [-p|--project <project_key>] [-u|--url <sonarqube_url>] [-h|--help] [-- [<build-commands>]]"
  get_help_scanning_options
}

if [[ $# -lt 1 ]]; then
  echo "Error missing argument. You should provide at least an auth token."
  echo
  get_help
  exit
fi

# Process command line args
while [[ "$1" =~ ^- && ! "$1" == "--" ]]; do
  case $1 in
  -h | --help)
    get_help
    exit
    ;;
  -p | --project)
    shift
    project=$1
    ;;
  -t | --token)
    shift
    auth_token=$1
    ;;
  -l | --login)
    shift
    auth_login=$1
    ;;
  -u | --url)
    shift
    url=$1
    ;;
  esac
  shift
done
# Skip optional '--'
if [[ "$1" == '--' ]]; then shift; fi
# Get all remaining args
CMD_ARGS=$@

# Get passed args value or set to default value
PROJECT_KEY=${project:=ecoCode-csharp-test-project}
SONAR_HOST=${url:=http://localhost:9000}
if [[ -z $CMD_ARGS ]]; then
  DOTNET_ARGS="build"
else
  DOTNET_ARGS=$CMD_ARGS
fi

if [[ -n $auth_login ]]; then
  SONAR_TOKEN_TYPE="sonar.login"
  SONAR_TOKEN=$auth_login
fi
if [[ -n $auth_token ]]; then
  SONAR_TOKEN_TYPE="sonar.token"
  SONAR_TOKEN=$auth_token
fi

# Check mandatory args
if [[ -z $SONAR_TOKEN ]]; then
  echo "Auth token is empty. You have to provide one with -t option."
  exit 1
fi

# Check requirements
# ---
# Sonar scanner installed ?
if [ $(dotnet tool list --global | grep dotnet-sonarscanner | wc -l) -ne 1 ]; then
  echo "'dotnet-sonarscanner' tool NOT installed : please, install it with command 'dotnet tool install --global dotnet-sonarscanner'"
  exit 1
fi
# Java JDK or JRE installed ?
if [ -z $(which java) ]; then
  echo "Java JDK/JRE is required to process Sonarque analysis. Please installed one."
  exit 1
fi

# Analyze code
dotnet sonarscanner begin /k:"$PROJECT_KEY" /d:sonar.host.url="$SONAR_HOST" /d:$SONAR_TOKEN_TYPE="$SONAR_TOKEN" || exit 1
dotnet $DOTNET_ARGS
dotnet sonarscanner end /d:$SONAR_TOKEN_TYPE="$SONAR_TOKEN"
