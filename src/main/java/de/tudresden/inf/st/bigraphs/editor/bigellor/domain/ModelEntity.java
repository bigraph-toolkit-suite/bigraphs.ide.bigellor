package de.tudresden.inf.st.bigraphs.editor.bigellor.domain;

import javax.persistence.*;

/**
 * Generic class for any bigraph-related model, including:
 * <ul>
 *     <li>Bigraphs</li>
 *     <li>Signatures</li>
 *     <li>Rules</li>
 *     <li>Predicates</li>
 *     <li>BRSs</li>
 * </ul>
 *
 * @author Dominik Grzelak
 */
@Entity
public class ModelEntity {
    public enum ModelType {
        BIGRAPH, SIGNATURE, RULE, PREDICATE, SYSTEM
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long modelStorageId;

    private String fileName;
    private ModelType modelType;
    private String uploadFolder;
    private long projectId;
    private transient String downloadUrl;

    public ModelEntity() {
    }

    public long getModelStorageId() {
        return modelStorageId;
    }

    public void setModelStorageId(long modelStorageId) {
        this.modelStorageId = modelStorageId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public ModelType getModelType() {
        return modelType;
    }

    public void setModelType(ModelType modelType) {
        this.modelType = modelType;
    }

    public String getUploadFolder() {
        return uploadFolder;
    }

    public void setUploadFolder(String uploadFolder) {
        this.uploadFolder = uploadFolder;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}
