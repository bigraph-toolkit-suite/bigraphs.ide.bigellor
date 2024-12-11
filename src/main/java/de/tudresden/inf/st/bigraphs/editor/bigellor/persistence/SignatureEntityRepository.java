package de.tudresden.inf.st.bigraphs.editor.bigellor.persistence;

import de.tudresden.inf.st.bigraphs.editor.bigellor.domain.SignatureEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SignatureEntityRepository extends CrudRepository<SignatureEntity, Long> {

    Optional<SignatureEntity> findByName(String name);
}
