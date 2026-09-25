FROM eclipse-temurin:25-jre-alpine
VOLUME /tmp
ARG JAR_FILE
ADD ${JAR_FILE} virtual-accounts.jar
CMD java $JVM_OPTS -Djava.security.egd=file:/dev/./urandom -jar /virtual-accounts.jar
COPY ./execute.sh ./execute.sh
RUN chmod 555 /execute.sh
ENTRYPOINT ["./execute.sh"]