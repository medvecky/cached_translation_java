import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class CachedTranslationServer {
    private static final Logger logger = Logger.getLogger(CachedTranslationServer.class.getName());
//    private static final CloudTranslationService cloudTranslationService = new CloudTranslationService();
//    private static final RedisCache redisCache = new RedisCache();

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
        private static CloudTranslationService cloudTranslationService;
        private static RedisCache redisCache;

        public CachedTranslationImpl() {
            cloudTranslationService = new CloudTranslationService();
            redisCache = new RedisCache();
        }

        public CachedTranslationImpl(
                CloudTranslationService cloudTranslationService,
                RedisCache redisCache) {
            this.cloudTranslationService = cloudTranslationService;
            this.redisCache = redisCache;
        }

        @Override
        public void getTranslations(
                CachedTranslationOuterClass.TranslationRequest request,
                StreamObserver<CachedTranslationOuterClass.TranslationReply> responseObserver) {

            CacheCheckResult cacheCheckResult = redisCache.getFromCache(
                    request.getTextsList(),
                    request.getSourceLanguage(),
                    request.getTargetLanguage());

            Map<String, TranslationData> cloudTranslations = getCloudTranslationAndSaveToCache(
                    request,
                    cacheCheckResult.getNotTranslatedTexts());

//            logger.info("Data from cache " + cacheCheckResult.getCachedTranslations().keySet().toString());
//            logger.info("Data from cloud " + cloudTranslations.keySet().toString());

            List<CachedTranslationOuterClass.Translation> resultTranslations =
                    mergeTranslations(request, cacheCheckResult.getCachedTranslations(), cloudTranslations);


            CachedTranslationOuterClass.TranslationReply reply =
                    CachedTranslationOuterClass
                            .TranslationReply
                            .newBuilder()
                            .addAllTranslations(resultTranslations)
                            .build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        public List<CachedTranslationOuterClass.Translation> mergeTranslations(
                CachedTranslationOuterClass.TranslationRequest request,
                Map<String, TranslationData> cachedTranslations,
                Map<String, TranslationData> cloudTranslations) {
            List<CachedTranslationOuterClass.Translation> resultTranslations = new ArrayList<>();

            for (String text : request.getTextsList()) {
                if (cachedTranslations.containsKey(text)) {
                    resultTranslations.add(getTranslation(cachedTranslations, text));
                    continue;
                }

                if (cloudTranslations.containsKey(text)) {
                    resultTranslations.add(getTranslation(cloudTranslations, text));
                }
            }

            return resultTranslations;
        }

        public CachedTranslationOuterClass.Translation getTranslation(
                Map<String, TranslationData> translations,
                String text) {

            CachedTranslationOuterClass.Translation translation;

            TranslationData translationData = translations.get(text);
            translation =
                    CachedTranslationOuterClass
                            .Translation.newBuilder()
                            .setInput(text)
                            .setDetectedSourceLanguage(translationData.getDetectedSourceLanguage())
                            .setTranslatedText(translationData.getTranslatedText())
                            .build();


            return translation;
        }


        public Map<String, TranslationData> getCloudTranslationAndSaveToCache(
                CachedTranslationOuterClass.TranslationRequest request,
                List<String> notTranslatedText) {

            Map<String, TranslationData> cloudTranslations;

            CachedTranslationOuterClass.TranslationRequest translationRequest =
                    CachedTranslationOuterClass
                            .TranslationRequest
                            .newBuilder()
                            .addAllTexts(notTranslatedText)
                            .setSourceLanguage(request.getSourceLanguage())
                            .setTargetLanguage(request.getTargetLanguage())
                            .build();
            if (translationRequest.getTextsList().size() > 0) {
                cloudTranslations = handleRequestToCloud(translationRequest);
            } else {
                cloudTranslations = new HashMap<>();
            }

            return cloudTranslations;
        }

        public Map<String, TranslationData> handleRequestToCloud(
                CachedTranslationOuterClass.TranslationRequest request) {


            List<CachedTranslationOuterClass.Translation> cloudResponse;

            if (!request.getSourceLanguage().equals("")) {
                cloudResponse = cloudTranslationService.translate(
                        request.getTextsList(),
                        request.getTargetLanguage(),
                        request.getSourceLanguage());
            } else {
                cloudResponse = cloudTranslationService.translate(
                        request.getTextsList(),
                        request.getTargetLanguage());
            }

            redisCache.saveToCache(cloudResponse, request.getSourceLanguage(), request.getTargetLanguage());

            return cloudTranslationsResultBuilder(cloudResponse, request);
        }

        public Map<String, TranslationData> cloudTranslationsResultBuilder(
                List<CachedTranslationOuterClass.Translation> cloudResponse,
                CachedTranslationOuterClass.TranslationRequest request) {

            Map<String, TranslationData> cloudTranslations = new HashMap<>();

            for (CachedTranslationOuterClass.Translation translation : cloudResponse) {
                if (!request.getSourceLanguage().equals("")) {
                    cloudTranslations.put(
                            translation.getInput(),
                            new TranslationData(
                                    translation.getTranslatedText(),
                                    ""));
                } else {
                    cloudTranslations.put(
                            translation.getInput(),
                            new TranslationData(
                                    translation.getTranslatedText(),
                                    translation.getDetectedSourceLanguage()));
                }
            }

            return cloudTranslations;
        }

    }


    public static void main(String[] args) throws IOException, InterruptedException {
        final CachedTranslationServer server = new CachedTranslationServer();
        server.start();
        server.blockUntilShutdown();
    }
}
