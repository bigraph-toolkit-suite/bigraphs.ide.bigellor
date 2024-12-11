package de.tudresden.inf.st.bigraphs.editor.bigellor.rest.exception;

public class ProjectSignatureNotFoundException extends FileStorageException {
    public ProjectSignatureNotFoundException(String message) {
        super(message);
    }

    public ProjectSignatureNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
