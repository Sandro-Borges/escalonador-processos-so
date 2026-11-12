FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY src/ ./src/

RUN javac src/*.java -d out

CMD ["java", "-cp", "out", "Escalonador"]
