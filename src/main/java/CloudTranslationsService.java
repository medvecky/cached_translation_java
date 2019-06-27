import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

import java.util.ArrayList;
import java.util.List;

public class CloudTranslationsService {

    private final Translate translate;

    public CloudTranslationsService() {
        translate = TranslateOptions.getDefaultInstance().getService();
    }

    public List<CachedTranslations.Translation> translate(
            List<String> texts,
            String targetLanguage,
            String sourceLanguage) {
        List<Translation> servicesTranslations = this.translate.translate(
                texts,
                Translate.TranslateOption.targetLanguage(targetLanguage),
                Translate.TranslateOption.sourceLanguage(sourceLanguage));

        List<CachedTranslations.Translation> translations = buildTranslationsResponse(texts, servicesTranslations);

        return translations;
    }

    public List<CachedTranslations.Translation> translate(List<String> texts, String targetLanguage) {
        List<Translation> servicesTranslations = this.translate.translate(
                texts,
                Translate.TranslateOption.targetLanguage(targetLanguage));

        List<CachedTranslations.Translation> translations = buildTranslationsResponse(texts, servicesTranslations);

        return translations;
    }

    private List<CachedTranslations.Translation> buildTranslationsResponse(
            List<String> texts,
            List<Translation> servicesTranslations) {
        List<CachedTranslations.Translation> translations = new ArrayList<>();

        for (int i = 0; i < texts.size(); i++) {
            Translation serviceTranslation = servicesTranslations.get(i);
            CachedTranslations.Translation translation =
                    CachedTranslations
                            .Translation
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