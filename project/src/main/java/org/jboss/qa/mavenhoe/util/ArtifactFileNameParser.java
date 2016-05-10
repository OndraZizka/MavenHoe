
package org.jboss.qa.mavenhoe.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jboss.qa.mavenhoe.JarInfo;

/**
 *
 * @author Ondrej Zizka
 */
public class ArtifactFileNameParser {

   public static JarInfo parseFileName( String fileName ) {

      //                           some name       version        classifier        extension
      final String PATTERN = "^([a-z][-a-z0-9]*)-([0-9][-.0-9_A-Za-z]+)(-([a-z][-a-z0-9]*))?\\.(jar|pom)";

      Pattern pat = Pattern.compile( PATTERN );

      Matcher mat = pat.matcher( fileName );

      if( !mat.matches() )
         return null;

      String name       = mat.group(1);
      String version    = mat.group(2);
      String classifier = mat.group(4);
      String packaging  = mat.group(5);

      return new JarInfo( name, version, null, null, fileName, null, mat.group(5) );
      
   }

}// class
