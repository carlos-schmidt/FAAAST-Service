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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.DummyInternalSegmentProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.DummyLinkedSegmentProviderConfig;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;


public class TimeSeriesSubmodelTemplateProcessorConfigTest {

    private static ObjectMapper mapper;

    @BeforeClass
    public static void init() {
        mapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }


    @Test
    @Ignore
    public void foo() {
        TimeSeriesSubmodelTemplateProcessorConfig config = TimeSeriesSubmodelTemplateProcessorConfig.builder()
                .internalSegmentProvider(new DummyInternalSegmentProviderConfig())
                .linkedSegmentProvider(new DummyLinkedSegmentProviderConfig())
                .build();
    }

}
