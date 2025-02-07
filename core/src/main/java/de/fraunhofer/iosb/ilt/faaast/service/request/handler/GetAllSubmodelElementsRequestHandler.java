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
package de.fraunhofer.iosb.ilt.faaast.service.request.handler;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionManager;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetAllSubmodelElementsResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.access.ElementReadEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.GetAllSubmodelElementsRequest;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElement;
import java.util.List;


/**
 * Class to handle a {@link de.fraunhofer.iosb.ilt.faaast.service.model.request.GetAllSubmodelElementsRequest} in the
 * service and to send the corresponding response
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetAllSubmodelElementsResponse}. Is responsible for
 * communication with the persistence and sends the corresponding events to the message bus.
 */
public class GetAllSubmodelElementsRequestHandler extends AbstractSubmodelInterfaceRequestHandler<GetAllSubmodelElementsRequest, GetAllSubmodelElementsResponse> {

    public GetAllSubmodelElementsRequestHandler(Persistence persistence, MessageBus messageBus, AssetConnectionManager assetConnectionManager) {
        super(persistence, messageBus, assetConnectionManager);
    }


    @Override
    public GetAllSubmodelElementsResponse doProcess(GetAllSubmodelElementsRequest request)
            throws AssetConnectionException, ValueMappingException, ResourceNotFoundException, MessageBusException {
        Reference reference = ReferenceHelper.toReference(request.getSubmodelId(), Submodel.class);
        List<SubmodelElement> submodelElements = persistence.getSubmodelElements(reference, null, request.getOutputModifier());
        syncWithAsset(reference, submodelElements);
        if (submodelElements != null) {
            submodelElements.forEach(LambdaExceptionHelper.rethrowConsumer(
                    x -> messageBus.publish(ElementReadEventMessage.builder()
                            .element(AasUtils.toReference(reference, x))
                            .value(x)
                            .build())));
        }
        return GetAllSubmodelElementsResponse.builder()
                .payload(submodelElements)
                .success()
                .build();
    }

}
