package de.tudresden.inf.st.bigraphs.editor.bigellor.rest.response;

public class CdoStatusResponse {
    public final static String SERVER_UP = "CDO server is running";
    public final static String SERVER_DOWN = "CDO server is not running";
    private String message;
    private boolean upAndRunning;

    public CdoStatusResponse() {
    }

    public CdoStatusResponse(boolean upAndRunning) {
        this.upAndRunning = upAndRunning;
        if (this.upAndRunning) {
            this.message = SERVER_UP;
        } else {
            this.message = SERVER_DOWN;
        }
    }

    public CdoStatusResponse(String message, boolean upAndRunning) {
        this.message = message;
        this.upAndRunning = upAndRunning;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isUpAndRunning() {
        return upAndRunning;
    }

    public void setUpAndRunning(boolean upAndRunning) {
        this.upAndRunning = upAndRunning;
    }
}
