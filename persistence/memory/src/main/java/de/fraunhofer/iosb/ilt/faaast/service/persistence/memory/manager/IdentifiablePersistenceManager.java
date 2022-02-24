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
package de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.manager;

import de.fraunhofer.iosb.ilt.faaast.service.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.AssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.GlobalAssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.SpecificAssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.util.EnvironmentHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.Asset;
import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.ConceptDescription;
import io.adminshell.aas.v3.model.Identifiable;
import io.adminshell.aas.v3.model.Identifier;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.Submodel;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;


/**
 * Class to handle identifiable elements
 */
public class IdentifiablePersistenceManager extends PersistenceManager {

    public <T extends Identifiable> T getIdentifiableById(Identifier id) throws ResourceNotFoundException {
        if (id == null || this.aasEnvironment == null) {
            return null;
        }
        Identifiable identifiable = EnvironmentHelper.findIdentifiableInListsById(id,
                this.aasEnvironment.getAssetAdministrationShells(),
                this.aasEnvironment.getSubmodels(),
                this.aasEnvironment.getConceptDescriptions(),
                this.aasEnvironment.getAssets());

        if (identifiable == null) {
            throw new ResourceNotFoundException("Resource not found with ID " + id.getIdentifier());
        }

        return (T) identifiable;
    }


    public List<AssetAdministrationShell> getAASs(String idShort, List<AssetIdentification> assetIds) {
        if (this.aasEnvironment == null) {
            return null;
        }
        if (StringUtils.isNoneBlank(idShort)) {
            return EnvironmentHelper.getDeepCopiedShells(x -> x.getIdShort().equalsIgnoreCase(idShort), this.aasEnvironment);
        }

        if (assetIds != null) {
            List<AssetAdministrationShell> shells = new ArrayList<>();
            for (AssetIdentification assetId: assetIds) {
                if (GlobalAssetIdentification.class.isAssignableFrom(assetId.getClass())) {
                    shells.addAll(EnvironmentHelper.getDeepCopiedShells(
                            x -> x.getAssetInformation() != null
                                    && x.getAssetInformation().getGlobalAssetId() != null
                                    && x.getAssetInformation().getGlobalAssetId().equals(((GlobalAssetIdentification) assetId).getReference()),
                            this.aasEnvironment));
                }

                if (SpecificAssetIdentification.class.isAssignableFrom(assetId.getClass())) {
                    shells.addAll(EnvironmentHelper.getDeepCopiedShells(
                            x -> x.getAssetInformation() != null
                                    && x.getAssetInformation().getSpecificAssetIds().stream()
                                            .anyMatch(y -> y.getKey().equalsIgnoreCase(((SpecificAssetIdentification) assetId).getKey())
                                                    && y.getValue().equalsIgnoreCase(((SpecificAssetIdentification) assetId).getValue())),
                            this.aasEnvironment));
                }
            }
            return shells;
        }

        //return all
        List<AssetAdministrationShell> shells = EnvironmentHelper.getDeepCopiedShells(x -> true, this.aasEnvironment);
        return shells;
    }


    public List<Submodel> getSubmodels(String idShort, Reference semanticId) {
        if (this.aasEnvironment == null) {
            return null;
        }

        if (StringUtils.isNoneBlank(idShort)) {
            List<Submodel> submodels = EnvironmentHelper.getDeepCopiedSubmodels(x -> x.getIdShort().equalsIgnoreCase(idShort), this.aasEnvironment);
            return submodels;
        }

        if (semanticId != null) {
            List<Submodel> submodels = EnvironmentHelper.getDeepCopiedSubmodels(x -> x.getSemanticId() != null
                    && ReferenceHelper.isEqualsIgnoringKeyType(x.getSemanticId(), semanticId), this.aasEnvironment);
            return submodels;
        }

        //return all
        List<Submodel> submodels = EnvironmentHelper.getDeepCopiedSubmodels(x -> true, this.aasEnvironment);
        return submodels;
    }


