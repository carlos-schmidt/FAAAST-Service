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
package de.fraunhofer.iosb.ilt.faaast.service.dataformat.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.ElementCollectionValueMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.PropertyValueMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.TypedValueMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.serializer.AnnotatedRelationshipElementValueSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.serializer.BlobValueSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.serializer.EntityValueSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.serializer.FileValueSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.serializer.ModifierAwareSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.serializer.MultiLanguagePropertyValueSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.serializer.ReferenceElementValueSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.serializer.RelationshipElementValueSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.serializer.SubmodelElementValueSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.serializer.SubmodelValueSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Extend;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Level;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.AnnotatedRelationshipElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.BlobValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementCollectionValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.EntityValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.FileValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.MultiLanguagePropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ReferenceElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.RelationshipElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.TypedValue;
import de.fraunhofer.iosb.ilt.faaast.service.util.ElementValueHelper;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElement;
import java.util.ArrayList;
import java.util.List;


public class ValueOnlyJsonSerializer {

    public static boolean isJreType(Class<?> type) {
        if (type.getClassLoader() == null || type.getClassLoader().getParent() == null) {
            return true;
        }
        String pkg = type.getPackage().getName();
        return pkg.startsWith("java.") || pkg.startsWith("com.sun") || pkg.startsWith("sun.");
    }

    private final SerializerWrapper wrapper;

    public ValueOnlyJsonSerializer() {
        this.wrapper = new SerializerWrapper(x -> modifyMapper(x));
    }


    public JsonMapper getMapper() {
        return wrapper.getMapper();
    }


    public String write(Object obj) throws SerializationException {
        return write(obj, Level.DEFAULT, Extend.DEFAULT);
    }


    public String write(Object obj, Level level) throws SerializationException {
        return write(obj, level, Extend.DEFAULT);
    }


    public String write(Object obj, Extend extend) throws SerializationException {
        return write(obj, Level.DEFAULT, extend);
    }

    //    private Object convertToValue(Object obj) {
    //        if (obj == null) {
    //            return null;
    //        }
    //        Class<?> type = obj.getClass();
    //        if (ElementValueHelper.isSerializableAsValue(type)) {
    //            return ElementValueMapper.toValue((SubmodelElement)obj);
    //        }
    //        if (type.isArray()) {
    //            return Stream.of((Object[]) obj)
    //                    .map(x -> convertToValue(x))
    //                    .toArray();
    //        }
    //        if (Collection.class.isAssignableFrom(type)) {
    //            return ((Collection) obj).stream()
    //                    .map(x -> convertToValue(x))
    //                    .collect(Collectors.toList());
    //        }
    //        if (Map.class.isAssignableFrom(type)) {
    //            return ((Map) obj).values().stream()
    //                    .collect(Collectors.toMap(
    //                            x -> x, 
    //                            x -> convertToValue(x) ));
    //        }
    //        return obj;
    //    }


    public String write(Object obj, Level level, Extend extend) throws SerializationException {
        if (!ElementValueHelper.isValueOnlySupported(obj)) {
            throw new SerializationException(
                    "Provided element is not supported by value-only serialization. Supported types are: all subtypes of DataElement, SubmodelElementCollection, ReferenceElement, RelationshipElement, AnnotatedRelationshipElement, and Entity as well as all subtypes of ElementValue");
        }
        try {
            return wrapper.getMapper().writer()
                    .withAttribute(ModifierAwareSerializer.LEVEL, level)
                    .withAttribute(ModifierAwareSerializer.EXTEND, extend)
                    .writeValueAsString(obj);
        }
        catch (JsonProcessingException ex) {
            throw new SerializationException("serialization failed", ex);
        }
    }


    protected JsonMapper modifyMapper(JsonMapper mapper) {
        mapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector());
        mapper.addMixIn(PropertyValue.class, PropertyValueMixin.class);
        mapper.addMixIn(ElementCollectionValue.class, ElementCollectionValueMixin.class);
        mapper.addMixIn(TypedValue.class, TypedValueMixin.class);
        SimpleModule module = new SimpleModule();
        module.addSerializer(MultiLanguagePropertyValue.class, new MultiLanguagePropertyValueSerializer());
        module.addSerializer(ReferenceElementValue.class, new ReferenceElementValueSerializer());
        module.addSerializer(FileValue.class, new FileValueSerializer());
        module.addSerializer(BlobValue.class, new BlobValueSerializer());
        module.addSerializer(RelationshipElementValue.class, new RelationshipElementValueSerializer());
        module.addSerializer(AnnotatedRelationshipElementValue.class, new AnnotatedRelationshipElementValueSerializer());
        module.addSerializer(EntityValue.class, new EntityValueSerializer());
        module.addSerializer(SubmodelElement.class, new SubmodelElementValueSerializer());
        module.addSerializer(Submodel.class, new SubmodelValueSerializer());
        ObjectMapper result = mapper.registerModule(module);
        result = result.registerModule(new SimpleModule() {
            @Override
            public void setupModule(SetupContext context) {
                super.setupModule(context);
                context.addBeanSerializerModifier(new BeanSerializerModifier() {
                    @Override
                    public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                                                                     BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
                        beanProperties.removeIf(property -> {
                            JavaType type = property.getType();
                            List<JavaType> usedTypes = new ArrayList<>();
                            if (type.isContainerType()) {
                                if (type.getContentType() != null) {
                                    usedTypes.add(type.getContentType());
                                }
                                if (type.getKeyType() != null) {
                                    usedTypes.add(type.getKeyType());
                                }
                                if (type.getBindings() != null) {
                                    usedTypes.addAll(type.getBindings().getTypeParameters());
                                }
                            }
                            return !usedTypes.stream().allMatch(x -> isJreType(x.getRawClass()) || ElementValueHelper.isValueOnlySupported(x.getRawClass()));
                        });
                        return beanProperties;
                    }
                });
            }
        });
        return (JsonMapper) result;
    }

}