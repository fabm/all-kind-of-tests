package pt.fabm;

import groovy.lang.Script;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.Element;
import java.util.concurrent.ConcurrentHashMap;

public class KeyspaceGeneration {

    private static ConcurrentHashMap<String, KeyspaceGeneration> map = new ConcurrentHashMap<>();

    private Script script;
    private String name;

    public KeyspaceGeneration(String name) {
        this.name = name;
    }

    public KeyspaceGeneration getKeyspaceGeneration(Element element) {
        Entity entity = element.getAnnotation(Entity.class);
        return map.computeIfAbsent(entity.keyspace(), KeyspaceGeneration::new);
    }

    public Script getScript() {
        return script;
    }

    public void setScript(Script script) {
        this.script = script;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
