FROM ubuntu:bionic

RUN apt-get -qq update \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*
RUN apt-get -qq install -y --no-install-recommends openjdk-11-jre

RUN apt-get install --reinstall -y locales
RUN sed -i 's/# en_GB.UTF-8 UTF-8/en_GB.UTF-8 UTF-8/' /etc/locale.gen
RUN locale-gen en_GB.UTF-8
ENV LANG en_GB.UTF-8
ENV LANGUAGE en_GB
ENV LC_ALL en_GB.UTF-8
RUN dpkg-reconfigure --frontend noninteractive locales

ARG OPTDIR=/opt/
ARG XQA=xqa-ingest
ARG XML=/xml

RUN mkdir -p ${OPTDIR}${XQA}
RUN mkdir ${XML}
COPY target/xqa-ingest-1.0.0-SNAPSHOT-jar-with-dependencies.jar ${OPTDIR}${XQA}

RUN useradd -r -M -d ${OPTDIR}${XQA} xqa
RUN chown -R xqa:xqa ${OPTDIR}${XQA} ${XML}

USER xqa

WORKDIR ${OPTDIR}${XQA}

ENTRYPOINT ["java", "-jar", "xqa-ingest-1.0.0-SNAPSHOT-jar-with-dependencies.jar"]
