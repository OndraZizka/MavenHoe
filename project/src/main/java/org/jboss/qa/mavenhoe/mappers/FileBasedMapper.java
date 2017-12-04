
package org.jboss.qa.mavenhoe.mappers;

import org.jboss.qa.mavenhoe.ex.MetadataReffersToNonExistentFileMavenhoeException;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.qa.mavenhoe.JarIndex;
import org.jboss.qa.mavenhoe.JarInfo;
import org.jboss.qa.mavenhoe.Options;
import org.jboss.qa.mavenhoe.Options.MappingFilePrecept;
import org.jboss.qa.mavenhoe.ex.MavenhoeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads the info from a file with each line of this form:
 *
 *   <fileNameInZip> <groupId> <artifactId> <version> <artifactFile>
 * 
 *
 * @author Ondrej Zizka
 */
public class FileBasedMapper implements IMavenAxisToJarInfoMapper
{
	private static final Logger log = LoggerFactory.getLogger(FileBasedMapper.class);


	private JarIndex jarIndex;

	private Map<String,String> fileNameByGACVP = new HashMap();
	private Map<String,String> fileNameByGAVP = new HashMap();
	private Map<String,String> fileNameByGAP = new HashMap();
	private Map<String,String> fileNameByAP = new HashMap();

    // Globally strip paths from the jar path defined in the mapping file?
    // TODO: Use MappingFileDictate.isStriPaths() instead, which is per file, not global.
	private boolean stripPaths;

    private boolean ignoreNonExistent = true;

	
	public FileBasedMapper( JarIndex jarIndex, List<MappingFilePrecept> mappingFiles, Options options ) throws MavenhoeException {
		this.jarIndex = jarIndex;
		this.stripPaths = options.isStripPaths();
		this.stripPaths = options.isIgnoreNonExistent();
						
		for( MappingFilePrecept mappingFile : mappingFiles ) {
			try {
				this.parseMappingFile( mappingFile );
			} catch (IOException ex) {
				throw new MavenhoeException("Error parsing metadata file ["+mappingFile.getFile()+"]: " + ex.getMessage(), ex);
			}
		}
	}


	/**
	 * Parses the file and stores a mapping from [groupId:]artifactId -> fileNameInZip.
    *
    * @param mapping.getPath( )  A  CSV-like mapping file from which the metadata are taken.
    *                      One file per line, space separated values.
    *
    * @param fakePoms      Whether we are going to fake .pom files for the jars described in that mapping file.
	 */
	private void parseMappingFile( MappingFilePrecept mapping ) throws IOException
	{
        log.debug("Parsing mapping file: "+mapping.getFile().getPath() + ( this.stripPaths ? " (with stripping)" : "" ) );

        List<String> lines = FileUtils.readLines(mapping.getFile(), "UTF-8");
        Collections.sort(lines);

        for( String line : lines ){

            line = line.trim();
            if( "".equals( line ) )       continue;  // Empty.
            if( line.startsWith("#"))     continue;  // #

            // <fileNameInZip> <groupId> <artifactId> [@<classifier>] <version> <artifactFile>
            String[] parts = StringUtils.split(line, ' ');
            if( parts.length < 5 ){
                log.warn("Invalid line in ["+mapping.getFile( ).getPath()+"]: " + line);
                continue;
            }

            String fileNameInZip = parts[0];
            String groupId       = parts[1];
            String artifactId    = parts[2];
            String classifier    = null;
            String classifierStr = "-";
            int clsOffset = 0;
            if( parts[3].startsWith("@") ){
                classifier = classifierStr = parts[3].substring(1);
                clsOffset = 1;
                if( parts.length < 6 ){
                    log.error("Line should contain 6 tokens when having @classifier; ignoring. ["+mapping.getFile( ).getPath()+"]: " + line);
                    log.info("   <fileNameInZip> <groupId> <artifactId> [@<classifier>] <version> <artifactFile>");
                    continue;
                }
            }
            String version       = parts[3 + clsOffset];
            String artifactFile  = parts[4 + clsOffset];

            groupId = StringUtils.stripEnd(groupId, "/").replace('/', '.');


            // Special var for index lookup - we preserve the whole path, strip when resolving.
            String fileNameInZip_forIndex = fileNameInZip;
            String fileNameInZip_stripped = new File( fileNameInZip ).getName();
            if( this.stripPaths ) {
                fileNameInZip_forIndex = fileNameInZip_stripped;
            }

            // Optionally check if the fileNameInZip exists. If not, skip.
            JarInfo jarInfo = jarIndex.getByFileNameMap().get( fileNameInZip_forIndex );
            if( null == jarInfo ){
                log.warn("    Mapped file not found in index - " + (this.ignoreNonExistent ? "will be ignored: " : "may cause HTTP 409 errors: ") + fileNameInZip_forIndex );
                if( this.ignoreNonExistent )
                   continue;
            }


            // CONSIDER: Is it ok to rely on the suffix?
            String packaging = StringUtils.substringAfterLast( fileNameInZip_stripped, "." );
            if( "".equals(packaging) ){
                log.warn("File name without suffix in '{}': {}", new Object[]{ mapping.getFile( ), fileNameInZip });
            }

            log.debug( String.format("  Adding to map: %s <- %s:%s:%s:%s:%s  %s", fileNameInZip,  groupId, artifactId, classifierStr, version, packaging, artifactFile));

            String prev;
            String key;

            prev = this.fileNameByGACVP.put( key = groupId+":"+artifactId+":"+classifierStr+":"+version+":"+packaging, fileNameInZip );
            if( null != prev )  log.warn("    Duplicate key for GACVP map: "+key);

            prev = this.fileNameByGAVP.put( key = groupId+":"+artifactId+":"+version+":"+packaging, fileNameInZip );
            if( null != prev )  log.warn("    Duplicate key for GAVP map: "+key);

            prev = this.fileNameByGAP .put( key = groupId+":"+artifactId+":"            +packaging, fileNameInZip );
            if( null != prev )  log.warn("    Duplicate key for GAP  map: "+key);

            prev = this.fileNameByAP  .put( key =             artifactId+":"            +packaging, fileNameInZip );
            if( null != prev )  log.warn("    Duplicate key for  AP  map: "+key);


            // Serve fake poms for jars?  See the -fakepoms  param.
            if( mapping.isFakePoms() && "jar".equals( packaging ) )
            {
                String pomFileName = StringUtils.removeEnd( fileNameInZip, ".jar" ) + ".pom";

                log.debug( String.format("    Adding fake .pom: %s <- %s:%s:%s:%s:%s  %s", pomFileName,  groupId, artifactId, classifierStr, version, packaging, ""));

                prev = this.fileNameByGACVP.put( groupId+":"+artifactId+":"+classifierStr+":"+version+":pom", pomFileName );
                if( null != prev )  log.warn("    Duplicate key for GACVP map: "+key);

                prev = this.fileNameByGAVP.put( groupId+":"+artifactId+":"+version+":pom", pomFileName );
                if( null != prev )  log.warn("    Duplicate key for GAVP map: "+key);

                prev = this.fileNameByGAP.put(  groupId+":"+artifactId            +":pom", pomFileName );
                if( null != prev )  log.warn("    Duplicate key for GAP  map: "+key);

                prev = this.fileNameByAP.put(               artifactId            +":pom", pomFileName );
                if( null != prev )  log.warn("    Duplicate key for  AP  map: "+key);
            }
        }
    }




