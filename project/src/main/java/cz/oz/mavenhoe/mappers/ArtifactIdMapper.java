
package cz.oz.mavenhoe.mappers;

import cz.oz.mavenhoe.JarIndex;
import cz.oz.mavenhoe.JarInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Ondrej Zizka
 */
public class ArtifactIdMapper extends AbstractJarIndexBasedMapper implements IMavenAxisToJarInfoMapper {
	private static final Logger log = LoggerFactory.getLogger(ArtifactIdMapper.class);

	public ArtifactIdMapper(JarIndex jarIndex) {
		super(jarIndex);
	}



	//@Override
	public JarInfo find( String group, String artifact, String classifier, String version, String fileName, String packaging )
	{
		log.debug( String.format("  Looking for: %s : %s @%s : %s : %s = %s ", group, artifact, classifier, version, packaging, fileName));
		String supposedFileNameInZip = artifact;  /* +".jar"; */ // .jar is removed in JarInfo.name. TODO: Don't remove?
		if( ! "jar".equals(packaging) ){
			log.debug( "  Not a .jar, dropping." );
			return null;
		}
		log.debug( "    Supposed file name: " + supposedFileNameInZip );

		return jarIndex.getByNameMap().get( supposedFileNameInZip );
	}




	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}


}
