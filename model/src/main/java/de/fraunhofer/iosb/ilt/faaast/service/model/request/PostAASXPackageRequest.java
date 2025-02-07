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

import de.fraunhofer.iosb.ilt.faaast.service.model.aasx.AASXPackageBase;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.PostAASXPackageResponse;
import io.adminshell.aas.v3.model.Identifier;
import io.adminshell.aas.v3.model.builder.ExtendableBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Request class for PostAASXPackage requests.
 */
public class PostAASXPackageRequest implements Request<PostAASXPackageResponse> {

    private List<Identifier> aasIds;
    private AASXPackageBase file;
    private String filename;

    public PostAASXPackageRequest() {
        this.aasIds = new ArrayList<>();
    }


    public List<Identifier> getAasIds() {
        return aasIds;
    }


    public void setAasIds(List<Identifier> aasIds) {
        this.aasIds = aasIds;
    }


    public AASXPackageBase getFile() {
        return file;
    }


    public void setFile(AASXPackageBase file) {
        this.file = file;
    }


    public String getFilename() {
        return filename;
    }


    public void setFilename(String filename) {
        this.filename = filename;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PostAASXPackageRequest that = (PostAASXPackageRequest) o;
        return Objects.equals(aasIds, that.aasIds)
                && Objects.equals(file, that.file)
                && Objects.equals(filename, that.filename);
    }


    @Override
    public int hashCode() {
        return Objects.hash(aasIds, file, filename);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends PostAASXPackageRequest, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B aasId(Identifier value) {
            getBuildingInstance().getAasIds().add(value);
            return getSelf();
        }


        public B aasIds(List<Identifier> value) {
            getBuildingInstance().setAasIds(value);
            return getSelf();
        }


        public B file(AASXPackageBase value) {
            getBuildingInstance().setFile(value);
            return getSelf();
        }


        public B filename(String value) {
            getBuildingInstance().setFilename(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<PostAASXPackageRequest, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected PostAASXPackageRequest newBuildingInstance() {
            return new PostAASXPackageRequest();
        }
    }
}
