
package org.jboss.qa.mavenhoe.util;


import java.io.File;
import java.io.IOException;
import org.jboss.qa.mavenhoe.JarIndex;
import org.jboss.qa.mavenhoe.JarInfo;
import org.jboss.qa.mavenhoe.ex.MavenhoeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.JDOMException;
import org.jdom.Namespace;



/**
 *
 * @author Ondrej Zizka
 */
public class PomParser
{
		private static final Logger log = LoggerFactory.getLogger(JarIndex.class);

		

		/**
		 * Parses pom.xml file and creates a JarInfo object from it's values.
		 */
		public static JarInfo parsePomXml(File file, File baseDir) throws MavenhoeException {
			log.debug( "Parsing POM: "+ file ); ///
			try {
				SAXBuilder builder = new SAXBuilder();
				Document doc = builder.build( file );


				//final Namespace POM_NS = Namespace.getNamespace("http://maven.apache.org/POM/4.0.0");

				Element projectElm = doc.getRootElement();
            Namespace NS = projectElm.getNamespace();
            Element parentElm = projectElm.getChild("parent", NS);
				

            // --- groupId ---

            String groupId = projectElm.getChildText("groupId", NS);
            noGroupId:
            if( null == groupId ){
					if( null == parentElm ) break noGroupId;
					groupId = parentElm.getChildText("groupId", NS);
            }
            if( null == groupId )
               throw new MavenhoeException(" Can't find <groupId> in: "+file.getPath() );


            // --- artifactId ---
				String artifactId = getAndValidate( projectElm, "artifactId", NS);

				// Archetype?
				if( "${artifactId}".equals(artifactId) ||
				    "${groupId}".equals(groupId) )
				{
					throw new MavenhoeException("Warning: Found ${} in artifactId and groupId, probably an archetype. Skipping.");
				}

				String classifier = projectElm.getChildText("classifier", NS);

				String version = projectElm.getChildText("version", NS);
				// Trick: Take version from parent. It must be defined at least there.
				noVersion:
				if( null == version ){
					if( null == parentElm ) break noVersion;
					version = parentElm.getChildText("version", NS);
				}
				if( null == version )
					throw new MavenhoeException("Can't find version in POM: "+file.getPath());

				String packaging = "pom"; // We want packaging for this file, not for the project's artifact.


            // Standard POM file name format.
				StringBuilder sb = new StringBuilder(artifactId).append('-');
				if( null != classifier )
					sb.append(classifier).append('-');
				String fileName = sb.append(version).append('.').append(packaging).toString();

            // JarInfo
            JarInfo jarInfo = new JarInfo( artifactId, version, groupId, file.getPath(), fileName, baseDir, packaging );
            jarInfo.setClassifier(classifier);

				return jarInfo;

			}
			catch( JDOMException ex ){
				throw new MavenhoeException("Error when parsing "+file.getPath()+": "+ex.getMessage());
			}
			catch( IOException ex ){
				throw new MavenhoeException("Error when reading "+file.getPath()+": "+ex.getMessage());
			}


		}// parsePomXml()

		private static String getAndValidate(Element projectElm, String elmName, Namespace NS) throws MavenhoeException {
			String val = projectElm.getChildText(elmName, NS);
			if( null == val )
				throw new MavenhoeException(" Element "+elmName+" not found. NS: "+NS.getURI());
			return val;
		}


}// class
