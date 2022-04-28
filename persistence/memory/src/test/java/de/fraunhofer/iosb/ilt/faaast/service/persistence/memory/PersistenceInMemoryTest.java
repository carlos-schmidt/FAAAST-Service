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
package de.fraunhofer.iosb.ilt.faaast.service.persistence.memory;

import de.fraunhofer.iosb.ilt.faaast.service.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.AASFull;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Message;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Result;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Extend;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Level;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.ExecutionState;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationHandle;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationResult;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.AssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.GlobalAssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.visitor.AssetAdministrationShellElementWalker;
import de.fraunhofer.iosb.ilt.faaast.service.model.visitor.DefaultAssetAdministrationShellElementVisitor;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.util.ReferenceBuilder;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.Blob;
import io.adminshell.aas.v3.model.ConceptDescription;
import io.adminshell.aas.v3.model.Identifiable;
import io.adminshell.aas.v3.model.Identifier;
import io.adminshell.aas.v3.model.IdentifierType;
import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.KeyType;
import io.adminshell.aas.v3.model.Referable;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import io.adminshell.aas.v3.model.impl.DefaultIdentifier;
import io.adminshell.aas.v3.model.impl.DefaultKey;
import io.adminshell.aas.v3.model.impl.DefaultReference;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class PersistenceInMemoryTest {

    private AssetAdministrationShellEnvironment environment;
    private Persistence persistence;

    @Before
    public void init() {
        environment = AASFull.createEnvironment();
        persistence = new PersistenceInMemory();
        persistence.setEnvironment(environment);
    }


    @Test
    public void getEnvironmentTest() {
        Assert.assertEquals(environment, persistence.getEnvironment());
    }


    @Test
    public void getSubmodelElementTest() throws ResourceNotFoundException {
        String assId = "https://acplt.org/Test_AssetAdministrationShell";
        String submodelId = "http://acplt.org/Submodels/Assets/TestAsset/BillOfMaterial";
        String submodelElementIdShort = "ExampleEntity2";
        Reference reference = ReferenceBuilder.build(
                assId,
                submodelId,
                submodelElementIdShort);
        SubmodelElement expected = environment.getSubmodels().stream()
                .filter(x -> x.getIdentification().getIdentifier().equalsIgnoreCase(submodelId))
                .findFirst().get()
                .getSubmodelElements().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase(submodelElementIdShort))
                .findFirst().get();
        SubmodelElement actual = persistence.get(reference, QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getSubmodelElementTestWithBlob() throws ResourceNotFoundException {
        String aasId = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        String submodelId = "https://acplt.org/Test_Submodel_Mandatory";
        String submodelElementCollectionIdShort = "ExampleSubmodelCollectionUnordered";
        String submodelElementIdShort = "ExampleBlob";
        Reference reference = ReferenceBuilder.build(
                aasId,
                submodelId,
                submodelElementCollectionIdShort,
                submodelElementIdShort);
        QueryModifier queryModifier = new QueryModifier.Builder().extend(Extend.WITH_BLOB_VALUE).build();
        SubmodelElement expected = ((SubmodelElementCollection) environment.getSubmodels().stream()
                .filter(x -> x.getIdentification().getIdentifier().equalsIgnoreCase(submodelId))
                .findFirst().get()
                .getSubmodelElements().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase(submodelElementCollectionIdShort))
                .findFirst().get())
                        .getValues().stream()
                        .filter(x -> x.getIdShort().equalsIgnoreCase(submodelElementIdShort))
                        .findFirst().get();
        SubmodelElement actual = persistence.get(reference, queryModifier);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getSubmodelElementTestWithOutBlob() throws ResourceNotFoundException {
        Reference reference = new DefaultReference.Builder()
                .key(new DefaultKey.Builder()
                        .idType(KeyType.IRI)
                        .type(KeyElements.ASSET_ADMINISTRATION_SHELL)
                        .value("https://acplt.org/Test_AssetAdministrationShell_Mandatory")
                        .build())
                .key(new DefaultKey.Builder()
                        .idType(KeyType.IRI)
                        .type(KeyElements.SUBMODEL)
                        .value("https://acplt.org/Test_Submodel_Mandatory")
                        .build())
                .key(new DefaultKey.Builder()
                        .idType(KeyType.ID_SHORT)
                        .type(KeyElements.SUBMODEL_ELEMENT)
                        .value("ExampleSubmodelCollectionUnordered")
                        .build())
                .key(new DefaultKey.Builder()
                        .idType(KeyType.ID_SHORT)
                        .type(KeyElements.BLOB)
                        .value("ExampleBlob")
                        .build())
                .build();
        Blob expected = DeepCopyHelper.deepCopy(AasUtils.resolve(reference, environment, Blob.class), Blob.class);
        expected.setValue(null);
        SubmodelElement actual = persistence.get(reference, QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getIdentifiableAASTest() throws ResourceNotFoundException {
        Identifier id = new DefaultIdentifier.Builder()
                .identifier("https://acplt.org/Test_AssetAdministrationShell_Mandatory")
                .idType(IdentifierType.IRI)
                .build();
        AssetAdministrationShell expected = environment.getAssetAdministrationShells().stream()
                .filter(x -> x.getIdentification().equals(id))
                .findFirst().get();
        AssetAdministrationShell actual = (AssetAdministrationShell) persistence.get(id, QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getIdentifiableSubmodelTest() throws ResourceNotFoundException {
        String submodelId = "https://acplt.org/Test_Submodel_Mandatory";
        Identifier id = new DefaultIdentifier.Builder()
                .identifier(submodelId)
                .idType(IdentifierType.IRI)
                .build();
        Submodel expected = environment.getSubmodels().stream()
                .filter(x -> x.getIdentification().equals(id))
                .findFirst().get();
        Submodel actual = (Submodel) persistence.get(id, QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getIdentifiableConceptDescriptionTest() throws ResourceNotFoundException {
        Identifier id = new DefaultIdentifier.Builder()
                .idType(IdentifierType.IRI)
                .identifier("https://acplt.org/Test_ConceptDescription")
                .build();
        ConceptDescription expected = environment.getConceptDescriptions().stream()
                .filter(x -> x.getIdentification().equals(id))
                .findFirst().get();
        ConceptDescription actual = (ConceptDescription) persistence.get(id, QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);

    }


    @Test
    public void getShellsNullTest() {
        String aasIdShort = "Test_AssetAdministrationShell_Mandatory";
        List<AssetAdministrationShell> expected = List.of();
        List<AssetAdministrationShell> actual = persistence.get(
                aasIdShort,
                List.of(GlobalAssetIdentification.builder()
                        .build()),
                QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getShellsAllTest() {
        List<AssetAdministrationShell> expected = environment.getAssetAdministrationShells();
        List<AssetAdministrationShell> actual = persistence.get("", (List<AssetIdentification>) null, QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getShellsWithIdShortTest() {
        String aasIdShort = "Test_AssetAdministrationShell_Mandatory";
        List<AssetAdministrationShell> expected = environment.getAssetAdministrationShells().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase(aasIdShort))
                .collect(Collectors.toList());
        List<AssetAdministrationShell> actual = persistence.get(aasIdShort, (List<AssetIdentification>) null, QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getShellsWithGlobalAssetIdentificationTest() {
        GlobalAssetIdentification globalAssetIdentification = new GlobalAssetIdentification();
        globalAssetIdentification.setReference(new DefaultReference.Builder()
                .key(new DefaultKey.Builder()
                        .type(KeyElements.ASSET)
                        .idType(KeyType.IRI)
                        .value("https://acplt.org/Test_Asset_Mandatory")
                        .build())
                .build());
        List<AssetAdministrationShell> expected = environment.getAssetAdministrationShells().stream()
                .filter(x -> x.getAssetInformation().getGlobalAssetId().equals(globalAssetIdentification.getReference()))
                .collect(Collectors.toList());
        List<AssetAdministrationShell> actual = persistence.get(null, List.of(globalAssetIdentification), QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getSubmodelsNullTest() {
        String aasId = "Test_AssetAdministrationShell_Mandatory";
        List<AssetAdministrationShell> expected = null;
        List<AssetAdministrationShell> actual = persistence.get(aasId, new DefaultReference(), QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getSubmodelsAllTest() {
        List<Submodel> expected = environment.getSubmodels();
        clearBlobs(expected);
        List<Submodel> actual = persistence.get(null, (Reference) null, QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getSubmodelsWithIdShortTest() {
        String submodelIdShort = "TestSubmodel";
        List<Submodel> expected = environment.getSubmodels().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase(submodelIdShort))
                .collect(Collectors.toList());
        List<Submodel> actual = persistence.get(submodelIdShort, (Reference) null, QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getSubmodelsWithSemanticIdTest() {
        Reference semanticId = new DefaultReference.Builder()
                .key(new DefaultKey.Builder()
                        .type(KeyElements.GLOBAL_REFERENCE)
                        .idType(KeyType.IRI)
                        .value("http://acplt.org/SubmodelTemplates/ExampleSubmodel")
                        .build())
                .build();
        List<Submodel> expected = environment.getSubmodels().stream()
                .filter(x -> x.getSemanticId() != null && x.getSemanticId().equals(semanticId))
                .collect(Collectors.toList());
        clearBlobs(expected);
        List<Submodel> actual = persistence.get("", semanticId, QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    private void clearBlobs(List<? extends Referable> referables) {
        referables.forEach(x -> clearBlobs(x));
    }


    private void clearBlobs(Referable referable) {
        AssetAdministrationShellElementWalker.builder()
                .visitor(new DefaultAssetAdministrationShellElementVisitor() {
                    public void visit(Blob blob) {
                        blob.setValue(null);
                    }
                })
                .build()
                .walk(referable);
    }


    @Test
    public void getSubmodelElementsTest() throws ResourceNotFoundException {
        String aasIdShort = "TestAssetAdministrationShell";
        String submodelId = "http://acplt.org/Submodels/Assets/TestAsset/Identification";
        Reference submodelReference = ReferenceBuilder.build(aasIdShort, submodelId);
        List<SubmodelElement> expected = environment.getSubmodels().stream()
                .filter(x -> x.getIdentification().getIdentifier().equalsIgnoreCase(submodelId))
                .findFirst().get()
                .getSubmodelElements();
        List<SubmodelElement> actual = persistence.getSubmodelElements(submodelReference, null, QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getSubmodelElementsWithSemanticIdTest() throws ResourceNotFoundException {
        String aasIdShort = "TestAssetAdministrationShell";
        String submodelId = "http://acplt.org/Submodels/Assets/TestAsset/Identification";
        Reference submodelReference = ReferenceBuilder.build(aasIdShort, submodelId);
        Reference semanticIdReference = new DefaultReference.Builder()
                .key(new DefaultKey.Builder()
                        .type(KeyElements.GLOBAL_REFERENCE)
                        .value("0173-1#02-AAO677#002")
                        .idType(KeyType.IRI)
                        .build())
                .build();
        List<SubmodelElement> expected = environment.getSubmodels().stream()
                .filter(x -> x.getIdentification().getIdentifier().equalsIgnoreCase(submodelId))
                .findFirst().get()
                .getSubmodelElements().stream().filter(x -> x.getSemanticId().equals(semanticIdReference)).collect(Collectors.toList());
        List<SubmodelElement> actual = persistence.getSubmodelElements(submodelReference, semanticIdReference, QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getSubmodelElementsFromSubmodelElementCollectionTest() throws ResourceNotFoundException {
        String aasId = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        String submodelId = "https://acplt.org/Test_Submodel_Mandatory";
        String submodelElementCollectionIdShort = "ExampleSubmodelCollectionUnordered";
        Reference reference = ReferenceBuilder.build(aasId, submodelId, submodelElementCollectionIdShort);
        List<SubmodelElement> expected = List.copyOf(((SubmodelElementCollection) environment.getSubmodels().stream()
                .filter(x -> x.getIdentification().getIdentifier().equalsIgnoreCase(submodelId))
                .findFirst().get()
                .getSubmodelElements().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase(submodelElementCollectionIdShort))
                .findFirst().get())
                        .getValues());
        List<SubmodelElement> actual = persistence.getSubmodelElements(reference, null, QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getConceptDescriptionsAllTest() {
        List<ConceptDescription> expected = environment.getConceptDescriptions();
        List<ConceptDescription> actual = persistence.get(null, null, (Reference) null, QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getConceptDescriptionsWithIdShortTest() {
        String conceptDescriptionIdShort = "TestConceptDescription";
        List<ConceptDescription> actual = persistence.get(conceptDescriptionIdShort, null, (Reference) null, QueryModifier.DEFAULT);
        List<ConceptDescription> expected = environment.getConceptDescriptions().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase(conceptDescriptionIdShort))
                .collect(Collectors.toList());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getConceptDescriptionsWithIsCaseOfTest() {
        String conceptDescriptionIdShort = "TestConceptDescription";
        Reference isCaseOf = new DefaultReference.Builder()
                .key(new DefaultKey.Builder()
                        .type(null)
                        .idType(KeyType.IRI)
                        .value("http://acplt.org/DataSpecifications/ConceptDescriptions/TestConceptDescription")
                        .build())
                .build();
        List<ConceptDescription> expected = environment.getConceptDescriptions().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase(conceptDescriptionIdShort))
                .collect(Collectors.toList());
        List<ConceptDescription> actual = persistence.get(null, isCaseOf, (Reference) null, QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getConceptDescriptionsWithDataSpecificationTest() {
        Reference dataSpecification = new DefaultReference.Builder()
                .key(new DefaultKey.Builder()
                        .type(KeyElements.GLOBAL_REFERENCE)
                        .idType(KeyType.IRI)
                        .value("http://acplt.org/ReferenceElements/DataSpecificationX")
                        .build())
                .build();
        List<ConceptDescription> expected = environment.getConceptDescriptions().stream()
                .filter(x -> x.getEmbeddedDataSpecifications() != null
                        && x.getEmbeddedDataSpecifications().stream()
                                .anyMatch(y -> y.getDataSpecification() != null && y.getDataSpecification().equals(dataSpecification)))
                .collect(Collectors.toList());
        List<ConceptDescription> actual = persistence.get(null, null, dataSpecification, QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getConceptDescriptionsWithCombination() {
        String conceptDescriptionIdShort = "TestConceptDescription";
        Reference isCaseOf = new DefaultReference.Builder()
                .key(new DefaultKey.Builder()
                        .type(KeyElements.GLOBAL_REFERENCE)
                        .idType(KeyType.IRI)
                        .value("http://acplt.org/DataSpecifications/ConceptDescriptions/TestConceptDescription")
                        .build())
                .build();
        List<ConceptDescription> expected = environment.getConceptDescriptions().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase(conceptDescriptionIdShort))
                .collect(Collectors.toList());
        List<ConceptDescription> actual = persistence.get(conceptDescriptionIdShort, isCaseOf, null, QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void putSubmodelElementNewInSubmodelTest() throws ResourceNotFoundException {
        SubmodelElement expected = DeepCopyHelper.deepCopy(environment.getSubmodels().get(0).getSubmodelElements().get(0),
                environment.getSubmodels().get(0).getSubmodelElements().get(0).getClass());
        String idShort = "NewIdShort";
        expected.setIdShort(idShort);
        String aasId = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        String submodelId = "https://acplt.org/Test_Submodel_Mandatory";
        Reference parent = ReferenceBuilder.build(aasId, submodelId);
        Reference submodelElementReference = ReferenceBuilder.build(aasId, submodelId, idShort);
        persistence.put(parent, null, expected);
        SubmodelElement actual = persistence.get(submodelElementReference, QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void putSubmodelElementChangeInSubmodelTest() throws ResourceNotFoundException {
        String aasId = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        String submodelId = "https://acplt.org/Test_Submodel_Mandatory";
        SubmodelElement submodelElement = environment.getSubmodels().stream()
                .filter(x -> x.getIdentification().getIdentifier().equalsIgnoreCase(submodelId))
                .findFirst().get().getSubmodelElements().get(0);
        SubmodelElement expected = DeepCopyHelper.deepCopy(submodelElement, submodelElement.getClass());
        String category = "NewCategory";
        expected.setCategory(category);
        Reference reference = ReferenceBuilder.build(aasId, submodelId, submodelElement.getIdShort());
        persistence.put(null, reference, expected);
        SubmodelElement actual = persistence.get(reference, QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void putSubmodelElementNewInSubmodelElementCollectionTest() throws ResourceNotFoundException {
        SubmodelElement expected = DeepCopyHelper.deepCopy(environment.getSubmodels().get(0).getSubmodelElements().get(0),
                environment.getSubmodels().get(0).getSubmodelElements().get(0).getClass());
        String idShort = "NewIdShort";
        expected.setIdShort(idShort);
        String aasId = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        String submodelId = "https://acplt.org/Test_Submodel_Mandatory";
        String submodelElementCollectionIdShort = "ExampleSubmodelCollectionUnordered";
        Reference parent = ReferenceBuilder.build(aasId, submodelId, submodelElementCollectionIdShort);
        Assert.assertEquals(((SubmodelElementCollection) environment.getSubmodels().stream()
                .filter(x -> x.getIdentification().getIdentifier().equalsIgnoreCase(submodelId))
                .findFirst().get().getSubmodelElements().stream()
                .filter(y -> y.getIdShort().equalsIgnoreCase(submodelElementCollectionIdShort))
                .findFirst().orElse(null))
                        .getValues().stream()
                        .filter(x -> x.getIdShort().equalsIgnoreCase(idShort)).findFirst().orElse(null),
                null);
        persistence.put(parent, null, expected);
        SubmodelElement actual = persistence.get(ReferenceBuilder.build(aasId, submodelId, submodelElementCollectionIdShort, idShort), QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void putSubmodelElementChangeInSubmodelElementCollectionTest() throws ResourceNotFoundException {
        String aasId = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        String submodelId = "https://acplt.org/Test_Submodel_Mandatory";
        String submodelElementCollectionIdShort = "ExampleSubmodelCollectionUnordered";
        SubmodelElement submodelElement = ((SubmodelElementCollection) environment.getSubmodels().stream()
                .filter(x -> x.getIdentification().getIdentifier().equalsIgnoreCase(submodelId))
                .findFirst().get()
                .getSubmodelElements().stream()
                .filter(y -> y.getIdShort().equalsIgnoreCase(submodelElementCollectionIdShort))
                .findFirst().orElse(null))
                        .getValues().stream()
                        .findFirst().orElse(null);
        SubmodelElement expected = DeepCopyHelper.deepCopy(submodelElement, submodelElement.getClass());
        String category = "NewCategory";
        expected.setCategory(category);
        Reference reference = ReferenceBuilder.build(aasId, submodelId, submodelElementCollectionIdShort, submodelElement.getIdShort());
        persistence.put(null, reference, expected);
        SubmodelElement actual = persistence.get(reference, new QueryModifier.Builder().extend(Extend.WITH_BLOB_VALUE).build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void removeSubmodelTest() throws ResourceNotFoundException {
        Identifier submodelId = new DefaultIdentifier.Builder()
                .idType(IdentifierType.IRI)
                .identifier("https://acplt.org/Test_Submodel_Mandatory")
                .build();
        Assert.assertNotNull(persistence.get(submodelId, QueryModifier.DEFAULT));
        persistence.remove(submodelId);
        Assert.assertThrows(ResourceNotFoundException.class, () -> persistence.get(submodelId, QueryModifier.DEFAULT));
    }


    @Test
    public void removeAASTest() throws ResourceNotFoundException {
        Identifier aasId = new DefaultIdentifier.Builder()
                .idType(IdentifierType.IRI)
                .identifier("https://acplt.org/Test_AssetAdministrationShell_Mandatory")
                .build();
        Assert.assertNotNull(persistence.get(aasId, QueryModifier.DEFAULT));
        persistence.remove(aasId);
        Assert.assertThrows(ResourceNotFoundException.class, () -> persistence.get(aasId, QueryModifier.DEFAULT));
    }


    @Test
    public void removeByReferenceTest() throws ResourceNotFoundException {
        String aasId = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        String submodelId = "https://acplt.org/Test_Submodel_Mandatory";
        String submodelElementCollectionIdShort = "ExampleSubmodelCollectionUnordered";
        Reference reference = ReferenceBuilder.build(aasId, submodelId, submodelElementCollectionIdShort);
        Assert.assertNotNull(persistence.get(reference, QueryModifier.DEFAULT));
        persistence.remove(reference);
        Assert.assertThrows(ResourceNotFoundException.class, () -> persistence.get(reference, QueryModifier.DEFAULT));
    }


    @Test
    public void removeByReferencePropertyInSubmodelElementCollectionTest() throws ResourceNotFoundException {
        String aasId = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        String submodelId = "https://acplt.org/Test_Submodel_Mandatory";
        String submodelElementCollectionIdShort = "ExampleSubmodelCollectionUnordered";
        String submodelElementIdShort = "ExampleFile";
        Reference reference = ReferenceBuilder.build(aasId, submodelId, submodelElementCollectionIdShort, submodelElementIdShort);
        Assert.assertNotNull(persistence.get(reference, new OutputModifier()));
        persistence.remove(reference);
        Assert.assertThrows(ResourceNotFoundException.class, () -> persistence.get(reference, QueryModifier.DEFAULT));
    }


    @Test
    public void removeByReferencePropertyTest() throws ResourceNotFoundException {
        String aasId = "https://acplt.org/Test_AssetAdministrationShell";
        String submodelId = "http://acplt.org/Submodels/Assets/TestAsset/BillOfMaterial";
        String submodelElementIdShort = "ExampleEntity2";
        Reference reference = ReferenceBuilder.build(aasId, submodelId, submodelElementIdShort);
        Assert.assertNotNull(persistence.get(reference, new OutputModifier()));
        persistence.remove(reference);
        Assert.assertThrows(ResourceNotFoundException.class, () -> persistence.get(reference, QueryModifier.DEFAULT));
    }


    @Test
    public void putIdentifiableNewTest() throws ResourceNotFoundException {
        Submodel expected = DeepCopyHelper.deepCopy(environment.getSubmodels().get(0),
                environment.getSubmodels().get(0).getClass());
        String idShort = "NewIdShort";
        expected.setIdShort(idShort);
        expected.setIdentification(new DefaultIdentifier.Builder()
                .identifier("http://newIdentifier.org")
                .idType(IdentifierType.IRI)
                .build());
        persistence.put(expected);
        Identifiable actual = persistence.get(expected.getIdentification(), QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void putIdentifiableChangeTest() throws ResourceNotFoundException {
        ConceptDescription expected = DeepCopyHelper.deepCopy(environment.getConceptDescriptions().get(0),
                environment.getConceptDescriptions().get(0).getClass());
        String category = "NewCategory";
        expected.setCategory(category);
        persistence.put(expected);
        ConceptDescription actual = (ConceptDescription) persistence.get(expected.getIdentification(), QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testQueryModifierExtend() throws ResourceNotFoundException {
        String aasId = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        String submodelId = "https://acplt.org/Test_Submodel_Mandatory";
        String submodelElementCollectionIdShort = "ExampleSubmodelCollectionUnordered";
        String submodelElementIdShort = "ExampleBlob";
        Reference reference = ReferenceBuilder.build(aasId, submodelId, submodelElementCollectionIdShort, submodelElementIdShort);
        QueryModifier queryModifier = new QueryModifier.Builder().extend(Extend.WITH_BLOB_VALUE).build();
        Identifier submodelIdentifier = new DefaultIdentifier.Builder()
                .idType(IdentifierType.IRI)
                .identifier(submodelId)
                .build();
        SubmodelElement expected = ((SubmodelElementCollection) environment.getSubmodels().stream()
                .filter(x -> x.getIdentification().equals(submodelIdentifier))
                .findFirst().get()
                .getSubmodelElements().stream()
                .filter(y -> y.getIdShort().equalsIgnoreCase(submodelElementCollectionIdShort))
                .findFirst().get())
                        .getValues().stream()
                        .filter(z -> z.getIdShort().equalsIgnoreCase(submodelElementIdShort))
                        .findFirst().get();
        SubmodelElement actual = persistence.get(reference, queryModifier);
        Assert.assertEquals(expected, actual);

        queryModifier = new QueryModifier.Builder().extend(Extend.WITHOUT_BLOB_VALUE).build();
        expected = DeepCopyHelper.deepCopy(expected, SubmodelElement.class);
        ((Blob) expected).setValue(null);
        actual = persistence.get(reference, queryModifier);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testQueryModifierLevel() throws ResourceNotFoundException {
        QueryModifier queryModifier = new QueryModifier.Builder().level(Level.DEEP).build();
        Identifier submodelId = new DefaultIdentifier.Builder()
                .idType(IdentifierType.IRI)
                .identifier("https://acplt.org/Test_Submodel_Mandatory")
                .build();
        Submodel expected = environment.getSubmodels().stream()
                .filter(x -> x.getIdentification().equals(submodelId)).findFirst().get();
        Submodel actual = (Submodel) persistence.get(submodelId, queryModifier);
        Assert.assertEquals(expected, actual);

        queryModifier = new QueryModifier.Builder().level(Level.CORE).build();
        actual = (Submodel) persistence.get(submodelId, queryModifier);
        List<SubmodelElement> submodelElementCollections = actual.getSubmodelElements().stream()
                .filter(x -> SubmodelElementCollection.class.isAssignableFrom(x.getClass()))
                .collect(Collectors.toList());
        Assert.assertTrue(submodelElementCollections.stream().allMatch(x -> ((SubmodelElementCollection) x).getValues().isEmpty()));
    }


    @Test
    public void testOperationHandle() {
        OperationResult operationResult = new OperationResult.Builder()
                .requestId("Test")
                .executionState(ExecutionState.INITIATED)
                .build();
        OperationHandle actual = persistence.putOperationContext(null, "Test", operationResult);
        OperationHandle expected = new OperationHandle.Builder()
                .handleId(actual.getHandleId())
                .requestId("Test")
                .build();
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testUpdateOperationResult() {
        OperationResult expected = new OperationResult.Builder()
                .requestId("Test")
                .executionState(ExecutionState.INITIATED)
                .build();
        OperationHandle operationHandle = persistence.putOperationContext(null, "Test", expected);
        expected.setExecutionState(ExecutionState.COMPLETED);
        expected.setExecutionResult(new Result.Builder()
                .message(new Message.Builder()
                        .code("test")
                        .build())
                .success(true)
                .build());
        persistence.putOperationContext(operationHandle.getHandleId(), null, expected);
        OperationResult actual = persistence.getOperationResult(operationHandle.getHandleId());
        Assert.assertEquals(expected, actual);

    }

}
