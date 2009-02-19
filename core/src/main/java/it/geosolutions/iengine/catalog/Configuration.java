package it.geosolutions.iengine.catalog;

public interface Configuration extends Identifiable {

    public boolean isDirty();

    public void setDirty(boolean dirty);

    /**
     * @return the serviceID
     */
    public String getServiceID();

    /**
     * @param serviceID
     *            the serviceID to set
     */
    public void setServiceID(String serviceID);

}