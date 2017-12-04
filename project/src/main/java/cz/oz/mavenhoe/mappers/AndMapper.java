
package cz.oz.mavenhoe.mappers;

import cz.oz.mavenhoe.JarInfo;
import cz.oz.mavenhoe.ex.MavenhoeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AND mapper - all child mappers must return a JarInfo object
 * Use JarInfo.DUMMY_INSTANCE to indicate "true" for the first mapper (filter).
 *
 * @return  the JarInfo returned by the last mapper.
 * @author Ondrej Zizka
 *
 * TODO: Might allow multiple mappers.
 */
public class AndMapper implements IMavenAxisToJarInfoMapper {
	private static final Logger log = LoggerFactory.getLogger(AndMapper.class);

	private final IMavenAxisToJarInfoMapper mapper1;
	private final IMavenAxisToJarInfoMapper mapper2;

	public AndMapper(IMavenAxisToJarInfoMapper mapper1, IMavenAxisToJarInfoMapper mapper2 ) {
		log.debug("Creating AND-mapper with "+mapper1+" AND "+mapper2);
		this.mapper1 = mapper1;
		this.mapper2 = mapper2;
	}



	//@Override
	public JarInfo find( String group, String artifact, String classifier, String version, String fileName, String packaging ) throws MavenhoeException
	{
		log.trace( String.format("  Looking for: %s : %s : %s : %s = %s ", group, artifact, version, packaging, fileName));
		JarInfo i1 = this.mapper1.find(group, artifact, classifier, version, fileName, packaging);
		if( null == i1 ) return null;
		return this.mapper2.find(group, artifact, classifier, version, fileName, packaging);
	}


	
	@Override
	public String toString() {
		return "AND-mapper{ "+mapper1+" AND "+mapper2 + " }";
	}



}// class
