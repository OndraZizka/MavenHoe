
package cz.oz.mavenhoe.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.filters.StringInputStream;
import cz.oz.mavenhoe.JarInfo;
import cz.oz.mavenhoe.ex.MavenhoeException;
import cz.oz.mavenhoe.ex.MetadataReffersToNonExistentFileMavenhoeException;
import cz.oz.mavenhoe.mappers.IMavenAxisToJarInfoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Ondrej Zizka
 */
public class JarFinderServlet extends HttpServlet
{
  private static final Logger log = LoggerFactory.getLogger(JarFinderServlet.class);


	/*
	// Thread-safe.
	JarIndex jarIndex;

	public JarFinderServlet(JarIndex jarIndex) {
		this.jarIndex = jarIndex;
	}
	 */

	private IMavenAxisToJarInfoMapper mapper;

	public JarFinderServlet(IMavenAxisToJarInfoMapper mapper) {
		this.mapper = mapper;
	}
	




	/**
	 * TODO: Serve the jar by name.
	 * First shot - proof of concept.
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{

		// Only accept on localhost.
		if( ! "127.0.0.1".equals( req.getLocalAddr() ) ){
			resp.sendError(403, "Sorry, only accessible through 127.0.0.1, you are on: "+req.getLocalAddr());
			return;
		}

		// http://.../jars?mvnPath=org/jboss/whatever/whatever/5.1.0.GA/whatever-5.1.0.GA.jar
		String mvnPath = req.getParameter("mvnPath");
		if( null == mvnPath ){
			resp.sendError(400, "Usage: /jars?mvnPath=<mvn axis> ; Be sure to include '/jars?mvnPath=' in your <repository>'s URL definition.\n"
							+ "Example: http://localhost:17283/jars?mvnPath=org/jboss/whatever/reslever/5.1.0.GA/roatever-5.1.0.GA.jar");
			return;
		}
			

		// group/artifact/version/name-version.jar or similar
		// TODO: Make it static.
		Pattern pat = Pattern.compile( "(.*/)+([^/]*)/([^/]*)/([^/]*)$" );
		//Pattern pat = Pattern.compile( "(.*)+/([^/]*)/([^/]*)/([^/]*)$" );
		Matcher mat = pat.matcher(mvnPath);

		if( !mat.matches() ){
			//throw new IllegalArgumentException("mvnPath URL param must be in format org/jboss/whatever/whatever/5.1.0.GA/whatever-5.1.0.GA.jar");
			resp.sendError(400, "mvnPath URL param must be in format org/jboss/whatever/whatever/5.1.0.GA/whatever-5.1.0.GA.jar :P");
			return;
		}


		String group    = mat.group(1);
		group = StringUtils.strip(group, "/").replace('/', '.');
		String artifact = mat.group(2);
		String version  = mat.group(3);
		String fileName = mat.group(4);
		//String packaging = StringUtils.substringAfterLast( fileName, "/" );
		String packaging = StringUtils.substringAfterLast( fileName, "." );

      // Recognize potential classifier.
      String classifier = null;
      String suffix = String.format("-%s.%s", version, packaging);
      String fileNameNoCls = artifact + suffix;
      if( ! fileNameNoCls.equals( fileName ) ){

         if( ! fileName.startsWith( artifact + "-" )  ||  ! fileName.endsWith( suffix ) ){
            log.warn("Filename does not match A[-C]-V.P, can't detect potential classifier:  "+ mvnPath);
         }
         else {
            // It contains a classifier:  a-c-v.p
            int suffixLen = 1 + version.length() + packaging.length() + 1;
            try {
               classifier = fileName.substring( artifact.length()+1,  fileName.length() - suffixLen );
            } catch ( IndexOutOfBoundsException ex ) {
               log.error("  Damn, IOOBEx! We will not have a classifier :(   "+ex.getMessage());
            }
         }
         
      }

      final JarInfo requestedArtifact = new JarInfo( artifact, version, group, classifier, mvnPath, fileName, null, packaging );

      String clsStr = StringUtils.defaultString(classifier, JarInfo.NULL_CLASSIFIER_STR);
		log.debug(String.format("Parsed Maven axis parts: %s : %s @%s : %s : %s / %s", group, artifact, clsStr, version, packaging, fileName));

		
		/******************************
		   Mappers.
		******************************/

		JarInfo artifactInfoFromMapper = null;
		try {
			artifactInfoFromMapper = this.mapper.find(group, artifact, classifier, version, fileName, packaging);
		}
		catch( MetadataReffersToNonExistentFileMavenhoeException ex ){
			String msg = "Metadata reffers to a non-existent file ["+ex.getMessage()+"].";
			log.error( msg );
			resp.sendError(409, msg);  // HTTP 409 - Conflict.
			return;
		}
		catch( MavenhoeException ex ) {
			String msg = "Error in mapper ["+mapper.getClass().getSimpleName()+"]: "+ex.toString();
			log.error( msg );
			resp.sendError(500, msg);
			return;
		}


		/******************************
		   Dispatch.
		******************************/
      
		if( null != artifactInfoFromMapper ) {

         // A virtual entry?  (Means it should be made up on the fly - e.g. no-deps poms for jars.)
         if( artifactInfoFromMapper.isVirtual() ){
            log.debug("  Found virtual: "+artifactInfoFromMapper);
            dispatchFakePom( requestedArtifact, artifactInfoFromMapper, req, resp );
         }
         // Not a virtual entry, let's serve the physical file.
         else{
            log.debug("  Found: "+artifactInfoFromMapper);
            dispatchJarForDownload( artifactInfoFromMapper, req, resp );
         }
			return;
		}
		

		// Nothing found.
		log.trace("  Nothing found.");
		resp.sendError(404, "No appropriate file was found.");

	}


	/**
	 * Replies the request with a jar file on a given path.
	 */
	private void dispatchJarForDownload( JarInfo artifactInfo, HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
		
		// Check the file existence.
		File file = new File(artifactInfo.getPath());
		if( !file.exists() )
			throw new IllegalStateException("File "+file.getPath()+" does not exist.");
		if( !file.isFile() )
			throw new IllegalStateException("File "+file.getPath()+" is not a regular file.");

		// HTTP stuff.
		resp.setContentLength( (int)file.length() );
		if( artifactInfo.getFileName().endsWith(".jar") )
			resp.setContentType("application/java-archive");
		else if( artifactInfo.getFileName().endsWith(".pom") )
			resp.setContentType("text/xml");
		else
			resp.setContentType("application/octet-stream");

		resp.setHeader("Content-Disposition", "attachment; filename="+file.getName());


		// Internal forward?
		/*req.setAttribute("file", file);
		req.setAttribute("base", jarInfo.getBaseDir());
		this.getServletContext().getRequestDispatcher("/download").forward(req, resp);
		/**/
		
		ServletOutputStream os = resp.getOutputStream();
		//BufferedOutputStream bufOs = new BufferedOutputStream(os, 32*1024);
		FileInputStream in = new FileInputStream(file);
		IOUtils.copy(in, os);
		in.close();
		os.close();
	}


	/**
	 * Sends a fake basic POM with no dependencies.
    *
    * TODO: TBD.
	 */
	public static void dispatchFakePom( JarInfo requestedArtifact, JarInfo fakePom, HttpServletRequest req, HttpServletResponse resp ) throws IOException, ServletException {

      resp.setContentType("text/xml");

      String POM_XML_RESOURCE_PATH = "cz/oz/mavenhoe/server/FakePom.xml";
      InputStream pomIS = JarFinderServlet.class.getClassLoader().getResourceAsStream( POM_XML_RESOURCE_PATH );

      if( null == pomIS )
         throw new ServletException("Can't read fake pom template - getResourceAsStream( POM_XML_RESOURCE_PATH ) == null");

      /*
      List tokens = new ArrayList(5); Arrays.asList( new String[]{ "@GRP@", "@ART@" , "@VER@", "@PACK@", "@NAME@" } );
      Map map = ArrayUtils.toMap(  new String[][]{
         {"@GRP@", artifactInfo.getGroup() },
         {"@ART@", artifactInfo.getName() },
         {"@VER@", artifactInfo.getVersion() },
         {"@PACK@", artifactInfo.getPackaging() },
         {"@NAME@", artifactInfo.getFileName() },
         {"@DESC@", req.getQueryString() },
      } );

      //FixedTokenListReplacementInputStream filterIS = new FixedTokenListReplacementInputStream( pomIS, tokens, new MappedTokenHandler(map) );
      
      //  This does not replace anything, no idea why. //
      ReplaceStringsInputStream replacingIS = new ReplaceStringsInputStream(pomIS, map);
      ReplaceStringInputStream replacingIS2 = new ReplaceStringInputStream(pomIS, "@VER@", "0.0-AAAAA");
      ReplaceStringInputStream replacingIS3 = new ReplaceStringInputStream(pomIS, "@", "#");
      
      ServletOutputStream os = resp.getOutputStream();
      IOUtils.copy( replacingIS, os );
      replacingIS.close();
      /**/

      try {
         String pomTemplate = IOUtils.toString(pomIS, StandardCharsets.UTF_8);
         pomTemplate = pomTemplate.replace("@GRP@", requestedArtifact.getGroup() );
         pomTemplate = pomTemplate.replace("@ART@", requestedArtifact.getName() );
         String classifierSnippet = requestedArtifact.getClassifier() == null ? "" : ("<classifier>"+requestedArtifact.getClassifier()+"</classifier>");
         pomTemplate = pomTemplate.replace("@CLAS@", classifierSnippet );
         pomTemplate = pomTemplate.replace("@VER@", requestedArtifact.getVersion() );
         pomTemplate = pomTemplate.replace("@PACK@", requestedArtifact.getPackaging() );
         pomTemplate = pomTemplate.replace("@NAME@", requestedArtifact.getFileName() );
         pomTemplate = pomTemplate.replace("@DESC@", fakePom.toStringLong() );
         pomTemplate = pomTemplate.replace("@URL@",  req.getRequestURL() + ( req.getQueryString() == null ? "" : ("?"+req.getQueryString()) ) );

         ServletOutputStream os = resp.getOutputStream();
         IOUtils.copy( new StringInputStream(pomTemplate), os );
         os.close();
      }
      catch(IOException ex) {
         throw ex;
      }

	}// dispatchFakePom()


}// class
