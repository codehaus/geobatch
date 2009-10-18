<%
/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.codehaus.org/
 *  Copyright (C) 2007-2008-2009 GeoSolutions S.A.S.
 *  http://www.geo-solutions.it
 *
 *  GPLv3 + Classpath exception
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
%>
<%@ page contentType="text/html" import="java.sql.*, java.io.*, java.util.*, it.geosolutions.geobatch.catalog.*" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<title wicket:id="pageTitle">GeoBatch</title>
      <link href="img/favicon.ico" rel="shortcut icon"/>
      <link rel="stylesheet" href="css/blueprint/screen.css" type="text/css" media="screen, projection" />
      <link rel="stylesheet" href="css/blueprint/print.css" type="text/css" media="print" />
	  <link rel="stylesheet" href="css/app.css" type="text/css" media="screen, projection" />
      <!--[if IE]>
        <link rel="stylesheet" href="css/blueprint/ie.css" type="text/css" media="screen, projection" />
	    <link rel="stylesheet" href="css/ie.css" type="text/css" media="screen, projection" />
      <![endif]-->
</head>
<body>
  <div id="header">
    <div class="wrap">
      <h2><a wicket:id="home" class="pngfix" href="#"><span>GeoBatch</span></a></h2>
      <div class="button-group selfclear">


      </div>
    </div><!-- /.wrap -->
  </div><!-- /#header -->
  <div id="main">
    <div class="wrap selfclear">
    <!--div id="sidebar">


    </div--><!-- /#sidebar -->
    <div id="page" class="selfclear">
      <div class="page-header">

        <div class="header-panel"></div>
        <p><img src="img/manageFTP-small.png" /></p>
        <br /><br />
        <p><a href="newFtpUser.form"><img src="img/add.png" />Add new user</a></p>
		<table width="100%" border="0" cellpadding="2" cellspacing="1">
			<thead>
				<tr bgcolor="black" style="color: white;">
					<th width="10%">USERID</th>
					<th width="10%">PASSWORD</th>
					<th width="20%">HOMEDIRECTORY</th>
					<th width="5%">WRITE PERMISSION</th>
					<th width="5%">UPLOAD RATE</th>
					<th width="5%">DOWNLOAD RATE</th>
					<th width="5%">ACTIONS</th>
				</tr>
			</thead>
			<tbody>
			<c:forEach var="us" items="${ftpUsers}">
				<tr >
					<td><c:out value="${us.userId}"/></td>
					<td><font style="font-style: italic; font-size: 12px"><c:out value="${us.userPassword}"/></font></td>
					<td><c:out value="${us.userId}"/></td>
					<td><c:out value="${us.writePermission}"/></td>
					<td><c:out value="${us.uploadRate}"/></td>
					<td><c:out value="${us.downloadRate}"/></td>
					<td align="center">
						<a href='delete.do?userId=${us.userId}'><image src='img/red.png' border='0' title='delete' alt='delete' width='16' height='16'/></a>
					</td>
				</tr>
			</c:forEach>
			
			</tbody>
		</table>
	</div>
      <div class="page-pane selfclear">

      </div>
    </div><!-- /#page -->
    </div><!-- /.wrap> -->
  </div><!-- /#main -->
  <center><p><img src="img/geoSolutions-logo.png" /></p>
  <p>Copyright &copy; 2005 - 2009 GeoSolutions.</p></center>
</body>
</html>