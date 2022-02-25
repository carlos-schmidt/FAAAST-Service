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
package de.fraunhofer.iosb.ilt.faaast.service.model.messagebus;

import java.util.Objects;
import java.util.UUID;


/**
 * Identifier of a subscription.
 */
public class SubscriptionId {

    private final UUID subscriptionId;

    public SubscriptionId() {
        this.subscriptionId = UUID.randomUUID();
    }


    public UUID getSubscriptionId() {
        return subscriptionId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SubscriptionId that = (SubscriptionId) o;
        return Objects.equals(subscriptionId, that.subscriptionId);
    }


    @Override
    public int hashCode() {
        return Objects.hash(subscriptionId);
    }
}
