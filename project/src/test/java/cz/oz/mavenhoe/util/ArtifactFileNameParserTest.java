/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.oz.mavenhoe.util;

import cz.oz.mavenhoe.JarInfo;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Ondrej Zizka
 */
public class ArtifactFileNameParserTest {

    public ArtifactFileNameParserTest() {
    }


   /**
    * Test of parseFileName method, of class ArtifactFileNameParser.
    */
   @Test
   public void testParseFileName() {
      System.out.println("parseFileName");
      String fileName = "postgresql-8.2-510.jdbc3.jar";

      JarInfo artInfo = ArtifactFileNameParser.parseFileName(fileName);

      assertEquals( fileName, artInfo.getFileName() );
      assertEquals( "postgresql", artInfo.getName() );
      assertEquals( "8.2-510.jdbc3", artInfo.getVersion() );
      assertEquals( "jar", artInfo.getPackaging() );
   }

}