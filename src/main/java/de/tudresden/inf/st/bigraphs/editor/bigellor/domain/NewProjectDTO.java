package de.tudresden.inf.st.bigraphs.editor.bigellor.domain;

import org.springframework.data.annotation.CreatedDate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
public class NewProjectDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long newProjectId;

    //    @NotEmpty(message = "Signature cannot be empty.")
    @NotNull(message = "Signature cannot be empty.")
    @Min(0)
    long sigId;

    //    @Size(max = 30)
    @NotNull(message = "Project name can not be null.")
    @NotEmpty(message = "Project name cannot be empty.")
    String projectName;

    @CreatedDate
//    @Column(name = "created_date")
    private Date createdDate;

    private long modelStorageEntityId;


    enum Status {
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
