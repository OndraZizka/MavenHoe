
package org.jboss.qa.mavenhoe.mappers;

import org.jboss.qa.mavenhoe.JarInfo;
import org.jboss.qa.mavenhoe.ex.MavenhoeException;

/**
 * Interface for implementations of mappers from Maven axis to a Jar info.
 *
 * @author Ondrej Zizka
 */
public interface IMavenAxisToJarInfoMapper {

	public JarInfo find( String group, String artifact, String classifier, String version, String fileName, String packaging ) throws MavenhoeException;

}// class
