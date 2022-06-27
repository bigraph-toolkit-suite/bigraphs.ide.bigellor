package de.tudresden.inf.st.bigraphs.editor.bigellor.persistence;

import de.tudresden.inf.st.bigraphs.editor.bigellor.domain.NewProjectDTO;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * A simple in-memory CRUD repository used at runtime to quickly access project details, which are primarily stored on
 * the filesystem.
 *
 * @author Dominik Grzelak
 */
@Repository
public interface NewProjectDTORepository extends CrudRepository<NewProjectDTO, Long> {

    Optional<NewProjectDTO> findByProjectName(String projectName);
}
