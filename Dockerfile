FROM amazoncorretto:17

ADD . /mbot
WORKDIR mbot
RUN ./gradlew bootjar

ENTRYPOINT java -jar build/libs/mbot-0.0.1-SNAPSHOT.jar
