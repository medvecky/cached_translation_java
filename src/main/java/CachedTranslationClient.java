import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CachedTranslationClient {
    private static final Logger logger = Logger.getLogger(CachedTranslationServer.class.getName());

    private final ManagedChannel channel;
    private final CachedTranslationGrpc.CachedTranslationBlockingStub blockingStub;

    public CachedTranslationClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
                // needing certificates.
                .usePlaintext()
                .build());
    }

    CachedTranslationClient(ManagedChannel channel) {
        this.channel = channel;
        blockingStub = CachedTranslationGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }


    public void getTranslations() {
        logger.info("Will try get info from server");
        CachedTranslationOuterClass.TranslationRequest request = CachedTranslationOuterClass
                .TranslationRequest
                .newBuilder()
                .setSourceLanguage("Source from request")
                .setTargetLanguage("Target request ")
                .addAllTexts(Arrays.asList("Text from request"))
                .build();

        CachedTranslationOuterClass.TranslationReply response;
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
        CachedTranslationClient client = new CachedTranslationClient("localhost", 50051);
        try {

            client.getTranslations();
        } finally {
            client.shutdown();
        }
    }

}
