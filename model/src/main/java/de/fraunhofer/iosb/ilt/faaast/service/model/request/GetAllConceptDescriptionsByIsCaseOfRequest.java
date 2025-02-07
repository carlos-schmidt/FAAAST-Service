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

import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetAllConceptDescriptionsByIsCaseOfResponse;
import io.adminshell.aas.v3.model.Reference;
import java.util.Objects;


/**
 * Request class for GetAllConceptDescriptionsByIsCaseOf requests.
 */
public class GetAllConceptDescriptionsByIsCaseOfRequest extends AbstractRequestWithModifier<GetAllConceptDescriptionsByIsCaseOfResponse> {

    private Reference isCaseOf;

    public Reference getIsCaseOf() {
        return isCaseOf;
    }


    public void setIsCaseOf(Reference isCaseOf) {
        this.isCaseOf = isCaseOf;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GetAllConceptDescriptionsByIsCaseOfRequest that = (GetAllConceptDescriptionsByIsCaseOfRequest) o;
        return super.equals(that)
                && Objects.equals(isCaseOf, that.isCaseOf);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), isCaseOf);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends GetAllConceptDescriptionsByIsCaseOfRequest, B extends AbstractBuilder<T, B>>
            extends AbstractRequestWithModifier.AbstractBuilder<T, B> {

        public B isCaseOf(Reference value) {
            getBuildingInstance().setIsCaseOf(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<GetAllConceptDescriptionsByIsCaseOfRequest, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected GetAllConceptDescriptionsByIsCaseOfRequest newBuildingInstance() {
            return new GetAllConceptDescriptionsByIsCaseOfRequest();
        }
    }
}
