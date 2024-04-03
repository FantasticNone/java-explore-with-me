FROM amazoncorretto:11
COPY /stats-server/target/*.jar stats.jar
ENTRYPOINT ["java","-jar","/stats.jar"]