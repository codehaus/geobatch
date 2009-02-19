package it.geosolutions.iengine.catalog;

public interface Resource extends Identifiable {

    /**
	 *
	 */
    public void dispose();

    /**
	 *
	 */
    public Catalog getCatalog();

    public void setCatalog(Catalog catalog);

}
