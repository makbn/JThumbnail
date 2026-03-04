# JThumbnail application image with LibreOffice and FFmpeg
FROM eclipse-temurin:21-jre-jammy

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
       libreoffice \
       libreoffice-writer \
       fonts-dejavu-core \
       ffmpeg \
       ca-certificates \
       curl \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Expect a Spring Boot fat JAR built by:
#   ./gradlew :jthumbnail-app:bootJar
COPY build/libs/*.jar app.jar

ENV JAVA_OPTS="-Xms256m -Xmx1024m"

EXPOSE 8081 9090

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

