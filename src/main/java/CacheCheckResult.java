import java.util.List;
import java.util.Map;

public class CacheCheckResult {
    private  Map<String, TranslationData> cachedTranslations;

    private  List<String> notTranslatedTexts;

    public CacheCheckResult(Map<String, TranslationData> cachedTranslations, List<String> notTranslatedTexts) {
        this.cachedTranslations = cachedTranslations;
        this.notTranslatedTexts = notTranslatedTexts;
    }

    public Map<String, TranslationData> getCachedTranslations() {
        return cachedTranslations;
    }

    public void setCachedTranslations(Map<String, TranslationData> cachedTranslations) {
        this.cachedTranslations = cachedTranslations;
    }

    public List<String> getNotTranslatedTexts() {
        return notTranslatedTexts;
    }

    public void setNotTranslatedTexts(List<String> notTranslatedTexts) {
        this.notTranslatedTexts = notTranslatedTexts;
    }
}
