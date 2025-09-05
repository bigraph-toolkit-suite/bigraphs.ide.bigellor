package de.tudresden.inf.st.bigraphs.editor.bigellor.service;

import org.bigraphs.framework.core.BigraphFileModelManagement;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import de.tudresden.inf.st.bigraphs.editor.bigellor.domain.SignatureEntity;
import de.tudresden.inf.st.bigraphs.editor.bigellor.persistence.SignatureEntityRepository;
import de.tudresden.inf.st.bigraphs.editor.bigellor.rest.exception.FileStorageException;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.emf.ecore.EObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class SignatureFileStorageService implements ModelStorageService<SignatureEntity> {

    @Autowired
    SignatureEntityRepository signatureEntityRepository;

    public static String RESOURCES_DIR_SIGNATURES = Paths.get("data/signatures/").toAbsolutePath().toString();

    @Override
    public void initialize() {
        try {
            Files.createDirectories(Paths.get(RESOURCES_DIR_SIGNATURES));
            Files.list(Paths.get(RESOURCES_DIR_SIGNATURES)).forEach(x -> {
                if (x.toFile().isFile() && x.toString().endsWith(".xmi")) {
                    String projectName = x.toFile().getName();
                    try {
                        List<EObject> eObjects = BigraphFileModelManagement.Load.signatureInstanceModel(
                                Paths.get(RESOURCES_DIR_SIGNATURES, FilenameUtils.removeExtension(projectName) + ".ecore").toString(),
                                Paths.get(RESOURCES_DIR_SIGNATURES, projectName).toString()
                        );
                        DynamicSignature signature = new DynamicSignature(eObjects.get(0));
                        SignatureEntity convertedSignature = SignatureEntity.convert(signature, FilenameUtils.removeExtension(projectName));
                        save(convertedSignature);
                        System.out.println("Signature loaded: " + convertedSignature.getId() + " --> Name: " + convertedSignature.getName());
                    } catch (Exception e) {
//                        throw new RuntimeException(e);
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Stores the instance and metamodel of the signature to the filesystem
     *
     * @param signature
     * @param location
     * @param fileName
     * @return
     */
    public String storeModel(DynamicSignature signature, Path location, String fileName) {
        try {
            Path targetLocation = location.resolve(fileName + ".xmi");
            Path targetLocationMetamodel = location.resolve(fileName + ".ecore");
            BigraphFileModelManagement.Store.exportAsInstanceModel(signature, new FileOutputStream(targetLocation.toFile()));
            BigraphFileModelManagement.Store.exportAsMetaModel(signature, new FileOutputStream(targetLocationMetamodel.toFile()));
            return fileName;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public String storeModel(SignatureEntity signatureEntity, Path location, String fileName) {
        try {
            DynamicSignature signature = SignatureEntity.convert(signatureEntity);
            Path targetLocation = location.resolve(fileName + ".xmi");
            Path targetLocationMetamodel = location.resolve(fileName + ".ecore");
            BigraphFileModelManagement.Store.exportAsInstanceModel(signature, new FileOutputStream(targetLocation.toFile()), targetLocationMetamodel.toFile().getName());
            BigraphFileModelManagement.Store.exportAsMetaModel(signature, new FileOutputStream(targetLocationMetamodel.toFile()));
            return fileName;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    /**
     * @param sigName name of the signature
     * @return id of signature model, otherwise -1
     */
    public long getIdByName(String sigName) {
        Optional<SignatureEntity> byName = signatureEntityRepository.findByName(sigName);
        return byName.map(SignatureEntity::getId).orElse(-1L);
    }

    public void updateModelForProject(long signatureId, long projectId) {
        //TODO check if project actually have the signature assigned
        // Update signature
//        SignatureEntity currentSignatureEntity = signatureFileStorageService.load(newProjectDTO.getSigId())
//                .orElseThrow(() -> new IllegalArgumentException("Invalid signature id:" + newProjectDTO.getSigId()));
//        signatureFileStorageService.storeModel(
//                SignatureEntity.convert(currentSignatureEntity),
//                Paths.get(RESOURCES_DIR_SIGNATURES),
//                "signature.xmi"
//        );
    }

    @Override
    public Stream<Path> loadAll() {
        return null;
    }

    @Override
    public SignatureEntity save(SignatureEntity modelEntity) {
        return signatureEntityRepository.save(modelEntity);
    }

    @Override
    public Path load(String location, String filename) {
        return null;
    }

    @Override
    public void deleteAll() {

    }

    public Iterable<SignatureEntity> findAll() {
        return signatureEntityRepository.findAll();
    }

    public Optional<SignatureEntity> findById(long id) {
        return signatureEntityRepository.findById(id);
    }

    public void delete(SignatureEntity signatureEntity) {
        signatureEntityRepository.delete(signatureEntity);
    }

    public void deleteModel(SignatureEntity signatureEntity) {
        deleteModel(signatureEntity.getName());
    }

    public void deleteModel(String sigName) {
        File fileXmi = Paths.get(RESOURCES_DIR_SIGNATURES, sigName + ".xmi").toFile();
        File fileEcore = Paths.get(RESOURCES_DIR_SIGNATURES, sigName + ".ecore").toFile();

        if (fileXmi.delete()) {
            System.out.println("File deleted successfully");
        } else {
            System.out.println("Failed to delete the file");
        }
        if (fileEcore.delete()) {
            System.out.println("File deleted successfully");
        } else {
            System.out.println("Failed to delete the file");
        }
    }
}
