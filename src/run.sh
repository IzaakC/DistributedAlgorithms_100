#!/bin/sh
# cd src/
javac *.java
rmic Process
java Main $1

