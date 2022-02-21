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
package de.fraunhofer.iosb.ilt.faaast.service.requesthandlers.aas;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionManager;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.GetAssetAdministrationShellByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetAssetAdministrationShellByIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.requesthandlers.RequestHandler;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.AssetAdministrationShell;


public class GetAssetAdministrationShellByIdRequestHandler extends RequestHandler<GetAssetAdministrationShellByIdRequest, GetAssetAdministrationShellByIdResponse> {

    public GetAssetAdministrationShellByIdRequestHandler(Persistence persistence, MessageBus messageBus, AssetConnectionManager assetConnectionManager) {
        super(persistence, messageBus, assetConnectionManager);
    }


    @Override
    public GetAssetAdministrationShellByIdResponse process(GetAssetAdministrationShellByIdRequest request) {
        GetAssetAdministrationShellByIdResponse response = new GetAssetAdministrationShellByIdResponse();
        try {
            AssetAdministrationShell shell = (AssetAdministrationShell) persistence.get(request.getId(), request.getOutputModifier());
            response.setPayload(shell);
            response.setStatusCode(StatusCode.Success);
            publishElementReadEventMessage(AasUtils.toReference(shell), shell);
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
