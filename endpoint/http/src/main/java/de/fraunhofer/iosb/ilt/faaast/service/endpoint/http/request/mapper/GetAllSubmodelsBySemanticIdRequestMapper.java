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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.mapper;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.exception.InvalidRequestException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetAllSubmodelsBySemanticIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.GetAllSubmodelsBySemanticIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import io.adminshell.aas.v3.model.Reference;
import java.util.Map;


/**
 * class to map HTTP-GET-Request path: submodels.
 */
public class GetAllSubmodelsBySemanticIdRequestMapper extends AbstractRequestMapperWithOutputModifier<GetAllSubmodelsBySemanticIdRequest, GetAllSubmodelsBySemanticIdResponse> {

    private static final String PATTERN = "submodels";

    public GetAllSubmodelsBySemanticIdRequestMapper(ServiceContext serviceContext) {
        super(serviceContext, HttpMethod.GET, PATTERN);
    }


    @Override
    public boolean matchesUrl(HttpRequest httpRequest) {
        return super.matchesUrl(httpRequest) && httpRequest.hasQueryParameter(QueryParameters.SEMANTIC_ID);
    }


    @Override
    public GetAllSubmodelsBySemanticIdRequest doParse(HttpRequest httpRequest, Map<String, String> urlParameters, OutputModifier outputModifier) throws InvalidRequestException {
        try {
            return GetAllSubmodelsBySemanticIdRequest.builder()
                    .semanticId(deserializer.read(EncodingHelper.base64UrlDecode(httpRequest.getQueryParameter(QueryParameters.SEMANTIC_ID)), Reference.class))
                    .build();
        }
        catch (DeserializationException e) {
            throw new InvalidRequestException(
                    String.format(
                            "error deserializing %s (value: %s)",
                            QueryParameters.SEMANTIC_ID,
                            httpRequest.getQueryParameter(QueryParameters.SEMANTIC_ID)),
                    e);
        }
    }
}
