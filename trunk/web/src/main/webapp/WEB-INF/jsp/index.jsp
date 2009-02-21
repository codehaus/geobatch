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
<%@ page contentType="text/html" import="java.sql.*, java.io.*, java.util.*, it.geosolutions.iengine.catalog.*" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<HEAD>
</HEAD>
<BODY>
	<H1>GeoBatch Control Panel</H1>
	<P>
		<H2>
		  Flow Managers
			<!-- a href="newFlowManager.form" --><a href="javascript:alert('Disabled!')"><image src='img/new_document.png' border='0' title='new FlowManager' alt='new FlowManager' width='16' height='16'/></a>
			<a href="javascript:alert('Disabled!')"><image src='img/save.png' border='0' title='save configuration' alt='save configuration' width='16' height='16'/></a>
		</H2>
		<HR/>
		<table width="100%" border="0" cellpadding="2" cellspacing="1">
			<thead>
				<tr bgcolor="black" style="color: white;">
					<th width="10%">ID</th>
					<th width="20%">DESCRIPTION</th>
					<th width="30%">INPUT DIR</th>
					<th width="30%">WORKING DIR</th>
					<th width="5%">STATUS</th>
					<th width="5%">ACTION</th>
				</tr>
			</thead>
			<tbody>
			<c:forEach var="fm" items="${flowManagers}">
				<tr >
					<td><c:out value="${fm.configuration.id}"/></td>
					<td><font style="font-style: italic; font-size: 12px"><c:out value="${fm.configuration.description}"/></font></td>
					<td><c:out value="${fm.configuration.eventGeneratorConfiguration.workingDirectory}"/></td>
					<td><c:out value="${fm.configuration.workingDirectory}"/></td>
					<td align="center">
						<c:choose> 
  							<c:when test="${fm.running}">
								<image src='img/green.png' border='0' title='running' alt='running' width='16' height='16'/>
							</c:when>
							<c:otherwise>
								<image src='img/red.png' border='0' title='paused' alt='paused' width='16' height='16'/>
							</c:otherwise>
						</c:choose>
					</td>
					<td align="center">
						<c:choose> 
  							<c:when test="${fm.running}">
  								<a href='pause.do?fmId=${fm.id}'><image src='img/pause.png' border='0' title='pause' alt='pause' width='16' height='16'/></a>
  							</c:when>
  							<c:otherwise>
  								<a href='resume.do?fmId=${fm.id}'><image src='img/play.png' border='0' title='resume' alt='resume' width='16' height='16'/></a>
  							</c:otherwise>
  						</c:choose>
						<a href="dispose.do?fmId=${fm.id}"><image src='img/dispose.png' border='0' title='dispose' alt='dispose' width='16' height='16'/></a>
					</td>
				</tr>
			</c:forEach>
			</tbody>
		</table>
	</P>
</BODY>