package de.tudresden.inf.st.bigraphs.editor.bigellor;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Service interface to implement model storage operations individually for bigraphs, rules, predicates and reactive systems.
 * Mainly suited for file-based operations.
 * <p>
 * To be used in combination with {@link de.tudresden.inf.st.bigraphs.editor.bigellor.domain.ModelStorageEntity}
 * and {@link de.tudresden.inf.st.bigraphs.editor.bigellor.persistence.ModelStorageRepository}.
 *
 * @author Dominik Grzelak
 */
public interface ModelStorageService {

    void initialize();

    String storeModel(MultipartFile file);

    Stream<Path> loadAll();

    Path load(String filename);

    Resource loadFileAsResource(String filename) throws Exception;

    void deleteAll();

}