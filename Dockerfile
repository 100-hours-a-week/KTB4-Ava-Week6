# syntax=docker/dockerfile:1.7

ARG JAVA_VERSION=26

FROM eclipse-temurin:${JAVA_VERSION}-jdk AS builder

WORKDIR /app

COPY gradlew .
COPY gradle ./gradle
COPY settings.gradle* build.gradle* gradle.properties* ./

RUN chmod +x gradlew

COPY src ./src

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew clean bootJar --no-daemon

RUN java -Djarmode=tools \
    -jar build/libs/app.jar \
    extract \
    --layers \
    --destination extracted


FROM eclipse-temurin:${JAVA_VERSION}-jre AS runtime

ARG APP_UID=10001
ARG APP_GID=10001

RUN groupadd --system --gid ${APP_GID} spring \
    && useradd \
        --uid ${APP_UID} \
        --gid spring \
        --no-log-init \
        --no-create-home \
        spring \
    && mkdir -p /data/h2 /data/uploads \
    && chown -R spring:spring /data

WORKDIR /app

COPY --from=builder --chown=spring:spring \
    /app/extracted/dependencies/ ./

COPY --from=builder --chown=spring:spring \
    /app/extracted/spring-boot-loader/ ./

COPY --from=builder --chown=spring:spring \
    /app/extracted/snapshot-dependencies/ ./

COPY --from=builder --chown=spring:spring \
    /app/extracted/application/ ./

USER spring:spring

EXPOSE 8080

ENV JAVA_TOOL_OPTIONS="\
-XX:MaxRAMPercentage=75.0 \
-XX:+ExitOnOutOfMemoryError"

ENV SPRING_PROFILES_ACTIVE=production
ENV SPRING_DATASOURCE_URL=jdbc:h2:file:/data/h2/week6db
ENV FILE_UPLOAD_DIR=/data/uploads

VOLUME ["/data"]

ENTRYPOINT ["java", "-cp", "lib/*:app.jar", "org.ktb.week6.Week6Application"]
