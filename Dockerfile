FROM amazoncorretto:17 as temp_build_image
ENV APP_HOME=/mbot
WORKDIR $APP_HOME
COPY build.gradle settings.gradle gradlew $APP_HOME/
COPY gradle $APP_HOME/gradle
RUN ./gradlew bootJar 2>/dev/null || true
COPY . .
RUN ./gradlew bootJar

FROM amazoncorretto:17
ENV ARTIFACT_NAME=mbot-1.0-SNAPSHOT.jar
ENV APP_HOME=/mbot
WORKDIR $APP_HOME
COPY --from=temp_build_image $APP_HOME/build/libs/$ARTIFACT_NAME $APP_HOME/build/libs/$ARTIFACT_NAME
ENV TZ="America/Denver"
ENV PROD="true"

#CMD exec /bin/bash -c "trap : TERM INT; sleep infinity & wait"

CMD java -jar /mbot/build/libs/mbot-1.0-SNAPSHOT.jar
