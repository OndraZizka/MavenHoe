
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
public class ArtifactIdSimilarityMapper extends AbstractJarIndexBasedMapper implements IMavenAxisToJarInfoMapper
{
  private static final Logger log = LoggerFactory.getLogger(ArtifactIdSimilarityMapper.class);


	private static final int LEV_DIST_RATIO_PERCENT = 20;


	public ArtifactIdSimilarityMapper(JarIndex jarIndex) {
		super(jarIndex);
	}


	//@Override
	public JarInfo find( String group, String artifact, String classifier, String version, String fileName, String packaging ) {

			JarInfo bestMatch = null;
			int minLevDist = 999999;
			for( JarInfo jarInfo : jarIndex.getList() ) {
				int curLevDist = StringUtils.getLevenshteinDistance(jarInfo.getName(), artifact);
				log.debug( "  "+jarInfo.getName() +" vs. "+ artifact + "  Levensh. distance: "+curLevDist );
				if( curLevDist < minLevDist ){
					minLevDist = curLevDist;
					bestMatch = jarInfo;
				}
			}
			if( null == bestMatch )
				return null;

			// If even the best match is too different, don't return it.
			int distanceRatio = 100 * minLevDist / Math.max( bestMatch.getName().length(), artifact.length() );
			log.debug("  Min dist: "+minLevDist+" for "+bestMatch.getName()+" -> Distance ratio: "+distanceRatio);

			if( distanceRatio > LEV_DIST_RATIO_PERCENT )
				return null;
			else
				return bestMatch;

	}

}
