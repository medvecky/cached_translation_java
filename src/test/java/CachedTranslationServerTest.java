import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.*;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

public class CachedTranslationServerTest {
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    RedisCache redisCacheMock;

    @Mock
    CloudTranslationService cloudTranslationServiceMock;

    @InjectMocks
    CachedTranslationServer.CachedTranslationImpl server;

    @Captor
    ArgumentCaptor<String> stringOneArgumentCaptor;

    @Captor
    ArgumentCaptor<String> stringTwoArgumentCaptor;

    @Captor
    ArgumentCaptor<List<String>> listOfStringsArgumentCaptor;

    @Captor
    ArgumentCaptor<List<CachedTranslationOuterClass.Translation>> listOfTranslationsArgumentCaptor;


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
    public void mergeTranslationsCachedTranslationsOnly() {
        List<String> texts = Arrays.asList("Text for translation");
        CachedTranslationOuterClass.TranslationRequest translationRequest =
                CachedTranslationOuterClass
                        .TranslationRequest
                        .newBuilder()
                        .addAllTexts(texts)
                        .setTargetLanguage("ru")
                        .build();
        TranslationData translationData =
                new TranslationData("Translated text", "en");
        Map<String, TranslationData> cachedTranslations = new HashMap<>();
        cachedTranslations.put("Text for translation", translationData);
        Map<String, TranslationData> cloudTranslations = new HashMap<>();


        List<CachedTranslationOuterClass.Translation> resultTranslations =
                server.mergeTranslations(translationRequest, cachedTranslations, cloudTranslations);

        assertThat(resultTranslations.size(), is(1));

        CachedTranslationOuterClass.Translation translation = resultTranslations.get(0);
        assertThat(translation.getInput(), is("Text for translation"));
        assertThat(translation.getTranslatedText(), is("Translated text"));
        assertThat(translation.getDetectedSourceLanguage(), is("en"));
    }

    @Test
    public void mergeTranslationsCachedTranslationsOnlyMultiString() {
        List<String> texts = Arrays.asList("Text for translation 1", "Text for translation 2");
        CachedTranslationOuterClass.TranslationRequest translationRequest =
                CachedTranslationOuterClass
                        .TranslationRequest
                        .newBuilder()
                        .addAllTexts(texts)
                        .setTargetLanguage("ru")
                        .build();
        TranslationData translationData1 =
                new TranslationData("Translated text 1", "en");
        TranslationData translationData2 =
                new TranslationData("Translated text 2", "en");
        Map<String, TranslationData> cachedTranslations = new HashMap<>();
        cachedTranslations.put("Text for translation 1", translationData1);
        cachedTranslations.put("Text for translation 2", translationData2);
        Map<String, TranslationData> cloudTranslations = new HashMap<>();


        List<CachedTranslationOuterClass.Translation> resultTranslations =
                server.mergeTranslations(translationRequest, cachedTranslations, cloudTranslations);

        assertThat(resultTranslations.size(), is(2));

        CachedTranslationOuterClass.Translation translation1 = resultTranslations.get(0);
        CachedTranslationOuterClass.Translation translation2 = resultTranslations.get(1);
        assertThat(translation1.getInput(), is("Text for translation 1"));
        assertThat(translation1.getTranslatedText(), is("Translated text 1"));
        assertThat(translation1.getDetectedSourceLanguage(), is("en"));
        assertThat(translation2.getInput(), is("Text for translation 2"));
        assertThat(translation2.getTranslatedText(), is("Translated text 2"));
        assertThat(translation2.getDetectedSourceLanguage(), is("en"));
    }

