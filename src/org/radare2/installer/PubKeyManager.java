package org.radare2.installer;

import java.math.BigInteger;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

// Many thanks to Nikolay Elenkov for feedback.
// Shamelessly based upon Moxie's example code (AOSP/Google did not offer code)
// http://www.thoughtcrime.org/blog/authenticity-is-broken-in-ssl-but-your-app-ha/
public final class PubKeyManager implements X509TrustManager {

	// DER encoded public key
	// echo | openssl s_client -connect raw.githubusercontent.com:443 | openssl x509 -pubkey -noout
	// base64 -D > rawcert.pub ; vim rawcert.pub
	// rax2 -S < rawcert.pub > rawcert.txt
	private static String PUB_KEY = "30820122300d06092a864886f70d0101"
			+ "0105000382010f003082010a0282010100b1d4dc3caffd"
			+ "f34eedc167ade6cb22e8b7e2ab28f2f7dc627008d10caf"
			+ "d6166a21b0364b170d366304aebfea2051956566f2bfb9"
			+ "4da40c29ebf515b1e835b3701094d51b59b4260fd68357"
			+ "599de17c09dde013ca4d6f439bcdcf873a15a785dd6683"
			+ "ed930cfe2b6d381c798890cfad58182d51d1c2a3f2478c"
			+ "6f3809b9b8ef4c930bcb839487eae0a3b5d97b9b6b0f43"
			+ "f9caee800d28a776f125f4c1353cf674adde6a33827bdc"
			+ "fd4b76a7c2eef26abfa924a65fe72e7c0edbc37473fa7e"
			+ "c6d8cf60eb365621b6c18ab824824d7824bae91da18aa7"
			+ "87be662569bfbe3b726e4fe0e4852508b19189b8d67465"
			+ "769b2c4f621fa1fa3abe9c24bf9fcab0c5c0678d020301"
			+ "0001";

	public void checkServerTrusted(X509Certificate[] chain, String authType)
			throws CertificateException {

		assert (chain != null);
		if (chain == null) {
			throw new IllegalArgumentException(
					"checkServerTrusted: X509Certificate array is null");
		}

		assert (chain.length > 0);
		if (!(chain.length > 0)) {
			throw new IllegalArgumentException(
					"checkServerTrusted: X509Certificate is empty");
		}

		assert (null != authType && authType.equalsIgnoreCase("RSA"));
		if (!(null != authType && authType.equalsIgnoreCase("RSA"))) {
			throw new CertificateException(
					"checkServerTrusted: AuthType is not RSA");
		}

		// Perform customary SSL/TLS checks
		TrustManagerFactory tmf;
		try {
			tmf = TrustManagerFactory.getInstance("X509");
			tmf.init((KeyStore) null);

			for (TrustManager trustManager : tmf.getTrustManagers()) {
				((X509TrustManager) trustManager).checkServerTrusted(
						chain, authType);
			}

		} catch (Exception e) {
			throw new CertificateException(e);
		}

		// Hack ahead: BigInteger and toString(). We know a DER encoded Public
		// Key starts with 0x30 (ASN.1 SEQUENCE and CONSTRUCTED), so there is
		// no leading 0x00 to drop.
		RSAPublicKey pubkey = (RSAPublicKey) chain[0].getPublicKey();
		String encoded = new BigInteger(1 /* positive */, pubkey.getEncoded())
				.toString(16);

		// Pin it!
		final boolean expected = PUB_KEY.equalsIgnoreCase(encoded);
		assert(expected);
		if (!expected) {
			throw new CertificateException(
					"checkServerTrusted: Expected public key: " + PUB_KEY
							+ ", got public key:" + encoded);
		}
	}

	public void checkClientTrusted(X509Certificate[] xcs, String string) {
		// throw new
		// UnsupportedOperationException("checkClientTrusted: Not supported yet.");
	}

	public X509Certificate[] getAcceptedIssuers() {
		// throw new
		// UnsupportedOperationException("getAcceptedIssuers: Not supported yet.");
		return null;
	}
}
