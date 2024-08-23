FROM amazoncorretto:21-alpine-jdk
COPY target/*.jar Api.jar
ENTRYPOINT ["java", "-jar", "/Api.jar"]