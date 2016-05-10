package org.jboss.qa.mavenhoe;

import java.io.IOException;
import java.net.URISyntaxException;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName ){
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite(){
        return new TestSuite( AppTest.class );
    }

    /**
     *   Artifact id and packaging mapper test.
		 *
     */
    public void testWrongArgs_NonExistentPaths() throws URISyntaxException, InterruptedException, IOException
    {
				Thread thread = new Thread(new Runnable() {
					public void run() {
						MavenHoeApp.main2(new String[]{"-map", "non-existent/foo", "-map", "non-existent/bar", "-i", "some/dir"});
					}
				});

				// Should fail immediately - non-existent.
				thread.start();
				thread.join( 5000 );
				assertFalse( thread.isAlive() );
		}

    public void xtestSimpleRun(){

				//HttpClient httpClient = HttpClient.New(new URI("http://localhost:17283/shutdown").toURL());

				//HttpClient httpClient = new HttpClient();

        // http://localhost:17283/jars?mvnPath=org/hibernate/hibernate-core/3.3.2.GA_CP03/blah.pom
				// http://localhost:17283/shutdown
    }
		
}
