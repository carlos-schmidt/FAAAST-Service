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
package opc.i4aas.server;

import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.TypeDefinitionId;
import com.prosysopc.ua.nodes.Optional;
import com.prosysopc.ua.nodes.UaProperty;
import com.prosysopc.ua.nodes.UaVariable;
import com.prosysopc.ua.server.GeneratedNodeInitializer;
import com.prosysopc.ua.server.NodeManagerUaNode;
import com.prosysopc.ua.server.ServiceContext;
import com.prosysopc.ua.stack.builtintypes.DiagnosticInfo;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import com.prosysopc.ua.stack.builtintypes.StatusCode;
import com.prosysopc.ua.stack.builtintypes.Variant;
import opc.i4aas.AASSubmodelElementCollectionType;


/**
 * Generated on 2021-12-15 11:39:02
 */
@TypeDefinitionId("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=1010")
public abstract class AASSubmodelElementCollectionTypeNodeBase extends AASSubmodelElementTypeNode implements AASSubmodelElementCollectionType {
    private static GeneratedNodeInitializer<AASSubmodelElementCollectionTypeNode> aASSubmodelElementCollectionTypeNodeInitializer;

    protected AASSubmodelElementCollectionTypeNodeBase(NodeManagerUaNode nodeManager, NodeId nodeId,
            QualifiedName browseName, LocalizedText displayName) {
        super(nodeManager, nodeId, browseName, displayName);
    }


    @Override
    public void afterCreate() {
        super.afterCreate();

        // Call afterCreate for each sub-node (if the node has any)
        GeneratedNodeInitializer<AASSubmodelElementCollectionTypeNode> impl = getAASSubmodelElementCollectionTypeNodeInitializer();
        if (impl != null) {
            impl.init((AASSubmodelElementCollectionTypeNode) this);
        }
    }


    public static GeneratedNodeInitializer<AASSubmodelElementCollectionTypeNode> getAASSubmodelElementCollectionTypeNodeInitializer() {
        return aASSubmodelElementCollectionTypeNodeInitializer;
    }


    public static void setAASSubmodelElementCollectionTypeNodeInitializer(GeneratedNodeInitializer<AASSubmodelElementCollectionTypeNode> aASSubmodelElementCollectionTypeNodeInitializerNewValue) {
        aASSubmodelElementCollectionTypeNodeInitializer = aASSubmodelElementCollectionTypeNodeInitializerNewValue;
    }


    @Optional
    @Override
    public UaProperty getAllowDuplicatesNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "AllowDuplicates");
        return getProperty(browseName);
    }


    @Optional
    @Override
    public Boolean isAllowDuplicates() {
        UaVariable node = getAllowDuplicatesNode();
        if (node == null) {
            return null;
        }
        Object value = node.getValue().getValue().getValue();
        return (Boolean) value;
    }


    @Optional
    @Override
    public void setAllowDuplicates(Boolean value) {
        UaVariable node = getAllowDuplicatesNode();
        if (node == null) {
            throw new RuntimeException("Setting AllowDuplicates failed: does not exist (Optional Nodes must be configured in NodeBuilder)");
        }
        try {
            node.setValue(value);
        }
        catch (StatusException e) {
            throw new RuntimeException("Setting AllowDuplicates failed unexpectedly", e);
        }
    }


    @Override
    public Variant[] callMethod(ServiceContext serviceContext, NodeId methodId,
                                Variant[] inputArguments, StatusCode[] inputArgumentResults,
                                DiagnosticInfo[] inputArgumentDiagnosticInfos)
            throws StatusException {
        return super.callMethod(serviceContext, methodId, inputArguments, inputArgumentResults, inputArgumentDiagnosticInfos);
    }
}