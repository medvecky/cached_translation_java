import io.lettuce.core.api.sync.RedisCommands;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.then;

public class RedisCacheTest {
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    RedisCommands<String, String> syncCommandsMock;

    @InjectMocks
    RedisCache redisCache;

    @Captor
    ArgumentCaptor<String> stringArgumentCaptor;

    @Captor
    ArgumentCaptor<Map<String, String>> mapArgumentCaptor;

    @Test
    public void saveToCacheWithSource() {
//        Given
        CachedTranslationOuterClass.Translation translation =
                CachedTranslationOuterClass
                        .Translation
                        .newBuilder()
                        .setInput("Text for translation")
                        .setTranslatedText("Translated text")
                        .build();

        List<CachedTranslationOuterClass.Translation> translations = new ArrayList<>();
        translations.add(translation);

//      When
        redisCache.saveToCache(translations, "en", "ru");

//      Then
        then(syncCommandsMock).should().hmset(stringArgumentCaptor.capture(), mapArgumentCaptor.capture());
        assertThat(stringArgumentCaptor.getValue(), is("en:ru"));
        assertTrue(mapArgumentCaptor.getValue().containsKey("Text for translation"));
        assertThat(mapArgumentCaptor.getValue().get("Text for translation"), is("Translated text"));
    }


    @Test
    public void saveToCacheWithSourceMultiString() {
//        Given
        CachedTranslationOuterClass.Translation translation1 =
                CachedTranslationOuterClass
                        .Translation
                        .newBuilder()
                        .setInput("Text for translation 1")
                        .setTranslatedText("Translated text 1")
                        .build();


        CachedTranslationOuterClass.Translation translation2 =
                CachedTranslationOuterClass
                        .Translation
                        .newBuilder()
                        .setInput("Text for translation 2")
                        .setTranslatedText("Translated text 2")
                        .build();

        List<CachedTranslationOuterClass.Translation> translations = new ArrayList<>();
        translations.add(translation1);
        translations.add(translation2);

//      When
        redisCache.saveToCache(translations, "en", "ru");

//      Then
        then(syncCommandsMock).should().hmset(stringArgumentCaptor.capture(), mapArgumentCaptor.capture());
        assertThat(stringArgumentCaptor.getValue(), is("en:ru"));
        assertThat(mapArgumentCaptor.getValue().size(), is(2));
        assertTrue(mapArgumentCaptor.getValue().containsKey("Text for translation 1"));
        assertThat(mapArgumentCaptor.getValue().get("Text for translation 1"), is("Translated text 1"));
        assertTrue(mapArgumentCaptor.getValue().containsKey("Text for translation 2"));
        assertThat(mapArgumentCaptor.getValue().get("Text for translation 2"), is("Translated text 2"));
    }


    @Test
    public void saveToCacheWithoutSource() {
//        Given
        CachedTranslationOuterClass.Translation translation =
                CachedTranslationOuterClass
                        .Translation
                        .newBuilder()
                        .setInput("Text for translation")
                        .setTranslatedText("Translated text")
                        .setDetectedSourceLanguage("en")
                        .build();

        List<CachedTranslationOuterClass.Translation> translations = new ArrayList<>();
        translations.add(translation);

//      When
        redisCache.saveToCache(translations, "", "ru");

//      Then
        then(syncCommandsMock).should().hmset(stringArgumentCaptor.capture(), mapArgumentCaptor.capture());
        assertThat(stringArgumentCaptor.getValue(), is("auto:ru"));
        assertTrue(mapArgumentCaptor.getValue().containsKey("Text for translation"));
        assertThat(
                mapArgumentCaptor
                        .getValue()
                        .get("Text for translation"),
                is("{\"translatedText\":\"Translated text\",\"detectedSourceLanguage\":\"en\"}"));
    }


    @Test
    public void saveToCacheWithoutSourceMultiString() {
//        Given
        CachedTranslationOuterClass.Translation translation1 =
                CachedTranslationOuterClass
                        .Translation
                        .newBuilder()
                        .setInput("Text for translation 1")
                        .setTranslatedText("Translated text 1")
                        .setDetectedSourceLanguage("en")
                        .build();

        CachedTranslationOuterClass.Translation translation2 =
                CachedTranslationOuterClass
                        .Translation
                        .newBuilder()
                        .setInput("Text for translation 2")
                        .setTranslatedText("Translated text 2")
                        .setDetectedSourceLanguage("lv")
                        .build();

        List<CachedTranslationOuterClass.Translation> translations = new ArrayList<>();
        translations.add(translation1);
        translations.add(translation2);

//      When
        redisCache.saveToCache(translations, "", "ru");

//      Then
        then(syncCommandsMock).should().hmset(stringArgumentCaptor.capture(), mapArgumentCaptor.capture());
        assertThat(stringArgumentCaptor.getValue(), is("auto:ru"));
        assertThat(mapArgumentCaptor.getValue().size(), is(2));
        assertTrue(mapArgumentCaptor.getValue().containsKey("Text for translation 1"));
        assertThat(
                mapArgumentCaptor
                        .getValue()
                        .get("Text for translation 1"),
                is("{\"translatedText\":\"Translated text 1\",\"detectedSourceLanguage\":\"en\"}"));

        assertTrue(mapArgumentCaptor.getValue().containsKey("Text for translation 2"));
        assertThat(
                mapArgumentCaptor
                        .getValue()
                        .get("Text for translation 2"),
                is("{\"translatedText\":\"Translated text 2\",\"detectedSourceLanguage\":\"lv\"}"));
    }

    @Test
    public void getFromCacheWithSource() {

    }

}