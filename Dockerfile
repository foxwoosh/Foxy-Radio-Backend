FROM gradle:7.4-jdk11-alpine as build

WORKDIR /app
COPY . /app
RUN gradle build

FROM openjdk:11-jre-slim

ENV JAR_NAME=studio.foxwoosh.foxy-radio-fat-0.0.1.jar

WORKDIR /app
COPY --from=build /app/build/libs/$JAR_NAME .

EXPOSE 443
EXPOSE 80

ENTRYPOINT java -jar $JAR_NAME
