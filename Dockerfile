FROM openjdk:8-jdk


#install gradle
RUN wget https://downloads.gradle-dn.com/distributions/gradle-6.8.3-bin.zip
RUN unzip gradle-6.8.3-bin.zip
ENV GRADLE_HOME /gradle-6.8.3
ENV PATH $PATH:/gradle-6.8.3/bin
#compile and run app
EXPOSE 8080
WORKDIR tomato
RUN gradle clean build --rerun-tasks --no-build-cache
ENTRYPOINT ["java", "-jar", "/tomato-pay.jar"]
