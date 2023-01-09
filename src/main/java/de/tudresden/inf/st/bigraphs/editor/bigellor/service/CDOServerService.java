package de.tudresden.inf.st.bigraphs.editor.bigellor.service;

import de.tudresden.inf.st.spring.data.cdo.CDOStandaloneServer;
import de.tudresden.inf.st.spring.data.cdo.CdoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class CDOServerService {
    public CDOStandaloneServer server;

    CdoClient cdoClient;

    @Autowired
    public CDOServerService(CdoClient cdoClient) {
        this.cdoClient = cdoClient;
    }

    @Async(value = "taskExecutor")
    public void startServer() {
        try {
            if (server == null) {
//            server = new CDOStandaloneServer("repo1");
                server = new CDOStandaloneServer(new File("src/main/resources/config/cdo-server.xml")); //TODO load resource instead
            }
//            System.out.println("Server starting now ...");
            Thread.sleep(500);
            CDOStandaloneServer.start(server);
        } catch (Exception e) {
            e.printStackTrace();
            server = null;
        }
    }

    public boolean isConnected() {
        if (server != null) {
            return server.isActiveAndRunning();
        }
        return false;
    }

    public void stopServer() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }
}