    @Test
    public void mergeTranslationsCloudTranslationsOnly() {
        List<String> texts = Arrays.asList("Text for translation");
        CachedTranslationOuterClass.TranslationRequest translationRequest =
                CachedTranslationOuterClass
                        .TranslationRequest
                        .newBuilder()
                        .addAllTexts(texts)
                        .setTargetLanguage("ru")
                        .build();
        TranslationData translationData =
                new TranslationData("Translated text", "en");
        Map<String, TranslationData> cloudTranslations = new HashMap<>();
        cloudTranslations.put("Text for translation", translationData);
        Map<String, TranslationData> cachedTranslations = new HashMap<>();


        List<CachedTranslationOuterClass.Translation> resultTranslations =
                server.mergeTranslations(translationRequest, cachedTranslations, cloudTranslations);

        assertThat(resultTranslations.size(), is(1));

        CachedTranslationOuterClass.Translation translation = resultTranslations.get(0);
        assertThat(translation.getInput(), is("Text for translation"));
        assertThat(translation.getTranslatedText(), is("Translated text"));
        assertThat(translation.getDetectedSourceLanguage(), is("en"));
    }

    @Test
    public void mergeTranslationsCloudTranslationsOnlyMultiString() {
        List<String> texts = Arrays.asList("Text for translation 1", "Text for translation 2");
        CachedTranslationOuterClass.TranslationRequest translationRequest =
                CachedTranslationOuterClass
                        .TranslationRequest
                        .newBuilder()
                        .addAllTexts(texts)
                        .setTargetLanguage("ru")
                        .build();
        TranslationData translationData1 =
                new TranslationData("Translated text 1", "en");
        TranslationData translationData2 =
                new TranslationData("Translated text 2", "en");
        Map<String, TranslationData> cloudTranslations = new HashMap<>();
        cloudTranslations.put("Text for translation 1", translationData1);
        cloudTranslations.put("Text for translation 2", translationData2);
        Map<String, TranslationData> cachedTranslations = new HashMap<>();


        List<CachedTranslationOuterClass.Translation> resultTranslations =
                server.mergeTranslations(translationRequest, cachedTranslations, cloudTranslations);

        assertThat(resultTranslations.size(), is(2));

        CachedTranslationOuterClass.Translation translation1 = resultTranslations.get(0);
        CachedTranslationOuterClass.Translation translation2 = resultTranslations.get(1);
        assertThat(translation1.getInput(), is("Text for translation 1"));
        assertThat(translation1.getTranslatedText(), is("Translated text 1"));
        assertThat(translation1.getDetectedSourceLanguage(), is("en"));
        assertThat(translation2.getInput(), is("Text for translation 2"));
        assertThat(translation2.getTranslatedText(), is("Translated text 2"));
        assertThat(translation2.getDetectedSourceLanguage(), is("en"));
    }

    @Test
    public void mergeTranslationsMix() {
        List<String> texts = Arrays.asList(
                "Text for translation 1",
                "Text for translation 2",
                "Text for translation 3",
                "Text for translation 4");

        CachedTranslationOuterClass.TranslationRequest translationRequest =
                CachedTranslationOuterClass
                        .TranslationRequest
                        .newBuilder()
                        .addAllTexts(texts)
                        .setTargetLanguage("ru")
                        .build();

        TranslationData translationData1 =
                new TranslationData("Translated text 1", "en");
        TranslationData translationData2 =
                new TranslationData("Translated text 2", "en");
        TranslationData translationData3 =
                new TranslationData("Translated text 3", "en");
        TranslationData translationData4 =
                new TranslationData("Translated text 4", "en");
        Map<String, TranslationData> cloudTranslations = new HashMap<>();
        cloudTranslations.put("Text for translation 1", translationData1);
        cloudTranslations.put("Text for translation 3", translationData3);

        Map<String, TranslationData> cachedTranslations = new HashMap<>();
        cachedTranslations.put("Text for translation 2", translationData2);
        cachedTranslations.put("Text for translation 4", translationData4);
        List<CachedTranslationOuterClass.Translation> resultTranslations =
                server.mergeTranslations(translationRequest, cachedTranslations, cloudTranslations);

        assertThat(resultTranslations.size(), is(4));

        CachedTranslationOuterClass.Translation translation1 = resultTranslations.get(0);
        CachedTranslationOuterClass.Translation translation2 = resultTranslations.get(1);
        CachedTranslationOuterClass.Translation translation3 = resultTranslations.get(2);
        CachedTranslationOuterClass.Translation translation4 = resultTranslations.get(3);

        assertThat(translation1.getInput(), is("Text for translation 1"));
        assertThat(translation1.getTranslatedText(), is("Translated text 1"));
        assertThat(translation1.getDetectedSourceLanguage(), is("en"));
        assertThat(translation2.getInput(), is("Text for translation 2"));
        assertThat(translation2.getTranslatedText(), is("Translated text 2"));
        assertThat(translation2.getDetectedSourceLanguage(), is("en"));
        assertThat(translation3.getInput(), is("Text for translation 3"));
        assertThat(translation3.getTranslatedText(), is("Translated text 3"));
        assertThat(translation3.getDetectedSourceLanguage(), is("en"));
        assertThat(translation4.getInput(), is("Text for translation 4"));
        assertThat(translation4.getTranslatedText(), is("Translated text 4"));
        assertThat(translation4.getDetectedSourceLanguage(), is("en"));
    }

