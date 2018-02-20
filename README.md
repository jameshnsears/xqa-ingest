# xqa-ingest [![Build Status](https://travis-ci.org/jameshnsears/xqa-ingest.svg?branch=master)](https://travis-ci.org/jameshnsears/xqa-ingest) [![Coverage Status](https://coveralls.io/repos/github/jameshnsears/xqa-ingest/badge.svg?branch=master)](https://coveralls.io/github/jameshnsears/xqa-ingest?branch=master)
* loads the contents of XML files, from the file system, into an AMQP queue.

## 1. Build
* docker-compose -p "dev" build --force-rm

## 2. Bring up
* docker-compose -p "dev" up -d xqa-message-broker
* chmod 777 $HOME/GIT_REPOS/xqa-test-data/*.xml
* docker run -d --net="dev_xqa" --name="dev_xqa-ingester_1" -v $HOME/GIT_REPOS/xqa-test-data:/opt/xqa-ingest/xml dev_xqa-ingester -message_broker_host dev_xqa-message-broker_1
* docker logs -f dev_xqa-ingester_1

## 3. Test
* see .travis.yml

## 4. Teardown
* docker-compose -p "dev" down -v
