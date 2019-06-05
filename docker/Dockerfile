FROM openjdk:8
MAINTAINER mikeldpl
WORKDIR /root

ARG JAR_FILE
COPY /target/${JAR_FILE} app.jar
EXPOSE 4567
CMD ["java", "-jar", "app.jar"]

