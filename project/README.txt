
To test, run  the project 

   java -jar Mavenhoe.jar  <path-to-EAP>


and try:


http://localhost:17283/status

  - Will list all indexed jars.

http://localhost:17283/jars?mvnPath=org/jboss/whatever/whatever/5.1.0.GA/whatever-5.1.0.GA.jar

  - Should give 404

http://localhost:17283/jars?mvnPath=org/jboss/whatever/hibernate-core/3.3.2.GA_CP03/hibernate-core-3.3.2.GA_CP03.jar

  - Should let you download the hibernate-core-3.3.2.GA_CP03.jar

http://localhost:17283/jars?mvnPath=foo/revolver/5.1.0.GA/foobar-5.1.0.GA.jar
  - Note how Mavenhoe (cur.ver.) only cares about the artifact name part of the Maven-style path, revolver.
  - Should let you download the resolver.jar
    Also note that it looks for the best match. That's because the name in the product may differ from the Maven artifact name,

http://localhost:17283/shutdown

  - Shuts the server down.



TODO:  normalizeName()  - split by dashes and _, remove numbers and dots; then compare these parts. Esp. first ones.







GoogleCode:

Fake Maven repository from .jar's in local  directory tree.
===========================================================

QA dept often needs to run a maven project with "faked" dependencies - the actual .jar files must be taken from a product's distribution, which are not in any Maven repository (like EAP's .zip or RPM distribution).

This utility is one of the ways to solve this problem. It scans a given directory for .jar files, indexes them, and opens a server acting as a Maven repository, in the sense of serving the indexed .jar files.

Which file will be server is determined by match of strings in the provided Maven URL path (localhost:17283/group/artifact/version/filename.jar). This algorithm is a matter of future improvement. Using static mapping file is a possibility.