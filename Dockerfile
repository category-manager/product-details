FROM amazoncorretto:11-alpine-jdk
WORKDIR /app
ARG USERNAME
ARG TOKEN
ENV USERNAME=${USERNAME}
ENV TOKEN=${TOKEN}
ADD ./  /app/
RUN ./gradlew assemble
COPY build/libs/product-details-0.0.1-SNAPSHOT.jar product-details.jar
EXPOSE 8090
EXPOSE 5432
ENTRYPOINT ["java", "-jar","product-details.jar"]