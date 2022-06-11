FROM openjdk:8-jdk-alpine
WORKDIR /readserver
ADD target/readserver.jar readserver.jar
COPY Data Data
EXPOSE 5050
ENTRYPOINT ["java","-jar","readserver.jar"]
