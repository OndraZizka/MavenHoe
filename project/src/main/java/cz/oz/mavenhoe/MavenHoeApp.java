package cz.oz.mavenhoe;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import javax.servlet.UnavailableException;

import cz.oz.mavenhoe.mappers.ArtifactIdAndPackagingMapper;
import cz.oz.mavenhoe.mappers.FileBasedMapper;
import org.apache.log4j.LogManager;
import cz.oz.mavenhoe.Options.MappingFilePrecept;
import cz.oz.mavenhoe.ex.MavenhoeException;
import cz.oz.mavenhoe.mappers.AndMapper;
import cz.oz.mavenhoe.mappers.IMavenAxisToJarInfoMapper;
import cz.oz.mavenhoe.mappers.OrMapper;
import cz.oz.mavenhoe.mappers.PackagingFiltrerMapper;
import cz.oz.mavenhoe.server.RunInJetty;
import cz.oz.mavenhoe.util.Log4jLoggerFlusher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 */
public class MavenHoeApp {
    private static final Logger log = LoggerFactory.getLogger(MavenHoeApp.class);

    private static final String CMD_LINE_USAGE =
            "  mavenhoe (-map <mapping-file> | -i (-fakepoms)? <dir-tree-with-jars> | -ipom <dir-tree-with-poms> )+\n" +
            "  where " +
            "      * <mapping-file> is an explicit G:A:V to file mapping text file (see documentation);\n" +
            "      * <dir-tree-with-jars> is a dir with .jar files to serve, -fakepoms will serve made-up pom.xml files;\n" +
            "      * <dir-tree-with-poms> is a dir with pom.xml's from which the metadata can be taken.\n\n" +
            "  At least one must be present. They supplement each other.";

    private JarIndex jarIndex;

    private References references = new References();


    /**
     * main() - wrapper for main2() - calls System.exit() on non-zero return val.
     */
    public static void main(String[] args) {
        int ret = main2(args);
        if (ret != 0) {
            System.exit(ret);
        }
    }


    /**
     * main() - processes args, creates App object and calls runApp().
     * Wrapped by main for test purposes.
     */
    public static int main2(String[] args) {
        String version = null;
        try {
            version = MavenHoeApp.class.getPackage().getImplementationVersion();
        } catch (Throwable ex) {
            ;
        }
        if (version == null) version = "";

        log.info("Starting Mavenhoe " + version + " - fake Maven repository.");


        // Create the options object.
        Options options;
        try {
            options = Options.createFromParams(args);
        } catch (MavenhoeException ex) {
            System.err.println("  " + ex.getMessage());
            System.err.println("\n  Usage:\n" + CMD_LINE_USAGE + "\n");
            return 1;
        }


        // Different configuration profile?
        String logProfile = options.getLogProfile();
        if (logProfile != null) {
            String logPath = "log4j-" + logProfile + ".properties";
            log.debug("  Loading log config from: " + logPath);
            URL conf = MavenHoeApp.class.getClassLoader().getResource(logPath);
            if (null == conf) {
                log.warn("  Can't find logging profile: " + logPath);
            } else {
                LogManager.resetConfiguration();
                org.apache.log4j.PropertyConfigurator.configure(conf);
            }
        }


        // Validate options.
        try {
            Options.validateOptions(options);
        } catch (MavenhoeException ex) {
            System.err.println("  " + ex.getMessage());
            return 2;
        }


        // Run the app.
        new MavenHoeApp().runApp(options);

        return 0;
    }


    private void runApp(Options options) {

        try {
            log.info("Creating jar index... ");
            this.jarIndex = createJarIndex(options.getInclExclList());


            // Prepare a mapper.

            log.info("Creating the mapper from: ");
            for (MappingFilePrecept mapping : options.getMappingPrecepts()) {
                log.info("    " + mapping.getFile());
            }

            IMavenAxisToJarInfoMapper mapper = createDefaultMapper(this.jarIndex, options, new Thief<FileBasedMapper>() {
                public void conceal(FileBasedMapper fbm) {
                    MavenHoeApp.this.references.setFileBasedMapper(fbm);
                }
            });


            // Run the server.
            log.info("Running the server... ");
            Log4jLoggerFlusher.flushLoggerByLoggingSpaces(log, 4, 512);
            //Log4jLoggerFlusher.flushLoggerByLoggingSpaces( log, 4, 512 );
            this.runServer(mapper);
        } catch (MavenhoeException ex) {
            log.error("Error when running the server: " + ex.getMessage(), ex);
        }
    }


    /**
     * Creates an index of JAR files in the given directory.
     *
     * @deprecated in favor of createJarIndex(List<InclExcl> inclExclList)
     */
    private static JarIndex createJarIndex(File dir) {
        try {
            return new JarIndex(dir);
        } catch (IOException ex) {
            String msg = "Error when indexing: " + ex.getMessage();
            //log.error(msg);
            throw new RuntimeException(ex);
        }
    }


    /**
     * Creates an index of JAR files, considering the given included and excluded directories.
     */
    private JarIndex createJarIndex(List<InclExclItem> inclExclList) {
        try {
            return new JarIndex(inclExclList);  //.sort();
        } catch (IOException ex) {
            String msg = "Error when indexing: " + ex.getMessage();
            //log.error(msg);
            throw new RuntimeException(ex);
        }
    }


    /**
     * Starts the server.
     */
    private void runServer(IMavenAxisToJarInfoMapper mapper) throws MavenhoeException {

        // Run Jetty
        try {
            new RunInJetty(this, mapper).run();
        } catch (UnavailableException ex) {
            throw new MavenhoeException("Servlet is unavailable: " + ex.getMessage(), ex);
        }
    }


    /**
     * Creates a default mapper, which will use given artifact index, and obey the given options.
     * The options also contain the mapping files list.
     */
    private static IMavenAxisToJarInfoMapper createDefaultMapper(JarIndex artifactIndex, Options options, Thief<FileBasedMapper> thief) throws MavenhoeException {
        List<MappingFilePrecept> mappings = options.getMappingPrecepts();

        IMavenAxisToJarInfoMapper mapper =
            new AndMapper(
                new OrMapper(
                    new PackagingFiltrerMapper("jar"),
                    new PackagingFiltrerMapper("pom")
                ),
                new OrMapper(
                    thief.steal(
                            new FileBasedMapper(artifactIndex, mappings, options)/*.setIgnoreNonExistent(true).setStripPaths(true)*/
                    ),
                    new ArtifactIdAndPackagingMapper(artifactIndex)
                )
            );

        return mapper;
    }


    public JarIndex getJarIndex() {
        return jarIndex;
    }

    public References getReferences() {
        return references;
    }
}
