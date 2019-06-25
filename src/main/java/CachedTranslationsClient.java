import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CachedTranslationsClient {
    private static final Logger logger = Logger.getLogger(CachedTranslationsServer.class.getName());

    private final ManagedChannel channel;
    private final CachedTranslationGrpc.CachedTranslationBlockingStub blockingStub;

    public CachedTranslationsClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
                // needing certificates.
                .usePlaintext()
                .build());
    }

    CachedTranslationsClient(ManagedChannel channel) {
        this.channel = channel;
        blockingStub = CachedTranslationGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }


    public void getTranslations() {
        logger.info("Will try get info from server");
        CachedTranslations.TranslationRequest request = CachedTranslations
                .TranslationRequest
                .newBuilder()
                .setSourceLanguage("Source from request")
                .setTargetLanguage("Target request ")
                .addAllTexts(Arrays.asList("Text from request"))
                .build();

        CachedTranslations.TranslationReply response;
        try {
            response = blockingStub.getTranslations(request);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
        logger.info("Greeting: " + response.getTranslations(0).getTranslatedText());
        logger.info("Greeting: " + response.getTranslations(0).getInput());
        logger.info("Greeting: " + response.getTranslations(0).getDetectedSourceLanguage());
    }

    public static void main(String[] args) throws Exception {
        CachedTranslationsClient client = new CachedTranslationsClient("localhost", 50051);
        try {

            client.getTranslations();
        } finally {
            client.shutdown();
        }
    }

}
