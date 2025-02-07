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
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.exception.InvalidRequestException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetOperationAsyncResultResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.GetOperationAsyncResultRequest;
import de.fraunhofer.iosb.ilt.faaast.service.util.ElementPathHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.RegExHelper;
import java.util.Map;


/**
 * class to map HTTP-GET-Request paths:
 * submodels/{submodelIdentifier}/submodel/submodel-elements/{idShortPath}/operation-Results/(.*),
 * shells/{aasIdentifier}/aas/submodels/{submodelIdentifier}/submodel/submodel-elements/{idShortPath}/operation-Results/(.*).
 */
public class GetOperationAsyncResultRequestMapper extends AbstractSubmodelInterfaceRequestMapper<GetOperationAsyncResultRequest, GetOperationAsyncResultResponse> {

    private static final String SUBMODEL_ELEMENT_PATH = RegExHelper.uniqueGroupName();
    private static final String HANDLE_ID = RegExHelper.uniqueGroupName();
    private static final String PATTERN = String.format("submodel-elements/%s/operation-results/%s",
            pathElement(SUBMODEL_ELEMENT_PATH),
            pathElement(HANDLE_ID));

    public GetOperationAsyncResultRequestMapper(ServiceContext serviceContext) {
        super(serviceContext, HttpMethod.GET, PATTERN);
    }


    @Override
    public GetOperationAsyncResultRequest doParse(HttpRequest httpRequest, Map<String, String> urlParameters, OutputModifier outputModifier) throws InvalidRequestException {
        return GetOperationAsyncResultRequest.builder()
                .path(ElementPathHelper.toKeys(urlParameters.get(SUBMODEL_ELEMENT_PATH)))
                .handleId(EncodingHelper.base64UrlDecode(urlParameters.get(HANDLE_ID)))
                .build();
    }
}
