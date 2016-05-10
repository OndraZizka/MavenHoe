
package org.jboss.qa.mavenhoe.server;


import java.net.BindException;
import java.net.URL;
import javax.servlet.UnavailableException;
import org.apache.commons.lang.StringUtils;
import org.jboss.qa.mavenhoe.JarIndex;
import org.jboss.qa.mavenhoe.MavenHoeApp;
import org.jboss.qa.mavenhoe.mappers.IMavenAxisToJarInfoMapper;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.thread.QueuedThreadPool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
		<init-param>
			<param-name>applicationClassName</param-name>
			<param-value>cz.dw.test.WicketApplication</param-value>
		</init-param>
 *
 * @author Ondrej Zizka
 */
public class RunInJetty {
   private static final Logger log = LoggerFactory.getLogger( RunInJetty.class );

   private static final int PORT = 17283;
   private static final int MAX_ACCEPT_TIME_MS = 2000;
   private static final int MAX_IDLE_TIME_MS = 2000;
   private static final int MAX_STOP_TIME_MS = 2100;
   private static final int GRACEFULL_SHUTDOWN_TIMEOUT_MS = 2500;

   private final MavenHoeApp mavenhoeApp;
   private final JarIndex jarIndex;
   private final IMavenAxisToJarInfoMapper mapper;

   public RunInJetty( MavenHoeApp mavenhoeApp, IMavenAxisToJarInfoMapper mapper ) {
      this.mavenhoeApp = mavenhoeApp;
      this.jarIndex = mavenhoeApp.getJarIndex();
      this.mapper = mapper;
   }


	 
   public void run() throws UnavailableException
   {

      Server server = new Server( PORT );
      server.setGracefulShutdown( GRACEFULL_SHUTDOWN_TIMEOUT_MS );
      server.setStopAtShutdown(true);


      Context ctx = new Context( server, "/", Context.NO_SECURITY | Context.SESSIONS );

      // Static content.
      //ServletHolder downloadSH = new ServletHolder(new org.mortbay.jetty.servlet.DefaultServlet());
      //downloadSH.setInitParameter("resourceBase", "/"); // ?? Let's rather do an internal forward from JarFinderServlet.
      //ctx.addServlet(downloadSH, "/download/*");


      // JSP test with DefaultServlet - DOES NOT WORK
      ServletHolder defaultSH = new ServletHolder(new org.mortbay.jetty.servlet.DefaultServlet());
      defaultSH.setName("DefaultServletForJSPs");
      defaultSH.setInitParameter("resourceBase", "org/jboss/qa/mavenhoe/web/jsp");
      ctx.addServlet(defaultSH, "/bjsp");/**/


      // Mavenhoe Servlets.

      // TODO: JSP pages - listings etc.
      //final ServletHolder defaultSH = new ServletHolder(new DefaultServlet());
      //ctx.addServlet( defaultSH, "/");

      final String WEBAPP_RESOURCES_PATH = "org/jboss/qa/mavenhoe/web/jsp";
      final String JSP_CONTEXT_PATH = "/jsp";

      // For localhost:port/jsp/index.html and whatever else is in the directory...
      final URL warUrl = this.getClass().getClassLoader().getResource(WEBAPP_RESOURCES_PATH);
      final String warUrlString = warUrl.toExternalForm();
      WebAppContext webAppContext = new WebAppContext(warUrlString, JSP_CONTEXT_PATH);
      webAppContext.setAttribute("mapper", mapper);
      // If addHandler(), the servlets work but JSP does not.
      // If setHandler(), JSP works but servlets do not.
      server.addHandler( webAppContext );


      // Status servlet - listing of indexed jars. Mainly for debugging purposes.
      final ServletHolder statusSH = new ServletHolder(new MavenhoeJarIndexStatusServlet(this.jarIndex));
      ctx.addServlet( statusSH, "/status");

      // FileBasedMapper status servlet - listing of maps. Mainly for debugging purposes.
      final ServletHolder fbmStatusSH = new ServletHolder(new MavenhoeFBMStatusServlet(this.mavenhoeApp.getReferences().getFileBasedMapper()));
      ctx.addServlet( fbmStatusSH, "/fbmStatus");


      // .jar's download.
      final ServletHolder mavenhoeSH = new ServletHolder(new JarFinderServlet(this.mapper));
      //mavenhoeSH.setInitParameter("someParam", "value");
      mavenhoeSH.setName("mavenhoeServlet");
      ctx.addServlet( mavenhoeSH, "/jars" );


      final JettyThreadedShutdownServlet shutdownServlet = new JettyThreadedShutdownServlet(server);
      final ServletHolder shutdownSH = new ServletHolder(shutdownServlet);
      shutdownSH.checkServletType();
      shutdownSH.setName("shutdownServlet");
      ctx.addServlet( shutdownSH, "/shutdown" );/**/
      //ctx.addServlet( JettyShutdownServlet.class, "/shutdown");



      /*/
      FilterHolder filterHolder = new FilterHolder( new WicketFilter() );
      filterHolder.setInitParameter("applicationClassName", cz.dw.test.WicketApplication.class.getName() );
      root.addFilter( filterHolder, "/*" , Handler.ALL );
      /**/

      try {
         server.start();

         // The threads hang for getMaxIdleTimeMs() after JettyShutdownServlet calls server.stop().
         // This is a workaround - it sets the time to a reasonable compromise.
         /*if( server.getThreadPool() instanceof QueuedThreadPool ){
            ((QueuedThreadPool) server.getThreadPool()).setMaxIdleTimeMs( MAX_IDLE_TIME_MS );
             ((QueuedThreadPool) server.getThreadPool()).setMaxStopTimeMs( MAX_STOP_TIME_MS );
        }/**/

         /*Connector[] connectors = server.getConnectors();
         for (Connector connector : connectors) {
             connector.setMaxIdleTime(MAX_ACCEPT_TIME);
         }*/

         /*log.debug("Suspending launcher thread, waiting for shutdown notification.");
         shutdownServlet.waitForShutdownNotification();
         log.debug("Shutdown notification received, interrupting thread pool.");
         // No way to get a list of threads??
         log.debug("Idle threads interrupted, hasta la vista.");
         /**/

         log.info("Mavenhoe server started.");
         System.out.append( StringUtils.repeat("  ", 512) );
         System.out.flush();

         log.debug("Suspending launcher thread, waiting for the server thread to end.");
         server.join();
         log.debug("Server thread has ended, closing stdin/out/err.");
         System.out.close();
         System.err.close();
         System.in.close();

      }
      catch( BindException ex ){
         throw new IllegalStateException("Can't bind: "+ex.getMessage());
      }
      catch(Exception ex) {
         ex.printStackTrace();
      }

	}

}// class
