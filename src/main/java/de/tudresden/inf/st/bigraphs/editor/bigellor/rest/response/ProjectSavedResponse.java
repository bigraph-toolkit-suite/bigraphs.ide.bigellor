package de.tudresden.inf.st.bigraphs.editor.bigellor.rest.response;

public class ProjectSavedResponse {
    private long projectId;
    private String filenameOfBigraph;


    public ProjectSavedResponse(long projectId, String filenameOfBigraph) {
        this.projectId = projectId;
        this.filenameOfBigraph = filenameOfBigraph;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public String getFilenameOfBigraph() {
        return filenameOfBigraph;
    }

    public void setFilenameOfBigraph(String filenameOfBigraph) {
        this.filenameOfBigraph = filenameOfBigraph;
    }
}

