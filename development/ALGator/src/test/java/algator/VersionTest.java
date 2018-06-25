package algator;



import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;


/**
 * @author jured
 */
public class VersionTest {

    @Test
    public void shouldReturnCorrectVersion() {
        String version = Version.getVersion();

        assertThat(version, equalTo("version 0.87 (June 2018)"));
    }
}
