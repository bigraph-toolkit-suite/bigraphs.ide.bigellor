package de.tudresden.inf.st.bigraphs.editor.bigellor.persistence;

import de.tudresden.inf.st.bigraphs.editor.bigellor.domain.NewProjectDTO;
import de.tudresden.inf.st.bigraphs.editor.bigellor.domain.SignatureEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewProjectDTORepository extends CrudRepository<NewProjectDTO, Long> {
}
