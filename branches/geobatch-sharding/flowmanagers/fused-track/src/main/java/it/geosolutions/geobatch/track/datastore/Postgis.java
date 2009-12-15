package it.geosolutions.geobatch.track.datastore;

import java.util.Map;

public class Postgis {

    Class<?> factoryClass;
    Map params;
    String shardingSystemTable;

    
    /**
     * @return the factoryClass
     */
    public Class<?> getFactoryClass() {
        return factoryClass;
    }

    /**
     * @param factoryClass the factoryClass to set
     */
    public void setFactoryClass(Class<?> factoryClass) {
        this.factoryClass = factoryClass;
    }

    /**
     * @return the params
     */
    public Map getParams() {
        return params;
    }

    /**
     * @param params
     *            the params to set
     */
    public void setParams(Map params) {
        this.params = params;
    }

    /**
     * @return the shardingSystemTable
     */
    public String getShardingSystemTable() {
        return shardingSystemTable;
    }

    /**
     * @param shardingSystemTable
     *            the shardingSystemTable to set
     */
    public void setShardingSystemTable(String shardingSystemTable) {
        this.shardingSystemTable = shardingSystemTable;
    }

}
