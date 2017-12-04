
package cz.oz.mavenhoe.mappers;

import cz.oz.mavenhoe.JarInfo;
import cz.oz.mavenhoe.ex.MavenhoeException;

/**
 * Interface for implementations of mappers from Maven axis to a Jar info.
 *
 * @author Ondrej Zizka
 */
public interface IMavenAxisToJarInfoMapper {

	JarInfo find(String group, String artifact, String classifier, String version, String fileName, String packaging ) throws MavenhoeException;

}
