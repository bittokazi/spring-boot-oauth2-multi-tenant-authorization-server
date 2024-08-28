FROM maven:3.8.5-openjdk-17 AS MAVEN_TOOL_CHAIN
LABEL maintainer=bitto.kazi@gmail.com
COPY ./spring-boot-oauth2-auth-server /tmp/app
COPY ./gateway /tmp/gateway
WORKDIR /tmp/app
RUN mvn clean package
WORKDIR /tmp/gateway
RUN mvn clean package

FROM timbru31/java-node
WORKDIR /app
COPY --from=MAVEN_TOOL_CHAIN /tmp/app/target/spring-boot-oauth2-auth-server-1.0-SNAPSHOT.jar /app/app.jar
COPY --from=MAVEN_TOOL_CHAIN /tmp/gateway/target/app-gateway-1.0-SNAPSHOT.jar /app/gateway.jar

#FROM node:14
WORKDIR /app/frontend
COPY ./auth-kit-frontend/package.json /app/frontend
COPY ./auth-kit-frontend/package-lock.json /app/frontend
COPY ./auth-kit-frontend /app/frontend
COPY ./info.json /app/
RUN npm install -g @angular/cli
RUN npm install
RUN npm run build


WORKDIR /app
COPY ./entrypoint.sh /app/entrypoint.sh

EXPOSE 5020
CMD ./entrypoint.sh
