
package cz.oz.mavenhoe.ex;

/**
 *
 * @author Ondrej Zizka
 */
public class MetadataReffersToNonExistentFileMavenhoeException extends MavenhoeException {

	public MetadataReffersToNonExistentFileMavenhoeException(String supposedFileNameInZip) {
		super(supposedFileNameInZip);
	}

}
