package it.gf.learning.ol.ioam.test;

import org.microshed.testing.SharedContainerConfiguration;
import org.microshed.testing.testcontainers.ApplicationContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;

import it.gf.learning.ol.ioam.client.api.UserAPI;

public class AppDeploymentConfig implements SharedContainerConfiguration {

	private static final String GREENMAIL_DOCKER_IMAGE_NAME = "greenmail/standalone:1.6.0";

	public static final String MAIL_DOMAIN = "TEST.IT";
	
	public static final String MAIL_USER = "MAILTEST";
	
	public static final String MAIL = MAIL_USER+"@"+MAIL_DOMAIN;
	
	public static final String MAIL_PWD = "pwd";

	public static final String MAILSERVER_NETWORK_ALIAS = "mailServer";
	
	public static final String USERAPISERVER_NETWORK_ALIAS = "mockserver";

	public static final int SMTP_MAILSERVER_PORT = 3025;

	public static final int IMAPS_MAILSERVER_PORT = 3993;

	public static Network network = Network.newNetwork();
	
	@Container
	public static MockServerContainer mockServer = new MockServerContainer().withNetwork(network).withNetworkAliases(USERAPISERVER_NETWORK_ALIAS);
	
	@Container
	public static GenericContainer<?> mailServer = new GenericContainer<>(GREENMAIL_DOCKER_IMAGE_NAME)
	        .withEnv("GREENMAIL_OPTS","-Dgreenmail.setup.test.all -Dgreenmail.hostname=0.0.0.0 -Dgreenmail.verbose -Dgreenmail.users="+MAIL_USER+":"+MAIL_PWD+"@"+MAIL_DOMAIN ) //-Dgreenmail.auth.disabled")
			.withNetworkAliases(MAILSERVER_NETWORK_ALIAS).withExposedPorts(SMTP_MAILSERVER_PORT, IMAPS_MAILSERVER_PORT)
			.withNetwork(network)
			.waitingFor(
					Wait.forLogMessage(".*Started imaps.*\\s", 1)
			    );
	
	@Container
	public static ApplicationContainer app = new ApplicationContainer().withAppContextRoot("/it-ol-api-mail")
			.withEnv("retrieveOTP_mail_account", MAIL_USER)
			.withEnv("retrieveOTP_mail_password", MAIL_PWD)
			.withEnv("retrieveOTP_mail_host", MAILSERVER_NETWORK_ALIAS)
			.withEnv("retrieveOTP_mail_port", String.valueOf(IMAPS_MAILSERVER_PORT))
			.withNetwork(network)
			.withMpRestClient(UserAPI.class, "http://" + USERAPISERVER_NETWORK_ALIAS + ":" + MockServerContainer.PORT)
			.dependsOn(mailServer,mockServer);

}
