import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TranslationData {
    @JsonProperty("translatedText")
    private String translatedText;
    @JsonProperty("detectedSourceLanguage")
    private String detectedSourceLanguage;

    @JsonCreator
    public TranslationData( @JsonProperty("translatedText") String translatedText,
                            @JsonProperty("detectedSourceLanguage") String detectedSourceLanguage) {
        this.translatedText = translatedText;
        this.detectedSourceLanguage = detectedSourceLanguage;
    }

    @JsonProperty("translatedText")
    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }

    @JsonProperty("detectedSourceLanguage")
    public void setDetectedSourceLanguage(String detectedSourceLanguage) {
        this.detectedSourceLanguage = detectedSourceLanguage;
    }

    @JsonProperty("translatedText")
    public String getTranslatedText() {
        return translatedText;
    }

    @JsonProperty("detectedSourceLanguage")
    public String getDetectedSourceLanguage() {
        return detectedSourceLanguage;
    }
}
