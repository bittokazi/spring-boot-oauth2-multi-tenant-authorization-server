FROM maven:3.9.9-amazoncorretto-21-debian AS maven_build
LABEL maintainer=bitto.kazi@gmail.com
COPY ./spring-boot-oauth2-auth-server /tmp/app
COPY ./gateway /tmp/gateway
COPY ./info.json /tmp/info.json
WORKDIR /tmp/app
RUN mvn clean package
WORKDIR /tmp/gateway
RUN mvn clean package

FROM gradle:jdk21-corretto AS gradle_build
COPY ./auth-kit-app-frontend /tmp/auth-kit-app-frontend
COPY ./frontend-server /tmp/frontend-server

WORKDIR /tmp/auth-kit-app-frontend
RUN gradle buildFrontend --no-daemon

RUN mv /tmp/auth-kit-app-frontend/build/dist/js/productionExecutable/index.html /tmp/frontend-server/src/main/resources/templates/cpanel.hbs
RUN cp -r /tmp/auth-kit-app-frontend/build/dist/js/productionExecutable/static /tmp/frontend-server/src/main/resources

WORKDIR /tmp/frontend-server
RUN gradle clean buildFatJar --no-daemon

FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY --from=maven_build /tmp/app/target/spring-boot-oauth2-auth-server-1.0-SNAPSHOT.jar /app/app.jar
COPY --from=maven_build /tmp/gateway/target/app-gateway-1.0-SNAPSHOT.jar /app/gateway.jar
COPY --from=maven_build /tmp/info.json /app/info.json
COPY --from=gradle_build /tmp/frontend-server/build/libs/*.jar /app/frontend-server.jar

WORKDIR /app
COPY ./entrypoint.sh /app/entrypoint.sh

EXPOSE 5020
CMD ./entrypoint.sh
