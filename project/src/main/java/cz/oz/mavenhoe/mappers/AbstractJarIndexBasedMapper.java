
package cz.oz.mavenhoe.mappers;

import cz.oz.mavenhoe.JarIndex;

/**
 * This is created with the JarIndex and keeps it.
 *
 * @author Ondrej Zizka
 */
public abstract class AbstractJarIndexBasedMapper implements IMavenAxisToJarInfoMapper {

	JarIndex jarIndex;

	public AbstractJarIndexBasedMapper( JarIndex jarIndex ){
		this.jarIndex = jarIndex;
	}
	


}// class
