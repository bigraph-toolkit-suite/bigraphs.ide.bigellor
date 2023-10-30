package de.tudresden.inf.st.bigraphs.editor.bigellor.domain;

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;

import javax.persistence.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

//@Entity
public class SimulationSpecification {
//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    protected Signature<?> signature;
    protected Bigraph<Signature<?>> agent;
    protected Map<String, ReactionRule<Bigraph<Signature<?>>>> ruleMap;

    public SimulationSpecification() {
    }

    public SimulationSpecification(Signature<?> signature, Bigraph<Signature<?>> agent) {
        this(signature, agent, new LinkedHashMap<>());
    }

    public SimulationSpecification(Signature<?> signature, Bigraph<Signature<?>> agent, Map<String, ReactionRule<Bigraph<Signature<?>>>> ruleMap) {
        this.signature = signature;
        this.agent = agent;
        this.ruleMap = ruleMap;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Signature<?> getSignature() {
        return signature;
    }

    public void setSignature(Signature<?> signature) {
        this.signature = signature;
    }

    public Bigraph<Signature<?>> getAgent() {
        return agent;
    }

    public void setAgent(Bigraph<Signature<?>> agent) {
        this.agent = agent;
    }

    public Map<String, ReactionRule<Bigraph<Signature<?>>>> getRuleMap() {
        return ruleMap;
    }

    public void setRuleMap(Map<String, ReactionRule<Bigraph<Signature<?>>>> ruleMap) {
        this.ruleMap = ruleMap;
    }
}
