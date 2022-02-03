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
package de.fraunhofer.iosb.ilt.faaast.service.model.v3.api;

import io.adminshell.aas.v3.model.builder.ExtendableBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class Result {
    private boolean success;
    private List<Message> message = new ArrayList<>();

    public boolean getSuccess() {
        return success;
    }


    public void setSuccess(boolean success) {
        this.success = success;
    }


    public List<Message> getMessage() {
        return message;
    }


    public void setMessage(List<Message> message) {
        this.message = message;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Result result = (Result) o;
        return Objects.equals(success, result.success)
                && Objects.equals(message, result.message);
    }


    @Override
    public int hashCode() {
        return Objects.hash(success, message);
    }


    public static Builder builder() {
        return new Builder();
    }

    private static abstract class AbstractBuilder<T extends Result, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B success(boolean value) {
            getBuildingInstance().setSuccess(value);
            return getSelf();
        }


        public B messages(List<Message> value) {
            getBuildingInstance().setMessage(value);
            return getSelf();
        }


        public B message(Message value) {
            getBuildingInstance().getMessage().add(value);
            return getSelf();
        }

    }

    public static class Builder extends AbstractBuilder<Result, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected Result newBuildingInstance() {
            return new Result();
        }
    }
}
