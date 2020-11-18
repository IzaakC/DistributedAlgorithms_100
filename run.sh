#!/bin/sh
cd src/
javac *.java
rmic Process

# rmiregistry 5000 &

# i=0
# while [ $i -lt $1 ]
# do
#     echo "starting process $i"
#     java Main $i $1 &
#     i=`expr $i + 1`
# done


# java Server $1