    public List<ConceptDescription> getConceptDescriptions(String idShort, Reference isCaseOf, Reference dataSpecification) {
        if (this.aasEnvironment == null) {
            return null;
        }

        List<ConceptDescription> conceptDescriptions = null;

        if (StringUtils.isNoneBlank(idShort)) {
            conceptDescriptions = this.aasEnvironment.getConceptDescriptions().stream()
                    .filter(x -> x.getIdShort().equalsIgnoreCase(idShort))
                    .collect(Collectors.toList());
        }

        if (isCaseOf != null) {
            Predicate<ConceptDescription> filter = x -> x.getIsCaseOfs().stream().anyMatch(y -> ReferenceHelper.isEqualsIgnoringKeyType(y, isCaseOf));
            if (conceptDescriptions == null) {
                conceptDescriptions = this.aasEnvironment.getConceptDescriptions().stream()
                        .filter(filter)
                        .collect(Collectors.toList());
            }
            else {
                conceptDescriptions = conceptDescriptions.stream()
                        .filter(filter)
                        .collect(Collectors.toList());
            }
        }

        if (dataSpecification != null) {
            Predicate<ConceptDescription> filter = x -> x.getEmbeddedDataSpecifications() != null
                    && x.getEmbeddedDataSpecifications().stream()
                            .anyMatch(y -> y.getDataSpecification() != null && ReferenceHelper.isEqualsIgnoringKeyType(y.getDataSpecification(), dataSpecification));
            if (conceptDescriptions == null) {
                conceptDescriptions = this.aasEnvironment.getConceptDescriptions().stream()
                        .filter(filter)
                        .collect(Collectors.toList());
            }
            else {
                conceptDescriptions = conceptDescriptions.stream().filter(filter).collect(Collectors.toList());
            }
        }

        if (StringUtils.isBlank(idShort) && isCaseOf == null && dataSpecification == null) {
            conceptDescriptions = this.aasEnvironment.getConceptDescriptions();
        }

        Class conceptDescriptionClass = conceptDescriptions != null && conceptDescriptions.size() > 0 ? conceptDescriptions.get(0).getClass() : ConceptDescription.class;
        return DeepCopyHelper.deepCopy(conceptDescriptions, conceptDescriptionClass);
    }


    public void remove(Identifier id) throws ResourceNotFoundException {
        if (id == null || this.aasEnvironment == null) {
            return;
        }

        Predicate<Identifiable> removeFilter = x -> !x.getIdentification().getIdentifier().equalsIgnoreCase(id.getIdentifier());

        Identifiable identifiable = getIdentifiableById(id);
        if (identifiable == null) {
            return;
        }

        //TODO: use reflection?
        if (AssetAdministrationShell.class.isAssignableFrom(identifiable.getClass())) {
            List<AssetAdministrationShell> newAASList;
            newAASList = this.aasEnvironment.getAssetAdministrationShells().stream().filter(removeFilter).collect(Collectors.toList());
            this.aasEnvironment.setAssetAdministrationShells(newAASList);
        }
        else if (Submodel.class.isAssignableFrom(identifiable.getClass())) {
            List<Submodel> newSubmodelList;
            newSubmodelList = this.aasEnvironment.getSubmodels().stream().filter(removeFilter).collect(Collectors.toList());
            this.aasEnvironment.setSubmodels(newSubmodelList);
            Reference referenceOfIdentifiable = AasUtils.toReference(identifiable);
            this.aasEnvironment.getAssetAdministrationShells().forEach(x -> x.getSubmodels().remove(referenceOfIdentifiable));
        }
        else if (ConceptDescription.class.isAssignableFrom(identifiable.getClass())) {
            List<ConceptDescription> newConceptDescriptionList;
            newConceptDescriptionList = this.aasEnvironment.getConceptDescriptions().stream().filter(removeFilter).collect(Collectors.toList());
            this.aasEnvironment.setConceptDescriptions(newConceptDescriptionList);
        }
        else if (Asset.class.isAssignableFrom(identifiable.getClass())) {
            List<Asset> newAssetList;
            newAssetList = this.aasEnvironment.getAssets().stream().filter(removeFilter).collect(Collectors.toList());
            this.aasEnvironment.setAssets(newAssetList);
            //TODO: Remove belonging AssetInformation of AAS?
        }
    }


    public Identifiable put(Identifiable identifiable) {
        if (identifiable == null || this.aasEnvironment == null) {
            return null;
        }

        if (Submodel.class.isAssignableFrom(identifiable.getClass())) {
            this.aasEnvironment.setSubmodels(EnvironmentHelper.updateIdentifiableList(Submodel.class, this.aasEnvironment.getSubmodels(), identifiable));
            return identifiable;
        }
        else if (AssetAdministrationShell.class.isAssignableFrom(identifiable.getClass())) {
            this.aasEnvironment.setAssetAdministrationShells(
                    EnvironmentHelper.updateIdentifiableList(AssetAdministrationShell.class, this.aasEnvironment.getAssetAdministrationShells(), identifiable));
            return identifiable;
        }
        else if (ConceptDescription.class.isAssignableFrom(identifiable.getClass())) {
            this.aasEnvironment
                    .setConceptDescriptions(EnvironmentHelper.updateIdentifiableList(ConceptDescription.class, this.aasEnvironment.getConceptDescriptions(), identifiable));
            return identifiable;
        }
        else if (Asset.class.isAssignableFrom(identifiable.getClass())) {
            this.aasEnvironment.setAssets(EnvironmentHelper.updateIdentifiableList(Asset.class, this.aasEnvironment.getAssets(), identifiable));
            //TODO: Add belonging AssetInformation to AAS?
            return identifiable;
        }
        return null;
    }

}