#! /bin/ksh

ant build
java -classpath /Users/gparks1/workspace/eclipse/filz/jackson-core-2.13.4.jar:/Users/gparks1/workspace/eclipse/filz/jackson-databind-2.13.4.jar:/Users/gparks1/workspace/eclipse/filz/jackson-annotations-2.13.4.jar:/Users/gparks1/workspace/eclipse/filz/target filz.Directory 5000
