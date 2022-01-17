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
package de.fraunhofer.iosb.ilt.faaast.service;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnection;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionManager;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.ServiceConfig;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.Endpoint;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.EndpointConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.Response;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.Referable;
import io.adminshell.aas.v3.model.Reference;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Service implements ServiceContext {

    private static Logger logger = LoggerFactory.getLogger(Service.class);
    private AssetAdministrationShellEnvironment aasEnvironment;
    private AssetConnectionManager assetConnectionManager;
    private ServiceConfig config;
    private List<Endpoint> endpoints;
    private MessageBus messageBus;
    private Persistence persistence;
    private RequestHandlerManager requestHandler;

    public Service(CoreConfig coreConfig, AssetAdministrationShellEnvironment aasEnvironment, Persistence persistence, MessageBus messageBus, List<Endpoint> endpoints) {
        if (coreConfig == null) {
            throw new IllegalArgumentException("coreConfig must be non-null");
        }
        if (aasEnvironment == null) {
            throw new IllegalArgumentException("aasEnvironment must be non-null");
        }
        if (persistence == null) {
            throw new IllegalArgumentException("persistence must be non-null");
        }
        if (messageBus == null) {
            throw new IllegalArgumentException("messageBus must be non-null");
        }
        if (endpoints == null) {
            throw new IllegalArgumentException("endpoints must be non-null");
        }
        this.aasEnvironment = aasEnvironment;
        this.config = ServiceConfig.builder()
                .core(coreConfig)
                .build();
        this.persistence = persistence;
        this.messageBus = messageBus;
        this.endpoints = endpoints;
        this.requestHandler = new RequestHandlerManager(this.config.getCore(), this.persistence, this.messageBus, this.assetConnectionManager);
    }


    public Service(ServiceConfig config) throws ConfigurationException {
        if (config == null) {
            throw new IllegalArgumentException("config must be non-null");
        }
        this.config = config;
        init();
    }


    public Response execute(Request request) {
        if (request == null) {
            throw new IllegalArgumentException("request must be non-null");
        }
        return this.requestHandler.execute(request);
    }


    @Override
    public Class<? extends Referable> getElementType(Reference reference) {
        return AasUtils.resolve(reference, aasEnvironment).getClass();
    }


    public AssetAdministrationShellEnvironment getEnvironment() {
        return aasEnvironment;
    }


    public void executeAsync(Request request, Consumer<Response> callback) {
        if (request == null) {
            throw new IllegalArgumentException("request must be non-null");
        }
        if (callback == null) {
            throw new IllegalArgumentException("callback must be non-null");
        }
        callback.accept(null);
        this.requestHandler.executeAsync(request, callback);
    }


    public void setAASEnvironment(AssetAdministrationShellEnvironment aasEnvironment) {
        this.aasEnvironment = aasEnvironment;
    }


    public MessageBus getMessageBus() {
        return messageBus;
    }


    public void start() throws Exception {
        //TODO set AASEnvironment properly
        if (this.aasEnvironment == null) {
            throw new IllegalArgumentException("AssetAdministrationEnvironment must be non-null");
        }
        persistence.setEnvironment(this.aasEnvironment);
        messageBus.start();
        for (Endpoint endpoint: endpoints) {
            endpoint.start();
        }
    }


    public void stop() {
        messageBus.stop();
        endpoints.forEach(Endpoint::stop);
    }


    private void init() throws ConfigurationException {
        if (config.getPersistence() == null) {
            throw new IllegalArgumentException("config.persistence must be non-null");
        }
        persistence = (Persistence) config.getPersistence().newInstance(config.getCore(), this);
        if (config.getMessageBus() == null) {
            throw new IllegalArgumentException("config.messagebus must be non-null");
        }
        messageBus = (MessageBus) config.getMessageBus().newInstance(config.getCore(), this);
        if (config.getAssetConnections() != null) {
            List<AssetConnection> assetConnections = new ArrayList<>();
            for (AssetConnectionConfig assetConnectionConfig: config.getAssetConnections()) {
                assetConnections.add((AssetConnection) assetConnectionConfig.newInstance(config.getCore(), this));
            }

            assetConnectionManager = new AssetConnectionManager(config.getCore(), assetConnections, this);
        }
        if (config.getEndpoints() == null || config.getEndpoints().isEmpty()) {
            // TODO maybe be less restrictive and only print warning
            //throw new InvalidConfigurationException("at least endpoint must be defined in the configuration");
            logger.warn("no endpoint configuration found, starting service without endpoint which means the service will not be accessible via any kind of API");
        }
        else {
            endpoints = new ArrayList<>();
            for (EndpointConfig endpointConfig: config.getEndpoints()) {
                Endpoint endpoint = (Endpoint) endpointConfig.newInstance(config.getCore(), this);
                endpoint.setService(this);
                endpoints.add(endpoint);
            }
        }
        this.requestHandler = new RequestHandlerManager(this.config.getCore(), this.persistence, this.messageBus, this.assetConnectionManager);
    }
}