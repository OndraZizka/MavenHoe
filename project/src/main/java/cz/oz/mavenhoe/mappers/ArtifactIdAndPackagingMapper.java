
package cz.oz.mavenhoe.mappers;

import org.apache.commons.lang.StringUtils;
import cz.oz.mavenhoe.JarIndex;
import cz.oz.mavenhoe.JarInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Ondrej Zizka
 */
public class ArtifactIdAndPackagingMapper extends AbstractJarIndexBasedMapper implements IMavenAxisToJarInfoMapper {
	private static final Logger log = LoggerFactory.getLogger(ArtifactIdAndPackagingMapper.class);

	public ArtifactIdAndPackagingMapper(JarIndex jarIndex) {
		super(jarIndex);
	}



	//@Override
	public JarInfo find( String group, String artifact, String classifier, String version, String fileName, String packaging )
	{
      classifier = StringUtils.defaultString(classifier, JarInfo.NULL_CLASSIFIER_STR);
		log.debug( String.format("  Looking for: %s : %s @%s : %s : %s = %s ", group, artifact, classifier, version, packaging, fileName));

		if( ! ( "jar".equals(packaging) || "pom".equals(packaging) ) ){
			log.debug( "  Not a .jar or .pom, dropping." );
			return null;
		}

		String mapKey = artifact + "|" + packaging;
		log.debug( "    Map key: " + mapKey );
		return jarIndex.getByNameAndPackMap().get( mapKey );
	}




	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}


}// class
