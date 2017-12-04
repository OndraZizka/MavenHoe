
package cz.oz.mavenhoe.server;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cz.oz.mavenhoe.MavenHoeApp;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet, which when requested, shuts down the Jetty server it runs in.
 * @author Ondrej Zizka
 */
public class JettyShutdownServlet extends HttpServlet
{
	private static final Logger log = LoggerFactory.getLogger( MavenHoeApp.class );


	/**
	 *  Blocks until nofifyShutdown() is called.
	 *  Not used in RunInJetty - there's no way to get a list of threads in the pool,
	 *  thus there's no point in notifying; it rather uses server.join().
	 */
	public void waitForShutdownNotification() {
		synchronized ( this ){
			try {
				this.wait();
			} catch (InterruptedException ex) {
				log.info("waitForShutdownNotification() was interrupted.");
			}
		}
	}

	private void notifyShutdown() {
		synchronized ( this ){
			this.notifyAll();
		}
	}

	
	private final Server server;

	JettyShutdownServlet(Server server) {
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
			log.info("Shutting down the server...");
			server.stop();

			Connector[] connectors = server.getConnectors();
			for (Connector connector : connectors) {
				 connector.stop();
			}

			this.notifyShutdown();

		} catch (Exception ex) {
			log.error("Error when stopping Jetty server: "+ex.getMessage(), ex);
		}

	}




}
