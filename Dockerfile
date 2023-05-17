FROM sbtscala/scala-sbt:graalvm-ce-22.3.0-b2-java11_1.8.2_3.2.2

COPY ./ /tsa-gbif-tool
WORKDIR /tsa-gbif-tool
RUN sbt compile