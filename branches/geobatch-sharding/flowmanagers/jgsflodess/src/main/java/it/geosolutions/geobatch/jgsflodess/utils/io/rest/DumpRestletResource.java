package it.geosolutions.geobatch.jgsflodess.utils.io.rest;

import it.geosolutions.geobatch.jgsflodess.config.global.JGSFLoDeSSGlobalConfig;
import it.geosolutions.geobatch.utils.IOUtils;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Resource;
import org.restlet.resource.StringRepresentation;

/**
 * 
 * @author Fabiani
 *
 */
public class DumpRestletResource extends Resource {
	
	private final static Logger log = Logger.getLogger(DumpRestletResource.class);
	
	
	
	/* (non-Javadoc)
	 * @see org.restlet.resource.Resource#allowDelete()
	 */
	@Override
	public boolean allowDelete() {
		return false;
	}



	/* (non-Javadoc)
	 * @see org.restlet.resource.Resource#allowGet()
	 */
	@Override
	public boolean allowGet() {
		return true;
	}



	/* (non-Javadoc)
	 * @see org.restlet.resource.Resource#allowPost()
	 */
	@Override
	public boolean allowPost() {
		return false;
	}



	/* (non-Javadoc)
	 * @see org.restlet.resource.Resource#allowPut()
	 */
	@Override
	public boolean allowPut() {
		return true;
	}



	/* (non-Javadoc)
	 * @see org.restlet.resource.Resource#handleGet()
	 */
	@Override
	public void handleGet() {
		Request request = getRequest();
		Response response = getResponse();
		
     	if (request.getMethod().equals(Method.GET)) {             		
 			log.info("Handling the call...");
     		    
 			String file = (String)request.getAttributes().get("file");
 			
 	        if(file == null){
 	        	response.setEntity(new StringRepresentation("Unrecognized extension: " + file, MediaType.TEXT_PLAIN));
 	            response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
 	            return;
 	        } 	        
 	        
 	        File inputFile = new File(JGSFLoDeSSGlobalConfig.getJGSFLoDeSSDirectory(), file);
 	        
 	        if (inputFile.exists() && inputFile.isFile()){
 	        	try {
 	        		if (IOUtils.acquireLock(this, inputFile)) {
 	        			response.setEntity(IOUtils.toString(inputFile), MediaType.TEXT_XML);
 	        		} else {
 	        			log.error("Could not acquire file lock: " + inputFile.getAbsolutePath());
 	        			response.setEntity(Status.SERVER_ERROR_INTERNAL);
 	        		}
				} catch (IOException e) {
					log.error(e.getLocalizedMessage());
					response.setEntity(Status.SERVER_ERROR_INTERNAL);
				} catch (InterruptedException e) {
					log.error(e.getLocalizedMessage());
					response.setEntity(Status.SERVER_ERROR_INTERNAL);
				} finally {
				}
 	        }else{
				log.error("Could not find file: " + inputFile.getAbsolutePath());
 	        	response.setEntity(Status.CLIENT_ERROR_BAD_REQUEST); 
 	        }
        } 
     	else {
            response.setStatus(Status.SERVER_ERROR_NOT_IMPLEMENTED);
        } 
    }
    
}
