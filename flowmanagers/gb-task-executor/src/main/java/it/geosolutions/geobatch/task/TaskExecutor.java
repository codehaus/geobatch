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
import java.io.IOException;
import java.util.List;
import java.util.Queue;

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

	public TaskExecutor(TaskExecutorConfiguration configuration) throws IOException {
    	this.configuration = configuration;
    }

	public Queue<FileSystemMonitorEvent> execute(
			Queue<FileSystemMonitorEvent> events) throws Exception {
		
		final Project project = new Project();
		project.init();

		final ExecTask execTask = new ExecTask();
		execTask.setProject(project);
		
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
		execTask.createArg().setLine(configuration.getArgument());
		execTask.execute();
		return events;
	}

}
