FROM gradle:4.7.0-jdk8-alpine AS build


#install gradle
RUN wget https://downloads.gradle-dn.com/distributions/gradle-6.8.3-bin.zip
RUN unzip gradle-6.8.3-bin.zip
ENV GRADLE_HOME /gradle-6.8.3
ENV PATH $PATH:/gradle-6.8.3/bin

#compile and run app
COPY --chown=gradle:gradle . /home/gradle/src

WORKDIR /home/gradle/src
RUN gradle build --no-daemon

FROM openjdk:8-jre-slim

EXPOSE 8080

RUN mkdir /app

COPY --from=build /home/gradle/src/build/libs/*.jar /app/spring-boot-application.jar

ENTRYPOINT ["java", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-Djava.security.egd=file:/dev/./urandom","-jar","/app/spring-boot-application.jar"]
