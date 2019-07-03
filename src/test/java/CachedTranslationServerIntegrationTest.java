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
    public void testCloudTranslationsResultBuilderWithSource() {

        List<String> texts = Arrays.asList("Text for translation");

        CachedTranslationOuterClass.TranslationRequest request = CachedTranslationOuterClass
                .TranslationRequest
                .newBuilder()
                .addAllTexts(texts)
                .setSourceLanguage("en")
                .setTargetLanguage("ru")
                .build();

        CachedTranslationOuterClass.Translation translation = CachedTranslationOuterClass
                .Translation
                .newBuilder()
                .setInput("Text for translation")
                .setTranslatedText("Translated text")
                .build();
        List<CachedTranslationOuterClass.Translation> translationList = new ArrayList<>();
        translationList.add(translation);
        Map<String, TranslationData> translationMap =
                server.cloudTranslationsResultBuilder(translationList, request);
        assertTrue(translationMap.containsKey("Text for translation"));
        assertThat(translationMap.get("Text for translation").getTranslatedText(), is("Translated text"));
        assertThat(translationMap.get("Text for translation").getDetectedSourceLanguage(), is(""));

    }

    @Test
    public void testCloudTranslationsResultBuilderWithSourceMultiString() {

        List<String> texts = Arrays.asList("Text for translation 1", "Text for translation 2");

        CachedTranslationOuterClass.TranslationRequest request = CachedTranslationOuterClass
                .TranslationRequest
                .newBuilder()
                .addAllTexts(texts)
                .setSourceLanguage("en")
                .setTargetLanguage("ru")
                .build();

        CachedTranslationOuterClass.Translation translation1 = CachedTranslationOuterClass
                .Translation
                .newBuilder()
                .setInput("Text for translation 1")
                .setTranslatedText("Translated text 1")
                .build();

        CachedTranslationOuterClass.Translation translation2 = CachedTranslationOuterClass
                .Translation
                .newBuilder()
                .setInput("Text for translation 2")
                .setTranslatedText("Translated text 2")
                .build();
        List<CachedTranslationOuterClass.Translation> translationList = new ArrayList<>();
        translationList.add(translation1);
        translationList.add(translation2);
        Map<String, TranslationData> translationMap =
                server.cloudTranslationsResultBuilder(translationList, request);
        assertThat(translationMap.size(), is(2));
        assertTrue(translationMap.containsKey("Text for translation 1"));
        assertThat(translationMap.get("Text for translation 1").getTranslatedText(), is("Translated text 1"));
        assertThat(translationMap.get("Text for translation 1").getDetectedSourceLanguage(), is(""));
        assertTrue(translationMap.containsKey("Text for translation 2"));
        assertThat(translationMap.get("Text for translation 2").getTranslatedText(), is("Translated text 2"));
        assertThat(translationMap.get("Text for translation 2").getDetectedSourceLanguage(), is(""));

    }


    @Test
    public void testCloudTranslationsResultBuilderWithoutSource() {

        List<String> texts = Arrays.asList("Text for translation");

        CachedTranslationOuterClass.TranslationRequest request = CachedTranslationOuterClass
                .TranslationRequest
                .newBuilder()
                .addAllTexts(texts)
                .setTargetLanguage("ru")
                .build();

        CachedTranslationOuterClass.Translation translation = CachedTranslationOuterClass
                .Translation
                .newBuilder()
                .setInput("Text for translation")
                .setTranslatedText("Translated text")
                .setDetectedSourceLanguage("en")
                .build();
        List<CachedTranslationOuterClass.Translation> translationList = new ArrayList<>();
        translationList.add(translation);
        Map<String, TranslationData> translationMap =
                server.cloudTranslationsResultBuilder(translationList, request);
        assertTrue(translationMap.containsKey("Text for translation"));
        assertThat(translationMap.get("Text for translation").getTranslatedText(), is("Translated text"));
        assertThat(translationMap.get("Text for translation").getDetectedSourceLanguage(), is("en"));

    }

    @Test
    public void testCloudTranslationsResultBuilderWithoutSourceMultiString() {

        List<String> texts = Arrays.asList("Text for translation 1", "Text for translation 2");

        CachedTranslationOuterClass.TranslationRequest request = CachedTranslationOuterClass
                .TranslationRequest
                .newBuilder()
                .addAllTexts(texts)
                .setTargetLanguage("ru")
                .build();

        CachedTranslationOuterClass.Translation translation1 = CachedTranslationOuterClass
                .Translation
                .newBuilder()
                .setInput("Text for translation 1")
                .setTranslatedText("Translated text 1")
                .setDetectedSourceLanguage("en")
                .build();

        CachedTranslationOuterClass.Translation translation2 = CachedTranslationOuterClass
                .Translation
                .newBuilder()
                .setInput("Text for translation 2")
                .setTranslatedText("Translated text 2")
                .setDetectedSourceLanguage("lv")
                .build();
        List<CachedTranslationOuterClass.Translation> translationList = new ArrayList<>();
        translationList.add(translation1);
        translationList.add(translation2);
        Map<String, TranslationData> translationMap =
                server.cloudTranslationsResultBuilder(translationList, request);
        assertThat(translationMap.size(), is(2));
        assertTrue(translationMap.containsKey("Text for translation 1"));
        assertThat(translationMap.get("Text for translation 1").getTranslatedText(), is("Translated text 1"));
        assertThat(translationMap.get("Text for translation 1").getDetectedSourceLanguage(), is("en"));
        assertTrue(translationMap.containsKey("Text for translation 2"));
        assertThat(translationMap.get("Text for translation 2").getTranslatedText(), is("Translated text 2"));
        assertThat(translationMap.get("Text for translation 2").getDetectedSourceLanguage(), is("lv"));

    }

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