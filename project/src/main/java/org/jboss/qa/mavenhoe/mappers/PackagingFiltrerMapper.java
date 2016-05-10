
package org.jboss.qa.mavenhoe.mappers;

import org.jboss.qa.mavenhoe.JarInfo;
import org.jboss.qa.mavenhoe.ex.MavenhoeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 *
 * @author Ondrej Zizka
 */
public class PackagingFiltrerMapper implements IMavenAxisToJarInfoMapper {
	private static final Logger log = LoggerFactory.getLogger(PackagingFiltrerMapper.class);

	private final String packaging;

	public PackagingFiltrerMapper( String packaging ) throws MavenhoeException {
		if( null == packaging )
			throw new MavenhoeException( PackagingFiltrerMapper.class.getSimpleName() +": packaging can't be null.");
		this.packaging = packaging;
	}



	//@Override
	public JarInfo find( String group, String artifact, String classifier, String version, String fileName, String packaging ) throws MavenhoeException
	{
		if( ! this.packaging.equals( packaging ) ){
			log.trace("Packaging '"+packaging+"' is not '"+this.packaging+"'.");
			return null;
		}
		return JarInfo.DUMMY_INSTANCE;
	}


	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "{"+this.packaging+"}";
	}



}// class
