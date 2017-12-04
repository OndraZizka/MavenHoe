
package cz.oz.mavenhoe.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map.Entry;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cz.oz.mavenhoe.MavenHoeApp;
import cz.oz.mavenhoe.mappers.FileBasedMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Ondrej Zizka
 */
public class MavenhoeFBMStatusServlet extends HttpServlet
{
  private static final Logger log = LoggerFactory.getLogger(MavenHoeApp.class);


	private FileBasedMapper fileBasedMapper;


	public MavenhoeFBMStatusServlet( FileBasedMapper fileBasedMapper ) {
		this.fileBasedMapper = fileBasedMapper;
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
		
		wr.println("# Mapped files: ");

		wr.println("# GAVP: "+this.fileBasedMapper.getFileNameByGAVPMap().size());
		for( Entry en : this.fileBasedMapper.getFileNameByGAVPMap().entrySet() ){
			wr.println( en.getKey() + " => " + en.getValue() );
		}

		wr.println("# GAP: "+this.fileBasedMapper.getMapFileNameByGAP().size());
		for( Entry en : this.fileBasedMapper.getMapFileNameByGAP().entrySet() ){
			wr.println( en.getKey() + " => " + en.getValue() );
		}

		wr.println("# AP: "+this.fileBasedMapper.getMapFileNameByAP().size());
		for( Entry en : this.fileBasedMapper.getMapFileNameByAP().entrySet() ){
			wr.println( en.getKey() + " => " + en.getValue() );
		}

		wr.close();
	}


}// class
