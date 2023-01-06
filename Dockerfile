FROM amazoncorretto:16-alpine

WORKDIR /app

COPY target/pxls-*.jar .
RUN mkdir -p resources
RUN mkdir -p maps
RUN mkdir -p backups
#COPY resources/roles-reference.conf resources/roles-reference.conf
#COPY resources/reference.conf resources/reference.conf
#COPY resources/palette-reference.conf resources/palette-reference.conf

CMD ["java", "-jar", "pxls-1.0-SNAPSHOT.jar"]