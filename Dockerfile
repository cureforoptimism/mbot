FROM amazoncorretto:17

ENV TZ="America/Denver"
ADD . /mbot
WORKDIR mbot
RUN ./gradlew bootJar

CMD java -jar /mbot/build/libs/mbot-1.0-SNAPSHOT.jar
