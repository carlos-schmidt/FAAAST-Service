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
package de.fraunhofer.iosb.ilt.faaast.service.model.request;

import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.PutSubmodelElementByPathResponse;
import io.adminshell.aas.v3.model.Key;
import io.adminshell.aas.v3.model.SubmodelElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Request class for PutSubmodelElementByPath requests.
 */
public class PutSubmodelElementByPathRequest extends AbstractSubmodelInterfaceRequest<PutSubmodelElementByPathResponse> {

    private List<Key> path;
    private SubmodelElement submodelElement;

    public PutSubmodelElementByPathRequest() {
        this.path = new ArrayList<>();
    }


    public List<Key> getPath() {
        return path;
    }


    public void setPath(List<Key> path) {
        this.path = path;
    }


    public SubmodelElement getSubmodelElement() {
        return submodelElement;
    }


    public void setSubmodelElement(SubmodelElement submodelElement) {
        this.submodelElement = submodelElement;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PutSubmodelElementByPathRequest that = (PutSubmodelElementByPathRequest) o;
        return super.equals(that)
                && Objects.equals(path, that.path)
                && Objects.equals(submodelElement, that.submodelElement);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), path, submodelElement);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends PutSubmodelElementByPathRequest, B extends AbstractBuilder<T, B>>
            extends AbstractSubmodelInterfaceRequest.AbstractBuilder<T, B> {

        public B path(List<Key> value) {
            getBuildingInstance().setPath(value);
            return getSelf();
        }


        public B submodelElement(SubmodelElement value) {
            getBuildingInstance().setSubmodelElement(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<PutSubmodelElementByPathRequest, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected PutSubmodelElementByPathRequest newBuildingInstance() {
            return new PutSubmodelElementByPathRequest();
        }
    }
}
