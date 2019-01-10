# xqa-ingest [![Build Status](https://travis-ci.org/jameshnsears/xqa-ingest.svg?branch=master)](https://travis-ci.org/jameshnsears/xqa-ingest) [![Coverage Status](https://coveralls.io/repos/github/jameshnsears/xqa-ingest/badge.svg?branch=master)](https://coveralls.io/github/jameshnsears/xqa-ingest?branch=master) [![sonarcloud.io](https://sonarcloud.io/api/project_badges/measure?project=jameshnsears_xqa-ingest&metric=alert_status)](https://sonarcloud.io/dashboard?id=jameshnsears_xqa-ingest) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/88d0edcfaaf14b4f987c8f1e1fedfbe9)](https://www.codacy.com/app/jameshnsears/xqa-ingest?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=jameshnsears/xqa-ingest&amp;utm_campaign=Badge_Grade)
* XML file loader.

## 1. AMQP Destinations
![xqa-ingest](uml/xqa-ingest.jpg)

## 2. Build
* ./build.sh

## 3. Bring up
* docker-compose up -d xqa-message-broker

### 4. Test

### 4.1. Maven
* see .travis.yml

### 4.2. CLI
* java -jar target/xqa-ingest-1.0.0-SNAPSHOT-jar-with-dependencies.jar -message_broker_host 127.0.0.1 -path $HOME/GIT_REPOS/xqa-test-data

or

* docker run -d --net="xqa-ingest_xqa" --name="xqa-ingest" -v $HOME/GIT_REPOS/xqa-test-data:/xml xqa-ingest:latest -message_broker_host xqa-message-broker -path /xml

## 5. Teardown
* docker-compose down -v
