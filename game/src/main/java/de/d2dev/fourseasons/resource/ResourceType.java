package de.d2dev.fourseasons.resource;

/**
 * Resource types are to be represented by singleton classes.
 * @author Sebastian Bordt
 *
 */
public abstract class ResourceType {
	
	private String typename;
	
	protected ResourceType(String typename) {
		this.typename = typename;
	}
	
	public String getTypeName() {
		return this.typename;
	}
	
	public boolean isType(String s) {
		return s.equals(typename);
	}
	
	public boolean isType(ResourceType p) {
		return p.typename.equals(typename);
	}
	
	public boolean isSameType(Resource r) {
		return r.getType().typename.equals(typename);
	}
}
