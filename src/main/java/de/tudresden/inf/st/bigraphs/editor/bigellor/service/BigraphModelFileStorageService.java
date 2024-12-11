package de.tudresden.inf.st.bigraphs.editor.bigellor.service;

import de.tudresden.inf.st.bigraphs.editor.bigellor.domain.ModelEntity;
import de.tudresden.inf.st.bigraphs.editor.bigellor.persistence.ModelStorageRepository;
import de.tudresden.inf.st.bigraphs.editor.bigellor.rest.exception.FileStorageException;
import org.bigraphs.framework.core.BigraphFileModelManagement;
import org.bigraphs.framework.core.EcoreBigraph;
import org.bigraphs.framework.core.exceptions.EcoreBigraphFileSystemException;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class BigraphModelFileStorageService implements ModelStorageService<ModelEntity> {

    @Value("${bigellor.model.storage.location}")
    public Path bigraphModelStorageLocation;

    @Autowired
    ModelStorageRepository modelStorageRepository;

    @Override
//    @PostConstruct
    public void initialize() {
//        try {
////            URL location = getClass().getProtectionDomain().getCodeSource().getLocation();
//            ApplicationHome home = new ApplicationHome(this.getClass());
////            File jarFile = home.getSource();
//            File jarDir = home.getDir();
//            String tmp = bigraphModelStorageLocation.toString();
//            if (bigraphModelStorageLocation.isAbsolute()) {
//                tmp = bigraphModelStorageLocation.getFileName().toString();
//            }
//            File uploadDir = new File(jarDir, tmp);
//            bigraphModelStorageLocation = uploadDir.toPath();
////            System.out.println("bigellor.model.storage.location is: " + bigraphModelStorageLocation);
//            Files.createDirectories(bigraphModelStorageLocation);
////            Files.createDirectories(Paths.get(location.toURI()));
//        } catch (IOException e) {
////            e.printStackTrace();
//            throw new FileStorageException("Could not initialize storage location", e);
//        }
    }

    @Override
    public Optional<ModelEntity> findById(long modelId) {
        // Check whether it is a rule or agent
//        ModelEntity modelEntity = modelStorageRepository.findById(modelId)
//                .orElseThrow(() -> new IllegalArgumentException("Invalid model storage entity id:" + modelId));
        return modelStorageRepository.findById(modelId);
    }

    /**
     * Same as {@link #storeModel(MultipartFile)} but directly takes a bigraph object and a filename under
     * which the bigraph model file should be stored.
     *
     * @param bigraph  the bigraph
     * @param fileName the filename under which the model should be stored
     * @return the filename, if the operation was successful
     */
    public String storeModel(PureBigraph bigraph, Path location, String fileName) {
        try {
            InputStream inputStreamOfInstanceModel = new EcoreBigraph.Stub<>(bigraph).getInputStreamOfInstanceModel();
            String nameWithoutExtension = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;
            String metaModelFilename = nameWithoutExtension + "mm.ecore";
            BigraphFileModelManagement.Store.exportAsMetaModel(
                    bigraph,
                    new FileOutputStream(location.resolve(metaModelFilename).toFile())
                    );
            // Copy file to the target location (Replacing existing file with the same name)

            Path targetLocation = location.resolve(fileName);
            Files.copy(inputStreamOfInstanceModel, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException | EcoreBigraphFileSystemException ex) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public ModelEntity save(ModelEntity modelEntity) {
        return modelStorageRepository.save(modelEntity);
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.bigraphModelStorageLocation, 1)
                    .filter(path -> !path.equals(this.bigraphModelStorageLocation))
                    .map(this.bigraphModelStorageLocation::relativize);
        } catch (IOException e) {
            throw new FileStorageException("Failed to read stored files", e);
        }
    }

    @Override
    public Path load(String location, String filename) {
//        return bigraphModelStorageLocation.resolve(filename).normalize();
        return Paths.get(location).resolve(filename).normalize();
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(bigraphModelStorageLocation.toFile());
    }

}
