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
package de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata;

import java.util.Arrays;
import java.util.Objects;


public class BlobValue extends DataElementValue {
    private String mimeType;
    private byte[] value;

    public String getMimeType() {
        return mimeType;
    }


    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }


    public byte[] getValue() {
        return value;
    }


    public void setValue(byte[] value) {
        this.value = value;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        BlobValue blobValue = (BlobValue) o;
        return Objects.equals(mimeType, blobValue.mimeType) && Arrays.equals(value, blobValue.value);
    }


    @Override
    public int hashCode() {
        int result = Objects.hash(mimeType);
        result = 31 * result + Arrays.hashCode(value);
        return result;
    }
}