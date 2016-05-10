
##  Extract and build the AS
if [ "" == "x" ] ; then
  #tar -xzvf jboss-5.1.0.GA-src.tar.gz
  cd jboss-5.1.0.GA-src/
  mvn -s ./settings.xml clean install
  mvn dependency:purge-local-repository -DreResolve=false
  mvn -s ./settings.xml clean install | tee ../BUILD.log
  cd ..
fi	



##  Get rid of the downloading numbers - <aven concat's the lines with 0x0D.
grep 'Downloading: .*.jar' BUILD.log | tr '\r' '\n' | grep 'Downloading: ' > downloaded-jars.txt
#cat downloaded-jars.txt | sed 's#\(Downloading: .*.jar\)#\1#g' > x0
#cat downloaded-jars.txt | tr '\r' '\n' 
#awk 1 downloaded-jars.txt > x0
#while read -r LINE ; do echo "$LINE\n" >> x0 ; done < downloaded-jars.txt

mv downloaded-jars.txt x0

##  Remove the unneeded stuff.
cat x0 | sed 's/Downloading: //g' > x1
#cat x | sed 's#http://[^/]*/##g'
cat x1 | sed 's#http://repository.jboss.org/nexus/content/groups/public-jboss/##g' > x2
cat x2 | sed 's#http://nexus.qa.jboss.com:8081/nexus/content/groups/public-all/##g' > x3
cat x3 | sed 's#http://repository.jboss.org/maven2/##g' > x4
cat x4 | sed 's#http://repo1.maven.org/maven2/##g' > x5

rm x0 x1 x2  x3 x4

mv x5 artifact-paths.txt

#cut -d'/' -f artifact-paths.txt

echo > extracted-metadata.txt

while read -r LINE ; do
  ##  org/jboss/ejb3/jboss-ejb3-proxy-impl/1.0.2/jboss-ejb3-proxy-impl-1.0.2.jar
  REV=`echo "$LINE" | rev`;
  #SPACED=`echo "$LINE" | tr '/' ' '`;
  JAR=`echo "$REV" | cut -d/ -f1 | rev`
  VER=`echo "$REV" | cut -d/ -f2 | rev`
  ART=`echo "$REV" | cut -d/ -f3 | rev`
  GRP=`echo "$LINE" | sed "s#$ART/$VER/$JAR##g"`

  ##  Removes the version.
  #FILE=`echo "$JAR" | sed 's#\([-a-z]*\)-\([0-9][.0-9A-Z_]\+\)\(-[a-z0-9]+\)?\.jar#\1#i'`
  #FILE=`echo "$JAR" | sed 's#\([-a-z]*\)-\([0-9][.0-9A-Z_]\+\)\.jar#\1#i'`
  #FILE=`echo "$JAR" | sed 's#^\([-a-z]*\)-\([.0-9_A-Z]\+\)\(-\([a-z][-a-z0-9]*\)\)\?\.jar#\1#i'`  # \2 = version,  \4 = classifier
   FILE=`echo "$JAR" | sed 's#^\([a-z][-a-z0-9]*\)-\([.0-9_A-Z]\+\)\(-\([a-z][-a-z0-9]*\)\)\?\.jar#\1#i'`.jar

  #echo "$JAR $GRP $ART $VER $JAR" >> extracted-metadata.txt
  printf "%-44s %-36s %-40s %-20s %s\n" $FILE $GRP $ART $VER $JAR >> extracted-metadata.txt
done < artifact-paths.txt

