import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        CachedTranslationServerTest.class,
        RedisCacheTest.class})
public class UnitTestsSuite {
}
