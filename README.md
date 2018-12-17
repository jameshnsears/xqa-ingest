# xqa-ingest [![Build Status](https://travis-ci.org/jameshnsears/xqa-ingest.svg?branch=master)](https://travis-ci.org/jameshnsears/xqa-ingest) [![Coverage Status](https://coveralls.io/repos/github/jameshnsears/xqa-ingest/badge.svg?branch=master)](https://coveralls.io/github/jameshnsears/xqa-ingest?branch=master) [![sonarcloud.io](https://sonarcloud.io/api/project_badges/measure?project=jameshnsears_xqa-ingest&metric=alert_status)](https://sonarcloud.io/api/project_badges/measure?project=jameshnsears_xqa-ingest&metric=alert_status)
* evenly distributes the ingested XML across one or more shards.

![High Level Design](https://github.com/jameshnsears/xqa-documentation/blob/master/uml/ingest-balancer-sequence-diagram.jpg)

## 1. Build
* ./build.sh

## 2. Bring up
* docker-compose up -d xqa-message-broker

### 3. Test

### 3.1. Maven
* mvn clean compile test
* mvn jacoco:report coveralls:report
* mvn site  # findbugs

### 3.2. CLI
* java -jar target/xqa-ingest-1.0.0-SNAPSHOT-jar-with-dependencies.jar -message_broker_host 127.0.0.1 -path $HOME/GIT_REPOS/xqa-test-data

or

* docker run -d --net="xqa-ingest_xqa" --name="xqa-ingest" -v $HOME/GIT_REPOS/xqa-test-data:/xml xqa-ingest:latest -message_broker_host xqa-message-broker -path /xml

## 4. Teardown
* docker-compose down -v
