#!/bin/sh
#sbt compile
#echo "compile done***********************"
#sbt assembly
#echo "assembly done***********************"

FILE=target/scala-2.13/engine-assembly-0.1.jar

if [ ! -f "$FILE" ]; then
    echo "$FILE doesn't exists!"
    exit 1
fi

source ./src/perfTest/parameters.sh

java -jar $FILE &
startTime=$(date +%s)

AppPID=$!
ps -p $AppPID >> /dev/null
psExist=$?

maxMem=0

while [ $psExist -eq 0 ]; do

read mem<<< $(ps -p $AppPID -o vsz | tail -n1)

check=`echo "$mem" | grep -E ^\-?[0-9]*\.?[0-9]+$`
if [ "$check" = '' ]; then
  break
fi

if (( $(echo "$mem > $maxMem" | bc -l) )); then
  maxMem=$mem
fi

ps -p $AppPID >> /dev/null
psExist=$?
done

timeDiff=$(( $(date +%s) - $startTime ))

memoryThreshold=$inputMemory+$inputMemory*$bound
if  (( $(echo "$maxMem > $memoryThreshold" | bc -l) )); then
    echo "Memory usage exceeds the threshold"
    exit 3
fi

timeThreshold=$inputTime+$inputTime*$bound
if  (( $(echo "$timeDiff > $timeThreshold" | bc -l) )); then
    echo "Execution time exceeds the threshold"
    exit 4
fi

echo "max Memory: $maxMem ExecutionTime: $timeDiff"
exit 0
