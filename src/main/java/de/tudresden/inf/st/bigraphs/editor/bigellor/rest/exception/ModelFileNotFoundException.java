package de.tudresden.inf.st.bigraphs.editor.bigellor.rest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ModelFileNotFoundException extends FileStorageException {

    public ModelFileNotFoundException(String message) {
        super(message);
    }

    public ModelFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}