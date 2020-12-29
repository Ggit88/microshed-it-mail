package it.gf.learning.ol.ioam.client.api;

import javax.enterprise.context.Dependent;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import it.gf.learning.ol.ioam.model.User;


@Path("/user")
@Consumes(MediaType.APPLICATION_JSON)
@Dependent
@RegisterRestClient(configKey = "UserAPIClient")
@RegisterProvider(UnknownUriExceptionMapper.class)
@RegisterProvider(ServerExceptionMapper.class)
public interface UserAPI extends AutoCloseable {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/info")
	public User getUserInfo(@QueryParam("userId") String userId) throws UnknownUriException, ServerException, ProcessingException;

}
