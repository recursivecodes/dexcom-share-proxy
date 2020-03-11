FROM adoptopenjdk/openjdk11:jre11u-alpine-nightly
COPY build/libs/*-all.jar app.jar
CMD java ${JAVA_OPTS} -jar app.jar
