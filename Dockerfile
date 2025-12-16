FROM openjdk:17-jdk-slim as build

WORKDIR /app

COPY mvnw .
COPY stock-sage/.mvn .mvn
COPY pom.xml .

RUN chmod +x ./mvnw
RUN ./mvnw dependency:go-offline -B

COPY stock-sage/src src

RUN ./mvnw package -DskipTests
RUN mkdir -p target/dependency 

FROM openjdk:17-jdk-slim

ARG DEPENDENCY=/app/target/dependency

COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

ENTRYPOINT ["java", "-cp", "app:app/lib/*", "com.portfolio.stocksage.StockSageApplication"]
