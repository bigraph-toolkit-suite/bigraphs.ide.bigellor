package de.tudresden.inf.st.bigraphs.editor.bigellor.domain;

import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * NewProjectDTO is a smaller copy of a complete {@link Project} entity containing only the necessary information
 * for the current view in the UI.
 *
 * @author Dominik Grzelak
 */
@Entity
public class NewProjectDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long newProjectId = -1;

    @Version
    Integer version;
    //    @NotEmpty(message = "Signature cannot be empty.")
    @NotNull(message = "Signature cannot be empty.")
    @Min(0)
    long sigId;

    @NotNull(message = "Project name can not be null.")
    @NotEmpty(message = "Project name cannot be empty.")
    @Size(max = 120)
    String projectName;

    @CreatedDate
//    @Column(name = "created_date")
    private Date createdDate;

    private long modelStorageEntityId; //current model


    public enum Status {
        CREATED, SAVED, DIRTY
    }

    Status projectStatus = Status.CREATED;

    public long getNewProjectId() {
        return newProjectId;
    }

    public void setNewProjectId(long newProjectId) {
        this.newProjectId = newProjectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String name) {
        this.projectName = name;
    }

    public long getSigId() {
        return sigId;
    }

    public void setSigId(long sigId) {
        this.sigId = sigId;
    }

    public Status getProjectStatus() {
        return projectStatus;
    }

    public void setProjectStatus(Status projectStatus) {
        this.projectStatus = projectStatus;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public long getModelStorageEntityId() {
        return modelStorageEntityId;
    }

    public void setModelStorageEntityId(long modelStorageEntityId) {
        this.modelStorageEntityId = modelStorageEntityId;
    }
}
