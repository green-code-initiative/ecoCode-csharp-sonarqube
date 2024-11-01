# syntax=docker/dockerfile:1.0

ARG MAVEN_BUILDER=3-openjdk-17-slim

# Default Sonarqube version to 9.9 Community (Current LTS)
ARG SONARQUBE_VERSION=9.9-community

FROM maven:${MAVEN_BUILDER} AS builder

WORKDIR /usr/src/ecocode
COPY src src/
COPY pom.xml tool_build.sh ./

RUN ./tool_build.sh

FROM sonarqube:${SONARQUBE_VERSION}
COPY --from=builder /usr/src/ecocode/target/ecocode-*.jar /opt/sonarqube/extensions/plugins/
USER sonarqube
