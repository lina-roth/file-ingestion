# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /usr/src/app

# Copy the Gradle wrapper and build files
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle ./
COPY settings.gradle ./

# Download and install Gradle dependencies (to cache them)
RUN ./gradlew build --no-daemon -x test

# Copy the rest of the application source code
COPY src ./src

# Build the application (skip tests for the Docker build process)
RUN ./gradlew build --no-daemon -x test

# Make the ingestion and completed directories in the container
RUN mkdir -p /usr/src/app/ingestion
RUN mkdir -p /usr/src/app/ingestion-completed
RUN mkdir -p /usr/src/app/ingestion-error

ENV INGESTION_DIR=/usr/src/app/ingestion
ENV COMPLETED_DIR=/usr/src/app/ingestion-completed
ENV ERROR_DIR=/usr/src/app/ingestion-error

# Expose any ports if required (optional)
# EXPOSE 8080

# Run the application
CMD ["java", "-jar", "build/libs/file-ingestion-0.0.1-SNAPSHOT.jar", "${INGESTION_DIR}", "${COMPLETED_DIR}", "${COMPLETED_DIR}"]
