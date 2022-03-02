FROM openjdk:11-slim
ARG PORT
ARG VERSION
ENV JAR_NAME="bin-collection-service-${VERSION}-all.jar"
COPY ./build/libs/$JAR_NAME /
EXPOSE $PORT
ENTRYPOINT java -jar -Dapplication.properties.path=bin-collection-service.properties -Dlogback.configurationFile=bin-collection-service-logback.xml /$JAR_NAME
