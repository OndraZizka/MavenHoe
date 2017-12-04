
package org.jboss.qa.mavenhoe;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.*;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang.StringUtils;
import org.jboss.qa.mavenhoe.ex.MavenhoeException;
import org.jboss.qa.mavenhoe.util.PomParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;





/**
 *
 * Keep it thread-safe!
 *
 * TODO: Create Map<String, List<JarInfo>> to map baseDirs -> .jar's found in it.
 *       Or rename this class to JarIndexesManager and introduce a class  BaseDirJarIndex.
 *       Manager will hold more BaseDirJarIndex-es.
 *
 * TODO: Split .jar's and pom.xml's. Or, make InclExclItem able to carry multiple types, so we could do:
 *         -types jar -i eap/client -types jar,pom -i hibernate/core
 *
 * @author Ondrej Zizka
 */
public class JarIndex
{
    private static final Logger log = LoggerFactory.getLogger(JarIndex.class);


    /**
     *  List of jars found.
     */
    private List<JarInfo> jars = new ArrayList();

    /**
     *  Map -   artifact name -> JarInfo
     */
    private SortedMap<String, JarInfo> byName = new TreeMap();

    /**
     *  Map -   file name -> JarInfo
     */
    private SortedMap<String, JarInfo> byFileName = new TreeMap();

    /**
     *  Map -   artifact name | packaging -> JarInfo
     */
    private SortedMap<String, JarInfo> byNameAndPack = new TreeMap();

    /**
     *  Map -   artifact name | version | packaging -> JarInfo
     */
    private SortedMap<String, JarInfo> byNameAndVersionAndPack = new TreeMap();


    /**
     *  Base dirs - each for one scanned directory. Needed to create resourceBase for the server.
     */
    private List<File> baseDirs = new ArrayList();



    /**
     *  Constructor which scans the given directory for .jar's.
     *
     * @param dir
     * @throws IOException
     *
     * @deprecated in favor of JarIndex(List<InclExcl> inclExclList) .
     */
    JarIndex( File dir ) throws IOException {
            baseDirs.add( dir );
            this.addJarsAndPomsFromDirTree( dir, JAR_FILTER, null, false );
    }

    /**
     *  Constructor which scans the given INCLUDE directories for .jar's.
     */
    JarIndex(List<InclExclItem> inclExclList) throws IOException {
        this.indexInclExcl( inclExclList );
    }


    /**
     * This only treats includes; excludes cause an exception.
     */
    private void indexIncludes(List<InclExclItem> inclExclList) throws IOException, MavenhoeException {
        for( InclExclItem inclExcl : inclExclList ) {
            if( inclExcl.getMode() != InclExclItem.Mode.INCLUDE )
                //continue;
                throw new MavenhoeException("Exludes ('-e') not supported when using indexIncludes().");

            File includedFile = inclExcl.getFileObject();
            baseDirs.add( includedFile );
            this.addJarsAndPomsFromDirTree( includedFile, JAR_FILTER, null, inclExcl.fakePoms );
        }
    }


    /**
     * This works as described in class InclExclFilter.
     */
    private void indexInclExcl(List<InclExclItem> inclExclList) throws IOException {

        InclExclFilter wholeFilter = new InclExclFilter(inclExclList, 0, true);
        //List<File> rootIncludes = wholeFilter.getRootIncludes();

        for (int i = 0; i < inclExclList.size(); i++) {
            InclExclItem inclExcl = inclExclList.get(i);

            if( ! ( inclExcl.getType() == InclExclItem.Type.JAR  &&  wholeFilter.isRootInclude(inclExcl) ) ){
                //log.debug(" -- Not a root include of type jar, skipping: "+ inclExcl );
                continue;
            }

            log.debug(" -- Indexing .jar's: "+ inclExcl );

            // +1 - skips the currently scanned dir.
            // TODO: Creating the same filter again only with differing offset is not needed;
            //       Create some InclExclWrapOffsetFilter class, which calls otherFilter.isAllowed(..., offset).
            InclExclFilter inclExclFilter = new InclExclFilter( inclExclList, i+1, true );

            File includedFile = inclExcl.getFileObject();
            baseDirs.add( includedFile );
            this.addJarsAndPomsFromDirTree( includedFile, JAR_FILTER, inclExclFilter, inclExcl.fakePoms );

        }


        // -ipom  - scan for pom.xml's.

        for (int i = 0; i < inclExclList.size(); i++) {
            InclExclItem inclExcl = inclExclList.get(i);

            if( ! ( inclExcl.getType() == InclExclItem.Type.POM ) ){
                //log.debug(" -- Not a pom.xml include, skipping: "+ inclExcl );
                continue;
            }

            log.debug(" -- Indexing pom.xml's: "+ inclExcl );

            File includedFile = inclExcl.getFileObject();
            baseDirs.add( includedFile );
            this.addJarsAndPomsFromDirTree( includedFile, POM_XML_FILTER, null, false );

        }

        // TODO: Support for -ipom and separated scanning for pom.xml .
    }




    synchronized
    public void addJarInfo( JarInfo info ){
        this.jars.add(info);
        this.byName.put( info.getName(), info );
        this.byFileName.put( info.getFileName(), info );
        this.byNameAndPack.put( info.getName()+"|"+info.getPackaging(), info );
        this.byNameAndVersionAndPack.put( info.getName()+"|"+info.getVersion()+"|"+info.getPackaging(), info );
    }

