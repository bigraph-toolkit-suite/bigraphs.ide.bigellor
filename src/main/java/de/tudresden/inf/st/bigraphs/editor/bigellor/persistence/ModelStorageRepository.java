package de.tudresden.inf.st.bigraphs.editor.bigellor.persistence;

import de.tudresden.inf.st.bigraphs.editor.bigellor.domain.ModelStorageEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModelStorageRepository extends CrudRepository<ModelStorageEntity, Long> {

    // see: https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods.query-creation
    List<ModelStorageEntity> findModelStorageEntitiesByProjectIdAndModelType(long projectId, ModelStorageEntity.ModelType modelType);
}
