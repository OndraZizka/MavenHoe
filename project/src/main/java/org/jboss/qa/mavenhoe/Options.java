package org.jboss.qa.mavenhoe;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jboss.qa.mavenhoe.ex.MavenhoeException;

/**
 *  Options - the mapping files, included & excluded paths, and some global options.
 * 
 *  @author Ondrej Zizka
 */
public class Options {

    private List<InclExclItem> inclExclList = new ArrayList();
    private List<MappingFilePrecept> mappings;
    private boolean stripPaths = true;
    private boolean ignoreNonExistent = true;

    private String logProfile = null;


    public Options( String mappingFilePath ) {
        this.mappings = new ArrayList();
        this.mappings.add( new MappingFilePrecept( new File( mappingFilePath ) ) );
    }

    private Options( List<MappingFilePrecept> mappingFilesPaths, List<InclExclItem> inclExclList ) {
        this.mappings = mappingFilesPaths;
        this.inclExclList = inclExclList;
    }


    /**  Modes of params parsing. */
    enum ParamsMode { INCLUDE, EXCLUDE, MAP, NONE, INCLUDE_POM };


    /**
    *  Parse Options from the given app's arguments.
    */
    public static Options createFromParams( String[] args ) throws MavenhoeException
    {
        List<InclExclItem> inclExclList = new ArrayList();
        List<MappingFilePrecept> mappings = new ArrayList();

        // First argument is a map if not "-...".
        ParamsMode mode = ParamsMode.MAP;

        // Should Mavenhoe emit fake .pom files for these jars? (Valid poms with made-up G:A:V:P, no deps)
        boolean fakePomsFlag = false;

        Options options = new Options( mappings, inclExclList );
        options.stripPaths = false;


        // For each argument...
        for( int i = 0; i < args.length; i++ ) {
            String arg = args[i];
            if( arg.startsWith("-logProfile=")){  options.logProfile = arg.substring("-logProfile=".length());   continue; }
            if( "-i".equals(arg)){     mode = ParamsMode.INCLUDE; fakePomsFlag = false; continue; }
            if( "-fakepoms".equals(arg)){  fakePomsFlag = true; continue; }
            if( "-e".equals(arg)){     mode = ParamsMode.EXCLUDE; continue; }
            if( "-map".equals(arg)){   mode = ParamsMode.MAP; fakePomsFlag = false; continue; }
            if( "-ipom".equals(arg)){  mode = ParamsMode.INCLUDE_POM; continue; }
            if( "--strip-paths".equals(arg) || "-sp".equals(arg)){  options.stripPaths = true; continue; }
            if( arg.startsWith("-") ){  throw new MavenhoeException("Unknown option: "+arg); }

            File argFile = new File(arg);

            switch( mode ){
                case MAP: mappings.add( options.new MappingFilePrecept( argFile, fakePomsFlag ) ); mode = ParamsMode.INCLUDE; break;
                case INCLUDE: inclExclList.add( new InclExclItem(InclExclItem.Mode.INCLUDE, argFile, InclExclItem.Type.JAR, fakePomsFlag)); break;
                case EXCLUDE: inclExclList.add( new InclExclItem(InclExclItem.Mode.EXCLUDE, argFile, InclExclItem.Type.JAR)); break;
                case INCLUDE_POM: inclExclList.add( new InclExclItem(InclExclItem.Mode.INCLUDE, argFile, InclExclItem.Type.POM)); break;
                // Currently not used, we assume that after -map comes -i, and keep the last -i or -e.
                case NONE: throw new MavenhoeException("What should I do with this param?  => "+arg);
            }
        }

        return options;
    }


    /**
    *   Checks whether all the paths exist.
    */
    public static void validateOptions( Options options ) throws MavenhoeException {

        //List<String> nonexistentPaths = new LinkedList();
        StringBuilder  sb = new StringBuilder();


        // All map files exist?
        for( MappingFilePrecept mapping : options.getMappingPrecepts() ){
            if( mapping.getFile().exists() )  continue;
            sb.append("  ").append( mapping.getFile() ).append(" (map file)\n");
        }


        // All include paths exist?
        for( InclExclItem ie : options.getInclExclList() ){
        if( ie.getMode() == InclExclItem.Mode.EXCLUDE )
        continue;

        File included = ie.getFileObject();
        if( included.exists() ) continue;

        sb.append("  ").append( included.getPath() ).append('\n');
        }

        if( sb.length() != 0 ){
        sb.insert(0, "The following paths do not exist:\n");
        throw new MavenhoeException( sb.toString() );
        }

    }// validateOptions()



    public List<InclExclItem> getInclExclList() {		return this.inclExclList;	}
    public List<MappingFilePrecept> getMappingPrecepts() {		return Collections.unmodifiableList(this.mappings);	}
    public boolean isStripPaths() {		return this.stripPaths; 	}
    public boolean isIgnoreNonExistent() {            return this.ignoreNonExistent;         }
    //public void setIgnoreNonExistent(boolean ignoreNonExistent) {            this.ignoreNonExistent = ignoreNonExistent;         }
    public String getLogProfile() {					return this.logProfile;				}




    // TODO: Use this instead of a simple File.
    // "Mandate", "Precept"
    public class MappingFilePrecept
    {
        private File    path;
        private boolean stripPaths = true;
        private boolean fakePoms   = false;
        private String  basePath = System.getProperty("user.dir");

        public MappingFilePrecept(File path, boolean stripPaths, String basePath) {
            this.path = path;
            this.stripPaths = stripPaths;
            this.basePath = basePath;
        }

        public MappingFilePrecept(File path, boolean fakePoms) {
            this(path);
            this.fakePoms = fakePoms;
        }

        public MappingFilePrecept(File path) {
        this.path = path;
        }

        @Override
        public String toString() {
            return MappingFilePrecept.class.getSimpleName()+"{ "+ (stripPaths ? "(strip)" : "") +" @"+ path +", base: "+basePath+"}";
        }


        public String getBasePath() {					return basePath;				}
        public void setBasePath(String basePath) {					this.basePath = basePath;				}
        public File getFile() {					return path;				}
        public void setPath(File path) {					this.path = path;				}
        public boolean isStripPaths() {					return stripPaths;				}
        public void setStripPaths(boolean stripPaths) {					this.stripPaths = stripPaths;				}
        public boolean isFakePoms() {         return fakePoms;      }
        public void setFakePoms(boolean fakePoms) {         this.fakePoms = fakePoms;      }


    }// class MappingFileDictate


}// class
