# =============================
# === BUILD STAGE ============
# =============================
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /build

# Install Maven
RUN apk add --no-cache maven

# Copy source code
COPY . .

# Build the application
RUN mvn clean package -DskipTests

# =============================
# === RUNTIME STAGE ==========
# =============================
FROM eclipse-temurin:17-jre-alpine

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# Copy JAR
COPY --from=build /build/dist/bigellor-starter-*-exec.jar /app/bigellor.jar

# Create writable directories
RUN mkdir -p /app/config /app/data \
    && chown -R spring:spring /app

# Set environment variables
ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENV SPRING_PROFILES_ACTIVE=prod

# Use non-root user
USER spring

# Expose app port
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar bigellor.jar"]

