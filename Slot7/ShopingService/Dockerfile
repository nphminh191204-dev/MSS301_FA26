# ==========================================
# Optimized & Shared Multi-Stage Dockerfile for Spring Boot Microservices
# Compatible with: order-service, inventory-service, product-service, etc.
# ==========================================

# ------------------------------------------
# Stage 1: Extract JAR layers for optimal Docker caching
# ------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS builder

WORKDIR /builder

# Dynamic build argument to support any service JAR
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# Extract Spring Boot layers (supports Spring Boot 3.3+ tools mode and legacy layertools)
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --destination extracted 2>/dev/null || \
    (mkdir -p extracted && cd extracted && java -Djarmode=layertools -jar ../app.jar extract)

# ------------------------------------------
# Stage 2: Minimal runtime image
# ------------------------------------------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Expose default port (can be overridden via build arg or environment)
ARG PORT=8080
ENV PORT=${PORT}
EXPOSE ${PORT}

# Security: Create non-root user and group
RUN addgroup -S spring && adduser -S spring -G spring

# Copy extracted layers in order of change frequency (least frequent to most frequent)
COPY --from=builder /builder/extracted/dependencies/ ./
COPY --from=builder /builder/extracted/spring-boot-loader/ ./
COPY --from=builder /builder/extracted/snapshot-dependencies/ ./
COPY --from=builder /builder/extracted/application/ ./

# Change ownership to non-root user
RUN chown -R spring:spring /app

USER spring:spring

# JVM Container awareness & memory optimization
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Launch via Spring Boot's JarLauncher (supports Spring Boot 3.2+ and earlier)
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher 2>/dev/null || exec java $JAVA_OPTS org.springframework.boot.loader.JarLauncher"]
