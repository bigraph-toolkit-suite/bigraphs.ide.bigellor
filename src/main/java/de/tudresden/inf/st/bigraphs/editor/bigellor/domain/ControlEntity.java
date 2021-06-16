package de.tudresden.inf.st.bigraphs.editor.bigellor.domain;

import de.tudresden.inf.st.bigraphs.core.ControlStatus;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class ControlEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long ctrlId;

    String ctrlLbl = "";
    int portCnt = 0;
    ControlStatus status;

    public ControlEntity() {
    }

    public long getCtrlId() {
        return ctrlId;
    }

    public void setCtrlId(long ctrlId) {
        this.ctrlId = ctrlId;
    }

    public String getCtrlLbl() {
        return ctrlLbl;
    }

    public void setCtrlLbl(String ctrlLbl) {
        this.ctrlLbl = ctrlLbl;
    }

    public int getPortCnt() {
        return portCnt;
    }

    public void setPortCnt(int portCnt) {
        this.portCnt = portCnt;
    }

    public ControlStatus getStatus() {
        return status;
    }

    public void setStatus(ControlStatus status) {
        this.status = status;
    }
}