    synchronized
    public boolean removeJarInfo( JarInfo info ){
        this.byName.remove(info.getName());
        this.byFileName.remove(info.getFileName());
        this.byNameAndPack.remove( info.getName()+"|"+info.getPackaging() );
     this.byNameAndVersionAndPack.remove( info.getName()+"|"+info.getVersion()+"|"+info.getPackaging() );
        return jars.remove(info);
    }

    /**
     * Sorts the list of JarInfos by their path.
     * @returns this.
     */
    synchronized
    public JarIndex sort(){
        Collections.sort( this.jars );
        return this;
    }



    private final static IOFileFilter JAR_FILTER = FileFilterUtils.or(
                        FileFilterUtils.directoryFileFilter()
                        ,FileFilterUtils.suffixFileFilter(".jar")
    );

    private final static IOFileFilter POM_XML_FILTER = FileFilterUtils.or(
                        FileFilterUtils.directoryFileFilter()
                        ,FileFilterUtils.nameFileFilter("pom.xml")
    );


    /**
     * Indexes .jar and pom.xml files in the given directory baseDir.
     *
     * @param filter             JAR_FILTER, POM_XML_FILTER and such.
     * @param additionalFilter   used for -i and -e includes and excludes.
     *
     * TODO: Separate .jar's and pom.xml's? Or parametrize this method to do so?
     */
    private void addJarsAndPomsFromDirTree( final File baseDir, IOFileFilter filter, final FileFilter additionalFilter, final boolean fakePoms ) throws IOException {

        // Additional filter - used for -i and -e includes and excludes.
        if( null != additionalFilter ){
            filter = FileFilterUtils.and( filter, FileFilterUtils.asFileFilter(additionalFilter) );
        }


        new DirectoryWalker( filter, -1 )
        {

            @Override
            protected void handleFile( File file, int depth, Collection results ) throws IOException {
                //log.info("  Handling file: " + file.getPath());///

                if( "pom.xml".equals( file.getName() ) ){
                    handlePomXml( file, depth, results );
                }
                else {
                    handleJar( file, depth, results );
                }
            }

            // *.jar
            private void handleJar( File file, int depth, Collection results ) {

                // Name is derived from filename - simply without a suffix.
                String name = StringUtils.removeEndIgnoreCase( file.getName(), ".jar" );

                try{
                    JarFile jarFile = new JarFile(file);
                    Manifest mf = jarFile.getManifest();
                    String version = mf.getMainAttributes().getValue("Implementation-Version");

                    if( version == null ){
                        log.warn("Missing 'Implementation-Version' in MANIFEST.MF: " + file.getPath() );
                    }

                    JarInfo jarInfo = new JarInfo( name, version, null, file.getPath(), file.getName(), baseDir, "jar" );
                    log.debug("   "+jarInfo.toString()); ///
                    JarIndex.this.addJarInfo(jarInfo);

                    // We should also provide fake .pom for this jar - so let's add a "virtual pom" entry to the index.
                    if( fakePoms ){
                        // Replace .jar ext. with .pom to get this virtual file indexed properly.
                        // But leave the original path, for reference. That might change in the future.
                        String pomFileName = StringUtils.removeEnd( file.getName(), ".jar" ) + ".pom";

                        JarInfo fakePomInfo = new JarInfo( name, version, null, file.getPath(), pomFileName, baseDir, "pom" );
                        fakePomInfo.setVirtual( true );
                        log.debug("   "+fakePomInfo.toString()); ///
                        JarIndex.this.addJarInfo(fakePomInfo);
                    }
                }
                catch( IOException ex ) {
                    log.error( "Error handling '"+file.getPath()+"': " + ex.getMessage() ); // TODO: Better handling - propagate up?
                }
            }

            // pom.xml
            private void handlePomXml(File file, int depth, Collection results) {
                try {
                    //log.debug("Parsing: "+file.getPath()); ///
                    JarInfo pomInfo = PomParser.parsePomXml(file, baseDir);
                    log.debug("   " + pomInfo.toString()); ///
                    JarIndex.this.addJarInfo(pomInfo);
                }
                catch (MavenhoeException ex) {
                    log.error( ex.getMessage() ); // TODO: Better handling - propagate up?
                }
            }


            @Override
            protected boolean handleDirectory( File directory, int depth, Collection results ) throws IOException {
                //log.info( "  Handling dir: "+directory.getPath() );
                return true;
            }

            // Sort each dir's content.
            @Override
            protected File[] filterDirectoryContents( File directory, int depth, File[] files ) throws IOException {
                Arrays.sort(files, new Comparator<File>(){
                    public int compare( File f1, File f2 ) {
                        return f1.getAbsolutePath().compareTo( f2.getAbsolutePath() ); // Or getCanonicalPath() ?
                    }
                });
                //return super.filterDirectoryContents(directory, depth, files);
                return files;
            }

            public void doWalk() throws IOException{
                this.walk( baseDir, new LinkedList() );
            }

        }.doWalk();


    }// addJarsFromDirTree()


    // <editor-fold defaultstate="collapsed" desc="get / set">

    /** @returns Unmodifiable list of .jar's in this index. */
    public List<JarInfo> getList(){
        return Collections.unmodifiableList(jars);
    }

    /** Artifact name -> JarInfo map. */
    public Map<String, JarInfo> getByNameMap() {
        return Collections.unmodifiableMap(this.byName);
    }

    /** File name -> JarInfo map. */
    public Map<String, JarInfo> getByFileNameMap() {
        return Collections.unmodifiableMap(this.byFileName);
    }

    /** File name -> JarInfo map. */
    public Map<String, JarInfo> getByNameAndPackMap() {
        return Collections.unmodifiableMap(this.byNameAndPack);
    }

    // </editor-fold>

	
}// class
