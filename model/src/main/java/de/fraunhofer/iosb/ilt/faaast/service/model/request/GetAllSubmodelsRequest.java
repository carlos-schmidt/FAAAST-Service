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

import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetAllSubmodelsResponse;
import java.util.Objects;


/**
 * Request class for GetAllSubmodels requests.
 */
public class GetAllSubmodelsRequest extends AbstractRequestWithModifier<GetAllSubmodelsResponse> {

    public GetAllSubmodelsRequest() {
        super(OutputModifierConstraints.SUBMODEL);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GetAllSubmodelsRequest that = (GetAllSubmodelsRequest) o;
        return super.equals(that);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends GetAllSubmodelsRequest, B extends AbstractBuilder<T, B>> extends AbstractRequestWithModifier.AbstractBuilder<T, B> {}

    public static class Builder extends AbstractBuilder<GetAllSubmodelsRequest, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected GetAllSubmodelsRequest newBuildingInstance() {
            return new GetAllSubmodelsRequest();
        }
    }
}
