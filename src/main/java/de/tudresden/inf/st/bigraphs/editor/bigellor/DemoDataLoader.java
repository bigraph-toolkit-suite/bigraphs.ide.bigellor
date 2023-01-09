package de.tudresden.inf.st.bigraphs.editor.bigellor;

import de.tudresden.inf.st.bigraphs.core.BigraphFileModelManagement;
import de.tudresden.inf.st.bigraphs.core.ControlStatus;
import de.tudresden.inf.st.bigraphs.core.impl.signature.DefaultDynamicSignature;
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

    @Value("${bigellor.cdo.embedded}")
    private boolean useEmbeddedCdoServer;

    @Autowired
    public DemoDataLoader(SignatureEntityRepository signatureEntityRepository) {
        this.signatureEntityRepository = signatureEntityRepository;
    }

    public void run(ApplicationArguments args) {
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

        if (genTestData) {
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
                DefaultDynamicSignature convert = SignatureEntity.convert(signatureEntity);
                BigraphFileModelManagement.Store.exportAsInstanceModel(convert, new FileOutputStream("smarthome.xmi"), "x");
                BigraphFileModelManagement.Store.exportAsMetaModel(convert, new FileOutputStream("smarthome.ecore"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            this.signatureEntityRepository.save(signatureEntity);
        }
    }
}