FROM openjdk:8u312-jre-slim
COPY target/signably.jar /opt/signably.jar
ENTRYPOINT ["java", "-jar", "/opt/signably.jar"]
