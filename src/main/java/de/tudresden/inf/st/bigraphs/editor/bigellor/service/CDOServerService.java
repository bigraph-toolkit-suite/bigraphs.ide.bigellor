package de.tudresden.inf.st.bigraphs.editor.bigellor.service;

import de.tudresden.inf.st.spring.data.cdo.CDOStandaloneServer;
import de.tudresden.inf.st.spring.data.cdo.CdoClient;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;

@Service
public class CDOServerService {
    public CDOStandaloneServer server;

    CdoClient cdoClient;

    @Value(value = "${bigellor.cdo.config:config/cdo-server.xml}")
    public Path dbConfig;

    @Autowired
    public CDOServerService(CdoClient cdoClient) {
        this.cdoClient = cdoClient;
    }

    @Async(value = "taskExecutor")
    public void startServer() {
        try {
            if (server == null) {
                File cdoConfigFile = dbConfig.toFile();
                if (!cdoConfigFile.exists()) {
                    ClassPathResource resource = new ClassPathResource(dbConfig.toString(), this.getClass().getClassLoader());
                    if (!resource.exists()) {
                        throw new RuntimeException("The Eclipse CDO database configuration file 'config/cdo-server.xml' could not be found in the resources folder nor relative to the running application." +
                                "Try to specify a database configuration file (*.xml) via the program argument: --bigellor.cdo.config=<PATH/cdo-server.xml>");
                    }
                    System.out.println("No CDO database server configuration file 'cdo-server.xml' provided. Default will be populated ...");
                    cdoConfigFile = new File(dbConfig.toString());
                    FileUtils.copyInputStreamToFile(resource.getInputStream(), cdoConfigFile);
                    System.out.println("\tNew CDO configuration file: " + cdoConfigFile);
                } else {
                    System.out.println("Reading CDO server configuration from: " + cdoConfigFile.getAbsolutePath());
                }
//            server = new CDOStandaloneServer("repo1");
                server = new CDOStandaloneServer(cdoConfigFile);
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
