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
import de.fraunhofer.iosb.ilt.faaast.service.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.PutConceptDescriptionByIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.PutConceptDescriptionByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.ConceptDescription;


public class PutConceptDescriptionByIdRequestHandler extends RequestHandler<PutConceptDescriptionByIdRequest, PutConceptDescriptionByIdResponse> {

    public PutConceptDescriptionByIdRequestHandler(Persistence persistence, MessageBus messageBus, AssetConnectionManager assetConnectionManager) {
        super(persistence, messageBus, assetConnectionManager);
    }


    @Override
    public PutConceptDescriptionByIdResponse process(PutConceptDescriptionByIdRequest request) {
        PutConceptDescriptionByIdResponse response = new PutConceptDescriptionByIdResponse();

        try {
            ConceptDescription conceptDescription = (ConceptDescription) persistence.get(request.getConceptDescription().getIdentification(), new OutputModifier());
            conceptDescription = (ConceptDescription) persistence.put(request.getConceptDescription());
            response.setPayload(conceptDescription);
            response.setStatusCode(StatusCode.Success);
            publishElementUpdateEventMessage(AasUtils.toReference(conceptDescription), conceptDescription);
        }
        catch (ResourceNotFoundException ex) {
            response.setStatusCode(StatusCode.ClientErrorResourceNotFound);
        }
        catch (Exception ex) {
            response.setStatusCode(StatusCode.ServerInternalError);
        }
        return response;
    }

}