import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class CachedTranslationsServer {
    private static final Logger logger = Logger.getLogger(CachedTranslationsServer.class.getName());

    private Server server;

    private void start() throws IOException {
        /* The port on which the server should run */
        int port = 50051;
        server = ServerBuilder.forPort(port)
                .addService(new CachedTranslationImpl())
                .build()
                .start();
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                CachedTranslationsServer.this.stop();
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }


    static class CachedTranslationImpl extends CachedTranslationGrpc.CachedTranslationImplBase {

        @Override
        public void getTranslations(
                CachedTranslations.TranslationRequest request,
                StreamObserver<CachedTranslations.TranslationReply> responseObserver) {

            CloudTranslationsService cloudTranslationsService = new CloudTranslationsService();

            List<CachedTranslations.Translation> translations = new ArrayList<>();

            if (request.getSourceLanguage() != "") {
                translations = cloudTranslationsService.translate(
                        request.getTextsList(),
                        request.getTargetLanguage(),
                        request.getSourceLanguage());

            } else {
                translations = cloudTranslationsService.translate(
                        request.getTextsList(),
                        request.getTargetLanguage());
            }


            CachedTranslations.TranslationReply reply =
                    CachedTranslations
                            .TranslationReply
                            .newBuilder()
                            .addAllTranslations(translations)
                            .build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final CachedTranslationsServer server = new CachedTranslationsServer();
        server.start();
        server.blockUntilShutdown();
    }
}
