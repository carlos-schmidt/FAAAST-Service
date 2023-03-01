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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.provider.OpcUaOperationProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.provider.OpcUaSubscriptionProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.provider.OpcUaValueProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.provider.config.OpcUaOperationProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.provider.config.OpcUaSubscriptionProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.provider.config.OpcUaValueProviderConfig;
import java.util.Objects;


/**
 * Config file for {@link OpcUaAssetConnection}.
 */
public class OpcUaAssetConnectionConfig
        extends AssetConnectionConfig<OpcUaAssetConnection, OpcUaValueProviderConfig, OpcUaOperationProviderConfig, OpcUaSubscriptionProviderConfig> {

    public static final int DEFAULT_REQUEST_TIMEOUT = 3000;
    public static final int DEFAULT_ACKNOWLEDGE_TIMEOUT = 10000;
    public static final int DEFAULT_RETRIES = 1;

    private String host;
    private String username;
    private String password;
    private int requestTimeout;
    private int acknowledgeTimeout;
    private int retries;

    public OpcUaAssetConnectionConfig() {
        this.requestTimeout = DEFAULT_REQUEST_TIMEOUT;
        this.acknowledgeTimeout = DEFAULT_ACKNOWLEDGE_TIMEOUT;
        this.retries = DEFAULT_RETRIES;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OpcUaAssetConnectionConfig that = (OpcUaAssetConnectionConfig) o;
        return super.equals(that)
                && Objects.equals(host, that.host)
                && Objects.equals(username, that.username)
                && Objects.equals(password, that.password)
                && Objects.equals(requestTimeout, that.requestTimeout)
                && Objects.equals(acknowledgeTimeout, that.acknowledgeTimeout)
                && Objects.equals(retries, that.retries);
    }


    public String getHost() {
        return host;
    }


    public void setHost(String host) {
        this.host = host;
    }


    public String getUsername() {
        return username;
    }


    public void setUsername(String username) {
        this.username = username;
    }


    public String getPassword() {
        return password;
    }


    public void setPassword(String password) {
        this.password = password;
    }


    public int getRequestTimeout() {
        return requestTimeout;
    }


    public void setRequestTimeout(int requestTimeout) {
        this.requestTimeout = requestTimeout;
    }


    public int getAcknowledgeTimeout() {
        return acknowledgeTimeout;
    }


    public void setAcknowledgeTimeout(int acknowledgeTimeout) {
        this.acknowledgeTimeout = acknowledgeTimeout;
    }


    public int getRetries() {
        return retries;
    }


    public void setRetries(int retries) {
        this.retries = retries;
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), host, username, password, requestTimeout, acknowledgeTimeout, retries);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends OpcUaAssetConnectionConfig, B extends AbstractBuilder<T, B>>
            extends
            AssetConnectionConfig.AbstractBuilder<OpcUaAssetConnectionConfig, OpcUaValueProviderConfig, OpcUaValueProvider, OpcUaOperationProviderConfig, OpcUaOperationProvider, OpcUaSubscriptionProviderConfig, OpcUaSubscriptionProvider, OpcUaAssetConnection, B> {

        public B host(String value) {
            getBuildingInstance().setHost(value);
            return getSelf();
        }


        public B username(String value) {
            getBuildingInstance().setUsername(value);
            return getSelf();
        }


        public B password(String value) {
            getBuildingInstance().setPassword(value);
            return getSelf();
        }


        public B requestTimeout(int value) {
            getBuildingInstance().setRequestTimeout(value);
            return getSelf();
        }


        public B acknowledgeTimeout(int value) {
            getBuildingInstance().setAcknowledgeTimeout(value);
            return getSelf();
        }


        public B retries(int value) {
            getBuildingInstance().setRetries(value);
            return getSelf();
        }

    }

    public static class Builder extends AbstractBuilder<OpcUaAssetConnectionConfig, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected OpcUaAssetConnectionConfig newBuildingInstance() {
            return new OpcUaAssetConnectionConfig();
        }
    }

}
