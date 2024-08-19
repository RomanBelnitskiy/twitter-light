# Twitter Light Web App

This is a Spring Boot web application that can be run locally or in a Dockerized environment. It uses MongoDB for data storage and RabbitMQ for message brokering.

## Prerequisites

- Java 17
- Maven 3.6+
- Docker & Docker Compose

## Profiles

The application has two profiles:
1. **local**: For running the application locally.
2. **docker**: For running the application in a Docker container.

## Running Locally

1. Clone the repository:
    ```bash
    git clone https://github.com/RomanBelnitskiy/twitter-light.git
    cd twitter-light
    ```

2. Build the application:
    ```bash
    mvn clean package
    ```

3. Run the application with the `local` profile:
    ```bash
    mvn spring-boot:run -Dspring-boot.run.profiles=local
    ```

The application will be accessible at `http://localhost:8080`.

## Running with Docker

1. Build the Docker image:
    ```bash
    docker-compose build
    ```

2. Start the containers:
    ```bash
    docker-compose up
    ```

This will start the following services:
- `twitter-web-app` on `http://localhost:8080`
- `mongodb` on `localhost:27017`
- `mongo-express` (MongoDB GUI) on `http://localhost:8081`
- `rabbitmq` on `http://localhost:15672` (username: `user`, password: `password`)

3. Stop the containers:
    ```bash
    docker-compose down
    ```

## Docker Compose Details

The `docker-compose.yaml` file defines the following services:

- **twitter-web-app**: The Spring Boot application running with the `docker` profile.
- **mongodb**: MongoDB instance for data storage.
- **mongo-express**: Web-based MongoDB admin interface.
- **rabbitmq**: Message broker for handling queues.

## Dockerfile

The `Dockerfile` is used to build the Docker image for the Spring Boot application.

```dockerfile
FROM openjdk:17-jdk-alpine
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
