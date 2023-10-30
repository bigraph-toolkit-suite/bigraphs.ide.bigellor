package de.tudresden.inf.st.bigraphs.editor.bigellor.domain;

import org.bigraphs.framework.core.ControlStatus;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicControl;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.LinkedList;
import java.util.List;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;

@Entity
public class SignatureEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotBlank(message = "Name is mandatory")
    private String name;

    //    @ElementCollection
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "id")
    List<ControlEntity> controlEntityList = new LinkedList<>();


    public SignatureEntity() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<ControlEntity> getControlEntityList() {
        return controlEntityList;
    }

    public void setControlEntityList(List<ControlEntity> controlEntityList) {
        this.controlEntityList = controlEntityList;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static SignatureEntity convert(DefaultDynamicSignature signature, String name) {
        SignatureEntity signatureEntity = new SignatureEntity();
        signatureEntity.setName(name);
        for (DefaultDynamicControl each : signature.getControls()) {
            ControlEntity controlEntity = new ControlEntity();
            controlEntity.setCtrlLbl(each.getNamedType().stringValue());
            controlEntity.setPortCnt(each.getArity().getValue());
            controlEntity.setStatus(ControlStatus.fromString(each.getControlKind().toString()));
            signatureEntity.getControlEntityList().add(controlEntity);
        }
        return signatureEntity;
    }

    public static DefaultDynamicSignature convert(SignatureEntity signatureEntity) {
        DynamicSignatureBuilder signatureBuilder = pureSignatureBuilder();
        for (int i = 0; i < signatureEntity.getControlEntityList().size(); i = i + 1) {
            ControlEntity controlEntity = signatureEntity.getControlEntityList().get(i);
            String name = controlEntity.getCtrlLbl();
            int arity = controlEntity.getPortCnt();
            ControlStatus status = controlEntity.getStatus();
            signatureBuilder.newControl().identifier(StringTypedName.of(name)).arity(FiniteOrdinal.ofInteger(arity))
                    .status(status)
                    .assign();
        }

        return signatureBuilder.create();
    }
}
