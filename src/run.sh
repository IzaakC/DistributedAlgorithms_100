#!/bin/sh
# cd src/
javac *.java
rmic Petersons
java Main $1 $2

