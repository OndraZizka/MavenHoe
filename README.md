
# Mavenhoe – fake Maven repository from .jar's in local directory tree

## Purpose

QA dept often needs to run a maven project with “faked” dependencies – the actual .jar files must be taken from a product's distribution, which are not in any Maven repository (like EAP's `.zip` or RPM distribution).

This utility is one of the ways to solve this problem. It scans a given directory for .jar files, indexes them, and opens a server acting as a Maven repository, in the sense of serving the indexed `.jar` files.

Which file will be server is determined by match of strings in the provided Maven URL path (`localhost:17283/<group>/<artifact>/<version>/<filename>.jar`). This algorithm is a matter of future improvement. Using static mapping file is a possibility.

## Usage

### Configuration

Prepare a mapping file (see e.g. extracted-metadata.txt.zip in the sources) :


    jboss-managed.jar      org/jboss/man/             jboss-managed      2.1.0.SP1     jboss-managed-2.1.0.SP1.jar
    getopt.jar             gnu-getopt/                getopt             1.0.12-brew   getopt-1.0.12-brew.jar
    jboss-kernel.jar       org/jboss/microcontainer/  jboss-kernel       2.0.6.GA      jboss-kernel-2.0.6.GA.jar
    jboss-logging-spi.jar  org/jboss/logging/         jboss-logging-spi  2.1.0.GA      jboss-logging-spi-2.1.0.GA.jar
    ...

The first column is the `filename` in the `.zip`; Then `groupId` (with either slashes or dots), `artifactId`, `version`, and `artifact file name`, respectively. This file maps these metadata (when requested) to the particular file.

This file can be prepared e.g. from the local `~/.m2/repository`:

    cd ~/.m2/repository && find org/some/groupId -name *.jar | sed -E 's#(.*)/([^/]+)/([^/]+)/([^/]+\.jar)#\4 \1 \2 \3 \4#' > /tmp/list && cd -
    
Also, Maven Appassembler plugin is capable of doing such lists when building the zip.


## Maven project preparation

* Disable the central repository; see http://community.jboss.org/thread/89912 . One (IMO the best) option is to override it in `pom.xml`:

      <repository>
          <id>central</id>
          <url>http://some.url</url>
          <snapshots><enabled>false</enabled></snapshots>
          <releases><enabled>false</enabled></releases>
      </repository>

* Add this to your .pom:

      <repository>
          <id>mavenhoe-repo</id>
          <url>http://localhost:17283/jars?mvnPath=</url>
      </repository>

* Alternatively, you can also add the repo to `~/.m2/settings.xml` (or any path and use `mvn -s settings-local.xml`):

      <?xml version="1.0" encoding="UTF-8"?>
      <settings>

      <localRepository>/home/ondra/work/hbn/runner/EAP-5.1/work-space/m2repo</localRepository>

      <profiles>
        <!-- Mavenhoe fake repository -->
        <profile>
          <id>mavenhoe-repo</id>
          <activation><activeByDefault>true</activeByDefault></activation>
          <repositories>
            <repository>
              <id>mavenhoe</id>
              <url>http://localhost:17283/jars?mvnPath=</url>
            </repository>
          </repositories>
        </profile>
      </profiles>

      </settings>

## Tryout / debugging

Run Mavenhoe:

    java -jar Mavenhoe.jar  <path-to-EAP>

and try:

  * http://localhost:17283/status – Will list all indexed jars.
  * http://localhost:17283/jars?mvnPath=org/jboss/whatever/whatever/5.1.0.GA/whatever-5.1.0.GA.jar – Should give 404
  * http://localhost:17283/jars?mvnPath=org/jboss/whatever/hibernate-core/3.3.2.GA_CP03/hibernate-core-3.3.2.GA_CP03.jar – Should let you download the hibernate-core-3.3.2.GA_CP03.jar
  * http://localhost:17283/jars?mvnPath=foo/revolver/5.1.0.GA/foobar-5.1.0.GA.jar
    * Note how Mavenhoe (cur.ver.) only cares about the artifact name part of the Maven-style path, revolver.
       Should let you download the resolver.jar
       Also note that it looks for the best match. That's because the name in the product may differ from the Maven artifact name.
    * “Best match” removed, didn't provide good results.
    * Currently, it's an OR combination of
        1. FileBasedMapper – see the mapping file above
        2. ArtifactIdMapper – looks for <artifactId>.jar .
    * This is a subject to become configurable in some simple way

 * http://localhost:17283/shutdown – Shuts the server down.

## Sample log output:

    DEBUG OrMapper   Looking for: jdom:jdom:1.0 = jdom-1.0.jar
    DEBUG FileBasedMapper   Looking for: jdom:jdom:1.0 = jdom-1.0.jar
    DEBUG FileBasedMapper     Thus  for: 'jdom:jdom'
    DEBUG FileBasedMapper     Supposed file name: null
    DEBUG ArtifactIdMapper   Looking for: jdom:jdom:1.0:jar = jdom-1.0.jar
    DEBUG ArtifactIdMapper     Supposed file name: jdom
    DEBUG org.jboss
