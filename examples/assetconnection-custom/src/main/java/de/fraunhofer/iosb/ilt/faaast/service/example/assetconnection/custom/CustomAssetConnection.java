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
package de.fraunhofer.iosb.ilt.faaast.service.example.assetconnection.custom;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AbstractAssetConnection;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.example.assetconnection.custom.provider.CustomOperationProvider;
import de.fraunhofer.iosb.ilt.faaast.service.example.assetconnection.custom.provider.CustomSubscriptionProvider;
import de.fraunhofer.iosb.ilt.faaast.service.example.assetconnection.custom.provider.CustomValueProvider;
import de.fraunhofer.iosb.ilt.faaast.service.example.assetconnection.custom.provider.config.CustomOperationProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.example.assetconnection.custom.provider.config.CustomSubscriptionProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.example.assetconnection.custom.provider.config.CustomValueProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.Reference;


public class CustomAssetConnection extends
        AbstractAssetConnection<CustomAssetConnection, CustomAssetConnectionConfig, CustomValueProviderConfig, CustomValueProvider, CustomOperationProviderConfig, CustomOperationProvider, CustomSubscriptionProviderConfig, CustomSubscriptionProvider> {

    @Override
    public void close() throws AssetConnectionException {
        // nothing to do here
    }


    @Override
    protected CustomOperationProvider createOperationProvider(Reference reference, CustomOperationProviderConfig providerConfig) throws AssetConnectionException {
        try {
            return new CustomOperationProvider(reference, providerConfig, serviceContext);
        }
        catch (ConfigurationInitializationException e) {
            throw new AssetConnectionException(String.format("creating value provider failed (reference: %s)", AasUtils.asString(reference)), e);
        }
    }


    @Override
    protected CustomSubscriptionProvider createSubscriptionProvider(Reference reference, CustomSubscriptionProviderConfig providerConfig) throws AssetConnectionException {
        try {
            return new CustomSubscriptionProvider(reference, providerConfig, serviceContext);
        }
        catch (ValueMappingException e) {
            throw new AssetConnectionException(String.format("creating value provider failed (reference: %s)", AasUtils.asString(reference)), e);
        }
    }


    @Override
    protected CustomValueProvider createValueProvider(Reference reference, CustomValueProviderConfig providerConfig) throws AssetConnectionException {
        try {
            return new CustomValueProvider(reference, providerConfig, serviceContext);
        }
        catch (ValueMappingException e) {
            throw new AssetConnectionException(String.format("creating value provider failed (reference: %s)", AasUtils.asString(reference)), e);
        }
    }


    @Override
    protected void initConnection(CustomAssetConnectionConfig config) throws ConfigurationInitializationException {
        // nothing to do here
    }

}