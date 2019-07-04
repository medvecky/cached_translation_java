import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        CachedTranslationServerIntegrationTest.class,
        CloudTranslationServiceIntegrationTest.class,
        RedisCacheIntegrationTest.class})
public class IntegrationTestsSuite {
}
