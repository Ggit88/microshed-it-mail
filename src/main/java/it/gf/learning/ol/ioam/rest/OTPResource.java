package it.gf.learning.ol.ioam.rest;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.search.RecipientTerm;
import javax.mail.search.SearchTerm;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import it.gf.learning.ol.ioam.client.api.ServerException;
import it.gf.learning.ol.ioam.client.api.UnknownUriException;
import it.gf.learning.ol.ioam.client.api.UserAPI;
import it.gf.learning.ol.ioam.client.imap.ImapClient;
import it.gf.learning.ol.ioam.model.OTPInfo;
import it.gf.learning.ol.ioam.model.User;

@Path("otp")
public class OTPResource {

	Logger logger = Logger.getLogger(OTPResource.class.getName());

	@Inject
	@RestClient
	private UserAPI userApi;

	@Resource(lookup = "mail/retrieveOTP")
	private Session mailSession;

	@GET
	@Path("/{userId}")
	@Produces(MediaType.APPLICATION_JSON)
	@APIResponses(value = {
			@APIResponse(responseCode = "404", description = "OTP not retrieved", content = @Content(mediaType = "text/plain")),
			@APIResponse(responseCode = "200", description = "OTP retrieved for userId", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OTPInfo.class))) })
	@Operation(summary = "Retrieve the last OTP sent to the email address for the given user id.", description = "Retrive the last OTP sent to the email address associated to the given user id by the UserAPI."
			+ "Once the OTP has been extracted, the email is moved from INBOX to PROCESSED folder.")
	public Response getOtpForUserId(
			@Parameter(description = "User id to which has been sent the OTP.", required = true, example = "USER000000", schema = @Schema(type = SchemaType.STRING)) @PathParam("userId") String userId) {

		logger.info("Start getOtpForUserId for userId: " + userId);

		String userEmail = retrieveEmailAddress(userId);
		if (userEmail == null) {
			logger.warning("Email Address not found for userId "+userId);
			return Response.status(Response.Status.NOT_FOUND)
					.entity("{ \"error\" : \"Email Address not found for userId\"}").build();
		}

		String otp = retrieveOTP(userEmail);
		if (otp == null) {
			logger.warning("OTP not found for userId "+userId);
			return Response.status(Response.Status.NOT_FOUND)
					.entity("{ \"error\" : \"OTP not found for userId\" }").build();
		}

		OTPInfo otpBean = new OTPInfo(otp);
		logger.info("End getOtpForUserId with otp: " + otpBean.getOtp());
		return Response.status(Response.Status.OK).entity(otpBean).build();
	}

	private String retrieveEmailAddress(String userId) {
		logger.info("retrieveEmailAddress for userId: " + userId);
		try {
			User userInfo = this.userApi.getUserInfo(userId);
			return userInfo.getEmail();
		} catch (UnknownUriException | ProcessingException | ServerException e) {
			logger.log(Level.WARNING, "Error calling getUserInfo", e);
			return null;
		}
	}

	private String retrieveOTP(String email) {
		logger.info("retrieveOTP for email address: " + email);
		ImapClient gmailImapClient = new ImapClient(this.mailSession);
		SearchTerm searchFilter;
		try {
			searchFilter = new RecipientTerm(Message.RecipientType.TO, new InternetAddress(email));
			return gmailImapClient.extractOtp(searchFilter);
		} catch (AddressException e) {
			logger.log(Level.WARNING, "Error with email address", e);
			return null;
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error extracting OTP", e);
			return null;
		}
	}

}
