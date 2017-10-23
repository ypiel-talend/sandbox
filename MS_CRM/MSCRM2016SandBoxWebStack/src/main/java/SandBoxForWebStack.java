
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.request.retrieve.EdmMetadataRequest;
import org.apache.olingo.client.api.communication.request.retrieve.ODataEntitySetIteratorRequest;
import org.apache.olingo.client.api.communication.request.retrieve.ODataServiceDocumentRequest;
import org.apache.olingo.client.api.communication.response.ODataDeleteResponse;
import org.apache.olingo.client.api.communication.response.ODataReferenceAddingResponse;
import org.apache.olingo.client.api.communication.response.ODataRetrieveResponse;
import org.apache.olingo.client.api.domain.*;
import org.apache.olingo.client.api.http.HttpClientException;
import org.apache.olingo.client.api.serialization.ODataSerializer;
import org.apache.olingo.client.api.uri.URIBuilder;
import org.apache.olingo.client.core.ODataClientFactory;
import org.apache.olingo.client.core.http.HttpPatch;
import org.apache.olingo.client.core.http.NTLMAuthHttpClientFactory;
import org.apache.olingo.commons.api.Constants;
import org.apache.olingo.commons.api.edm.*;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpMethod;

import javax.annotation.Resource;
import javax.naming.ServiceUnavailableException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * This class follows the example from: https://templth.wordpress.com/2014/12/03/accessing-odata-v4-service-with-olingo/
 */
public class SandBoxForWebStack {

    public static final String ACCOUNT_ID_UPD = "4A3EAB07-4999-E711-A95A-000D3A36863D";
    public static final String ACCOUNT_ID_DEL = "4E3EAB07-4999-E711-A95A-000D3A36863D";
    public static final String NS_MICROSOFT_DYNAMICS_CRM = "Microsoft.Dynamics.CRM";
    public static String selectedEntity = "accounts";
    public static String[] selectedFields = new String[]{"accountid", "accountnumber", "primarycontactid", "_primarycontactid_value"}; // with "primarycontactid", "_primarycontactid_value" only "_primarycontactid_value" is retrieved
    public static String orderByField = "accountnumber desc";
    public static String[] navLinktoRetrieve = new String[]{"_primarycontactid_value", "primarycontactid"};
    public static String[] navLinkAutoExpand = new String[]{"primarycontactid"};

    public final static String[] CONTACTS = {"12393596-0A9D-E711-A95F-000D3A316204", "B03EAB07-4999-E711-A95A-000D3A36863D", "B23EAB07-4999-E711-A95A-000D3A36863D", "B43EAB07-4999-E711-A95A-000D3A36863D", "B63EAB07-4999-E711-A95A-000D3A36863D", "B83EAB07-4999-E711-A95A-000D3A36863D", "BA3EAB07-4999-E711-A95A-000D3A36863D"};
    public final static NTLMAuthHttpClientFactory ntlm = new NTLMAuthHttpClientFactory(Resources.USERNAME, //
            Resources.PASSWORD,
            Resources.HOST,
            Resources.DOMAIN
    );

    public final static void main(String[] args) {


        // Createhe OData client
        ODataClient client = ODataClientFactory.getClient();

        // NTLM Authentication


        client.getConfiguration().setHttpClientFactory(ntlm);

        // Call metadata service to determine which data can be used
        ODataServiceDocumentRequest docReq = client.getRetrieveRequestFactory().getServiceDocumentRequest(Resources.SERVICEROOT);
        ODataRetrieveResponse<ClientServiceDocument> docResp = docReq.execute();

        ClientServiceDocument serviceDoc = docResp.getBody();

        Collection<String> entitySetNames = serviceDoc.getEntitySetNames();
        Map<String, URI> entitySets = serviceDoc.getEntitySets();
        Map<String, URI> singletons = serviceDoc.getSingletons();
        Map<String, URI> functionImports = serviceDoc.getFunctionImports();
        URI selEntityUri = serviceDoc.getEntitySetURI(selectedEntity);
        System.out.println("accounts URI: " + selEntityUri);

        //listMetadata(client);
        listProperties(client);
        //retrieveDatas(client);
        //updateData(client);
        //updateDataNavigationLink(client);
        //updateDataNavigationLinkWithNULL(client);
        //updateDataNavigationLinkFromStudio(client);
        //updateDataNavigationLinkFromStudioWithNull(client);

    }

