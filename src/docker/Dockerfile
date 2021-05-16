FROM openjdk:11-jre-slim
VOLUME /tmp
ARG JAR_FILE
EXPOSE 8080
COPY ${JAR_FILE} lead_validation.jar
ENTRYPOINT ["java","-jar","/lead_validation.jar"]