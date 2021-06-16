package de.tudresden.inf.st.bigraphs.editor.bigellor;

import de.tudresden.inf.st.bigraphs.core.ControlStatus;
import de.tudresden.inf.st.bigraphs.editor.bigellor.domain.ControlEntity;
import de.tudresden.inf.st.bigraphs.editor.bigellor.domain.DomainUtils;
import de.tudresden.inf.st.bigraphs.editor.bigellor.domain.SignatureEntity;
import de.tudresden.inf.st.bigraphs.editor.bigellor.persistence.SignatureEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DemoDataLoader implements ApplicationRunner {

    boolean serverWasStarted = false;

    @Autowired
    private CDOServerService serverService;
    private final SignatureEntityRepository signatureEntityRepository;

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

            this.signatureEntityRepository.save(signatureEntity);
        }
    }
}