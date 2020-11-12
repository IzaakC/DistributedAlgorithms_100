#DA assignement 1

based on:
https://www.javatpoint.com/RMI

However, here all clients can receive, not sure if that is going to work..

in src/
javac *.java
rmic Process

terminal 1:
rmiregistry 5000 

terminal 2:
java Main 0 2

terminal 3:
java Server
