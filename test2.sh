#! /bin/ksh

CNT=200000
ant compile
time java -classpath /Users/gparks1/workspace/eclipse/filz/jackson-core-2.13.4.jar:/Users/gparks1/workspace/eclipse/filz/jackson-databind-2.13.4.jar:/Users/gparks1/workspace/eclipse/filz/jackson-annotations-2.13.4.jar:/Users/gparks1/workspace/eclipse/filz/target \
    filz.Directory $CNT y > ant_run_${CNT}_`date +%Y-%m-%d_%H%M`.txt 2>&1
