package org.ypiel.sandbox.mscrm;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.KeyStore.LoadStoreParameter;
import java.security.KeyStore.ProtectionParameter;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.crypto.Cipher;
import javax.crypto.CipherSpi;
import javax.net.ssl.KeyStoreBuilderParameters;

import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.olingo.client.core.http.NTLMAuthHttpClientFactory;
import org.apache.olingo.commons.api.http.HttpMethod;

public class NTLMHttpClientFactoryWithHttpsCert extends NTLMAuthHttpClientFactory {

	/**
	 * @param username
	 * @param password
	 * @param workstation
	 * @param domain
	 */
	public NTLMHttpClientFactoryWithHttpsCert(String username, String password, String workstation, String domain) {
		super(username, password, workstation, domain);
	}

	@Override
	public DefaultHttpClient create(final HttpMethod method, final URI uri) {
		try {
			/*Security.addProvider(new BouncyCastlePQCProvider());*/
			CertificateFactory certFact = CertificateFactory.getInstance("X.509");

			Certificate cert = null;
			try (FileInputStream fis = new FileInputStream(new File("C:\\certifs\\MSCRM2016ECERTFromChrome.pem.cer"))) {
				cert = certFact.generateCertificate(fis);
			} catch (Exception e) {
				e.printStackTrace();
			}

			// The keystore contains authorized certificates apart from Certified CA
			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load(null);
			ks.setCertificateEntry("talend", cert);

			DefaultHttpClient httpClient = super.create(method, uri);

			HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 10 * 1000);
			HttpConnectionParams.setSoTimeout(httpClient.getParams(), 10 * 1000);

			// ------------------------------- Don't ignore self-certified
			httpClient.getConnectionManager().getSchemeRegistry()
					.register(new Scheme("https", 443, new SSLSocketFactory("TLS", ks, null, null, null,
							new TrustSelfSignedStrategy(), SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)));
			// ------------------------------------------------------------------------------------

			return httpClient;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

}
