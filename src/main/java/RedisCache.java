import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RedisCache {
    private RedisClient redisClient;
    private RedisCommands<String, String> syncCommands;
    private StatefulRedisConnection<String, String> connection;

    class TranslationData {
        private String translatedText;
        private String detectedSourceLanguage;

        public TranslationData(String translatedText, String detectedSourceLanguage) {
            this.translatedText = translatedText;
            this.detectedSourceLanguage = detectedSourceLanguage;
        }

        public void setTranslatedText(String translatedText) {
            this.translatedText = translatedText;
        }

        public void setDetectedSourceLanguage(String detectedSourceLanguage) {
            this.detectedSourceLanguage = detectedSourceLanguage;
        }

        public String getTranslatedText() {
            return translatedText;
        }

        public String getDetectedSourceLanguage() {
            return detectedSourceLanguage;
        }
    }

    public RedisCache() {
        if (System.getenv("ENV").equals("docker")) {
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
}
