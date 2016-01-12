#!/bin/sh

cd `dirname $0`

# set path to eclipse folder. If local folder, use '.'; otherwise, use /path/to/eclipse/
lib_folder="lib";


# get path to jar files inside $lib_folder folder
cp=$(find $lib_folder -name "*.jar" | sort);
cp=$(echo $cp | sed 's/ /:/g');

cp="./conf/:"$cp

if [ -z "$cp" ]; then
    echo "Error: Could not find jar files in path $lib_folder" 1>&2
    exit 1
fi

# debug options
debug_opts="-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=8001,server=y,suspend=y"

# program args
prog_args="-Dlogback.configurationFile=./conf/logback.xml -Dapp.logdir=./logs"

echo Launching Teleinfo reader app...

java $debug_opts  $prog_args \
    -Djava.library.path=lib \
    -classpath $cp \
    org.openhab.binding.teleinfo.reader.app.App \
    $* 