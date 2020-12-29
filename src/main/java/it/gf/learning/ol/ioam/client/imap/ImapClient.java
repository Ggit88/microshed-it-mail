package it.gf.learning.ol.ioam.client.imap;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.SearchTerm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImapClient {
	private final Session mailSession;

	final Logger logger = LoggerFactory.getLogger(ImapClient.class);

	public ImapClient(Session mailSession) {
		this.mailSession = mailSession;
	}

	public String extractOtp(SearchTerm filter) throws Exception {

		Folder inbox = null;
		Folder processed = null;
		Store store = null;
		String otp = null;

		try {

			store = this.mailSession.getStore();
			store.connect();

			inbox = store.getFolder("INBOX");
			processed = store.getFolder("PROCESSED");

			inbox.open(Folder.READ_WRITE);
			Message[] messages = inbox.search(filter);
			inbox.setFlags(messages, new Flags(Flags.Flag.SEEN), true);

			logger.warn("extractOtp() -- Number of messages found in INBOX: " + messages.length);

			String textMessage = null;
			if (messages.length > 0) {
				textMessage = this.getTextFromMessage(messages[messages.length - 1]);
				inbox.copyMessages(messages, processed);
				inbox.setFlags(messages, new Flags(Flags.Flag.DELETED), true);
				logger.warn("extractOtp() -- Messages moved from INBOX to PROCESSED label");

			} else {
				logger.warn("extractOtp() -- No new message");
				return null;
			}

			if (textMessage != null) {
				otp = this.findOTP(textMessage);
				if (otp != null) {
					logger.info("extractOtp() -- Found a valid OTP");
					return otp;

				} else {
					logger.error("extractOtp() -- NO valid OTP!");
					return null;
				}
			} else {
				logger.error("extractOtp() -- NO valid textMessage!");
				return null;
			}
		} catch (Exception e) {
			logger.error("Extracting OTP from email resulted in error", e);
		} finally {
			if (inbox != null && inbox.isOpen()) {
				inbox.close();
			}
			if (store != null) {
				store.close();
			}
		}
		return otp;
	}

	private String getTextFromMessage(Message message) throws Exception {
		String result = "";
		if (message.isMimeType("text/plain")) {
			result = message.getContent().toString();
		} else if (message.isMimeType("multipart/*")) {
			MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
			int count = mimeMultipart.getCount();
			for (int i = 0; i < count; i++) {
				BodyPart bodyPart = mimeMultipart.getBodyPart(i);
				if (bodyPart.isMimeType("text/plain")) {
					result = result + bodyPart.getContent().toString();
				}
			}
		}
		return result;
	}

	private String findOTP(String message) {
		String otp = null;
		Pattern pattern = Pattern.compile("OTP\\W+(\\d+)");
		Matcher matcher = pattern.matcher(message);

		if (matcher.find()) {
			otp = matcher.group(1);
		}

		return otp;
	}

}
