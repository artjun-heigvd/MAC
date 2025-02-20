# syntax=docker/dockerfile:1
FROM apache/zeppelin:0.11.2

ENV SPARK_VERSION=3.5.4
ENV SCALA_VERSION=2.13
ENV HADOOP_VERSION=3
ENV SPARK_HOME=/opt/spark
ENV PATH=$SPARK_HOME/bin:$PATH

USER root

RUN SPARK_DOWNLOAD_URL="https://dlcdn.apache.org/spark/spark-${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}-scala${SCALA_VERSION}.tgz" \
    && wget --verbose -O apache-spark.tgz "${SPARK_DOWNLOAD_URL}" \
    && mkdir -p $SPARK_HOME \
    && tar -xf apache-spark.tgz -C $SPARK_HOME --strip-components=1 \
    && rm apache-spark.tgz

USER 1000
