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

package it.geosolutions.geobatch.task;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.flow.event.action.Action;
import it.geosolutions.geobatch.flow.event.action.BaseAction;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.Queue;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.ExecTask;
import org.apache.tools.ant.types.Environment.Variable;

/**
 * Action to execute tasks.
 * 
 * @author Daniele Romagnoli, GeoSolutions S.a.S.
 */
public class TaskExecutor extends BaseAction<FileSystemMonitorEvent> implements Action<FileSystemMonitorEvent> {

    private TaskExecutorConfiguration configuration;

	public TaskExecutor(final TaskExecutorConfiguration configuration) throws IOException {
    	this.configuration = configuration;
    }

	public Queue<FileSystemMonitorEvent> execute(
			Queue<FileSystemMonitorEvent> events) throws Exception {
		
		 // looking for file
        if (events.size() != 1)
            throw new IllegalArgumentException("Wrong number of elements for this action: "+ events.size());
        
        if (configuration == null) {
            throw new IllegalStateException("DataFlowConfig is null.");
        }
        
        // get the first event
        final FileSystemMonitorEvent event = events.remove();
        final File inputFile = event.getSource();
        
        //Getting XSL file definition
        final String xsl = configuration.getXsl();
		if (xsl == null || xsl.trim().length()<1)
			throw new IllegalArgumentException("Invalid XSL file");
		final File xslFile = new File(xsl);
		if (xslFile == null || !xslFile.exists())
			throw new IllegalArgumentException("The specified XSL file hasn't been found: "+xsl);
		
		//Setup an XML source from the input XML file
		final Source xmlSource = new StreamSource(inputFile);
		InputStream is = null;
		try{
			is = new FileInputStream(xslFile);
	         if (is != null){
	        	
	        	//XML parsing to setup a command line
				final TransformerFactory f = TransformerFactory.newInstance();
				final StringWriter result = new StringWriter();
				final Templates transformation = f.newTemplates(new StreamSource(is));
				final Transformer transformer = transformation.newTransformer();
				transformer.transform(xmlSource, new StreamResult(result));
				final String argument = result.toString();
				   
				final Project project = new Project();
				project.init();
		
				final ExecTask execTask = new ExecTask();
				execTask.setProject(project);
				
				// Setting environment variables
				List<String> variables = configuration.getVariables();
				for (String variable: variables){
					String keyValuePair[] = variable.split(" ");
					Variable var = new Variable();
					var.setKey(keyValuePair[0]);
					if (keyValuePair.length == 2)
						var.setValue(keyValuePair[1]);
					else{
						//Handle environment variables with spaces in value
						StringBuilder sb = new StringBuilder();
						int i=1;
						for (; i<keyValuePair.length-1; i++){
							sb.append(keyValuePair[i]).append(" ");
						}
						sb.append(keyValuePair[i]);
						var.setValue(sb.toString());
					}
					execTask.addEnv(var);
				}
				
				//Setting executable
				execTask.setExecutable(configuration.getExecutable());
				
				final String errorFile = configuration.getErrorFile();
				if (errorFile!=null && errorFile.trim().length()>0){
					execTask.setLogError(true);
					execTask.setError(new File(errorFile));
					execTask.setFailonerror(true);
				}
					
				Long timeOut = configuration.getTimeOut();
				if (timeOut!=null){
					execTask.setTimeout(timeOut);
				}
				
				//Setting argument
				execTask.createArg().setLine(argument);
				
				//Executing
				execTask.execute();
	         }
		}finally{
			try{
				if (is!=null)
					is.close();
			}catch (Throwable t){
					//eat me
			}
		}
		return events;
	}
	
}
