
#FROM tomcat:8.5-jre8-alpine
FROM openjdk:8-alpine

RUN mkdir -p /data/cas/config /app

ARG ARTIFACT_URL=https://nexus.ala.org.au/service/local/repositories/releases/content/au/org/ala/cas/5.3.12-1/cas-5.3.12-1-exec.war
ARG WAR_NAME=cas-exec.war

RUN apk add --update curl zip tini && \
    wget $ARTIFACT_URL -q -O /app/$WAR_NAME

COPY ./config/* /data/cas/config/
#COPY ./tomcat-conf/* /usr/local/tomcat/conf/
#COPY ./web.new.xml /usr/local/tomcat/webapps/cas/WEB-INF/web.xml

EXPOSE 9000

# NON-ROOT
#RUN addgroup -g 101 tomcat && \
#    adduser -G tomcat -u 101 -S tomcat && \
#    chown -R tomcat:tomcat /app /data

#USER tomcat

#ENV JAVA_OPTS '-Dport.shutdown=8005 -Dport.http=8080'

ENTRYPOINT ["tini", "--"]
# java -Dcas.standalone.configurationDirectory=/data/cas/config -Dala.password.properties=/data/cas/config/pwe.properties -jar /app/cas-exec.war
CMD ["java","-Dcas.standalone.configurationDirectory=/data/cas/config","-Dala.password.properties=/data/cas/config/pwe.properties", "-jar", "/app/cas-exec.war"]
