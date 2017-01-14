import junit.framework.TestCase;

import java.awt.*;

/**
 * Created by user on 05.05.14.
 */
public class TestTestTest extends TestCase {

    public void testOne() throws InterruptedException {
        Thread.sleep(2000);
        System.out.println(GraphicsEnvironment.isHeadless());
    }

    public void testFailure2() {
        fail();
    }
}
