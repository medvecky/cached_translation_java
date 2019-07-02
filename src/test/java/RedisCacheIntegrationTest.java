import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class RedisCacheIntegrationTest {

    private RedisClient redisClient;
    private RedisCommands<String, String> syncCommands;
    private StatefulRedisConnection<String, String> connection;
    private RedisCache redisCache;


    @Test
    public void saveToCacheWithSourceSingleString() {
        List<CachedTranslationOuterClass.Translation> translations = new ArrayList<>();
        CachedTranslationOuterClass.Translation translation =
                CachedTranslationOuterClass
                        .Translation
                        .newBuilder()
                        .setTranslatedText("Translated text")
                        .setInput("Text for translation")
                        .build();
        translations.add(translation);

        String[] keys = new String[]{"Text for translation"};

        redisCache.saveToCache(translations, "en", "ru");
        List<KeyValue<String, String>> cachedDataList = syncCommands.hmget("en:ru", keys);
        assertThat(cachedDataList.size(), is(1));
        KeyValue<String, String> cachedData1 = cachedDataList.get(0);
        assertThat(cachedData1.getKey(), is("Text for translation"));
        assertThat(cachedData1.getValue(), is("Translated text"));
    }

    @Test
    public void saveToCacheWithSourceMultiString() {
        List<CachedTranslationOuterClass.Translation> translations = new ArrayList<>();
        CachedTranslationOuterClass.Translation translation1 =
                CachedTranslationOuterClass
                        .Translation
                        .newBuilder()
                        .setTranslatedText("Translated text 1")
                        .setInput("Text for translation 1")
                        .build();

        CachedTranslationOuterClass.Translation translation2 =
                CachedTranslationOuterClass
                        .Translation
                        .newBuilder()
                        .setTranslatedText("Translated text 2")
                        .setInput("Text for translation 2")
                        .build();
        translations.add(translation1);
        translations.add(translation2);

        String[] keys = new String[]{"Text for translation 1", "Text for translation 2"};

        redisCache.saveToCache(translations, "en", "ru");
        List<KeyValue<String, String>> cachedDataList = syncCommands.hmget("en:ru", keys);
        assertThat(cachedDataList.size(), is(2));
        KeyValue<String, String> cachedData1 = cachedDataList.get(0);
        KeyValue<String, String> cachedData2 = cachedDataList.get(1);
        assertThat(cachedData1.getKey(), is("Text for translation 1"));
        assertThat(cachedData1.getValue(), is("Translated text 1"));
        assertThat(cachedData2.getKey(), is("Text for translation 2"));
        assertThat(cachedData2.getValue(), is("Translated text 2"));
    }

    @Test
    public void saveToCacheWithoutSourceSingleString() {
        List<CachedTranslationOuterClass.Translation> translations = new ArrayList<>();
        CachedTranslationOuterClass.Translation translation =
                CachedTranslationOuterClass
                        .Translation
                        .newBuilder()
                        .setTranslatedText("Translated text")
                        .setInput("Text for translation")
                        .setDetectedSourceLanguage("en")
                        .build();
        translations.add(translation);

        String[] keys = new String[]{"Text for translation"};

        redisCache.saveToCache(translations, "", "ru");
        List<KeyValue<String, String>> cachedDataList = syncCommands.hmget("auto:ru", keys);
        assertThat(cachedDataList.size(), is(1));
        KeyValue<String, String> cachedData1 = cachedDataList.get(0);
        assertThat(cachedData1.getKey(), is("Text for translation"));

        ObjectMapper mapper = new ObjectMapper();
        String jsonString = cachedData1.getValue();
        try {
            TranslationData translationData = mapper.readValue(jsonString, TranslationData.class);
            assertThat(translationData.getTranslatedText(), is("Translated text"));
            assertThat(translationData.getDetectedSourceLanguage(), is("en"));
        } catch (IOException e) {
            e.printStackTrace();
            assert (false);
        }
    }


    @Test
    public void saveToCacheWithoutSourceMultiString() {
        List<CachedTranslationOuterClass.Translation> translations = new ArrayList<>();
        CachedTranslationOuterClass.Translation translation1 =
                CachedTranslationOuterClass
                        .Translation
                        .newBuilder()
                        .setTranslatedText("Translated text 1")
                        .setInput("Text for translation 1")
                        .setDetectedSourceLanguage("en")
                        .build();

        CachedTranslationOuterClass.Translation translation2 =
                CachedTranslationOuterClass
                        .Translation
                        .newBuilder()
                        .setTranslatedText("Translated text 2")
                        .setInput("Text for translation 2")
                        .setDetectedSourceLanguage("lv")
                        .build();
        translations.add(translation1);
        translations.add(translation2);

        String[] keys = new String[]{"Text for translation 1", "Text for translation 2"};

        redisCache.saveToCache(translations, "", "ru");
        List<KeyValue<String, String>> cachedDataList = syncCommands.hmget("auto:ru", keys);
        assertThat(cachedDataList.size(), is(2));
        KeyValue<String, String> cachedData1 = cachedDataList.get(0);
        KeyValue<String, String> cachedData2 = cachedDataList.get(1);
        assertThat(cachedData1.getKey(), is("Text for translation 1"));
        assertThat(cachedData2.getKey(), is("Text for translation 2"));

        ObjectMapper mapper = new ObjectMapper();
        String jsonString1 = cachedData1.getValue();
        String jsonString2 = cachedData2.getValue();
        try {
            TranslationData translationData1 = mapper.readValue(jsonString1, TranslationData.class);
            assertThat(translationData1.getTranslatedText(), is("Translated text 1"));
            assertThat(translationData1.getDetectedSourceLanguage(), is("en"));
            TranslationData translationData2 = mapper.readValue(jsonString2, TranslationData.class);
            assertThat(translationData2.getTranslatedText(), is("Translated text 2"));
            assertThat(translationData2.getDetectedSourceLanguage(), is("lv"));
        } catch (IOException e) {
            e.printStackTrace();
            assert (false);
        }
    }

    @Test
    public void getFromCacheWithSourceSingleString() {
        Map<String, String> setForCaching = new HashMap<>();
        setForCaching.put("Text for translation", "Translated text");
        syncCommands.hmset("en:ru", setForCaching);
        List<String> texts = new ArrayList<>();
        texts.add("Text for translation");

        CacheCheckResult resultFromCache = redisCache.getFromCache(texts, "en", "ru");
        assertThat(resultFromCache.getNotTranslatedTexts().size(), is(0));
        assertThat(resultFromCache.getCachedTranslations().size(), is(1));
        Map<String, TranslationData> cachedTranslations = resultFromCache.getCachedTranslations();
        assertThat(cachedTranslations.containsKey("Text for translation"), is(true));
        assertThat(cachedTranslations.get("Text for translation").getTranslatedText(), is("Translated text"));
        assertThat(cachedTranslations.get("Text for translation").getDetectedSourceLanguage(), is("en"));
    }

    @Test
    public void getFromCacheWithSourceMultiString() {
        Map<String, String> setForCaching = new HashMap<>();
        setForCaching.put("Text for translation 1", "Translated text 1");
        setForCaching.put("Text for translation 2", "Translated text 2");
        syncCommands.hmset("en:ru", setForCaching);
        List<String> texts = new ArrayList<>();
        texts.add("Text for translation 1");
        texts.add("Text for translation 2");

        CacheCheckResult resultFromCache = redisCache.getFromCache(texts, "en", "ru");
        assertThat(resultFromCache.getNotTranslatedTexts().size(), is(0));
        assertThat(resultFromCache.getCachedTranslations().size(), is(2));
        Map<String, TranslationData> cachedTranslations = resultFromCache.getCachedTranslations();
        assertThat(cachedTranslations.containsKey("Text for translation 1"), is(true));
        assertThat(cachedTranslations.containsKey("Text for translation 2"), is(true));
        assertThat(cachedTranslations.get("Text for translation 1").getTranslatedText(), is("Translated text 1"));
        assertThat(cachedTranslations.get("Text for translation 1").getDetectedSourceLanguage(), is("en"));
        assertThat(cachedTranslations.get("Text for translation 2").getTranslatedText(), is("Translated text 2"));
        assertThat(cachedTranslations.get("Text for translation 2").getDetectedSourceLanguage(), is("en"));
    }

    @Test
    public void getFromCacheWithoutSourceSingleString() {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> setForCaching = new HashMap<>();
        TranslationData translationData =
                new TranslationData(
                        "Translated text",
                        "en");
        try {
            String jsonInString = mapper.writeValueAsString(translationData);
            setForCaching.put("Text for translation", jsonInString);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        syncCommands.hmset("auto:ru", setForCaching);
        List<String> texts = new ArrayList<>();
        texts.add("Text for translation");

        CacheCheckResult resultFromCache = redisCache.getFromCache(texts, "", "ru");
        assertThat(resultFromCache.getNotTranslatedTexts().size(), is(0));
        assertThat(resultFromCache.getCachedTranslations().size(), is(1));
        Map<String, TranslationData> cachedTranslations = resultFromCache.getCachedTranslations();
        assertThat(cachedTranslations.containsKey("Text for translation"), is(true));
        assertThat(cachedTranslations.get("Text for translation").getTranslatedText(), is("Translated text"));
        assertThat(cachedTranslations.get("Text for translation").getDetectedSourceLanguage(), is("en"));
    }

    @Test
    public void getFromCacheWithoutSourceMultiString() {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> setForCaching = new HashMap<>();
        TranslationData translationData1 =
                new TranslationData(
                        "Translated text 1",
                        "en");

        TranslationData translationData2 =
                new TranslationData(
                        "Translated text 2",
                        "lv");
        try {
            String jsonInString1 = mapper.writeValueAsString(translationData1);
            String jsonInString2 = mapper.writeValueAsString(translationData2);
            setForCaching.put("Text for translation 1", jsonInString1);
            setForCaching.put("Text for translation 2", jsonInString2);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        syncCommands.hmset("auto:ru", setForCaching);
        List<String> texts = new ArrayList<>();
        texts.add("Text for translation 1");
        texts.add("Text for translation 2");

        CacheCheckResult resultFromCache = redisCache.getFromCache(texts, "", "ru");
        assertThat(resultFromCache.getNotTranslatedTexts().size(), is(0));
        assertThat(resultFromCache.getCachedTranslations().size(), is(2));
        Map<String, TranslationData> cachedTranslations = resultFromCache.getCachedTranslations();
        assertThat(cachedTranslations.containsKey("Text for translation 1"), is(true));
        assertThat(cachedTranslations.containsKey("Text for translation 2"), is(true));
        assertThat(cachedTranslations.get("Text for translation 1").getTranslatedText(), is("Translated text 1"));
        assertThat(cachedTranslations.get("Text for translation 1").getDetectedSourceLanguage(), is("en"));
        assertThat(cachedTranslations.get("Text for translation 2").getTranslatedText(), is("Translated text 2"));
        assertThat(cachedTranslations.get("Text for translation 2").getDetectedSourceLanguage(), is("lv"));
    }

    @Test
    public void getFromCacheNotTranslatedOnlySingleString() {
        List<String> texts = new ArrayList<>();
        texts.add("Not translated text");

        CacheCheckResult resultFromCache = redisCache.getFromCache(texts, "", "ru");

        assertThat(resultFromCache.getCachedTranslations().size(), is(0));
        assertThat(resultFromCache.getNotTranslatedTexts().size(), is(1));
        String notTranslatedText = resultFromCache.getNotTranslatedTexts().get(0);
        assertThat(notTranslatedText, is("Not translated text"));

    }

    @Test
    public void getFromCacheNotTranslatedOnlyMultiString() {
        List<String> texts = new ArrayList<>();
        texts.add("Not translated text 1");
        texts.add("Not translated text 2");

        CacheCheckResult resultFromCache = redisCache.getFromCache(texts, "", "ru");

        assertThat(resultFromCache.getCachedTranslations().size(), is(0));
        assertThat(resultFromCache.getNotTranslatedTexts().size(), is(2));
        String notTranslatedText1 = resultFromCache.getNotTranslatedTexts().get(0);
        String notTranslatedText2 = resultFromCache.getNotTranslatedTexts().get(1);
        assertThat(notTranslatedText1, is("Not translated text 1"));
        assertThat(notTranslatedText2, is("Not translated text 2"));

    }

    @Test
    public void getFromCacheTranslatedNotTranslatedMix() {
        Map<String, String> setForCaching = new HashMap<>();
        setForCaching.put("Text for translation 1", "Translated text 1");
        setForCaching.put("Text for translation 2", "Translated text 2");
        syncCommands.hmset("en:ru", setForCaching);
        List<String> texts = new ArrayList<>();
        texts.add("Text for translation 1");
        texts.add("Not translated text 1");
        texts.add("Text for translation 2");
        texts.add("Not translated text 2");

        CacheCheckResult resultFromCache = redisCache.getFromCache(texts, "en", "ru");
        assertThat(resultFromCache.getNotTranslatedTexts().size(), is(2));
        assertThat(resultFromCache.getCachedTranslations().size(), is(2));
        Map<String, TranslationData> cachedTranslations = resultFromCache.getCachedTranslations();
        assertThat(cachedTranslations.containsKey("Text for translation 1"), is(true));
        assertThat(cachedTranslations.get("Text for translation 1").getTranslatedText(), is("Translated text 1"));
        assertThat(cachedTranslations.get("Text for translation 1").getDetectedSourceLanguage(), is("en"));
        assertThat(cachedTranslations.containsKey("Text for translation 2"), is(true));
        assertThat(cachedTranslations.get("Text for translation 2").getTranslatedText(), is("Translated text 2"));
        assertThat(cachedTranslations.get("Text for translation 2").getDetectedSourceLanguage(), is("en"));
        String notTranslatedText1 = resultFromCache.getNotTranslatedTexts().get(0);
        String notTranslatedText2 = resultFromCache.getNotTranslatedTexts().get(1);
        assertThat(notTranslatedText1, is("Not translated text 1"));
        assertThat(notTranslatedText2, is("Not translated text 2"));

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