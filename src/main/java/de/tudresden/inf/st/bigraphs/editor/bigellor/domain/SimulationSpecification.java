package de.tudresden.inf.st.bigraphs.editor.bigellor.domain;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.ReactionRule;

import javax.persistence.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class SimulationSpecification {

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
