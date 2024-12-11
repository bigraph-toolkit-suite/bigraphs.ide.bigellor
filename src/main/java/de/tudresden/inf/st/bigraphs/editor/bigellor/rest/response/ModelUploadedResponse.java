package de.tudresden.inf.st.bigraphs.editor.bigellor.rest.response;

public class ModelUploadedResponse {

    private String fileName;
    private String uri;
    private String fileType;
    private long size;


    public ModelUploadedResponse(String fileName, String uri, String fileType, long size) {
        this.fileName = fileName;
        this.uri = uri;
        this.fileType = fileType;
        this.size = size;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}

