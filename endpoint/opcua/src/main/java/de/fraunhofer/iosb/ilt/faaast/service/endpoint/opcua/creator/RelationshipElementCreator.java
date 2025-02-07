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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.creator;

import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.UaQualifiedName;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import com.prosysopc.ua.stack.core.Identifiers;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.AasServiceNodeManager;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.ObjectData;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.SubmodelElementData;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper.AasSubmodelElementHelper;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.AnnotatedRelationshipElement;
import io.adminshell.aas.v3.model.DataElement;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.RelationshipElement;
import io.adminshell.aas.v3.model.Submodel;
import opc.i4aas.AASAnnotatedRelationshipElementType;
import opc.i4aas.AASRelationshipElementType;


/**
 * Helper class to create RelationshipElements and integrate them into the
 * OPC UA address space.
 */
public class RelationshipElementCreator extends SubmodelElementCreator {

    /**
     * Adds an AAS Relationship Element to the given node.
     *
     * @param node The desired UA node
     * @param aasRelElem The corresponding AAS Relationship Element
     * @param submodel The corresponding Submodel as parent object of the data element
     * @param parentRef The AAS reference to the parent object
     * @param ordered Specifies whether the entity should be added ordered
     *            (true) or unordered (false)
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     */
    public static void addAasRelationshipElement(UaNode node, RelationshipElement aasRelElem, Submodel submodel, Reference parentRef, boolean ordered,
                                                 AasServiceNodeManager nodeManager)
            throws StatusException {
        if ((node != null) && (aasRelElem != null)) {
            Reference relElemRef = AasUtils.toReference(parentRef, aasRelElem);

            String name = aasRelElem.getIdShort();
            AASRelationshipElementType relElemNode;
            QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASRelationshipElementType.getNamespaceUri(), name)
                    .toQualifiedName(nodeManager.getNamespaceTable());
            NodeId nid = nodeManager.getDefaultNodeId();
            if (aasRelElem instanceof AnnotatedRelationshipElement) {
                relElemNode = createAnnotatedRelationshipElement((AnnotatedRelationshipElement) aasRelElem, submodel, relElemRef, nid, nodeManager);
            }
            else {
                relElemNode = nodeManager.createInstance(AASRelationshipElementType.class, nid, browseName, LocalizedText.english(name));
            }

            if (relElemNode != null) {
                addSubmodelElementBaseData(relElemNode, aasRelElem, nodeManager);

                AasSubmodelElementHelper.setAasReferenceData(aasRelElem.getFirst(), relElemNode.getFirstNode(), false);
                AasSubmodelElementHelper.setAasReferenceData(aasRelElem.getSecond(), relElemNode.getSecondNode(), false);

                nodeManager.addSubmodelElementAasMap(relElemNode.getFirstNode().getKeysNode().getNodeId(),
                        new SubmodelElementData(aasRelElem, submodel, SubmodelElementData.Type.RELATIONSHIP_ELEMENT_FIRST, relElemRef));
                nodeManager.addSubmodelElementAasMap(relElemNode.getSecondNode().getKeysNode().getNodeId(),
                        new SubmodelElementData(aasRelElem, submodel, SubmodelElementData.Type.RELATIONSHIP_ELEMENT_SECOND, relElemRef));

                nodeManager.addSubmodelElementOpcUA(relElemRef, relElemNode);

                if (ordered) {
                    node.addReference(relElemNode, Identifiers.HasOrderedComponent, false);
                }
                else {
                    node.addComponent(relElemNode);
                }

                nodeManager.addReferable(relElemRef, new ObjectData(aasRelElem, relElemNode, submodel));
            }
        }
    }


    /**
     * Creates an Annotated Relationship Element.
     *
     * @param aasRelElem The AAS Annotated Relationship Element
     * @param submodel The corresponding Submodel as parent object of the data element
     * @param relElemRef The AAS reference to the AnnotatedRelationshipElement
     * @param nodeId The desired NodeId for the node to be created
     * @param nodeManager The corresponding Node Manager
     * @return The create UA Annotated Relationship Element
     * @throws StatusException If the operation fails
     */
    private static AASRelationshipElementType createAnnotatedRelationshipElement(AnnotatedRelationshipElement aasRelElem, Submodel submodel, Reference relElemRef, NodeId nodeId,
                                                                                 AasServiceNodeManager nodeManager)
            throws StatusException {
        AASRelationshipElementType retval = null;

        AASAnnotatedRelationshipElementType relElemNode = nodeManager.createInstance(
                AASAnnotatedRelationshipElementType.class, nodeId, UaQualifiedName
                        .from(opc.i4aas.ObjectTypeIds.AASAnnotatedRelationshipElementType.getNamespaceUri(), aasRelElem.getIdShort())
                        .toQualifiedName(nodeManager.getNamespaceTable()),
                LocalizedText.english(aasRelElem.getIdShort()));

        // Annotations 
        for (DataElement de: aasRelElem.getAnnotations()) {
            DataElementCreator.addAasDataElement(relElemNode.getAnnotationNode(), de, submodel, relElemRef, false, nodeManager);
        }

        retval = relElemNode;

        return retval;
    }

}
