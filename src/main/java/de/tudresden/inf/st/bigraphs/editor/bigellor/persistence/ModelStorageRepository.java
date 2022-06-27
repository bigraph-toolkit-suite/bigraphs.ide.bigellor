package de.tudresden.inf.st.bigraphs.editor.bigellor.persistence;

import de.tudresden.inf.st.bigraphs.editor.bigellor.domain.ModelEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModelStorageRepository extends CrudRepository<ModelEntity, Long> {

    // see: https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods.query-creation
    List<ModelEntity> findModelEntitiesByProjectIdAndModelType(long projectId, ModelEntity.ModelType modelType);
}
