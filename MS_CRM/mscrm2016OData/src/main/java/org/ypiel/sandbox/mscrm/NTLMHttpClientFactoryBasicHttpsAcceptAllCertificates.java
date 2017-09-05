package org.ypiel.sandbox.mscrm;

import java.net.URI;

import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.olingo.client.core.http.NTLMAuthHttpClientFactory;
import org.apache.olingo.commons.api.http.HttpMethod;

public class NTLMHttpClientFactoryBasicHttpsAcceptAllCertificates extends NTLMAuthHttpClientFactory {

	/**
	 * @param username
	 * @param password
	 * @param workstation
	 * @param domain
	 */
	public NTLMHttpClientFactoryBasicHttpsAcceptAllCertificates(String username, String password, String workstation, String domain) {
		super(username, password, workstation, domain);
	}

	@Override
	public DefaultHttpClient create(final HttpMethod method, final URI uri) {
		
		try {
			TrustStrategy acceptingTrsutStrategy = (cert, authType) -> true;
			SSLSocketFactory sf = new SSLSocketFactory(acceptingTrsutStrategy,
					SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("https", 443, sf));
			
			DefaultHttpClient httpClient = super.create(method, uri);
			
			httpClient.getConnectionManager().getSchemeRegistry().register(registry.get("https"));
			
			HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 10 * 1000);
			HttpConnectionParams.setSoTimeout(httpClient.getParams(), 10 * 1000);

			return httpClient;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

}
