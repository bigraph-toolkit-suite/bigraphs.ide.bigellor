package de.tudresden.inf.st.bigraphs.editor.bigellor;

import de.tudresden.inf.st.bigraphs.core.EcoreBigraph;
import de.tudresden.inf.st.bigraphs.core.exceptions.EcoreBigraphFileSystemException;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.editor.bigellor.rest.exception.FileStorageException;
import de.tudresden.inf.st.bigraphs.editor.bigellor.rest.exception.ModelFileNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

@Service
public class BigraphModelFileStorageService implements ModelStorageService {

    @Value("${bigellor.model.storage.location}")
    public Path bigraphModelStorageLocation;

    @Override
    @PostConstruct
    public void initialize() {
        try {
//            URL location = getClass().getProtectionDomain().getCodeSource().getLocation();
            ApplicationHome home = new ApplicationHome(this.getClass());
//            File jarFile = home.getSource();
            File jarDir = home.getDir();
            String tmp = bigraphModelStorageLocation.toString();
            if (bigraphModelStorageLocation.isAbsolute()) {
                tmp = bigraphModelStorageLocation.getFileName().toString();
            }
            File uploadDir = new File(jarDir, tmp);
            bigraphModelStorageLocation = uploadDir.toPath();
//            System.out.println("bigellor.model.storage.location is: " + bigraphModelStorageLocation);
            Files.createDirectories(bigraphModelStorageLocation);
//            Files.createDirectories(Paths.get(location.toURI()));
        } catch (IOException e) {
//            e.printStackTrace();
            throw new FileStorageException("Could not initialize storage location", e);
        }
    }

    /**
     * Stores a model file on the filesystem.
     *
     * @param file the uploaded model file to store
     * @return the filename of the stored model, if the operation was successful
     */
    public String storeModel(MultipartFile file) {

        // Normalize file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            if (file.isEmpty()) {
                throw new FileStorageException("Failed to store empty file " + fileName);
            }
            // Check if the file's name contains invalid characters
            if (fileName.contains("..")) {
                throw new FileStorageException("Filename contains an invalid path sequence " + fileName);
            }

            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = this.bigraphModelStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    /**
     * Same as {@link #storeModel(MultipartFile)} but directly takes a bigraph object and a filename under
     * which the bigraph model file should be stored.
     *
     * @param bigraph  the bigraph
     * @param fileName the filename under which the model should be stored
     * @return the filename, if the operation was successful
     */
    public String storeModel(PureBigraph bigraph, String fileName) {
        try {
            InputStream inputStreamOfInstanceModel = new EcoreBigraph.Stub<>(bigraph).getInputStreamOfInstanceModel();
            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = this.bigraphModelStorageLocation.resolve(fileName);
            Files.copy(inputStreamOfInstanceModel, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException | EcoreBigraphFileSystemException ex) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        }
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
    public Path load(String filename) {
        return bigraphModelStorageLocation.resolve(filename).normalize();
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(bigraphModelStorageLocation.toFile());
    }

    public Resource loadFileAsResource(String fileName) throws Exception {
        try {
            Path filePath = load(fileName); //this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new ModelFileNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new ModelFileNotFoundException("File not found " + fileName);
        }
    }

}
