#!/bin/bash
set -e

export file=$1

#!/bin/bash
f () {
    errcode=$? # save the exit code as the first thing done in the trap function
    echo "error $errorcode"
    echo "the command executing at the time of the error was"
    echo "$BASH_COMMAND"
    echo "on line ${BASH_LINENO[0]}"
    # do some error handling, cleanup, logging, notification
    # $BASH_COMMAND contains the command that was being executed at the time of the trap
    # ${BASH_LINENO[0]} contains the line number in the script of that command
    # exit the script or return to try again, etc.
    # creating stack...
    docker-compose -f $file down
    exit $errcode  # or use some other value or do return instead
}
trap f ERR

all_great(){
    # for testing
    echo "Verifying Process"
    running=`docker-compose -f $1 ps | grep Up | wc -l`
    if [ "$running" < "$2" ]; then
        # for logging
        docker-compose -f $1 ps
        # debug
        docker-compose -f $1 logs
        exit 1
    fi
}

kafka_tests(){
    echo "Testing Kafka"
    topic="test_1"
    if grep -q kafka3 $1; then replication_factor="3"; else replication_factor="1"; fi
    for i in 1 2 3 4 5; do echo "Deleting test topic" && kafka-topics --delete --topic $topic --if-exists --zookeeper $DOCKER_HOST_IP:2181 && break || sleep 5; done
    for i in 1 2 3 4 5; do echo "Creating test topic" && kafka-topics --create --topic $topic --if-not-exists --replication-factor $replication_factor --partitions 12 --zookeeper $DOCKER_HOST_IP:2181 && break || sleep 5; done
    echo "Listing created topic" && kafka-topics --list --zookeeper $DOCKER_HOST_IP:2181 | grep "test_"

    for x in {1..10}; do echo $x | kafka-console-producer --broker-list $DOCKER_HOST_IP:9092 --topic $topic; done
    rows=`kafka-console-consumer --bootstrap-server $DOCKER_HOST_IP:9092 --topic $topic --from-beginning --timeout-ms 2000 | wc -l`
    if [ "$rows" != "10" ]; then
        kafka-console-consumer --bootstrap-server $DOCKER_HOST_IP:9092 --topic $topic --from-beginning --timeout-ms 2000 | wc -l
        exit 1
    else
        echo "Kafka Test Success"
    fi
}

# logging
docker-compose -f $file ps
# tests
all_great $1 $2
kafka_tests $1
all_great $1 $2
echo "Success!"
