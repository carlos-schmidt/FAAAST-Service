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

import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.SubmodelElementCollectionValue;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import io.adminshell.aas.v3.model.Referable;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * Converts between {@link io.adminshell.aas.v3.model.SubmodelElementCollection} and
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.value.SubmodelElementCollectionValue}.
 */
public class SubmodelElementCollectionValueMapper implements DataValueMapper<SubmodelElementCollection, SubmodelElementCollectionValue> {

    @Override
    public SubmodelElementCollectionValue toValue(SubmodelElementCollection submodelElement) throws ValueMappingException {
        if (submodelElement == null) {
            return null;
        }
        SubmodelElementCollectionValue value = SubmodelElementCollectionValue.builder().build();
        if (submodelElement.getValues() != null && submodelElement.getValues().stream().noneMatch(Objects::isNull)) {
            value.setValues(submodelElement.getValues().stream().collect(Collectors.toMap(
                    Referable::getIdShort,
                    LambdaExceptionHelper.rethrowFunction(ElementValueMapper::toValue))));
        }
        else {
            value.setValues(null);
        }
        return value;
    }


    @Override
    public SubmodelElementCollection setValue(SubmodelElementCollection submodelElement, SubmodelElementCollectionValue value) {
        DataValueMapper.super.setValue(submodelElement, value);
        if (submodelElement.getValues() != null) {
            for (SubmodelElement element: submodelElement.getValues()) {
                if (element != null && value.getValues() != null && value.getValues().containsKey(element.getIdShort())) {
                    ElementValueMapper.setValue(element, value.getValues().get(element.getIdShort()));
                }
            }
        }
        return submodelElement;
    }
}
