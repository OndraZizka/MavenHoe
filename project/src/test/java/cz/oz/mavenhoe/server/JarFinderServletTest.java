package cz.oz.mavenhoe.server;

import java.io.InputStream;
import java.io.File;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import cz.oz.mavenhoe.JarInfo;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Test;
import org.mortbay.jetty.Response;
import static org.junit.Assert.*;

/**
 *
 * @author ondra
 */
public class JarFinderServletTest {

   private static final String TEST_QUERY_STRING = "mvnPath=/postgresql/postgresql/8.2-510/postgresql-8.2-510.pom";
   private static final String TEST_REQUEST_URL = "http://localhost:17283/jars";

   static HttpServletRequest req = new HttpServletRequest() {

      final StringBuffer urlSB = new StringBuffer( TEST_REQUEST_URL );

      public String getQueryString() {
         return TEST_QUERY_STRING;
      }
      
      public StringBuffer getRequestURL() {
         return urlSB;
      }

      // <editor-fold defaultstate="collapsed" desc="other overrides">
      public String getAuthType() {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public Cookie[] getCookies() {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public long getDateHeader(String name) {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public String getHeader(String name) {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public Enumeration getHeaders(String name) {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public Enumeration getHeaderNames() {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public int getIntHeader(String name) {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public String getMethod() {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public String getPathInfo() {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public String getPathTranslated() {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public String getContextPath() {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public String getRemoteUser() {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public boolean isUserInRole(String role) {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public Principal getUserPrincipal() {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public String getRequestedSessionId() {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public String getRequestURI() {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public String getServletPath() {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public HttpSession getSession(boolean create) {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public HttpSession getSession() {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public boolean isRequestedSessionIdValid() {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public boolean isRequestedSessionIdFromCookie() {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public boolean isRequestedSessionIdFromURL() {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public boolean isRequestedSessionIdFromUrl() {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public Object getAttribute(String name) {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public Enumeration getAttributeNames() {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public String getCharacterEncoding() {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public int getContentLength() {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public String getContentType() {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public ServletInputStream getInputStream() throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public String getParameter(String name) {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public Enumeration getParameterNames() {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public String[] getParameterValues(String name) {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public Map getParameterMap() {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public String getProtocol() {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public String getScheme() {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public String getServerName() {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public int getServerPort() {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public BufferedReader getReader() throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public String getRemoteAddr() {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public String getRemoteHost() {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public void setAttribute(String name, Object o) {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public void removeAttribute(String name) {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public Locale getLocale() {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public Enumeration getLocales() {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public boolean isSecure() {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public RequestDispatcher getRequestDispatcher(String path) {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public String getRealPath(String path) {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public int getRemotePort() {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public String getLocalName() {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public String getLocalAddr() {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      public int getLocalPort() {
         throw new UnsupportedOperationException("Not supported yet.");
      }
      // </editor-fold>
   };




   /**
    * Test of doGet method, of class JarFinderServlet.
    */
   public void xtestDoGet() throws Exception {
      
      System.out.println("doGet");

      HttpServletResponse resp = null;
      JarFinderServlet instance = null;
      instance.doGet(req, resp);
      // TODO review the generated test code and remove the default call to fail.
      fail("The test case is a prototype.");
   }


   
   /**
    * Test of dispatchFakePom method, of class JarFinderServlet.
    */
   @Test
   public void testDispatchFakePom() throws Exception {
      System.out.println("dispatchFakePom");

      JarInfo requestedArtifact = new JarInfo("postgresql", "8.2-510", "org.postgresql",
              "/postgresql/postgresql/8.2-510/postgresql-8.2-510.pom", "postgresql-8.2-510.pom", null, "pom");

      JarInfo artifactInfo = new JarInfo("MyName", "0.0.0-VER", "org.mygroup.qa", "some/path.jar", "fileName.jar", new File("/base/dir"), "pack");


      // Where to print the result.
      //final PrintStream os = System.out;
      ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
      final PrintStream ps = new PrintStream( baos );

      /**  A mock implenting all methods used in dispatchFakePom(). */
      HttpServletResponse resp = new Response(null){
         @Override
         public ServletOutputStream getOutputStream() throws IOException {
            return new MyServletOutputStream( ps );
         }
         @Override public void setContentType(String contentType) { }
      };

      JarFinderServlet.dispatchFakePom(requestedArtifact, artifactInfo, req, resp);
      
      String resultPom = baos.toString();

      // All parts are in place?
      /*
      assertTrue( resultPom.contains("MyName") );
      assertTrue( resultPom.contains("0.0.0-VER") );
      assertTrue( resultPom.contains("org.mygroup.qa") );
      //assertTrue( resultPom.contains("some/path.jar") );
      assertTrue( resultPom.contains("fileName.jar") );
      assertTrue( resultPom.contains("pack") );
       */

      /*
         pomTemplate = pomTemplate.replace("@GRP@", requestedArtifact.getGroup() );
         pomTemplate = pomTemplate.replace("@ART@", requestedArtifact.getName() );
         pomTemplate = pomTemplate.replace("@VER@", requestedArtifact.getVersion() );
         pomTemplate = pomTemplate.replace("@PACK@", requestedArtifact.getPackaging() );
         pomTemplate = pomTemplate.replace("@NAME@", requestedArtifact.getFileName() );
         pomTemplate = pomTemplate.replace("@DESC@", fakePom.toStringLong() );
       */

      assertTrue( resultPom, resultPom.contains("<groupId>org.postgresql</groupId>") );
      assertTrue( resultPom, resultPom.contains("<artifactId>postgresql</artifactId>") );
      assertTrue( resultPom, resultPom.contains("<version>8.2-510</version>") );
      assertTrue( resultPom, resultPom.contains("<name>postgresql-8.2-510.pom</name>") );
      //assertTrue( resultPom, resultPom.contains("<description>/postgresql/postgresql/8.2-510/postgresql-8.2-510.pom") );
      assertTrue( resultPom, resultPom.contains("<url>"+TEST_REQUEST_URL+"?"+TEST_QUERY_STRING+"</url>") );
      assertTrue( resultPom, resultPom.contains("<packaging>pom</packaging>") );

      


      // Same as reference result?
      String POM_XML_RESOURCE_PATH = "cz/oz/mavenhoe/server/FakePomResult.xml";
      InputStream pomIS = JarFinderServlet.class.getClassLoader().getResourceAsStream( POM_XML_RESOURCE_PATH );
      String referenceResultPom = IOUtils.toString(pomIS);
      assertEquals( referenceResultPom, resultPom );
   }
}




/**
 *  ServletOutputStream implementation which writes everything to the given PrintStream; System.out by default.
 * @author ondra
 */
class MyServletOutputStream extends ServletOutputStream {

   private final PrintStream out;

   public MyServletOutputStream( PrintStream out ) {
      this.out = out;
   }
   public MyServletOutputStream() {
      this.out = System.out;
   }

   // <editor-fold defaultstate="collapsed" desc="overrides">
   public void write(byte[] buf, int off, int len) {
      out.write(buf, off, len);
   }

   public void write(int b) {
      out.write(b);
   }

   public void println(Object x) {
      out.println(x);
   }

   public void println(String x) {
      out.println(x);
   }

   public void println(char[] x) {
      out.println(x);
   }

   public void println(double x) {
      out.println(x);
   }

   public void println(float x) {
      out.println(x);
   }

   public void println(long x) {
      out.println(x);
   }

   public void println(int x) {
      out.println(x);
   }

   public void println(char x) {
      out.println(x);
   }

   public void println(boolean x) {
      out.println(x);
   }

   public void println() {
      out.println();
   }

   public PrintStream printf(Locale l, String format, Object... args) {
      return out.printf(l, format, args);
   }

   public PrintStream printf(String format, Object... args) {
      return out.printf(format, args);
   }

   public void print(Object obj) {
      out.print(obj);
   }

   public void print(String s) {
      out.print(s);
   }

   public void print(char[] s) {
      out.print(s);
   }

   public void print(double d) {
      out.print(d);
   }

   public void print(float f) {
      out.print(f);
   }

   public void print(long l) {
      out.print(l);
   }

   public void print(int i) {
      out.print(i);
   }

   public void print(char c) {
      out.print(c);
   }

   public void print(boolean b) {
      out.print(b);
   }

   public PrintStream format(Locale l, String format, Object... args) {
      return out.format(l, format, args);
   }

   public PrintStream format(String format, Object... args) {
      return out.format(format, args);
   }

   public void flush() {
      out.flush();
   }

   public void close() {
      out.close();
   }

   public boolean checkError() {
      return out.checkError();
   }

   public PrintStream append(char c) {
      return out.append(c);
   }

   public PrintStream append(CharSequence csq, int start, int end) {
      return out.append(csq, start, end);
   }

   public PrintStream append(CharSequence csq) {
      return out.append(csq);
   }// </editor-fold>
}