    private static void deleteData(ODataClient client) {
        URI accountUri = client.newURIBuilder(Resources.SERVICEROOT).appendEntitySetSegment("accounts").appendKeySegment(ACCOUNT_ID_DEL).build();
        ODataDeleteResponse resp = client.getCUDRequestFactory().getDeleteRequest(accountUri).execute();

        if (resp.getStatusCode() == 204) {
            System.out.println("Delete OK.");
        } else {
            System.out.println("Delete Fails: " + resp.getStatusMessage());
        }
    }

    private static void updateDataNavigationLinkFromStudioWithNull(ODataClient client) {
        //URI accountsUri = client.newURIBuilder(Resources.SERVICEROOT).appendEntitySetSegment("accounts").appendKeySegment(UUID.fromString(ACCOUNT_ID_UPD)).build();

        ClientEntity accountUpd = client.getObjectFactory().newEntity(new FullQualifiedName(NS_MICROSOFT_DYNAMICS_CRM, "account"));
        accountUpd.getProperties().add(
                client.getObjectFactory().newPrimitiveProperty("accountnumber", client.getObjectFactory().newPrimitiveValueBuilder().buildString("123"))
        );
        accountUpd.getProperties().add(
                client.getObjectFactory().newPrimitiveProperty("name", client.getObjectFactory().newPrimitiveValueBuilder().buildString("YPL2"))
        );

        try {
            /*accountUpd.getNavigationLinks().add(
                    client.getObjectFactory().newEntityNavigationLink(
                            "primarycontactid",
                            new URI("contacts" + "()")
                    )
            );*/

            updateEntity(client, accountUpd, ACCOUNT_ID_UPD);
            URI accountPrimContactUri = client.newURIBuilder(Resources.SERVICEROOT).appendEntitySetSegment("accounts")
                    .appendKeySegment(UUID.fromString(ACCOUNT_ID_UPD))
                    .appendNavigationSegment("primarycontactid").appendRefSegment().build();
            ODataDeleteResponse delResp = client.getCUDRequestFactory().getDeleteRequest(accountPrimContactUri).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void updateDataNavigationLinkFromStudio(ODataClient client) {
        //URI accountsUri = client.newURIBuilder(Resources.SERVICEROOT).appendEntitySetSegment("accounts").appendKeySegment(UUID.fromString(ACCOUNT_ID_UPD)).build();

        ClientEntity accountUpd = client.getObjectFactory().newEntity(new FullQualifiedName(NS_MICROSOFT_DYNAMICS_CRM, "account"));
        accountUpd.getProperties().add(
                client.getObjectFactory().newPrimitiveProperty("accountnumber", client.getObjectFactory().newPrimitiveValueBuilder().buildString("123"))
        );
        accountUpd.getProperties().add(
                client.getObjectFactory().newPrimitiveProperty("name", client.getObjectFactory().newPrimitiveValueBuilder().buildString("YPL2"))
        );

        try {
            accountUpd.getNavigationLinks().add(
                    client.getObjectFactory().newEntityNavigationLink(
                            "primarycontactid",
                            new URI("contacts" + "(" + CONTACTS[0] + ")")
                    )
            );

            // Now should be :
//            URI primaryContactRef = client.newURIBuilder(Resources.SERVICEROOT).appendEntitySetSegment("contacts").appendKeySegment(UUID.fromString(CONTACTS[0])).build();
//            accountUpd.getNavigationLinks().add(
//                    client.getObjectFactory().newEntityNavigationLink("primarycontactid", primaryContactRef)


            updateEntity(client, accountUpd, ACCOUNT_ID_UPD);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void updateDataNavigationLinkWithNULL(ODataClient client) {
        URI accountsUri = client.newURIBuilder(Resources.SERVICEROOT).appendEntitySetSegment("accounts").appendKeySegment(UUID.fromString(ACCOUNT_ID_UPD)).build();

        ClientEntity accountUpd = client.getObjectFactory().newEntity(new FullQualifiedName(NS_MICROSOFT_DYNAMICS_CRM, "account"));
        accountUpd.getProperties().add(
                client.getObjectFactory().newPrimitiveProperty("accountnumber", client.getObjectFactory().newPrimitiveValueBuilder().buildString("123"))
        );
        accountUpd.getProperties().add(
                client.getObjectFactory().newPrimitiveProperty("name", client.getObjectFactory().newPrimitiveValueBuilder().buildString("YPL2"))
        );

        // Set the Navigation link with null in the same way we set it : Caused by: org.apache.olingo.client.api.http.HttpClientException: linkPath should have 2 segments
        /*URI primaryContactRef = client.newURIBuilder(Resources.SERVICEROOT).appendEntitySetSegment("contacts").appendKeySegment(null).build();
        accountUpd.getNavigationLinks().add(
                client.getObjectFactory().newEntityNavigationLink("primarycontactid", primaryContactRef)
        );*/

        try {
            updateEntityNew(client, accountsUri, accountUpd);
        } catch (ServiceUnavailableException e) {
            e.printStackTrace();
        }
    }

    private static void updateDataNavigationLinkWithNULL_old(ODataClient client) {
        URI accountsUri = client.newURIBuilder(Resources.SERVICEROOT).appendEntitySetSegment("accounts").appendKeySegment(UUID.fromString(ACCOUNT_ID_UPD)).build();

        ClientEntity accountUpd = client.getObjectFactory().newEntity(new FullQualifiedName(NS_MICROSOFT_DYNAMICS_CRM, "account"));
        accountUpd.getProperties().add(
                client.getObjectFactory().newPrimitiveProperty("accountnumber", client.getObjectFactory().newPrimitiveValueBuilder().buildString("456"))
        );
        accountUpd.getProperties().add(
                client.getObjectFactory().newPrimitiveProperty("name", client.getObjectFactory().newPrimitiveValueBuilder().buildString("YPL3"))
        );

        URI accountPrimContactUri = client.newURIBuilder(Resources.SERVICEROOT)
                .appendEntitySetSegment("accounts")
                .appendKeySegment(ACCOUNT_ID_UPD)
                .appendNavigationSegment("primarycontactid")
                .appendRefSegment().build();
        URI primContactRef = client.newURIBuilder(Resources.SERVICEROOT)
                .appendEntitySetSegment("contacts")
                .appendKeySegment(UUID.fromString(CONTACTS[3])).build();
        URI serviceRoot = client.newURIBuilder(Resources.SERVICEROOT).build();
        ODataReferenceAddingResponse updLinkResp = client.getCUDRequestFactory().getReferenceSingleChangeRequest(serviceRoot, accountPrimContactUri, primContactRef).execute();

        // Delete the Navigation link
        //URI accountPrimContactUnlink = client.newURIBuilder(Resources.SERVICEROOT).appendEntitySetSegment("accounts").appendKeySegment(ACCOUNT_ID_UPD).appendNavigationSegment("primarycontactid").appendRefSegment().build();

        try {
            updateEntity(client, accountUpd, ACCOUNT_ID_UPD);
            //updateEntity(client, accountUpd, ACCOUNT_ID_UPD);
            //ODataDeleteResponse delResp = client.getCUDRequestFactory().getDeleteRequest(accountPrimContactUnlink).execute();
        } catch (ServiceUnavailableException e) {
            e.printStackTrace();
        }
    }

    private static void updateDataNavigationLink(ODataClient client) {
        URI accountsUri = client.newURIBuilder(Resources.SERVICEROOT).appendEntitySetSegment("accounts").appendKeySegment(UUID.fromString(ACCOUNT_ID_UPD)).build();

        ClientEntity accountUpd = client.getObjectFactory().newEntity(new FullQualifiedName(NS_MICROSOFT_DYNAMICS_CRM, "account"));
        accountUpd.getProperties().add(
                client.getObjectFactory().newPrimitiveProperty("accountnumber", client.getObjectFactory().newPrimitiveValueBuilder().buildString("123"))
        );
        accountUpd.getProperties().add(
                client.getObjectFactory().newPrimitiveProperty("name", client.getObjectFactory().newPrimitiveValueBuilder().buildString("YPL2"))
        );

        // Set the Navigation link
        URI primaryContactRef = client.newURIBuilder(Resources.SERVICEROOT).appendEntitySetSegment("contacts").appendKeySegment(UUID.fromString(CONTACTS[0])).build();
        accountUpd.getNavigationLinks().add(
                client.getObjectFactory().newEntityNavigationLink("primarycontactid", primaryContactRef)
        );

        try {
            updateEntityNew(client, accountsUri, accountUpd);
        } catch (ServiceUnavailableException e) {
            e.printStackTrace();
        }
    }

    /**
     * Two ways of update :
     * - Full updates : UpdateType.REPLACE
     * - Partial updates : UpdateType.PATCH
     * It returns code [204 / No Content]
     *
     * @param client
     */
    private static void updateData(ODataClient client) {
        URI accountsUri = client.newURIBuilder(Resources.SERVICEROOT).appendEntitySetSegment("accounts").appendKeySegment(UUID.fromString(ACCOUNT_ID_UPD)).build();

        ClientEntity accountUpd = client.getObjectFactory().newEntity(new FullQualifiedName(NS_MICROSOFT_DYNAMICS_CRM, "account"));
        accountUpd.getProperties().add(
                client.getObjectFactory().newPrimitiveProperty("accountnumber", client.getObjectFactory().newPrimitiveValueBuilder().buildString("951"))
        );
        accountUpd.getProperties().add(
                client.getObjectFactory().newPrimitiveProperty("name", client.getObjectFactory().newPrimitiveValueBuilder().buildString("YPL4"))
        );

        try {
            updateEntityNew(client, accountsUri, accountUpd);
            //updateEntity(client, accountUpd, ACCOUNT_ID_UPD);
        } catch (ServiceUnavailableException e) {
            e.printStackTrace();
        }

        /* Generates : Caused by: org.apache.http.client.NonRepeatableRequestException: Cannot retry request with a non-repeatable request entity.
        ODataEntityUpdateRequest<ClientEntity> req = client.getCUDRequestFactory().getEntityUpdateRequest(accountsUri, UpdateType.PATCH, accountUpd);
        ODataEntityUpdateResponse<ClientEntity> res =req.execute();

        if(res.getStatusCode() == 204){
            System.out.println("Updated OK.");
        }
        else{
            System.out.println("Update Fails: " +  res.getStatusMessage());
        }*/
    }

    public static HttpResponse deleteEntity(ODataClient client, ClientEntity entity, String keySegment) throws ServiceUnavailableException {
        URIBuilder updateURIBuilder = client.newURIBuilder(Resources.SERVICEROOT).appendEntitySetSegment("accounts")
                .appendKeySegment(UUID.fromString(keySegment));
        HttpEntity httpEntity = convertToHttpEntity(client, entity);
        return createAndExecuteRequest(client, updateURIBuilder.build(), httpEntity, HttpMethod.DELETE);
    }

    public static HttpResponse updateEntityNew(ODataClient client, URI uri, ClientEntity entity) throws ServiceUnavailableException {
        HttpEntity httpEntity = convertToHttpEntity(client, entity);
        return createAndExecuteRequest(client, uri, httpEntity, HttpMethod.PATCH);
    }

    public static HttpResponse updateEntity(ODataClient client, ClientEntity entity, String keySegment) throws ServiceUnavailableException {
        URIBuilder updateURIBuilder = client.newURIBuilder(Resources.SERVICEROOT).appendEntitySetSegment("accounts")
                .appendKeySegment(UUID.fromString(keySegment));
        HttpEntity httpEntity = convertToHttpEntity(client, entity);
        return createAndExecuteRequest(client, updateURIBuilder.build(), httpEntity, HttpMethod.PATCH);
    }

    protected static HttpResponse createAndExecuteRequest(ODataClient odataClient, URI uri, HttpEntity httpEntity, HttpMethod method)
            throws ServiceUnavailableException {
        boolean hasRetried = false;
        while (true) {
            try {
                DefaultHttpClient httpClient = ntlm.create(null, null);
                HttpRequestBase request = null;
                if (method == HttpMethod.POST) {
                    request = new HttpPost(uri);
                } else if (method == HttpMethod.PATCH) {
                    request = new HttpPatch(uri);
                } else if (method == HttpMethod.DELETE) {
                    request = new HttpDelete(uri);
                } else {
                    throw new HttpClientException("Unsupported operation:" + method);
                }

                if (request instanceof HttpEntityEnclosingRequestBase) {
                    ((HttpEntityEnclosingRequestBase) request).setEntity(httpEntity);
                }
                HttpResponse response = httpClient.execute(request);
                if (isResponseSuccess(response.getStatusLine().getStatusCode())) {
                    request.releaseConnection();
                    EntityUtils.consume(response.getEntity());
                    return response;
                } else {
                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED && !hasRetried) {
                        hasRetried = true;
                        continue;
                    }
                    HttpEntity entity = response.getEntity();
                    String message = null;
                    if (entity != null) {
                        message = odataClient.getDeserializer(ContentType.JSON).toError(entity.getContent()).getMessage();
                    } else {
                        message = response.getStatusLine().getReasonPhrase();
                    }
                    throw new HttpClientException(message);
                }
            } catch (Exception e) {
                throw new HttpClientException(e);
            }
        }
    }

    public static boolean isResponseSuccess(int statusCode) {
        return statusCode == HttpStatus.SC_NO_CONTENT || statusCode == HttpStatus.SC_CREATED || statusCode == HttpStatus.SC_OK;
    }

    protected static HttpEntity convertToHttpEntity(ODataClient client, ClientEntity entity) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(output, Constants.UTF8);
            final ODataSerializer serializer = client.getSerializer(org.apache.olingo.commons.api.format.ContentType.JSON);
            serializer.write(writer, client.getBinder().getEntity(entity));
            HttpEntity httpEntity = new ByteArrayEntity(output.toByteArray(),
                    org.apache.http.entity.ContentType.APPLICATION_JSON);
            return httpEntity;
        } catch (Exception e) {
            throw new HttpClientException(e);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    private static void retrieveDatas(ODataClient client) {
        URI selEntityUri;// Retrieve datas
        // add .appendKeySegment(UUID.fromString("4E3EAB07-4999-E711-A95A-000D3A36863D")) to select only one entity
        // But in that cas it not a SET anymore
        selEntityUri = client.newURIBuilder(Resources.SERVICEROOT).appendEntitySetSegment(selectedEntity).select(selectedFields).orderBy(orderByField).expand(navLinkAutoExpand).build();
        ODataEntitySetIteratorRequest<ClientEntitySet, ClientEntity> request = client.getRetrieveRequestFactory().getEntitySetIteratorRequest(selEntityUri);
        request.setFormat(ContentType.JSON_NO_METADATA);
        ODataRetrieveResponse<ClientEntitySetIterator<ClientEntitySet, ClientEntity>> resp = request.execute();
        ClientEntitySetIterator<ClientEntitySet, ClientEntity> selEntityIt = resp.getBody();
        System.out.println("* Retrieve all " + selectedEntity + ": ======================================");
        while (selEntityIt.hasNext()) {
            ClientEntity entity = selEntityIt.next();
            System.out.println("\t* " + selectedEntity + " : " + entity.getId());

            // Retrieve a navigation link : Exception in thread "main" org.apache.olingo.client.api.communication.ODataClientErrorException: null [HTTP/1.1 401 Unauthorized]
            /*ClientProperty navVal = entity.getProperty(navLinktoRetrieve[0]);
            System.out.println("navVal : " + navVal );
            if (navVal != null) {
                ClientLink link = entity.getNavigationLink(navLinktoRetrieve[1]);
                URI linkUri = client.newURIBuilder(Resources.SERVICEROOT).appendNavigationSegment(link.getLink().toString()).build();
                ODataRetrieveResponse<ClientEntitySetIterator<ClientEntitySet, ClientEntity>> respLink = client.getRetrieveRequestFactory().getEntitySetIteratorRequest(linkUri).execute();
                ClientEntitySetIterator<ClientEntitySet, ClientEntity> linkIt = respLink.getBody();
                System.out.println("\t* " + selectedEntity + " : " + entity.getId());

                System.out.println("\t\t Navigation links (" + navLinktoRetrieve + "):");
                while (linkIt.hasNext()) {
                    ClientEntity l = linkIt.next();
                    System.out.println("\t\t\t - " + l.getId());
                }
            }*/

            /*System.out.println("\t\t- Navigation links:");
            List<ClientLink> links = entity.getNavigationLinks();
            for(ClientLink l : links){
                System.out.println("\t\t\t- " + l.getName()+":");
                URI linkUri = client.newURIBuilder(Resources.SERVICEROOT).appendNavigationSegment(l.getLink().toString()).build();
                ODataRetrieveResponse<ClientEntitySetIterator<ClientEntitySet, ClientEntity>> respLink = client.getRetrieveRequestFactory().getEntitySetIteratorRequest(linkUri).execute();
                ClientEntitySetIterator<ClientEntitySet, ClientEntity> itLink = respLink.getBody();

                while(itLink.hasNext()){
                    ClientEntity entLink = itLink.next();
                    System.out.println("\t\t\t\t- "+entLink.getId());
                }
            }*/

            // Navigation properties auto-expand : OK
            List<ClientLink> links = entity.getNavigationLinks();
            for (ClientLink l : links) {
                ClientInlineEntity inlineE = l.asInlineEntity();
                if (inlineE != null) {
                    ClientEntity e = l.asInlineEntity().getEntity();
                    System.out.println("\t\t- NavLink " + l.getName());
                    System.out.println("\t\t\t- " + e.getId());
                    showProperties(e, 4);
                } else {
                    System.out.println("\t\t- NO NavLink !");
                }
            }

            // Legacy properties
            showProperties(entity, 2);
        }
    }

    private static void showProperties(ClientEntity entity, int nbTabs) {
        List<ClientProperty> props = entity.getProperties();
        for (ClientProperty p : props) {
            String name = p.getName();
            ClientValue v = p.getValue();
            String t = v.getTypeName();
            String tabs = "";
            for (int i = 0; i < nbTabs; i++) {
                tabs += "\t";
            }
            System.out.println(tabs + "- " + name + " (" + t + ") : " + v);
        }
    }

    private static void listProperties(ODataClient client) {
        // Deeper introspection
        // Need Olingo V4.4.0 to avoid error : "where scheme, host, or port is different from the main metadata document URI"
        // see : https://issues.apache.org/jira/browse/OLINGO-1008
        EdmMetadataRequest metadataRequest = client.getRetrieveRequestFactory().getMetadataRequest(Resources.SERVICEROOT);
        ODataRetrieveResponse<Edm> metadataResp = metadataRequest.execute();

        String entityName = "account";
        try (FileOutputStream fos = new FileOutputStream(new File("C:/temp/MSCRM_Props_of" + entityName + ".txt"))) {
            // Retrieve all metadata of selectedEntity
            Edm edm = metadataResp.getBody();
            EdmEntityType accountType = edm.getEntityType(new FullQualifiedName(NS_MICROSOFT_DYNAMICS_CRM, entityName));
            List<String> propertyNames = accountType.getPropertyNames();
            List<String> modifiablePropertyNames = new ArrayList<>(propertyNames);
            Collections.sort(modifiablePropertyNames, new Comparator<String>() {
                @Override
                public int compare(String a, String b) {
                    return a.compareTo(b);
                }
            });

            fos.write(("* Details of " + selectedEntity + " =========================\n").getBytes());
            for (String propName : modifiablePropertyNames) {
                EdmProperty prop = accountType.getStructuralProperty(propName);
                FullQualifiedName typeName = prop.getType().getFullQualifiedName();
                fos.write(("\t- " + prop.getName() + " : " + typeName+"\n").getBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void listMetadata(ODataClient client) {
        // Deeper introspection
        // Need Olingo V4.4.0 to avoid error : "where scheme, host, or port is different from the main metadata document URI"
        // see : https://issues.apache.org/jira/browse/OLINGO-1008
        EdmMetadataRequest metadataRequest = client.getRetrieveRequestFactory().getMetadataRequest(Resources.SERVICEROOT);
        ODataRetrieveResponse<Edm> metadataResp = metadataRequest.execute();
        try (FileOutputStream fos = new FileOutputStream(new File("c:/temp/MSCRM_METADATA.txt"))) {

            // Retrieve all metadata of selectedEntity
            Edm edm = metadataResp.getBody();
            List<EdmSchema> schemas = edm.getSchemas();
            fos.write("* Metadata Schema ====================================================\n".getBytes());
            for (EdmSchema schema : schemas) {
                String namespace = schema.getNamespace();
                fos.write(("\tName space :" + namespace + "\n").getBytes());

                fos.write(("\t* complex types:\n").getBytes());
                for (EdmComplexType complexType : schema.getComplexTypes()) {
                    FullQualifiedName name = complexType.getFullQualifiedName();
                    fos.write(("\t\t- " + name + "\n").getBytes());
                }

                fos.write("\t* entity types:\n".getBytes());
                for (EdmEntityType entityType : schema.getEntityTypes()) {
                    FullQualifiedName name = entityType.getFullQualifiedName();
                    fos.write(("\t\t- " + name + "\n").getBytes());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