    @Test
    public void handleRequestToCloudWithSourceOneString() {
//        given

        List<String> texts = Arrays.asList("Text for translation");

        CachedTranslationOuterClass.TranslationRequest request =
                CachedTranslationOuterClass
                        .TranslationRequest
                        .newBuilder()
                        .addAllTexts(texts)
                        .setTargetLanguage("ru")
                        .setSourceLanguage("en")
                        .build();
        CachedTranslationOuterClass.Translation translation =
                CachedTranslationOuterClass
                        .Translation.newBuilder()
                        .setTranslatedText("Translated text")
                        .setInput("Text for translation")
                        .build();
        List<CachedTranslationOuterClass.Translation> translations = Arrays.asList(translation);


        given(cloudTranslationServiceMock.translate(
                request.getTextsList(),
                "ru",
                "en"))
                .willReturn(translations);
//        When

        Map<String, TranslationData> cloudResponse = server.handleRequestToCloud(request);

//        Then

        then(cloudTranslationServiceMock).should().translate(
                listOfStringsArgumentCaptor.capture(),
                stringOneArgumentCaptor.capture(),
                stringTwoArgumentCaptor.capture());

        assertThat(listOfStringsArgumentCaptor.getValue().get(0), is("Text for translation"));
        assertThat(stringOneArgumentCaptor.getValue(), is("ru"));
        assertThat(stringTwoArgumentCaptor.getValue(), is("en"));

        then(redisCacheMock).should().saveToCache(
                listOfTranslationsArgumentCaptor.capture(),
                stringOneArgumentCaptor.capture(),
                stringTwoArgumentCaptor.capture());
        assertThat(listOfTranslationsArgumentCaptor.getValue().size(), is(1));
        String translatedText = listOfTranslationsArgumentCaptor.getValue().get(0).getTranslatedText();
        assertThat(translatedText, is("Translated text"));
        assertThat(stringOneArgumentCaptor.getValue(), is("en"));
        assertThat(stringTwoArgumentCaptor.getValue(), is("ru"));

        assertThat(cloudResponse.size(), is(1));
        assertTrue(cloudResponse.containsKey("Text for translation"));
        TranslationData translationData = cloudResponse.get("Text for translation");
        assertThat(translationData.getTranslatedText(), is("Translated text"));
        assertThat(translationData.getDetectedSourceLanguage(), is(""));

    }

