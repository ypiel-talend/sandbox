package org.ypiel.sandbox.mscrm;

import java.util.List;

import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.request.retrieve.EdmMetadataRequest;
import org.apache.olingo.client.api.communication.request.retrieve.ODataEntitySetRequest;
import org.apache.olingo.client.api.communication.response.ODataRetrieveResponse;
import org.apache.olingo.client.api.domain.ClientEntity;
import org.apache.olingo.client.api.domain.ClientEntitySet;
import org.apache.olingo.client.api.uri.URIBuilder;
import org.apache.olingo.client.core.ODataClientFactory;
import org.apache.olingo.commons.api.edm.Edm;
import org.apache.olingo.commons.api.edm.EdmAction;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmSchema;
import org.ypiel.sandbox.mscrm.NTLMHttpClientFactoryBasicHttpsAcceptAllCertificates;

public class MsCrm2016OData {
	public final static void main(String[] args) {
		try {
			//System.setProperty("javax.net.ssl.trustStore", "C:\\certifs\\MSCRM2016ECERTPub.pfx");
			
//			KeyStore ks = KeyStore.getInstance("JKS");
//			
//			try(InputStream is = new FileInputStream("C:\\certifs\\MSCRM2016ECERTPub.pfx")){
//				ks.load(is, "R&D_talend_0409".toCharArray());
//			}
//			catch(Exception e){
//				e.printStackTrace();
//			}
			
			/*HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
				
				@Override
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			});*/
			
			ODataClient client = ODataClientFactory.getClient();

			NTLMHttpClientFactoryBasicHttpsAcceptAllCertificates ntlm = new NTLMHttpClientFactoryBasicHttpsAcceptAllCertificates(org.ypiel.sandbox.mscrm.Resources.USERNAME, //
					org.ypiel.sandbox.mscrm.Resources.PASSWORD, //
					org.ypiel.sandbox.mscrm.Resources.HOST, //
					org.ypiel.sandbox.mscrm.Resources.DOMAIN);//
			
			client.getConfiguration().setHttpClientFactory(ntlm);

			EdmMetadataRequest request = client.getRetrieveRequestFactory().getMetadataRequest(org.ypiel.sandbox.mscrm.Resources.SERVICEROOT);
			ODataRetrieveResponse<Edm> response = request.execute();
			Edm edm = response.getBody();

			for (EdmSchema schema : edm.getSchemas()) {
				System.out.println("=> " + schema.getNamespace() + " **********");
				System.out.println("\t** Actions:");
				List<EdmAction> acts = schema.getActions();
				for (EdmAction a : acts) {
					System.out.println("\t\t- " + a.getName());
				}

				System.out.println("\t** Entities:");
				List<EdmEntityType> etts = schema.getEntityTypes();
				for (EdmEntityType e : etts) {
					System.out.println("\t\t- " + e.getName());
				}
			}

			URIBuilder uriBuilder = client.newURIBuilder(org.ypiel.sandbox.mscrm.Resources.SERVICEROOT).appendEntitySetSegment("contacts");
			ODataEntitySetRequest<ClientEntitySet> req = client.getRetrieveRequestFactory()
					.getEntitySetRequest(uriBuilder.build());
			ODataRetrieveResponse<ClientEntitySet> resp = req.execute();
			ClientEntitySet cs = resp.getBody();

			System.out.println("=> Contacts ********");
			List<ClientEntity> es = cs.getEntities();
			for (ClientEntity e : es) {
				System.out.println("\t- " + e.getProperty("lastname"));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
