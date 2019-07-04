import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

import java.util.ArrayList;
import java.util.List;

public class CloudTranslationService {

    private final Translate translate;

    public CloudTranslationService() {
        translate = TranslateOptions.getDefaultInstance().getService();
    }

    public List<CachedTranslationOuterClass.Translation> translate(
            List<String> texts,
            String targetLanguage,
            String sourceLanguage) {
        List<Translation> servicesTranslations = this.translate.translate(
                texts,
                Translate.TranslateOption.targetLanguage(targetLanguage),
                Translate.TranslateOption.sourceLanguage(sourceLanguage));

        return buildTranslationsResponse(texts, servicesTranslations);
    }

    public List<CachedTranslationOuterClass.Translation> translate(List<String> texts, String targetLanguage) {
        List<Translation> servicesTranslations = this.translate.translate(
                texts,
                Translate.TranslateOption.targetLanguage(targetLanguage));

        return buildTranslationsResponse(texts, servicesTranslations);
    }

    public List<CachedTranslationOuterClass.Translation> buildTranslationsResponse(
            List<String> texts,
            List<Translation> servicesTranslations) {
        List<CachedTranslationOuterClass.Translation> translations = new ArrayList<>();

        for (int i = 0; i < texts.size(); i++) {
            Translation serviceTranslation = servicesTranslations.get(i);
            CachedTranslationOuterClass.Translation translation =
                    CachedTranslationOuterClass.Translation
                            .newBuilder()
                            .setTranslatedText(serviceTranslation.getTranslatedText())
                            .setDetectedSourceLanguage(serviceTranslation.getSourceLanguage())
                            .setInput(texts.get(i))
                            .build();
            translations.add(translation);
        }
        return translations;
    }
}