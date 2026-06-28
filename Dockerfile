FROM eclipse-temurin:25-jdk AS build

WORKDIR /app
COPY src ./src
RUN mkdir -p build && javac -d build $(find src -name "*.java")

FROM eclipse-temurin:25-jre

WORKDIR /app
COPY --from=build /app/build ./build
COPY index.html styles.css app.js ./
COPY assets ./assets

ENV HOST=0.0.0.0
ENV PORT=10000
EXPOSE 10000

CMD ["java", "-cp", "build", "guesswho.GameServer"]
