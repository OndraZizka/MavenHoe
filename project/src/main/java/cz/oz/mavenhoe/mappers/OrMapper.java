
package cz.oz.mavenhoe.mappers;

import cz.oz.mavenhoe.JarInfo;
import cz.oz.mavenhoe.ex.MavenhoeException;
import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Iterates over child mappers, trying to get JarInfo;
 * Once any returns a JarInfo instance, it is returned,
 * and further mappers are not processed.
 *
 * @author Ondrej Zizka
 * @returns  the first JarInfo instance returned from child mappers;
 *           null if all child mappers returned null.
 * 
 * TODO: Might allow multiple mappers.
 */
public class OrMapper implements IMavenAxisToJarInfoMapper {
	private static final Logger log = LoggerFactory.getLogger(OrMapper.class);

	private final IMavenAxisToJarInfoMapper mapper1;
	private final IMavenAxisToJarInfoMapper mapper2;

	public OrMapper(IMavenAxisToJarInfoMapper mapper1, IMavenAxisToJarInfoMapper mapper2 ) {
		log.debug("Creating OR-mapper with "+mapper1+" OR "+mapper2);
		this.mapper1 = mapper1;
		this.mapper2 = mapper2;
	}



	//@Override
	public JarInfo find(String group, String artifact, String classifier, String version, String fileName, String packaging ) throws MavenhoeException
	{
		log.trace( String.format("  Looking for: %s : %s : %s : %s = %s ", group, artifact, version, packaging, fileName));
		return (JarInfo) ObjectUtils.defaultIfNull(
						this.mapper1.find(group, artifact, classifier, version, fileName, packaging),
						this.mapper2.find(group, artifact, classifier, version, fileName, packaging)
		);
	}


	
	@Override
	public String toString() {
		return "OR-mapper{ "+mapper1+" OR "+mapper2 + " }";
	}



}// class
