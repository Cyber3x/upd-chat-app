# shellcheck disable=SC2124
args="$@"
mvn compile exec:java -Dexec.mainClass="hr.fer.oprpp2.chat.server.Main" -Dexec.args="$args"