    @Test
    public void handleRequestToCloudWithSourceMultiString() {
//        given

        List<String> texts = Arrays.asList(
                "Text for translation 1",
                "Text for translation 2");

        CachedTranslationOuterClass.TranslationRequest request =
                CachedTranslationOuterClass
                        .TranslationRequest
                        .newBuilder()
                        .addAllTexts(texts)
                        .setTargetLanguage("ru")
                        .setSourceLanguage("en")
                        .build();
        CachedTranslationOuterClass.Translation translation1 =
                CachedTranslationOuterClass
                        .Translation.newBuilder()
                        .setTranslatedText("Translated text 1")
                        .setInput("Text for translation 1")
                        .build();

        CachedTranslationOuterClass.Translation translation2 =
                CachedTranslationOuterClass
                        .Translation.newBuilder()
                        .setTranslatedText("Translated text 2")
                        .setInput("Text for translation 2")
                        .build();
        List<CachedTranslationOuterClass.Translation> translations = Arrays.asList(translation1, translation2);


        given(cloudTranslationServiceMock.translate(
                request.getTextsList(),
                "ru",
                "en"))
                .willReturn(translations);
//        When

        Map<String, TranslationData> cloudResponse = server.handleRequestToCloud(request);

//        Then

        then(cloudTranslationServiceMock).should().translate(
                listOfStringsArgumentCaptor.capture(),
                stringOneArgumentCaptor.capture(),
                stringTwoArgumentCaptor.capture());

        assertThat(listOfStringsArgumentCaptor.getValue().size(), is(2));
        assertThat(stringOneArgumentCaptor.getValue(), is("ru"));
        assertThat(stringTwoArgumentCaptor.getValue(), is("en"));

        then(redisCacheMock).should().saveToCache(
                listOfTranslationsArgumentCaptor.capture(),
                stringOneArgumentCaptor.capture(),
                stringTwoArgumentCaptor.capture());
        assertThat(listOfTranslationsArgumentCaptor.getValue().size(), is(2));
        String translatedText1 = listOfTranslationsArgumentCaptor.getValue().get(0).getTranslatedText();
        String translatedText2 = listOfTranslationsArgumentCaptor.getValue().get(1).getTranslatedText();
        assertThat(translatedText1, is("Translated text 1"));
        assertThat(translatedText2, is("Translated text 2"));
        assertThat(stringOneArgumentCaptor.getValue(), is("en"));
        assertThat(stringTwoArgumentCaptor.getValue(), is("ru"));

        assertThat(cloudResponse.size(), is(2));
        assertTrue(cloudResponse.containsKey("Text for translation 1"));
        assertTrue(cloudResponse.containsKey("Text for translation 2"));
        TranslationData translationData1 = cloudResponse.get("Text for translation 1");
        TranslationData translationData2 = cloudResponse.get("Text for translation 2");
        assertThat(translationData1.getTranslatedText(), is("Translated text 1"));
        assertThat(translationData1.getDetectedSourceLanguage(), is(""));
        assertThat(translationData2.getTranslatedText(), is("Translated text 2"));
        assertThat(translationData2.getDetectedSourceLanguage(), is(""));
    }

    @Test
    public void handleRequestToCloudWithoutSourceOneString() {
//        given

        List<String> texts = Arrays.asList("Text for translation");

        CachedTranslationOuterClass.TranslationRequest request =
                CachedTranslationOuterClass
                        .TranslationRequest
                        .newBuilder()
                        .addAllTexts(texts)
                        .setTargetLanguage("ru")
                        .build();
        CachedTranslationOuterClass.Translation translation =
                CachedTranslationOuterClass
                        .Translation.newBuilder()
                        .setTranslatedText("Translated text")
                        .setInput("Text for translation")
                        .setDetectedSourceLanguage("en")
                        .build();
        List<CachedTranslationOuterClass.Translation> translations = Arrays.asList(translation);


        given(cloudTranslationServiceMock
                .translate(request.getTextsList(),
                        "ru"))
                .willReturn(translations);
//        When

        Map<String, TranslationData> cloudResponse = server.handleRequestToCloud(request);

//        Then

        then(cloudTranslationServiceMock).should().translate(
                listOfStringsArgumentCaptor.capture(),
                stringOneArgumentCaptor.capture());

        assertThat(listOfStringsArgumentCaptor.getValue().get(0), is("Text for translation"));
        assertThat(stringOneArgumentCaptor.getValue(), is("ru"));

        then(redisCacheMock).should().saveToCache(
                listOfTranslationsArgumentCaptor.capture(),
                stringOneArgumentCaptor.capture(),
                stringTwoArgumentCaptor.capture());
        assertThat(listOfTranslationsArgumentCaptor.getValue().size(), is(1));
        String translatedText = listOfTranslationsArgumentCaptor.getValue().get(0).getTranslatedText();
        assertThat(translatedText, is("Translated text"));
        assertThat(stringOneArgumentCaptor.getValue(), is(""));
        assertThat(stringTwoArgumentCaptor.getValue(), is("ru"));

        assertThat(cloudResponse.size(), is(1));
        assertTrue(cloudResponse.containsKey("Text for translation"));
        TranslationData translationData = cloudResponse.get("Text for translation");
        assertThat(translationData.getTranslatedText(), is("Translated text"));
        assertThat(translationData.getDetectedSourceLanguage(), is("en"));
    }

