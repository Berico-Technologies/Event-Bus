package orion.esp;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple EventStreamProcessor.
 */
public class EventStreamProcessorTest
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public EventStreamProcessorTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( EventStreamProcessorTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testEventStreamProcessor()
    {
        assertTrue( true );
    }
}
