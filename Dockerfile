FROM openjdk:11

ENV VEHICLES_API=""

ENV EXTRA_ARGUMENTS=""

ENV ROUTER_NAME=""

ENV MEMORY_IN_MB=5000

ENV PORT=8000

ENV SECURE_PORT=8001

COPY ./target/otp-1.5.0-SNAPSHOT-shaded.jar /otp.jar

CMD java -Xmx"$MEMORY_IN_MB"M -jar /otp.jar --basePath /otp --insecure --inMemory --sharedVehiclesApi $VEHICLES_API --port $PORT --securePort $SECURE_PORT --router $ROUTER_NAME $EXTRA_ARGUMENTS
