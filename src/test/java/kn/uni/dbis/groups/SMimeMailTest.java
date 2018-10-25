package kn.uni.dbis.groups;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;

import org.bouncycastle.mail.smime.SMIMEException;
import org.bouncycastle.operator.OperatorCreationException;

import kn.uni.dbis.groups.mail.SMimeMail;

public class SMimeMailTest {
	public static void main(String[] args)
			throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException,
			UnrecoverableEntryException, OperatorCreationException, MessagingException, SMIMEException {
		final Properties props = new Properties();
		try (InputStream in = Files.newInputStream(Paths.get("mail.props"))) {
			props.load(in);
		}
		final SMimeMail sender = new SMimeMail(props);
		sender.sendSignedMail(body -> {
			try {
				final Address from = new InternetAddress("...@uni-konstanz.de", "...", "UTF-8");
				final Address to = new InternetAddress("...@uni-konstanz.de", "...", "UTF-8");
				body.setFrom(from);
				body.addRecipient(RecipientType.TO, to);
				body.setSubject("Test456", "UTF-8");
				body.setText("Bla bla blub...", "UTF-8");
			} catch (UnsupportedEncodingException | MessagingException e) {
				throw new Error(e);
			}
		});
	}
}
