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
package de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.access;

import io.adminshell.aas.v3.model.Referable;
import java.util.Objects;


public class ElementReadEventMessage extends ReadEventMessage {

    private Referable value;

    public Referable getValue() {
        return value;
    }


    public void setValue(Referable value) {
        this.value = value;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        ElementReadEventMessage that = (ElementReadEventMessage) o;
        return Objects.equals(value, that.value);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value);
    }
}