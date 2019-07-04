import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.KeyValue;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class CachedTranslationServerIntegrationTest {
    CachedTranslationServer.CachedTranslationImpl server = new CachedTranslationServer.CachedTranslationImpl();
    private RedisClient redisClient;
    private RedisCommands<String, String> syncCommands;
    private StatefulRedisConnection<String, String> connection;
    private RedisCache redisCache;

    @Test
    public void handleRequestToCloudWithSource() {
        List<String> texts = Arrays.asList("Dog");
        String[] keys = new String[]{"Dog"};
        CachedTranslationOuterClass.TranslationRequest request = CachedTranslationOuterClass
                .TranslationRequest
                .newBuilder()
                .addAllTexts(texts)
                .setTargetLanguage("ru")
                .setSourceLanguage("en")
                .build();
        Map<String, TranslationData> responseFromCloud = server.handleRequestToCloud(request);
        List<KeyValue<String, String>> cachedDataList = syncCommands.hmget("en:ru", keys);
        assertThat(responseFromCloud.size(), is(1));
        assertTrue(responseFromCloud.containsKey("Dog"));
        assertThat(responseFromCloud.get("Dog").getTranslatedText(), is("Собака"));
        assertThat(responseFromCloud.get("Dog").getDetectedSourceLanguage(), is(""));
        assertThat(cachedDataList.size(), is(1));
        KeyValue<String, String> cachedData = cachedDataList.get(0);
        assertThat(cachedData.getKey(), is("Dog"));
        assertThat(cachedData.getValue(), is("Собака"));
    }

    @Test
    public void handleRequestToCloudWithSourceMultiString() {
        List<String> texts = Arrays.asList("Dog", "Cat");
        String[] keys = new String[]{"Dog", "Cat"};
        CachedTranslationOuterClass.TranslationRequest request = CachedTranslationOuterClass
                .TranslationRequest
                .newBuilder()
                .addAllTexts(texts)
                .setTargetLanguage("ru")
                .setSourceLanguage("en")
                .build();
        Map<String, TranslationData> responseFromCloud = server.handleRequestToCloud(request);
        List<KeyValue<String, String>> cachedDataList = syncCommands.hmget("en:ru", keys);
        assertThat(responseFromCloud.size(), is(2));
        assertTrue(responseFromCloud.containsKey("Dog"));
        assertTrue(responseFromCloud.containsKey("Cat"));
        assertThat(responseFromCloud.get("Dog").getTranslatedText(), is("Собака"));
        assertThat(responseFromCloud.get("Dog").getDetectedSourceLanguage(), is(""));
        assertThat(responseFromCloud.get("Cat").getTranslatedText(), is("Кошка"));
        assertThat(responseFromCloud.get("Cat").getDetectedSourceLanguage(), is(""));
        assertThat(cachedDataList.size(), is(2));
        KeyValue<String, String> cachedData1 = cachedDataList.get(0);
        KeyValue<String, String> cachedData2 = cachedDataList.get(1);
        assertThat(cachedData1.getKey(), is("Dog"));
        assertThat(cachedData1.getValue(), is("Собака"));
        assertThat(cachedData2.getKey(), is("Cat"));
        assertThat(cachedData2.getValue(), is("Кошка"));
    }


    @Test
    public void handleRequestToCloudWithoutSource() {
        List<String> texts = Arrays.asList("Dog");
        String[] keys = new String[]{"Dog"};
        CachedTranslationOuterClass.TranslationRequest request = CachedTranslationOuterClass
                .TranslationRequest
                .newBuilder()
                .addAllTexts(texts)
                .setTargetLanguage("ru")
                .build();
        Map<String, TranslationData> responseFromCloud = server.handleRequestToCloud(request);
        List<KeyValue<String, String>> cachedDataList = syncCommands.hmget("auto:ru", keys);
        assertThat(responseFromCloud.size(), is(1));
        assertTrue(responseFromCloud.containsKey("Dog"));
        assertThat(responseFromCloud.get("Dog").getTranslatedText(), is("Собака"));
        assertThat(responseFromCloud.get("Dog").getDetectedSourceLanguage(), is("en"));
        assertThat(cachedDataList.size(), is(1));
        KeyValue<String, String> cachedData = cachedDataList.get(0);
        assertThat(cachedData.getKey(), is("Dog"));

        ObjectMapper mapper = new ObjectMapper();
        String jsonString = cachedData.getValue();

        try {
            TranslationData translationData = mapper.readValue(jsonString, TranslationData.class);
            assertThat(translationData.getTranslatedText(), is("Собака"));
            assertThat(translationData.getDetectedSourceLanguage(), is("en"));
        } catch (IOException e) {
            e.printStackTrace();
            assert (false);
        }

    }


    @Test
    public void handleRequestToCloudWithoutSourceMultiString() {
        List<String> texts = Arrays.asList("Dog", "Cat");
        String[] keys = new String[]{"Dog", "Cat"};
        CachedTranslationOuterClass.TranslationRequest request = CachedTranslationOuterClass
                .TranslationRequest
                .newBuilder()
                .addAllTexts(texts)
                .setTargetLanguage("ru")
                .build();
        Map<String, TranslationData> responseFromCloud = server.handleRequestToCloud(request);
        List<KeyValue<String, String>> cachedDataList = syncCommands.hmget("auto:ru", keys);
        assertThat(responseFromCloud.size(), is(2));
        assertTrue(responseFromCloud.containsKey("Dog"));
        assertTrue(responseFromCloud.containsKey("Cat"));
        assertThat(responseFromCloud.get("Dog").getTranslatedText(), is("Собака"));
        assertThat(responseFromCloud.get("Dog").getDetectedSourceLanguage(), is("en"));
        assertThat(responseFromCloud.get("Cat").getTranslatedText(), is("Кошка"));
        assertThat(responseFromCloud.get("Cat").getDetectedSourceLanguage(), is("en"));
        assertThat(cachedDataList.size(), is(2));
        KeyValue<String, String> cachedData1 = cachedDataList.get(0);
        KeyValue<String, String> cachedData2 = cachedDataList.get(1);
        assertThat(cachedData1.getKey(), is("Dog"));
        assertThat(cachedData2.getKey(), is("Cat"));

        ObjectMapper mapper = new ObjectMapper();
        String jsonString1 = cachedData1.getValue();
        String jsonString2 = cachedData2.getValue();

        try {
            TranslationData translationData1 = mapper.readValue(jsonString1, TranslationData.class);
            TranslationData translationData2 = mapper.readValue(jsonString2, TranslationData.class);
            assertThat(translationData1.getTranslatedText(), is("Собака"));
            assertThat(translationData1.getDetectedSourceLanguage(), is("en"));
            assertThat(translationData2.getTranslatedText(), is("Кошка"));
            assertThat(translationData2.getDetectedSourceLanguage(), is("en"));
        } catch (IOException e) {
            e.printStackTrace();
            assert (false);
        }

    }


    @Before
    public void setUp() throws Exception {

        redisClient = RedisClient.create("redis://localhost:6379");
        connection = redisClient.connect();
        syncCommands = connection.sync();

        redisCache = new RedisCache();
    }

    @After
    public void tearDown() throws Exception {
        syncCommands.flushall();
        syncCommands.flushdb();
    }
}