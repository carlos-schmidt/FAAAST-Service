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
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.GetAllAssetAdministrationShellsByAssetIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.response.GetAllAssetAdministrationShellsByAssetIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.requesthandlers.RequestHandler;


public class GetAllAssetAdministrationShellsByAssetIdRequestHandler
        extends RequestHandler<GetAllAssetAdministrationShellsByAssetIdRequest, GetAllAssetAdministrationShellsByAssetIdResponse> {

    public GetAllAssetAdministrationShellsByAssetIdRequestHandler(Persistence persistence, MessageBus messageBus, AssetConnectionManager assetConnectionManager) {
        super(persistence, messageBus, assetConnectionManager);
    }


    @Override
    public GetAllAssetAdministrationShellsByAssetIdResponse process(GetAllAssetAdministrationShellsByAssetIdRequest request) {
        GetAllAssetAdministrationShellsByAssetIdResponse response = new GetAllAssetAdministrationShellsByAssetIdResponse();
        try {
            //TODO: Unclear what to do here? GetAllAssetAdministrationShellsByAssetIdResponse should have an AssetIdentification attribute
            //TODO: What about specific asset id?
            //            GlobalAssetIdentification assetIdentification = new GlobalAssetIdentification();
            //            assetIdentification.setReference(new DefaultReference.Builder()
            //                    .key(AasUtils.parseKey(request.getKey()))
            //                    .build());
            //            List<AssetAdministrationShell> shells = persistence.get(null, assetIdentification, request.getOutputModifier());
            //            response.setPayload(shells);
            //            response.setStatusCode(StatusCode.Success);
            //            if (shells != null) {
            //                shells.forEach(x -> publishElementReadEventMessage(AasUtils.toReference(x), x));
            //            }
        }
        catch (Exception ex) {
            response.setStatusCode(StatusCode.ServerInternalError);
        }
        return response;
    }

}