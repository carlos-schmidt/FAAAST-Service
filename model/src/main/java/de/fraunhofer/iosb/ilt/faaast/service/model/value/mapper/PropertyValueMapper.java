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
package de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper;

import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.TypedValueFactory;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.ValueFormatException;
import io.adminshell.aas.v3.model.Property;


public class PropertyValueMapper extends DataValueMapper<Property, PropertyValue> {

    @Override
    public PropertyValue toValue(Property submodelElement) {
        if (submodelElement == null) {
            return null;
        }
        PropertyValue propertyValue = new PropertyValue();
        try {
            propertyValue.setValue(TypedValueFactory.create(submodelElement.getValueType(), submodelElement.getValue()));
        }
        catch (ValueFormatException ex) {
            // TODO properly throw?
            throw new RuntimeException("invalid data value");
        }
        return propertyValue;
    }


    @Override
    public Property setValue(Property submodelElement, PropertyValue value) {
        if (submodelElement == null || value == null) {
            return null;
        }
        submodelElement.setValueType(value.getValue().getDataType().getName());
        submodelElement.setValue(value.getValue().asString());
        return submodelElement;
    }

}