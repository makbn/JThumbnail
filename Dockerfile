# JThumbnail with FFmpeg for video thumbnail extraction
FROM eclipse-temurin:21-jre-jammy AS runtime

# Install FFmpeg (required for FfmpegThumbnailer; fallback to JavaCV/MPEGThumbnailer if disabled)
RUN apt-get update \
    && apt-get install -y --no-install-recommends ffmpeg \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Build first: ./gradlew bootJar
COPY build/libs/*.jar app.jar

# Optional: run with test profile (no LibreOffice) or pass --spring.profiles.active=local for full stack
ENV JAVA_OPTS="-Xmx512m"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
