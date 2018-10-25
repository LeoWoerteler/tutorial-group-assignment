package kn.uni.dbis.groups.mail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.function.Consumer;

import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Authenticator;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.IssuerAndSerialNumber;
import org.bouncycastle.asn1.smime.SMIMECapabilitiesAttribute;
import org.bouncycastle.asn1.smime.SMIMECapability;
import org.bouncycastle.asn1.smime.SMIMECapabilityVector;
import org.bouncycastle.asn1.smime.SMIMEEncryptionKeyPreferenceAttribute;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.mail.smime.SMIMEException;
import org.bouncycastle.mail.smime.SMIMESignedGenerator;
import org.bouncycastle.operator.OperatorCreationException;

public final class SMimeMail {

	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	private final Session session;
	private final SMIMESignedGenerator gen;

	public SMimeMail(final Properties props) throws KeyStoreException, NoSuchAlgorithmException, CertificateException,
			IOException, UnrecoverableEntryException, OperatorCreationException {
		final KeyStore keyStore = KeyStore.getInstance("PKCS12");

		// Provide location of Java Keystore and password for access
		final char[] keystorePass = props.getProperty("smime.key.password").toCharArray();
		keyStore.load(Files.newInputStream(Paths.get(props.getProperty("smime.key.path"))), keystorePass);

		// Find the first legit alias in the keystore and use it
		Enumeration<String> es = keyStore.aliases();
		final String key;
		for (;;) {
			if (!es.hasMoreElements()) {
				throw new AssertionError("No private key.");
			}
			final String k = es.nextElement();
			if (keyStore.isKeyEntry(k)) {
				key = k;
				break;
			}
		}
		final KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(key,
				new KeyStore.PasswordProtection(keystorePass));
		final PrivateKey myPrivateKey = pkEntry.getPrivateKey();

		// Load certificate chain
		final Certificate[] chain = keyStore.getCertificateChain(key);

		// Create the SMIMESignedGenerator
		final SMIMECapabilityVector capabilities = new SMIMECapabilityVector();
		capabilities.addCapability(SMIMECapability.dES_EDE3_CBC);
		capabilities.addCapability(SMIMECapability.rC2_CBC, 128);
		capabilities.addCapability(SMIMECapability.dES_CBC);
		capabilities.addCapability(SMIMECapability.aES256_CBC);
		capabilities.addCapability(SMIMECapability.aES128_CBC);
		capabilities.addCapability(SMIMECapability.aES192_CBC);

		// Cert info
		final X509Certificate cert0 = (X509Certificate) chain[0];
		final X500Name x500 = new X500Name(cert0.getIssuerDN().getName());
		final IssuerAndSerialNumber serialNumber = new IssuerAndSerialNumber(x500, cert0.getSerialNumber());

		final ASN1EncodableVector signedAttrs = new ASN1EncodableVector();
		signedAttrs.add(new SMIMEEncryptionKeyPreferenceAttribute(serialNumber));
		signedAttrs.add(new SMIMECapabilitiesAttribute(capabilities));

		// Set X509
		final X509Certificate cert = (X509Certificate) keyStore.getCertificate(key);
		// Signing generator
		gen = new SMIMESignedGenerator();
		gen.addSignerInfoGenerator(new JcaSimpleSignerInfoGeneratorBuilder().setProvider("BC")
				.setSignedAttributeGenerator(new AttributeTable(signedAttrs)).build("SHA1withRSA", myPrivateKey, cert));
		gen.addCertificates(new JcaCertStore(Collections.singleton(cert)));

		session = Session.getDefaultInstance(props, new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(props.getProperty("mail.user"), props.getProperty("mail.password"));
			}
		});
	}

	public void sendSignedMail(final Consumer<MimeMessage> callback) throws MessagingException, SMIMEException {
		final MimeMessage body = new MimeMessage(session);
		callback.accept(body);
		final MimeMessage cloneOriginal = new MimeMessage(body);
		final MimeMultipart mainPart = gen.generate(body);
		// Set the content of the signed message
		cloneOriginal.setContent(mainPart, mainPart.getContentType());
		cloneOriginal.saveChanges();
		// Send the message
		Transport.send(cloneOriginal);
	}
}
