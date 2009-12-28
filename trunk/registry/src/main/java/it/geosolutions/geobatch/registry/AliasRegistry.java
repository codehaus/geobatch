/**
 * 
 */
package it.geosolutions.geobatch.registry;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 */
public class AliasRegistry implements Iterable<Map.Entry<String, Class>>{

    private Map<String, Class> alias = new HashMap<String, Class>();

    public AliasRegistry() {
    }

    public int size() {
        return alias.size();
    }

    public void putAlias(String name, Class clazz) {
        System.out.println("Adding alias " + name + " for class " + clazz.getSimpleName());
        alias.put(name, clazz);
    }

    public Iterator<Entry<String, Class>> iterator() {
        return alias.entrySet().iterator();
    }
}