	/**
	 * Finds the requested artifact in this mapper's index (built from the file).
	 * The index contains "supposed filename", which is a filename expected to be present
	 * in some group of jar's (e.g. some project's binary distribution directory).
	 * This assumes that the file name only appears once in all indexed dir's (-i param).
	 *
	 * @returns  JarInfo
	 * @throws MetadataReffersToNonExistentFileMavenhoeException  when the requested jar 
	 *         is mentioned in the mapping file, but the file is not found in indexed dirs.
	 */
	//@Override
	public JarInfo find( String group, String artifact, String classifier, String version, String fileName, String packaging ) throws MetadataReffersToNonExistentFileMavenhoeException
	{
        String clsStr = StringUtils.defaultString(classifier, JarInfo.NULL_CLASSIFIER_STR);
		log.debug( String.format("  Looking for: %s : %s @%s : %s : %s / %s ", group, artifact, clsStr, version, packaging, fileName));
		String try1_GACVP = group + ":" + artifact + ":" + clsStr + ":" + version + ":" + packaging;
		String try1_GAVP  = group + ":" + artifact + ":"                + version + ":" + packaging;
		String try2_GAP   = group + ":" + artifact + ":"                                + packaging;
		log.debug(               "    Thus  for: '"+try1_GACVP+"', then '"+try1_GAVP+"', then '"+try2_GAP+"'");

		// Try G:A:C:V:P first.
		String supposedFileNameInZip = this.fileNameByGACVP.get( try1_GACVP );

		// Try G:A:V:P second.
		if( null == supposedFileNameInZip ){
            supposedFileNameInZip = this.fileNameByGAVP.get( try1_GAVP );
        }

		// Fallback to only G:A:P
		if( null == supposedFileNameInZip ){
			supposedFileNameInZip = this.fileNameByGAP.get( try2_GAP );
		}

		// Nothing found?
		if( null == supposedFileNameInZip )
			return null;


		// Strip path to a base name.
        // TODO: Change to mapping.isStripPaths().
		log.trace( "    Supposed file name: " + supposedFileNameInZip + ( this.stripPaths ? " (+ to be stripped)" : "") );
		if( this.stripPaths )
			//supposedFileNameInZip = StringUtils.substringAfterLast( supposedFileNameInZip, "/");
			supposedFileNameInZip = new File( supposedFileNameInZip ).getName();
		log.debug( "    Supposed file name: " + supposedFileNameInZip + ( this.stripPaths ? " (stripped)" : "") );


		// Find jar info for that filename.
		JarInfo jarInfo = jarIndex.getByFileNameMap().get( supposedFileNameInZip );
		if( null == jarInfo ){
			throw new MetadataReffersToNonExistentFileMavenhoeException( supposedFileNameInZip );
		}
		return jarInfo;
	}


    @Override
    public String toString() {
		return this.getClass().getSimpleName() + "{ "+ this.getFile() + " }";
	}


    private String getFile() {
        return "TODO: Keep metadata file path.";
    }


    // <editor-fold defaultstate="collapsed" desc="get/set">
    public boolean isIgnoreNonExistent() {
      return ignoreNonExistent;
    }

    public FileBasedMapper setIgnoreNonExistent( boolean ignoreNonExistent ) {
        this.ignoreNonExistent = ignoreNonExistent;
        return this;
    }

    public boolean isStripPaths() {
      return stripPaths;
    }

    public FileBasedMapper setStripPaths( boolean stripPaths ) {
        this.stripPaths = stripPaths;
        return this;
    }

    public Map<String, String> getMapFileNameByAP() {
      return Collections.unmodifiableMap( fileNameByAP );
    }

    public Map<String, String> getMapFileNameByGAP() {
      return Collections.unmodifiableMap( fileNameByGAP );
    }

    public Map<String, String> getFileNameByGAVPMap() {
      return Collections.unmodifiableMap( fileNameByGAVP );
    }

    // </editor-fold>


}// class
