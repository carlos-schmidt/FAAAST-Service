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
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.PostConceptDescriptionResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementCreateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.PostConceptDescriptionRequest;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import io.adminshell.aas.v3.model.ConceptDescription;


/**
 * Class to handle a {@link de.fraunhofer.iosb.ilt.faaast.service.model.request.PostConceptDescriptionRequest} in the
 * service and to send the corresponding response
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.response.PostConceptDescriptionResponse}. Is responsible for
 * communication with the persistence and sends the corresponding events to the message bus.
 */
public class PostConceptDescriptionRequestHandler extends AbstractRequestHandler<PostConceptDescriptionRequest, PostConceptDescriptionResponse> {

    public PostConceptDescriptionRequestHandler(Persistence persistence, MessageBus messageBus, AssetConnectionManager assetConnectionManager) {
        super(persistence, messageBus, assetConnectionManager);
    }


    @Override
    public PostConceptDescriptionResponse process(PostConceptDescriptionRequest request) throws Exception {
        ConceptDescription conceptDescription = (ConceptDescription) persistence.put(request.getConceptDescription());
        messageBus.publish(ElementCreateEventMessage.builder()
                .element(conceptDescription)
                .value(conceptDescription)
                .build());
        return PostConceptDescriptionResponse.builder()
                .payload(conceptDescription)
                .created()
                .build();
    }

}
