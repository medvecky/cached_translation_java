import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.KeyValue;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import java.io.IOException;
import java.util.*;

public class RedisCache {
    private RedisClient redisClient;
    private RedisCommands<String, String> syncCommands;
    private StatefulRedisConnection<String, String> connection;

    public RedisCache() {

        if (System.getenv().containsKey("ENV") && System.getenv("ENV").equals("docker")) {
            redisClient = RedisClient.create("redis://redis:6379");
        } else {
            redisClient = RedisClient.create("redis://localhost:6379");
        }

        connection = redisClient.connect();
        syncCommands = connection.sync();
    }

    public void saveToCache(
            List<CachedTranslationOuterClass.Translation> translations,
            String source,
            String target) {
        Map<String, String> setForCaching = new HashMap<>();
        String key = null;


        if (!source.equals("")) {
            key = source + ":" + target;

            for (CachedTranslationOuterClass.Translation translation : translations) {
                setForCaching.put(translation.getInput(), translation.getTranslatedText());
            }
        } else {
            ObjectMapper mapper = new ObjectMapper();
            key = "auto:" + target;
            String jsonInString;
            for (CachedTranslationOuterClass.Translation translation : translations) {
                TranslationData translationData =
                        new TranslationData(
                                translation.getTranslatedText(),
                                translation.getDetectedSourceLanguage());
                try {
                    jsonInString = mapper.writeValueAsString(translationData);
                    setForCaching.put(translation.getInput(), jsonInString);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        }
        syncCommands.hmset(key, setForCaching);
    }

    public CacheCheckResult getFromCache(List<String> texts, String source, String target) {
        Map<String, TranslationData> cachedTranslations = new HashMap<>();
        List<String> notTranslatedTexts = new ArrayList<>();
        String key;

        if (texts.size() > 0) {
            if (!source.equals("")) {
                key = source + ":" + target;
            } else {
                key = "auto:" + target;

            }
            String[] textsArray = new String[texts.size()];

            texts.toArray(textsArray);

            List<KeyValue<String, String>> cacheResponse = syncCommands.hmget(key, textsArray);
            Map<String, String> resultsFromCache = new HashMap<>();
            Iterator<KeyValue<String, String>> iterator = cacheResponse.iterator();
            while (iterator.hasNext()) {
                KeyValue<String, String> next = iterator.next();
                if (next.hasValue()) {
                    resultsFromCache.put(next.getKey(), next.getValue());
                }
            }

            for (String text : texts) {
                if (resultsFromCache.containsKey(text)) {
                    if (source.equals("")) {
                        ObjectMapper mapper = new ObjectMapper();
                        String jsonString = resultsFromCache.get(text);
                        try {
                            TranslationData translationData = mapper.readValue(jsonString, TranslationData.class);
                            cachedTranslations.put(text, translationData);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        String translation = resultsFromCache.get(text);
                        TranslationData translationData = new TranslationData(translation, source);
                        cachedTranslations.put(text, translationData);
                    }
                } else {
                    notTranslatedTexts.add(text);
                }
            }

        }

        CacheCheckResult cacheCheckResult = new CacheCheckResult(cachedTranslations, notTranslatedTexts);
        return cacheCheckResult;
    }
}
