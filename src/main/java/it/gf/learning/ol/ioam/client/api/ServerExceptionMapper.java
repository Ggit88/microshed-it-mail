package it.gf.learning.ol.ioam.client.api;

import java.util.logging.Logger;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

@Provider
public class ServerExceptionMapper implements ResponseExceptionMapper<ServerException> {
	
	  Logger LOG = Logger.getLogger(ServerExceptionMapper.class.getName());

	  @Override
	  public boolean handles(int status, MultivaluedMap<String, Object> headers) {
	    LOG.info("status = " + status);
	    return status == 500;
	  }

	  @Override
	  public ServerException toThrowable(Response response) {
	    return new ServerException();
	  }

}
