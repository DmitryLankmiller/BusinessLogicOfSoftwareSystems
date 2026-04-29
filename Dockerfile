FROM gradle:8.10.2-jdk21 AS builder

WORKDIR /app

COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradle gradle
COPY gradlew .
COPY src src

RUN chmod +x gradlew
RUN ./gradlew clean bootWar --no-daemon


FROM quay.io/wildfly/wildfly:latest-jdk21

USER root

RUN mkdir -p /opt/jboss/wildfly/customization

ADD https://jdbc.postgresql.org/download/postgresql-42.7.4.jar /opt/jboss/wildfly/postgresql.jar

COPY wildfly/postgres-datasource.cli /opt/jboss/wildfly/customization/postgres-datasource.cli

RUN /opt/jboss/wildfly/bin/jboss-cli.sh --file=/opt/jboss/wildfly/customization/postgres-datasource.cli

RUN /opt/jboss/wildfly/bin/add-user.sh admin admin123 --silent

COPY --from=builder /app/build/libs/lab.war /opt/jboss/wildfly/standalone/deployments/lab.war

EXPOSE 8080
EXPOSE 9990

CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]