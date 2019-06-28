FROM java:9

RUN mkdir -p /root/grpc-service
COPY build/install/cached_translation  /root/grpc-service
COPY cached_translations-f927008f4f77.json  /root/grpc-service
WORKDIR /root/grpc-service
ENV JAVA_CONF_DIR=$JAVA_HOME/conf

RUN bash -c '([[ ! -d $JAVA_SECURITY_DIR ]] && ln -s $JAVA_HOME/lib $JAVA_HOME/conf) || (echo "Found java conf dir, package has been fixed, remove this hack"; exit -1)'

ENV GOOGLE_APPLICATION_CREDENTIALS=/root/grpc-service/cached_translations-f927008f4f77.json
ENV ENV=docker

CMD ["./bin/cached-translation-server"]