    @Test
    public void handleRequestToCloudWithoutSourceMultiString() {
//        given

        List<String> texts = Arrays.asList(
                "Text for translation 1",
                "Text for translation 2");

        CachedTranslationOuterClass.TranslationRequest request =
                CachedTranslationOuterClass
                        .TranslationRequest
                        .newBuilder()
                        .addAllTexts(texts)
                        .setTargetLanguage("ru")
                        .build();
        CachedTranslationOuterClass.Translation translation1 =
                CachedTranslationOuterClass
                        .Translation.newBuilder()
                        .setTranslatedText("Translated text 1")
                        .setInput("Text for translation 1")
                        .setDetectedSourceLanguage("en")
                        .build();

        CachedTranslationOuterClass.Translation translation2 =
                CachedTranslationOuterClass
                        .Translation.newBuilder()
                        .setTranslatedText("Translated text 2")
                        .setInput("Text for translation 2")
                        .setDetectedSourceLanguage("lv")
                        .build();
        List<CachedTranslationOuterClass.Translation> translations = Arrays.asList(translation1, translation2);


        given(cloudTranslationServiceMock.translate(
                request.getTextsList(),
                "ru"))
                .willReturn(translations);
//        When

        Map<String, TranslationData> cloudResponse = server.handleRequestToCloud(request);

//        Then

        then(cloudTranslationServiceMock).should().translate(
                listOfStringsArgumentCaptor.capture(),
                stringOneArgumentCaptor.capture());

        assertThat(listOfStringsArgumentCaptor.getValue().size(), is(2));
        assertThat(stringOneArgumentCaptor.getValue(), is("ru"));

        then(redisCacheMock).should().saveToCache(
                listOfTranslationsArgumentCaptor.capture(),
                stringOneArgumentCaptor.capture(),
                stringTwoArgumentCaptor.capture());
        assertThat(listOfTranslationsArgumentCaptor.getValue().size(), is(2));
        String translatedText1 = listOfTranslationsArgumentCaptor.getValue().get(0).getTranslatedText();
        String translatedText2 = listOfTranslationsArgumentCaptor.getValue().get(1).getTranslatedText();
        assertThat(translatedText1, is("Translated text 1"));
        assertThat(translatedText2, is("Translated text 2"));
        assertThat(stringOneArgumentCaptor.getValue(), is(""));
        assertThat(stringTwoArgumentCaptor.getValue(), is("ru"));

        assertThat(cloudResponse.size(), is(2));
        assertTrue(cloudResponse.containsKey("Text for translation 1"));
        assertTrue(cloudResponse.containsKey("Text for translation 2"));
        TranslationData translationData1 = cloudResponse.get("Text for translation 1");
        TranslationData translationData2 = cloudResponse.get("Text for translation 2");
        assertThat(translationData1.getTranslatedText(), is("Translated text 1"));
        assertThat(translationData1.getDetectedSourceLanguage(), is("en"));
        assertThat(translationData2.getTranslatedText(), is("Translated text 2"));
        assertThat(translationData2.getDetectedSourceLanguage(), is("lv"));
    }

}