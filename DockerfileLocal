FROM eclipse-temurin:21-jre-alpine

COPY target/lab-svc*.jar /app/lab-svc.jar
WORKDIR /app
#Expose server port
EXPOSE 50040
#Expose gRPC port
EXPOSE 50045
CMD ["java", "-jar", "lab-svc.jar"]