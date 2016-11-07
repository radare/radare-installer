package org.radare.radare2installer;

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
	private static String PUB_KEY = ""
		+ "30820122300d06092a864886f70d01010105000382010f003082010a"
		+ "0282010100fbd5940a0ae050dc0ffc90b771479f2c05de0e9abc2a8f"
		+ "d4f29f0846f9f2d118b423a52ad2df913ff9c5d0b240bdd6bc40762e"
		+ "8dd81e0d378f7a9057efe3a2c0116103460efab3370b667c21168dfe"
		+ "2f5e2e59fe63273af3ed73f84d74b35117759aed0c6bcde8c1eaca01"
		+ "ac75f91729f04b509d4164486cf6c0677dc8eade487981974102b746"
		+ "f65e4da5d99086d71e6851ac3e25ae2711b14734b88bde6f7941d692"
		+ "13291180c410175c0c6c6a02bbd00afcd296781db6d4027f1f0e5240"
		+ "536f7040da89294f0c097ea3ecc557ad03aa91ed435cf9f55be8a1f0"
		+ "be6d1bce2dab437c70dc3fecc911f074c929a150d03c2938dc7f56b9"
		+ "f81f04a45e9fcedd170203010001";

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

		assert (null != authType && authType.equalsIgnoreCase("ECDHE_RSA"));
		if (!(null != authType && authType.equalsIgnoreCase("ECDHE_RSA"))) {
			throw new CertificateException(
					"checkServerTrusted: AuthType is not ECDHE_RSA ("+authType+")");
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
