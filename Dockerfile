FROM sbtscala/scala-sbt:graalvm-ce-22.3.0-b2-java11_1.8.2_3.2.2 as builder

COPY ./ /tsa-gbif-tool
WORKDIR /tsa-gbif-tool
#RUN sbt compile
RUN sbt universal:packageBin

FROM openjdk:11
COPY --from=builder /tsa-gbif-tool/target/universal/stage /tsa-gbif-tool
WORKDIR /tsa-gbif-tool
COPY ./entrypoint.sh /tsa-gbif-tool/entrypoint.sh