package de.tudresden.inf.st.bigraphs.editor.bigellor.domain;

import de.tudresden.inf.st.bigraphs.core.ControlStatus;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.persistence.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.pureSignatureBuilder;

@Entity
public class SignatureEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    //    @NotBlank(message = "Name is mandatory")
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
        for (DefaultDynamicControl each: signature.getControls()) {
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
