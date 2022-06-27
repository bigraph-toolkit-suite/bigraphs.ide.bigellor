package de.tudresden.inf.st.bigraphs.editor.bigellor.service;

import de.tudresden.inf.st.bigraphs.editor.bigellor.rest.exception.FileStorageException;
import de.tudresden.inf.st.bigraphs.editor.bigellor.rest.exception.ModelFileNotFoundException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class DownloadUtils {

    public static Resource loadFileAsResource(String location, String fileName) throws Exception {
        try {
            Path filePath = Paths.get(location).resolve(fileName).normalize();
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

    /**
     * Stores a model file on the filesystem.
     *
     * @param file the uploaded model file to store
     * @return the filename of the stored model, if the operation was successful
     */
    public static String storeModel(String location, MultipartFile file) {

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
            Path targetLocation = Paths.get(location, fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }
}
