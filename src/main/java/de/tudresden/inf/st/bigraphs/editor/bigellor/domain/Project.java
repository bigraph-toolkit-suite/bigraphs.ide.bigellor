package de.tudresden.inf.st.bigraphs.editor.bigellor.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * An entity containing the entire information about a modelling project.
 * It contains agents and rules, which a stored in lists as {@link ModelEntity} instances.
 *
 * It is not stored anywhere explicitly (e.g., in a repository, database etc.) but acquired at startup and stored
 * in cache (see {@link de.tudresden.inf.st.bigraphs.editor.bigellor.service.ProjectCacheLoader} //TODO add other classes
 *
 * @author Dominik Grzelak
 */
public class Project {
    String name;
    long projectId;

    SignatureEntity signature;
    List<ModelEntity> bigraphs = new ArrayList<>();
    List<ModelEntity> rules = new ArrayList<>();

    private Date createdDate;
    private Date modifiedDate;

    public Project(String projectName) {
        this.name = projectName;
    }

    public List<ModelEntity> getBigraphs() {
        return bigraphs;
    }

    public void setBigraphs(List<ModelEntity> bigraphs) {
        this.bigraphs = bigraphs;
    }

    public List<ModelEntity> getRules() {
        return rules;
    }

    public void setRules(List<ModelEntity> rules) {
        this.rules = rules;
    }

    public SignatureEntity getSignature() {
        return signature;
    }

    public void setSignature(SignatureEntity signature) {
        this.signature = signature;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
}
