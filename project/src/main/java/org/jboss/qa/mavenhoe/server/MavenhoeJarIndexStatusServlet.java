
package org.jboss.qa.mavenhoe.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.qa.mavenhoe.JarIndex;
import org.jboss.qa.mavenhoe.JarInfo;
import org.jboss.qa.mavenhoe.MavenHoeApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Ondrej Zizka
 */
public class MavenhoeJarIndexStatusServlet extends HttpServlet
{
  private static final Logger log = LoggerFactory.getLogger(MavenHoeApp.class);


	// Thread-safe.
	JarIndex jarIndex;

	public MavenhoeJarIndexStatusServlet(JarIndex jarIndex) {
		this.jarIndex = jarIndex;
	}



	/**
	 *  This servlet lists the indexed jars.
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{

		// Only accept on localhost.
		if( ! "127.0.0.1".equals( req.getLocalAddr() ) ){
			resp.sendError(403, "Sorry, only accessible through 127.0.0.1, you are on: "+req.getLocalAddr());
			return;
		}

		resp.setContentType("text/plain");

		PrintWriter wr = resp.getWriter();
		
		wr.println("# Indexed jars: "+jarIndex.getList().size());
		String ctxPath = this.getServletContext().getContextPath();
		String shutdownUrl = StringUtils.substringBeforeLast( req.getRequestURL().toString(), "/" );
		wr.println("# To shut the server down, go to "+shutdownUrl+"/shutdown");


		for( JarInfo jarInfo : jarIndex.getList() ){
			wr.println( jarInfo.toString() );
		}

		wr.close();
	}


}// class
