package de.tudresden.inf.st.bigraphs.editor.bigellor.service;

import de.tudresden.inf.st.bigraphs.editor.bigellor.domain.ModelEntity;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

//TODO implement signaturestorageservice

/**
 * Service interface to implement model storage operations individually for bigraphs, rules, predicates and reactive systems.
 * Mainly suited for file-based operations.
 * <p>
 * To be used in combination with {@link ModelEntity}
 * and {@link de.tudresden.inf.st.bigraphs.editor.bigellor.persistence.ModelStorageRepository}.
 *
 * @author Dominik Grzelak
 */
public interface ModelStorageService<T> {

    void initialize();

    @Deprecated
        //TODO does not make sense when notion of project is introduced
    Stream<Path> loadAll();

    Optional<T> findById(long modelId);

    T save(T modelEntity);

    /**
     * Loads the path of the model, which has to exist.
     * It is specified by its location and filename.
     *
     * @param location The location of the model
     * @param filename the filename of the model
     * @return the existing complete path of the model
     */
    Path load(String location, String filename);

    @Deprecated
        //TODO does not make sense when notion of project is introduced
    void deleteAll();

}