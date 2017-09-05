package org.ypiel.sandbox.mscrm;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.olingo.client.core.http.NTLMAuthHttpClientFactory;
import org.apache.olingo.commons.api.http.HttpMethod;

public class NTLMHttpClientFactory extends NTLMAuthHttpClientFactory {

	/**
	 * @param username
	 * @param password
	 * @param workstation
	 * @param domain
	 */
	public NTLMHttpClientFactory(String username, String password, String workstation, String domain) {
		super(username, password, workstation, domain);
	}

	@Override
	public DefaultHttpClient create(final HttpMethod method, final URI uri) {
		try {
			DefaultHttpClient httpClient = super.create(method, uri);

			KeyStore ks = KeyStore.getInstance("JKS");

			try (InputStream is = new FileInputStream("C:\\certifs\\MSCRM2016ECERTPub.pfx")) {
				ks.load(is, "R&D_talend_0409".toCharArray());
			} catch (Exception e) {
				e.printStackTrace();
			}

			HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 10 * 1000);
			HttpConnectionParams.setSoTimeout(httpClient.getParams(), 10 * 1000);

			// --------- Don't check certificate at all
//			httpClient.getConnectionManager().getSchemeRegistry()
//					.register(new Scheme("https", 443, new SSLSocketFactory(new TrustStrategy() {
//
//						@Override
//						public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//							return true;
//						}
//
//					}, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)));
			// ---------------------------------------------------------

			//------------------------------- Don't ignore self-certified certificates -----------
			httpClient.getConnectionManager().getSchemeRegistry()
					.register(new Scheme("https", 443, new SSLSocketFactory("TLS", ks, "R&D_talend_0409", null, null,
							new TrustSelfSignedStrategy(), SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)));
			//------------------------------------------------------------------------------------

			return httpClient;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

}
