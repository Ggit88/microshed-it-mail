package it.gf.learning.ol.ioam.test;

import static it.gf.learning.ol.ioam.test.AppDeploymentConfig.IMAPS_MAILSERVER_PORT;
import static it.gf.learning.ol.ioam.test.AppDeploymentConfig.MAIL;
import static it.gf.learning.ol.ioam.test.AppDeploymentConfig.MAIL_PWD;
import static it.gf.learning.ol.ioam.test.AppDeploymentConfig.MAIL_USER;
import static it.gf.learning.ol.ioam.test.AppDeploymentConfig.SMTP_MAILSERVER_PORT;
import static it.gf.learning.ol.ioam.test.AppDeploymentConfig.mailServer;
import static it.gf.learning.ol.ioam.test.AppDeploymentConfig.mockServer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.Properties;
import java.util.logging.Logger;

import javax.json.JsonObject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.microshed.testing.SharedContainerConfig;
import org.microshed.testing.jaxrs.RESTClient;
import org.microshed.testing.jupiter.MicroShedTest;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.Parameter;

import it.gf.learning.ol.ioam.model.OTPInfo;
import it.gf.learning.ol.ioam.model.User;
import it.gf.learning.ol.ioam.rest.OTPResource;

@SuppressWarnings("resource")

@MicroShedTest
@SharedContainerConfig(AppDeploymentConfig.class)
public class OTPResourceIT {

	@RESTClient
	public static OTPResource otpResource;

	private static final Jsonb jsonb = JsonbBuilder.create();
	
	private static final Logger logger = Logger.getLogger(OTPResourceIT.class.getName());
	
	@BeforeAll
	public static void setup() throws Exception {
		createProcessedFolder();
		logger.info("Successfully created PROCESSED folder");
	}
	
	private static void createProcessedFolder() throws MessagingException {
		final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
		Properties propsImap = System.getProperties();
		propsImap.setProperty("mail.imaps.socketFactory.class", SSL_FACTORY);
		propsImap.put("mail.imaps.ssl.trust", "*");
		propsImap.setProperty("mail.imaps.socketFactory.fallback", "false");
		propsImap.setProperty("mail.imaps.port", mailServer.getMappedPort(IMAPS_MAILSERVER_PORT).toString());
		propsImap.setProperty("mail.imaps.socketFactory.port", mailServer.getMappedPort(IMAPS_MAILSERVER_PORT).toString());
		propsImap.put("mail.imaps.host", mailServer.getHost());
		Session imapSession = Session.getInstance(propsImap,  new javax.mail.Authenticator() {
		    protected PasswordAuthentication getPasswordAuthentication() {
		        return new PasswordAuthentication(MAIL_USER, MAIL_PWD);
		    }
		});
		Store store = imapSession.getStore("imaps");
		store.connect();
		Folder processedFolder = store.getFolder("PROCESSED");
		if (!processedFolder.exists()) {
			if (processedFolder.create(Folder.HOLDS_MESSAGES)) {
				processedFolder.setSubscribed(true);
			}
		}
	}
	
	@Test
	@DisplayName("OTP successfully retrieved when user id is present on user api and an email with the OTP has been sent at the address associated")
	public void testOTPRetrievedWhenUserIdPresentAndEmailSent() throws Exception {

		String userId = "MATRICOLATEST";
		String otp = "48377730";

		sendOTPMail(otp);
		logger.info("Sent email with test otp "+otp);

		User expectedUser = new User();
		expectedUser.setEmail(MAIL);
		Parameter parameter = new Parameter("userId", userId);
		new MockServerClient(mockServer.getContainerIpAddress(), mockServer.getServerPort())
				.when(request("/user/info").withQueryStringParameter(parameter))
				.respond(response().withBody(jsonb.toJson(expectedUser), com.google.common.net.MediaType.JSON_UTF_8));

		Response otpRdasResponse = otpResource.getOtpForUserId(userId);
		assertEquals(200, otpRdasResponse.getStatus());
		logger.info("Resource response is 200");
		OTPInfo otpBean = otpRdasResponse.readEntity(OTPInfo.class);
		String otpEmail = otpBean.getOtp();
		assertEquals(otp, otpEmail);
		logger.info("OTP successfully retrieved");

	}

	private void sendOTPMail(String otp) throws AddressException, MessagingException {
		Properties props = System.getProperties();
		props.put("mail.smtp.host", mailServer.getHost());
		props.put("mail.smtp.port", mailServer.getMappedPort(SMTP_MAILSERVER_PORT).toString());
		Session smtpSession = Session.getInstance(props, null);
		Message msg = new MimeMessage(smtpSession);
		msg.setFrom(new InternetAddress("no-reply@test.it"));
		msg.addRecipient(Message.RecipientType.TO, new InternetAddress(MAIL));
		msg.setSubject("NEW OTP");
		msg.setText("Some general signing service: this is your OTP " + otp
				+ " created at 09/12/2020 12:25:46");
		Transport.send(msg);
	}
	
	@Test
	@DisplayName("OTP is not retrieved when user id is not present on user api")
	public void testOTPNotRetrievedWhenUserIdNotPresent() throws Exception {
		String matricola = "MATRICOLATESTNONPRESENTE";
		Response otpRdasResponse = otpResource.getOtpForUserId(matricola);
		assertEquals(404, otpRdasResponse.getStatus());
		logger.info("Resource response is 404");
		JsonObject responseObject = otpRdasResponse.readEntity(JsonObject.class);
		assertEquals("Email Address not found for userId", responseObject.getString("error"));
		logger.info("Error message is correct");
	}
	
	
	@Test
	@DisplayName("OTP is not retrieved when user id is present on user api but no email has been sent to the address associated")
	public void testOTPNotRetrievedWhenUserIdPresentAndEmailNotSent() throws Exception {

		String matricola = "MATRICOLATEST";
		
		User expectedUser = new User();
		expectedUser.setEmail(MAIL);
		Parameter parameter = new Parameter("matricola", matricola);
		new MockServerClient(mockServer.getContainerIpAddress(), mockServer.getServerPort())
				.when(request("/user/info").withQueryStringParameter(parameter))
				.respond(response().withBody(jsonb.toJson(expectedUser), com.google.common.net.MediaType.JSON_UTF_8));

		Response otpRdasResponse = otpResource.getOtpForUserId(matricola);
		assertEquals(404, otpRdasResponse.getStatus());
		logger.info("Resource response is 404");
		JsonObject responseObject = otpRdasResponse.readEntity(JsonObject.class);
		assertEquals("OTP not found for userId", responseObject.getString("error"));
		logger.info("Error message is correct");
	}

}
