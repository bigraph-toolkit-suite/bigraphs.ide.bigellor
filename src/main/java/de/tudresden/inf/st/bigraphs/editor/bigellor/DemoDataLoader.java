package de.tudresden.inf.st.bigraphs.editor.bigellor;

import org.bigraphs.framework.core.BigraphFileModelManagement;
import org.bigraphs.framework.core.ControlStatus;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.editor.bigellor.domain.ControlEntity;
import de.tudresden.inf.st.bigraphs.editor.bigellor.domain.DomainUtils;
import de.tudresden.inf.st.bigraphs.editor.bigellor.domain.SignatureEntity;
import de.tudresden.inf.st.bigraphs.editor.bigellor.persistence.SignatureEntityRepository;
import de.tudresden.inf.st.bigraphs.editor.bigellor.service.CDOServerService;
import de.tudresden.inf.st.bigraphs.editor.bigellor.service.ProjectFileLocationService;
import de.tudresden.inf.st.bigraphs.editor.bigellor.service.SignatureFileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;

@Component
public class DemoDataLoader implements ApplicationRunner {

    boolean serverWasStarted = false;

    @Autowired
    private CDOServerService serverService;
    private final SignatureEntityRepository signatureEntityRepository;

    @Autowired
    ProjectFileLocationService projectFileLocationService;

    @Autowired
    SignatureFileStorageService signatureService;

    @Value("${bigellor.gen-test-data}")
    private boolean genTestData;

    @Value("${bigellor.model.storage.location:''}")
    private String modelStorageLocation;

    @Value("${bigellor.cdo.embedded}")
    private boolean useEmbeddedCdoServer;

    @Autowired
    public DemoDataLoader(SignatureEntityRepository signatureEntityRepository) {
        this.signatureEntityRepository = signatureEntityRepository;
    }

    public void run(ApplicationArguments args) {
        if (genTestData) {
            genTestData();
        }

        if (!serverWasStarted && useEmbeddedCdoServer) {
            serverService.startServer();
            serverWasStarted = true;
        }

        try {
            signatureService.initialize();
            projectFileLocationService.initProjects();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void genTestData() {
        SignatureEntity signatureEntity = new SignatureEntity();
        signatureEntity.setName(DomainUtils.createPlaceholderName());
        ControlEntity controlEntity = new ControlEntity();
        controlEntity.setCtrlLbl("User");
        controlEntity.setPortCnt(3);
        controlEntity.setStatus(ControlStatus.ACTIVE);
        signatureEntity.getControlEntityList().add(controlEntity);

        ControlEntity controlEntity2 = new ControlEntity();
        controlEntity2.setCtrlLbl("PC");
        controlEntity2.setPortCnt(3);
        controlEntity2.setStatus(ControlStatus.ACTIVE);
        signatureEntity.getControlEntityList().add(controlEntity2);

        ControlEntity controlEntity3 = new ControlEntity();
        controlEntity3.setCtrlLbl("Room");
        controlEntity3.setPortCnt(3);
        controlEntity3.setStatus(ControlStatus.ACTIVE);
        signatureEntity.getControlEntityList().add(controlEntity3);

        try {
            String resourceDataDir = Paths.get("data/signatures/").toAbsolutePath().toString();
            DefaultDynamicSignature convert = SignatureEntity.convert(signatureEntity);
            BigraphFileModelManagement.Store.exportAsInstanceModel(convert, new FileOutputStream(Paths.get(resourceDataDir, "smarthome.xmi").toFile()), "smarthome.ecore");
            BigraphFileModelManagement.Store.exportAsMetaModel(convert, new FileOutputStream(Paths.get(resourceDataDir, "smarthome.ecore").toFile()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.signatureEntityRepository.save(signatureEntity);
    }
}
