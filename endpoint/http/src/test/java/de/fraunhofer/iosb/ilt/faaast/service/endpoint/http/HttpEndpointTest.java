/*
 * Copyright (c) 2021 Fraunhofer IOSB, eine rechtlich nicht selbstaendige
 * Einrichtung der Fraunhofer-Gesellschaft zur Foerderung der angewandten
 * Forschung e.V.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import de.fraunhofer.iosb.ilt.faaast.service.Service;
import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.mapper.QueryParameters;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.serialization.HttpJsonApiDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.serialization.HttpJsonApiSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.util.HttpConstants;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.AASFull;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Result;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Content;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Level;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GenerateSerializationByIdsResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetAllAssetAdministrationShellsResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetAllSubmodelElementsResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetAssetAdministrationShellResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetSubmodelByIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetSubmodelElementByPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.PostSubmodelResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.GenerateSerializationByIdsRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.serialization.DataFormat;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeExtractor;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ResponseHelper;
import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.Identifier;
import io.adminshell.aas.v3.model.IdentifierType;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.impl.DefaultAssetAdministrationShell;
import io.adminshell.aas.v3.model.impl.DefaultAssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.impl.DefaultIdentifier;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import io.adminshell.aas.v3.model.impl.DefaultRange;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringRequestContent;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.skyscreamer.jsonassert.comparator.JSONComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HttpEndpointTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpEndpointTest.class);
    private static final String HOST = "localhost";
    private static final JSONComparator RESULT_COMPARATOR = new CustomComparator(JSONCompareMode.LENIENT, new Customization("**.timestamp", (o1, o2) -> true));
    private static int port;
    private static HttpClient client;
    private static HttpEndpoint endpoint;
    private static Service service;
    private static Persistence persistence;
    private static HttpJsonApiDeserializer deserializer;

    @BeforeClass
    public static void init() throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            Assert.assertNotNull(serverSocket);
            Assert.assertTrue(serverSocket.getLocalPort() > 0);
            port = serverSocket.getLocalPort();
        }
        deserializer = new HttpJsonApiDeserializer();
        persistence = mock(Persistence.class);
        endpoint = new HttpEndpoint();
        service = spy(new Service(CoreConfig.DEFAULT, persistence, mock(MessageBus.class), List.of(endpoint), List.of()));
        endpoint.init(
                CoreConfig.DEFAULT,
                HttpEndpointConfig.builder()
                        .port(port)
                        .cors(true)
                        .build(),
                service);
        service.start();
        client = new HttpClient();
        client.start();
    }


    @Before
    public void setUp() {
        Mockito.reset(persistence);
        Mockito.reset(service);
    }


    @AfterClass
    public static void cleanUp() {
        if (client != null) {
            try {
                client.stop();
            }
            catch (Exception e) {
                LOGGER.info("error stopping HTTP client", e);
            }
        }
        if (endpoint != null) {
            try {
                endpoint.stop();
            }
            catch (Exception e) {
                LOGGER.info("error stopping HTTP endpoint", e);
            }
        }
    }


    public ContentResponse execute(HttpMethod method, String path, Map<String, String> parameters) throws Exception {
        return execute(method, path, parameters, null, null, null);
    }


    public ContentResponse execute(HttpMethod method, String path) throws Exception {
        return execute(method, path, null, null, null, null);
    }


    public ContentResponse execute(HttpMethod method, String path, OutputModifier outputModifier) throws Exception {
        return execute(method, path, Map.of(
                "content", outputModifier.getContent().name().toLowerCase(),
                "level", outputModifier.getLevel().name().toLowerCase(),
                "extend", outputModifier.getExtent().name().toLowerCase()),
                null, null, null);
    }


    public ContentResponse execute(HttpMethod method, String path, Map<String, String> parameters, String body, String contentType, Map<String, String> headers) throws Exception {
        org.eclipse.jetty.client.api.Request request = client.newRequest(HOST, port)
                .method(method)
                .path(path);
        if (parameters != null) {
            for (Map.Entry<String, String> parameter: parameters.entrySet()) {
                request = request.param(parameter.getKey(), parameter.getValue());
            }
        }
        if (body != null) {
            if (contentType != null) {
                request = request.body(new StringRequestContent(contentType, body));
            }
            else {
                request = request.body(new StringRequestContent(body));
            }
        }
        if (headers != null) {
            for (Map.Entry<String, String> header: headers.entrySet()) {
                request = request.header(header.getKey(), header.getValue());
            }
        }
        return request.send();
    }


    @Test
    public void testInvalidUrl() throws Exception {
        ContentResponse response = execute(HttpMethod.GET, "/foo/bar");
        Assert.assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    }


    @Test
    public void testInvalidMethod() throws Exception {
        ContentResponse response = execute(HttpMethod.PATCH, "/shells");
        Assert.assertEquals(HttpStatus.METHOD_NOT_ALLOWED_405, response.getStatus());
    }


    @Test
    public void testCORSEnabled() throws Exception {
        ContentResponse response = execute(HttpMethod.GET, "/foo/bar");
        Assert.assertEquals("*", response.getHeaders().get(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER));
        Assert.assertEquals("true", response.getHeaders().get(CrossOriginFilter.ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER));
        Assert.assertEquals("Content-Type", response.getHeaders().get(CrossOriginFilter.ACCESS_CONTROL_ALLOW_HEADERS_HEADER));
    }


    @Test
    public void testPreflightedCORSRequestSupported() throws Exception {
        ContentResponse response = execute(HttpMethod.OPTIONS, "/shells", null, null, null,
                Map.of(CrossOriginFilter.ACCESS_CONTROL_REQUEST_METHOD_HEADER, "GET,POST"));
        assertAccessControllAllowMessageHeader(response, HttpMethod.GET, HttpMethod.POST, HttpMethod.OPTIONS);
        Assert.assertEquals(204, response.getStatus());
    }


    @Test
    public void testPreflightedCORSRequestUnsupported() throws Exception {
        ContentResponse response = execute(HttpMethod.OPTIONS, "/shells", null, null, null,
                Map.of(CrossOriginFilter.ACCESS_CONTROL_REQUEST_METHOD_HEADER, "PUT"));
        assertAccessControllAllowMessageHeader(response, HttpMethod.GET, HttpMethod.POST, HttpMethod.OPTIONS);
        Assert.assertEquals(400, response.getStatus());
    }


    @Test
    public void testPreflightedCORSRequestInvalidRequestMethodHeader() throws Exception {
        ContentResponse response = execute(HttpMethod.OPTIONS, "/shells", null, null, null,
                Map.of(CrossOriginFilter.ACCESS_CONTROL_REQUEST_METHOD_HEADER, "FOO"));
        Assert.assertEquals(400, response.getStatus());
    }


    @Test
    public void testPreflightedCORSRequestNoRequestMethodHeader() throws Exception {
        ContentResponse response = execute(HttpMethod.OPTIONS, "/", null, null, null, null);
        assertAccessControllAllowMessageHeader(response, HttpMethod.OPTIONS);
        Assert.assertEquals(204, response.getStatus());
    }


    @Test
    public void testPreflightedCORSRequestEmptyRequestMethodHeader() throws Exception {
        ContentResponse response = execute(HttpMethod.OPTIONS, "/", null, null, null,
                Map.of(CrossOriginFilter.ACCESS_CONTROL_REQUEST_METHOD_HEADER, ""));
        assertAccessControllAllowMessageHeader(response, HttpMethod.OPTIONS);
        Assert.assertEquals(204, response.getStatus());
    }


    @Test
    public void testParamContentNormal() throws Exception {
        Identifier id = new DefaultIdentifier();
        when(service.execute(any())).thenReturn(GetAssetAdministrationShellResponse.builder()
                .statusCode(StatusCode.SUCCESS)
                .build());
        ContentResponse response = execute(HttpMethod.GET, "/shells/" + EncodingHelper.base64UrlEncode(id.toString()) + "/aas?content=normal");
        Assert.assertEquals(HttpStatus.OK_200, response.getStatus());
    }


    @Test
    @Ignore("content=trimmed currently under discussion")
    public void testParamContentTrimmed() throws Exception {
        Identifier id = new DefaultIdentifier();
        when(service.execute(any())).thenReturn(GetAssetAdministrationShellResponse.builder()
                .statusCode(StatusCode.SUCCESS)
                .build());
        ContentResponse response = execute(HttpMethod.GET, "/shells/" + EncodingHelper.base64UrlEncode(id.toString()) + "/aas?content=trimmed");
        Assert.assertEquals(HttpStatus.OK_200, response.getStatus());
    }


    @Test
    public void testParamContentReference() throws Exception {
        Identifier id = new DefaultIdentifier();
        when(service.execute(any())).thenReturn(GetAssetAdministrationShellResponse.builder()
                .statusCode(StatusCode.SUCCESS)
                .build());
        ContentResponse response = execute(HttpMethod.GET, "/shells/" + EncodingHelper.base64UrlEncode(id.toString()) + "/aas?content=reference");
        Assert.assertEquals(HttpStatus.OK_200, response.getStatus());
    }


    @Test
    public void testParamContentLevelBogus() throws Exception {
        Identifier id = new DefaultIdentifier();
        when(service.execute(any())).thenReturn(GetAssetAdministrationShellResponse.builder()
                .statusCode(StatusCode.SUCCESS)
                .build());
        ContentResponse response = execute(HttpMethod.GET, "/shells/" + EncodingHelper.base64UrlEncode(id.toString()) + "/aas?content=bogus&level=bogus");
        Assert.assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
        String actual = response.getContentAsString();
        String expected = new HttpJsonApiSerializer().write(Result.error("invalid output modifier"));
        JSONAssert.assertEquals(expected, actual, RESULT_COMPARATOR);
    }


    @Test
    public void testInvalidAASIdentifier() throws Exception {
        when(service.execute(any())).thenReturn(GetAssetAdministrationShellResponse.builder()
                .statusCode(StatusCode.SUCCESS)
                .build());
        ContentResponse response = execute(HttpMethod.GET, "/shells/bogus/aas");
        Assert.assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    }


    @Test
    public void testInvalidAASIdentifierAndAdditionalPathElement() throws Exception {
        when(service.execute(any())).thenReturn(GetAssetAdministrationShellResponse.builder()
                .statusCode(StatusCode.SUCCESS)
                .build());
        ContentResponse response = execute(HttpMethod.DELETE, "/shells/bogus/test");
        Assert.assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    }


    @Test
    public void testInvalidBase64Param() throws Exception {
        ContentResponse response = execute(HttpMethod.GET, "/concept-descriptions/InvalidBase64");
        Assert.assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    }


    @Test
    public void testNonExistentId() throws Exception {
        String idShort = AASFull.SUBMODEL_3.getIdShort() + "123";
        when(service.execute(any())).thenReturn(GetSubmodelByIdResponse.builder()
                .statusCode(StatusCode.CLIENT_ERROR_RESOURCE_NOT_FOUND)
                .payload(null)
                .build());
        ContentResponse response = execute(HttpMethod.GET, "/submodels/" + EncodingHelper.base64UrlEncode(idShort));
        Assert.assertEquals(HttpStatus.NOT_FOUND_404, response.getStatus());
    }


    @Test
    public void testDoubleQueryValue() throws Exception {
        String idShort = AASFull.SUBMODEL_3.getIdShort() + "123";
        when(service.execute(any())).thenReturn(GetSubmodelByIdResponse.builder()
                .statusCode(StatusCode.SUCCESS)
                .payload(null)
                .build());
        ContentResponse response = execute(HttpMethod.GET, "/submodels/" + EncodingHelper.base64UrlEncode(idShort)
                + "/submodel/submodel-elements/ExampleRelationshipElement?level=normal&level=deep");
        Assert.assertEquals(HttpStatus.OK_200, response.getStatus());
    }


    @Test
    public void testMissingQueryValue() throws Exception {
        String idShort = AASFull.SUBMODEL_3.getIdShort() + "123";
        when(service.execute(any())).thenReturn(GetSubmodelByIdResponse.builder()
                .statusCode(StatusCode.SUCCESS)
                .payload(null)
                .build());
        ContentResponse response = execute(HttpMethod.GET, "/submodels/" + EncodingHelper.base64UrlEncode(idShort)
                + "/submodel/submodel-elements/ExampleRelationshipElement?level=normal&content=");
        Assert.assertEquals(HttpStatus.OK_200, response.getStatus());
    }


    @Test
    public void testBogusAndMissingQueryValue() throws Exception {
        String idShort = AASFull.SUBMODEL_3.getIdShort() + "123";
        when(service.execute(any())).thenReturn(GetSubmodelByIdResponse.builder()
                .statusCode(StatusCode.SUCCESS)
                .payload(null)
                .build());
        ContentResponse response = execute(HttpMethod.GET, "/submodels/" + EncodingHelper.base64UrlEncode(idShort)
                + "/submodel/submodel-elements/ExampleRelationshipElement?level=normal&bogus");
        Assert.assertEquals(HttpStatus.OK_200, response.getStatus());
    }


    @Test
    public void testWrongResponse() throws Exception {
        when(service.execute(any())).thenReturn(GetAllAssetAdministrationShellsResponse.builder()
                .statusCode(StatusCode.SUCCESS)
                .payload(null)
                .build());
        ContentResponse response = execute(HttpMethod.GET, "/GetAllSubmodels");
        // TODO: Discuss which status code is applicable 400/500 ?
        Assert.assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    }


    @Test
    public void testPostSubmodelNoData() throws Exception {
        when(service.execute(any())).thenReturn(PostSubmodelResponse.builder()
                .statusCode(StatusCode.SUCCESS)
                .payload(null)
                .build());
        ContentResponse response = execute(HttpMethod.POST, "/submodels");
        Assert.assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    }


    @Test
    public void testGetAllAssetAdministrationShells() throws Exception {
        List<AssetAdministrationShell> expectedPayload = List.of(AASFull.AAS_1);
        when(service.execute(any())).thenReturn(GetAllAssetAdministrationShellsResponse.builder()
                .statusCode(StatusCode.SUCCESS)
                .payload(expectedPayload)
                .build());
        ContentResponse response = execute(HttpMethod.GET, "/shells");
        Assert.assertEquals(HttpStatus.OK_200, response.getStatus());
        // TODO: server not returning character encoding
        LOGGER.info("http response encoding: {}", response.getEncoding());
        LOGGER.info("http response content: {}", new String(response.getContent(), "UTF-8"));
        List<AssetAdministrationShell> actualPayload = deserializer.readList(new String(response.getContent(), "UTF-8"), AssetAdministrationShell.class);
        Assert.assertEquals(expectedPayload, actualPayload);
    }


    @Test
    public void testSerializationJson() throws Exception {
        AssetAdministrationShellEnvironment expected = new DefaultAssetAdministrationShellEnvironment.Builder()
                .assetAdministrationShells(AASFull.AAS_2)
                .submodels(AASFull.SUBMODEL_4)
                .submodels(AASFull.SUBMODEL_5)
                .build();
        when(service.execute(any())).thenReturn(GenerateSerializationByIdsResponse.builder()
                .dataformat(DataFormat.JSON)
                .payload(expected)
                .statusCode(StatusCode.SUCCESS)
                .build());
        ContentResponse response = execute(
                HttpMethod.GET,
                "/serialization",
                Map.of(
                        "aasIds", EncodingHelper.base64UrlEncode(expected.getAssetAdministrationShells().stream()
                                .map(x -> x.getIdentification().getIdentifier())
                                .collect(Collectors.joining(","))),
                        "submodelIds", EncodingHelper.base64UrlEncode(expected.getSubmodels().stream()
                                .map(x -> x.getIdentification().getIdentifier())
                                .collect(Collectors.joining(","))),
                        QueryParameters.INCLUDE_CONCEPT_DESCRIPTIONS, "false"),
                null,
                null,
                Map.of(
                        HttpConstants.HEADER_ACCEPT, DataFormat.JSON.getContentType().withoutParameters().toString()));
        Assert.assertEquals(HttpStatus.OK_200, response.getStatus());
        LOGGER.info("http response encoding: {}", response.getEncoding());
        LOGGER.info("http response content: {}", response.getContentAsString());
        AssetAdministrationShellEnvironment actual = deserializer.read(response.getContentAsString(), AssetAdministrationShellEnvironment.class);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testSerializationWildcard() throws Exception {
        AssetAdministrationShellEnvironment expected = new DefaultAssetAdministrationShellEnvironment.Builder()
                .assetAdministrationShells(AASFull.AAS_2)
                .submodels(AASFull.SUBMODEL_4)
                .submodels(AASFull.SUBMODEL_5)
                .build();
        when(service.execute(argThat((GenerateSerializationByIdsRequest request) -> request.getSerializationFormat() == DataFormat.JSON)))
                .thenReturn(GenerateSerializationByIdsResponse.builder()
                        .dataformat(DataFormat.JSON)
                        .payload(expected)
                        .statusCode(StatusCode.SUCCESS)
                        .build());
        ContentResponse response = execute(
                HttpMethod.GET,
                "/serialization",
                Map.of(
                        "aasIds", EncodingHelper.base64UrlEncode(expected.getAssetAdministrationShells().stream()
                                .map(x -> x.getIdentification().getIdentifier())
                                .collect(Collectors.joining(","))),
                        "submodelIds", EncodingHelper.base64UrlEncode(expected.getSubmodels().stream()
                                .map(x -> x.getIdentification().getIdentifier())
                                .collect(Collectors.joining(","))),
                        QueryParameters.INCLUDE_CONCEPT_DESCRIPTIONS, "false"),
                null,
                null,
                Map.of(
                        HttpConstants.HEADER_ACCEPT, "*/*"));
        Assert.assertEquals(HttpStatus.OK_200, response.getStatus());
        LOGGER.info("http response encoding: {}", response.getEncoding());
        LOGGER.info("http response content: {}", response.getContentAsString());
        AssetAdministrationShellEnvironment actual = deserializer.read(response.getContentAsString(), AssetAdministrationShellEnvironment.class);
        Assert.assertEquals(expected, actual);
    }


    @Test
    @Ignore("value only serialization not defined for AssetAdministrationShells")
    public void testGetAllAssetAdministrationShellsValueOnly() throws Exception {
        List<AssetAdministrationShell> expectedPayload = List.of(AASFull.AAS_1);
        when(service.execute(any())).thenReturn(GetAllAssetAdministrationShellsResponse.builder()
                .statusCode(StatusCode.SUCCESS)
                .payload(expectedPayload)
                .build());
        ContentResponse response = execute(HttpMethod.GET, "/shells", new OutputModifier.Builder()
                .content(Content.VALUE)
                .build());
        Assert.assertEquals(HttpStatus.OK_200, response.getStatus());
        List<AssetAdministrationShell> actualPayload = deserializer.readList(new String(response.getContent()), AssetAdministrationShell.class);
        Assert.assertEquals(expectedPayload, actualPayload);
    }


    @Test
    public void testGetAllSubmodelElementsValueOnly() throws Exception {
        List<SubmodelElement> submodelElements = List.of(
                new DefaultProperty.Builder()
                        .idShort("property1")
                        .value("hello world")
                        .valueType("string")
                        .build(),
                new DefaultRange.Builder()
                        .idShort("range1")
                        .min("1.1")
                        .max("2.0")
                        .valueType("double")
                        .build());
        when(service.execute(any())).thenReturn(GetAllSubmodelElementsResponse.builder()
                .statusCode(StatusCode.SUCCESS)
                .payload(submodelElements)
                .build());
        ContentResponse response = execute(HttpMethod.GET, "/submodels/foo/submodel/submodel-elements", new OutputModifier.Builder()
                .content(Content.VALUE)
                .build());
        Assert.assertEquals(HttpStatus.OK_200, response.getStatus());
        List<ElementValue> actual = deserializer.readValueList(new String(response.getContent()), TypeExtractor.extractTypeInfo(submodelElements));
        List<ElementValue> expected = submodelElements.stream()
                .map(LambdaExceptionHelper.rethrowFunction(x -> (ElementValue) ElementValueMapper.toValue(x)))
                .collect(Collectors.toList());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAllSubmodelElements() throws Exception {
        List<SubmodelElement> expected = List.of(
                new DefaultProperty.Builder()
                        .idShort("property1")
                        .value("hello world")
                        .valueType("string")
                        .build(),
                new DefaultRange.Builder()
                        .idShort("range1")
                        .min("1.1")
                        .max("2.0")
                        .valueType("double")
                        .build());
        when(service.execute(any())).thenReturn(GetAllSubmodelElementsResponse.builder()
                .statusCode(StatusCode.SUCCESS)
                .payload(expected)
                .build());
        ContentResponse response = execute(HttpMethod.GET, "/submodels/foo/submodel/submodel-elements", new OutputModifier.Builder()
                .content(Content.NORMAL)
                .build());
        Assert.assertEquals(HttpStatus.OK_200, response.getStatus());
        List<SubmodelElement> actual = deserializer.readList(new String(response.getContent()), SubmodelElement.class);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAllSubmodelElementsInAasContext() throws Exception {
        String aasId = "aasId";
        String submodelId = "submodelId";
        Reference submodelRef = ReferenceHelper.build(aasId, submodelId);
        AssetAdministrationShell aas = new DefaultAssetAdministrationShell.Builder()
                .identification(new DefaultIdentifier.Builder()
                        .idType(IdentifierType.IRI)
                        .identifier(aasId)
                        .build())
                .submodel(submodelRef)
                .build();
        List<SubmodelElement> expected = List.of(
                new DefaultProperty.Builder()
                        .idShort("property1")
                        .value("hello world")
                        .valueType("string")
                        .build(),
                new DefaultRange.Builder()
                        .idShort("range1")
                        .min("1.1")
                        .max("2.0")
                        .valueType("double")
                        .build());
        when(service.execute(any())).thenReturn(
                GetAllSubmodelElementsResponse.builder()
                        .statusCode(StatusCode.SUCCESS)
                        .payload(expected)
                        .build());
        when(persistence.get(
                aas.getIdentification(),
                new OutputModifier.Builder()
                        .level(Level.CORE)
                        .build(),
                AssetAdministrationShell.class))
                        .thenReturn(aas);
        mockAasContext(service, aasId);
        ContentResponse response = execute(
                HttpMethod.GET,
                String.format("/shells/%s/aas/submodels/%s/submodel/submodel-elements",
                        EncodingHelper.base64UrlEncode(aasId),
                        EncodingHelper.base64UrlEncode(submodelId)),
                new OutputModifier.Builder()
                        .content(Content.NORMAL)
                        .build());
        Assert.assertEquals(HttpStatus.OK_200, response.getStatus());
        List<SubmodelElement> actual = deserializer.readList(new String(response.getContent()), SubmodelElement.class);
        Assert.assertEquals(expected, actual);
    }


    private void mockAasContext(ServiceContext serviceContext, String aasId) {
        when(serviceContext.getAASEnvironment()).thenReturn(new DefaultAssetAdministrationShellEnvironment.Builder()
                .assetAdministrationShells(new DefaultAssetAdministrationShell.Builder()
                        .identification(new DefaultIdentifier.Builder()
                                .identifier(aasId)
                                .build())
                        .build())
                .build());
    }


    @Test
    public void testResultServerError() throws Exception {
        Result expected = Result.error(HttpStatus.getMessage(500));
        when(service.execute(any())).thenReturn(GetSubmodelElementByPathResponse.builder()
                .statusCode(StatusCode.SERVER_INTERNAL_ERROR)
                .result(expected)
                .payload(null)
                .build());
        ContentResponse response = execute(HttpMethod.GET, "/shells/" + EncodingHelper.base64UrlEncode("Whatever") + "/aas");
        Result actual = deserializer.read(new String(response.getContent()), Result.class);
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testResultBadRequest() throws Exception {
        Result expected = Result.error("no matching request mapper found for URL 'shellsX'");
        ContentResponse response = execute(HttpMethod.GET, "/shellsX/");
        Result actual = deserializer.read(new String(response.getContent()), Result.class);
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testMethodNotAllowed() throws Exception {
        Result expected = Result.error("method 'PUT' not allowed for URL 'shells' (allowed methods: GET, POST)");
        ContentResponse response = execute(HttpMethod.PUT, "/shells");
        Result actual = deserializer.read(new String(response.getContent()), Result.class);
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testResultNotFound() throws Exception {
        Result expected = Result.error(HttpStatus.getMessage(404));
        when(service.execute(any())).thenReturn(GetSubmodelElementByPathResponse.builder()
                .statusCode(StatusCode.CLIENT_ERROR_RESOURCE_NOT_FOUND)
                .payload(null)
                .result(expected)
                .build());
        Identifier id = new DefaultIdentifier();
        ContentResponse response = execute(HttpMethod.GET, "/submodels/" + EncodingHelper.base64UrlEncode(id.toString()) + "/submodel/submodel-elements/Invalid");
        Result actual = deserializer.read(new String(response.getContent()), Result.class);
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    private void assertAccessControllAllowMessageHeader(ContentResponse response, HttpMethod... expected) {
        List<String> actual = Arrays.asList(response.getHeaders().get(CrossOriginFilter.ACCESS_CONTROL_ALLOW_METHODS_HEADER).split(HttpConstants.HEADER_VALUE_SEPARATOR));
        List<String> expectedAsString = Stream.of(expected).map(Enum::name).collect(Collectors.toList());
        Assert.assertTrue(actual.size() == expectedAsString.size()
                && actual.containsAll(expectedAsString)
                && expectedAsString.containsAll(actual));
    }
}
