
package org.jboss.qa.mavenhoe.server;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jboss.qa.mavenhoe.MavenHoeApp;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet, which when requested, shuts down the Jetty server it runs in.
 * @author Ondrej Zizka
 */
public class JettyThreadedShutdownServlet extends HttpServlet
{
	private static final Logger log = LoggerFactory.getLogger( MavenHoeApp.class );


	// This must be enough to the request thread to end,
	// otherwise we would get "WARN  org.mortbay.log 1 threads could not be stopped"
	private static final int SHUTDOWN_THREAD_DELAY = 1000;


	private final Server server;

	JettyThreadedShutdownServlet(Server server) {
		this.server = server;
	}

	

	/**
	 * Shuts the server down.
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		resp.setStatus(202, "Shutting down. Shouldn't take more than 10 seconds.");
		resp.setContentType("text/plain");
		ServletOutputStream os = resp.getOutputStream();
		os.println("Shutting down. Shouldn't take more than 10 seconds.");
		os.close();
		resp.flushBuffer();
		

		// Stop the server.
		try {

			new Thread(){
				 private final Thread requestThread = Thread.currentThread();
				 public void run() {
					 try {
						  log.info("Waiting for the request thread to finish...");
						  Thread.sleep( SHUTDOWN_THREAD_DELAY );
							/*while( requestThread.isAlive() ){
								requestThread.interrupt();  // Nope. The thread dones not get interrupted this way.
								requestThread.join(500);    // Nope. The thread lives in the pool.
							  log.info("  Still waiting for the request thread to finish...");
						  }*/
							log.info("Shutting down the server...");
							server.stop();
							log.info("Server has stopped.");
					 } catch (Exception ex) {
							log.error("Error when stopping Jetty server: "+ex.getMessage(), ex);
					 }
				 }
			}.start();

		} catch (Exception ex) {
			log.error("Error when stopping Jetty server: "+ex.getMessage(), ex);
		}

	}// doGet()



}// class
