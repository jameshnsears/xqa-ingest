#!/usr/bin/env bash

# one time password request / login
docker login -u jameshnsears

push_to_docker_hub xqa-ingest

docker search jameshnsears

exit $?
