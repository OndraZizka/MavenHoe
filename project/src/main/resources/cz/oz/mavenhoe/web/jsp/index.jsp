<%@page import="org.jboss.qa.mavenhoe.*" %>
<%--<jsp:useBean beanName="jarIndex" scope="application" id="jarIndex" type="org.jboss.qa.mavenhoe.JarIndex" />--%>

<%! JarIndex jarIndex; %>
<% jarIndex = (JarIndex) application.getAttribute("jarIndex"); %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <title>Mavenhoe - status</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  </head>
  <body>
    JSP in Jetty test <br/>
    <%if( jarIndex != null ){%>
      jarIndex is not null.
      <%--= jarIndex.getList() --%>
      <ul>
      <%for( JarInfo jarInfo : jarIndex.getList() ){%>
        <li><%= jarInfo %></li>
      <%}%>
      </ul>
    <%}%>
  </body>
</html>
