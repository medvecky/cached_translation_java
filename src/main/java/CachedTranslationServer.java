import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class CachedTranslationServer {
    private static final Logger logger = Logger.getLogger(CachedTranslationServer.class.getName());
    private static final CloudTranslationService cloudTranslationService = new CloudTranslationService() ;

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
                CachedTranslationServer.this.stop();
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
                CachedTranslationOuterClass.TranslationRequest request,
                StreamObserver<CachedTranslationOuterClass.TranslationReply> responseObserver) {



            List<CachedTranslationOuterClass.Translation> translations = new ArrayList<>();

            if (!request.getSourceLanguage().equals("")) {
                translations = cloudTranslationService.translate(
                        request.getTextsList(),
                        request.getTargetLanguage(),
                        request.getSourceLanguage());

            } else {
                translations = cloudTranslationService.translate(
                        request.getTextsList(),
                        request.getTargetLanguage());
            }


            CachedTranslationOuterClass.TranslationReply reply =
                    CachedTranslationOuterClass
                            .TranslationReply
                            .newBuilder()
                            .addAllTranslations(translations)
                            .build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final CachedTranslationServer server = new CachedTranslationServer();
        server.start();
        server.blockUntilShutdown();
    }
}
