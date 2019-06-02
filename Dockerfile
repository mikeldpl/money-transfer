FROM maven:3.6.1-jdk-8 AS builder
WORKDIR /workspace

#resolve dependencies
COPY pom.xml pom.xml
RUN mvn dependency:resolve-plugins dependency:resolve verify

#build application
COPY . .
RUN mvn clean package


FROM openjdk:8
MAINTAINER mikeldpl
WORKDIR /root

COPY --from=builder /workspace/target/money-transfer-1.0.jar .
EXPOSE 4567
CMD ["java", "-jar", "money-transfer-1.0.jar"]

