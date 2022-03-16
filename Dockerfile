FROM openjdk:11-slim
ARG PORT
ARG VERSION
COPY ./build/libs/bin-collection-service-${VERSION}-all.jar /bin-collection-service.jar
EXPOSE $PORT
ENTRYPOINT ["java", "-jar", "/bin-collection-service.jar"]
