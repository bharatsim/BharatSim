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

java -jar $FILE &
startTime=$(date +%s)

AppPID=$!
ps -p $AppPID >> /dev/null
psExist=$?

inputCPU=$1
inputMemory=$2
inputTime=$3

maxCpu=0
maxMem=0
bound=0.2

while [ $psExist -eq 0 ]; do

read cpu mem<<< $(ps -p $AppPID -o %cpu -o vsz | tail -n1)

if ! [ $cpu \> 0 ]; then
  break
fi

if ! [ $mem \> 0 ]; then
  break
fi

if (( $(echo "$cpu > $maxCpu" | bc -l) )); then
    maxCpu=$cpu
fi


if (( $(echo "$mem > $maxMem" | bc -l) )); then
  maxMem=$mem
fi


ps -p $AppPID >> /dev/null
psExist=$?
done

timeDiff=$(( $(date +%s) - $startTime ))

cpuThreshold=$inputCPU+$inputCPU*$bound
if  (( $(echo "$maxCpu > $cpuThreshold" | bc -l) )); then
    echo "CPU usage exceeds the threshold"
    exit 2
fi

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

echo "max CPU: $maxCpu Memory: $maxMem ExecutionTime: $timeDiff"
exit 0
