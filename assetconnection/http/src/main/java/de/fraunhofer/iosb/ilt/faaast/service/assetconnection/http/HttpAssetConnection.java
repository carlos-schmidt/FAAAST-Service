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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AbstractAssetConnection;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.HttpOperationProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.HttpSubscriptionProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.HttpValueProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.config.HttpOperationProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.config.HttpSubscriptionProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.config.HttpValueProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import io.adminshell.aas.v3.model.Reference;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.http.HttpClient;
import org.apache.commons.lang3.StringUtils;


/**
 * Implementation of {@link de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnection} for the HTTP
 * protocol.
 *
 * <p>Following asset connection operations are supported:
 * <ul>
 * <li>setting values (via HTTP PUT)
 * <li>reading values (via HTTP GET)
 * </ul>
 *
 * <p>Following asset connection operations are not supported:
 * <ul>
 * <li>subscribing to values
 * <li>executing operations
 * </ul>
 *
 * <p>This implementation currently only supports submodel elements of type {@link io.adminshell.aas.v3.model.Property}
 * resp. {@link de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue}.
 *
 * <p>This class uses a single underlying HTTP connection.
 */
public class HttpAssetConnection extends
        AbstractAssetConnection<HttpAssetConnection, HttpAssetConnectionConfig, HttpValueProviderConfig, HttpValueProvider, HttpOperationProviderConfig, HttpOperationProvider, HttpSubscriptionProviderConfig, HttpSubscriptionProvider> {

    private HttpClient client;

    public HttpAssetConnection() {
        super();
    }


    protected HttpAssetConnection(CoreConfig coreConfig, HttpAssetConnectionConfig config, ServiceContext serviceContext) throws ConfigurationInitializationException {
        super(coreConfig, config, serviceContext);
    }


    @Override
    public String getEndpointInformation() {
        return config.getBaseUrl().toString();
    }


    @Override
    protected HttpValueProvider createValueProvider(Reference reference, HttpValueProviderConfig providerConfig) {
        return new HttpValueProvider(serviceContext, reference, client, config, providerConfig);
    }


    @Override
    protected HttpOperationProvider createOperationProvider(Reference reference, HttpOperationProviderConfig providerConfig) {
        return new HttpOperationProvider(serviceContext, reference, client, config, providerConfig);
    }


    @Override
    protected HttpSubscriptionProvider createSubscriptionProvider(Reference reference, HttpSubscriptionProviderConfig providerConfig) {
        return new HttpSubscriptionProvider(serviceContext, reference, client, config, providerConfig);
    }


    @Override
    protected void doConnect() throws AssetConnectionException {
        HttpClient.Builder builder = HttpClient.newBuilder();
        if (StringUtils.isNotBlank(config.getUsername())) {
            builder = builder.authenticator(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(
                            config.getUsername(),
                            config.getPassword() != null
                                    ? config.getPassword().toCharArray()
                                    : new char[0]);
                }
            });
        }
        client = builder.build();
    }


    @Override
    protected void doDisconnect() throws AssetConnectionException {
        // no need to close a HTTP connection
    }

}
