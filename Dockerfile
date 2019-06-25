FROM java:9

RUN mkdir -p /grpc-service
COPY build/install/cached_translation  /grpc-service
WORKDIR /grpc-service
RUN apt-get -y update && \
    apt-get install -y --no-install-recommends wget unzip

CMD ["./bin/cached-translations-server"]