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

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionManager;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetAllConceptDescriptionsByDataSpecificationReferenceResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.access.ElementReadEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.GetAllConceptDescriptionsByDataSpecificationReferenceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import io.adminshell.aas.v3.model.ConceptDescription;
import java.util.List;


/**
 * Class to handle a
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.request.GetAllConceptDescriptionsByDataSpecificationReferenceRequest}
 * in the service and to send the corresponding response
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetAllConceptDescriptionsByDataSpecificationReferenceResponse}.
 * Is responsible for communication with the persistence and sends the corresponding events to the message bus.
 */
public class GetAllConceptDescriptionsByDataSpecificationReferenceRequestHandler
        extends AbstractRequestHandler<GetAllConceptDescriptionsByDataSpecificationReferenceRequest, GetAllConceptDescriptionsByDataSpecificationReferenceResponse> {

    public GetAllConceptDescriptionsByDataSpecificationReferenceRequestHandler(Persistence persistence, MessageBus messageBus, AssetConnectionManager assetConnectionManager) {
        super(persistence, messageBus, assetConnectionManager);
    }


    @Override
    public GetAllConceptDescriptionsByDataSpecificationReferenceResponse process(GetAllConceptDescriptionsByDataSpecificationReferenceRequest request) throws MessageBusException {
        List<ConceptDescription> conceptDescriptions = persistence.get(null, null, request.getDataSpecificationReference(), request.getOutputModifier());
        if (conceptDescriptions != null) {
            conceptDescriptions.forEach(LambdaExceptionHelper.rethrowConsumer(
                    x -> messageBus.publish(ElementReadEventMessage.builder()
                            .element(x)
                            .value(x)
                            .build())));
        }
        return GetAllConceptDescriptionsByDataSpecificationReferenceResponse.builder()
                .payload(conceptDescriptions)
                .success()
                .build();
    }

}
