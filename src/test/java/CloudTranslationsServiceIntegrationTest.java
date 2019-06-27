import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class CloudTranslationsServiceIntegrationTest {

    CloudTranslationsService cloudTranslationsService = new CloudTranslationsService();

    List<String> texts = Arrays.asList("Hello world", "Hello guys");

    @Test
    public void translateWithSource() {

        List<CachedTranslations.Translation> translations = cloudTranslationsService.translate(
                texts,
                "ru",
                "en");

        assertThat(translations.size(), is(2));

        CachedTranslations.Translation translation = translations.get(0);
        assertThat(translation.getTranslatedText(), is("Привет, мир"));
        assertThat(translation.getDetectedSourceLanguage(), is("en"));
        assertThat(translation.getInput(), is("Hello world"));

        translation = translations.get(1);
        assertThat(translation.getTranslatedText(), is("Привет ребята"));
        assertThat(translation.getDetectedSourceLanguage(), is("en"));
        assertThat(translation.getInput(), is("Hello guys"));
    }

    @Test
    public void translateWithoutSource() {

        List<CachedTranslations.Translation> translations = cloudTranslationsService.translate(
                texts,
                "ru");

        assertThat(translations.size(), is(2));

        CachedTranslations.Translation translation = translations.get(0);
        assertThat(translation.getTranslatedText(), is("Привет, мир"));
        assertThat(translation.getDetectedSourceLanguage(), is("en"));
        assertThat(translation.getInput(), is("Hello world"));

        translation = translations.get(1);
        assertThat(translation.getTranslatedText(), is("Привет ребята"));
        assertThat(translation.getDetectedSourceLanguage(), is("en"));
        assertThat(translation.getInput(), is("Hello guys"));
    }
}
