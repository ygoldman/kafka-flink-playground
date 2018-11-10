# kafka-flink-stack

This replicates as well as possible real deployment configurations, where you have your zookeeper, kafka, flink servers distinct from each other. This solves all the networking hurdles that comes with Docker and docker-compose, and is compatible cross platform.

## Stack version

  - Zookeeper version: 3.4.13
  - Kafka version: 2.0.0 (Confluent 5.0.1)
  - Kafka Schema Registry: Confluent 5.0.1
  - Kafka Schema Registry UI: 0.9.5
  - Kafka Rest Proxy: Confluent 5.0.1
  - Kafka Manager: latest
  - Kafka Connect: Confluent 5.0.0
  - Kafka Connect UI: 0.9.4
  - Zoonavigator: 0.4.0
  - Flink: latest
  - Graphite: latest
  - Grafana: latest


# Requirements

## Docker
Do not use the deprecated Docker Toolbox and there is no need to run VirtualBox. Mac Native emulation is good'nuff.

Export your environment before starting the stack:
export DOCKER_HOST_IP=127.0.0.1


## Full stack

 - Single Zookeeper: `$DOCKER_HOST_IP:2181`
 - Single Kafka: `$DOCKER_HOST_IP:9092`
 - Kafka Schema Registry: `$DOCKER_HOST_IP:8091`
 - Kafka Schema Registry UI: `$DOCKER_HOST_IP:8001`
 - Kafka Rest Proxy: `$DOCKER_HOST_IP:8082`
 - Kafka Manager: `$DOCKER_HOST_IP:9000` (use zoo1 for zookeeper instance when creating a new cluster)
 - Kafka Connect: `$DOCKER_HOST_IP:8083`
 - Kafka Connect UI: `$DOCKER_HOST_IP:8003`
 - Zoonavigator Web: `$DOCKER_HOST_IP:8004`(use `zoo1` as connection string, leave u/p blank)
 - Graphite Web: `$DOCKER_HOST_IP:8080`(u/p: guest/guest)
 - Grafana UI: `$DOCKER_HOST_IP:3000`(u/p: admin/admin)


 Run with:
 ```
 docker-compose -f full-stack.yml up
 docker-compose -f full-stack.yml down
 ```
 
# Run a simple producer and consumer test
```
./kafka-publisher-consumer-test.sh full-stack.yml
```

# What this gets you
![Running Services](./images/Screen%20Shot%202018-11-10%20at%204.41.16%20PM.png?raw=true "docker ps")
![Kafka Manager](./images/Screen%20Shot%202018-11-10%20at%204.42.59%20PM.png?raw=true "Kafka Manager")
![Kafka Connect](./images/Screen%20Shot%202018-11-10%20at%204.45.46%20PM.png?raw=true "Kafka Connect")
![Schema Registry](./images/Screen%20Shot%202018-11-10%20at%204.44.11%20PM.png?raw=true "Schema Registry")
![Zoo Navigator](./images/Screen%20Shot%202018-11-10%20at%204.45.16%20PM.png?raw=true "Zoo Navigator")
![Flink Manager](./images/Screen%20Shot%202018-11-10%20at%204.45.26%20PM.png?raw=true "Flink Manager")

# FAQ

## Kafka

**Q: How do I create a compacted topic**
A: A compacted topic retains the latest value of each primary key in a stream. To create one:
bin/kafka-topics.sh --zookeeper zoo1:2181 --create --topic myCompactedTopic --replication-factor 1 --partitions 1 --config min.insync.replicas=1 --config cleanup.policy=compact --config segment.bytes=1048576



**Q: Kafka's log is too verbose, how can I reduce it?**

A: Add the following line to your docker-compose environment variables: `KAFKA_LOG4J_LOGGERS: "kafka.controller=INFO,kafka.producer.async.DefaultEventHandler=INFO,state.change.logger=INFO"`. Full logging control can be accessed here: https://github.com/confluentinc/cp-docker-images/blob/master/debian/kafka/include/etc/confluent/docker/log4j.properties.template

**Q: How do I delete data to start fresh?**

A: First `docker-compose -f full-stack.yml down`, then remove the directory `full-stack`, for example by doing `rm -r -f full-stack`.

**Q: Can I change the zookeeper ports?**

A: yes. Say you want to change `zoo1` port to `12181` (only relevant lines are shown):
```
  zoo1:
    ports:
      - "12181:12181"
    environment:
        ZOO_PORT: 12181

  kafka1:
    environment:
      KAFKA_ZOOKEEPER_CONNECT: "zoo1:12181"
```

**Q: Can I change the Kafka ports?**

A: yes. Say you want to change `kafka1` port to `12345` (only relevant lines are shown). Note only `LISTENER_DOCKER_EXTERNAL` changes:
```
  kafka1:
    image: confluentinc/cp-kafka:5.0.0
    hostname: kafka1
    ports:
      - "12345:12345"
    environment:
      KAFKA_ADVERTISED_LISTENERS: LISTENER_DOCKER_INTERNAL://kafka1:19092,LISTENER_DOCKER_EXTERNAL://${DOCKER_HOST_IP:-127.0.0.1}:12345
```

Thanks to Stephane Maarek for getting me started by way of: https://github.com/simplesteph/kafka-stack-docker-compose.
