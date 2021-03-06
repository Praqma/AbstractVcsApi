package net.praqma.vcs.model;

import java.io.Serializable;

public class Repository implements Serializable {

	private static final long serialVersionUID = 1610469854659236534L;

	private String location;
	
	/**
	 * Sometimes the name equals the branch name. This is the case with Git
	 */
	private String name = "";
	
    /**
     * example: origin https://some.repo.git
     * @param location
     * @param name 
     */
	public Repository( String location, String name ) {
		this.name = name;
		this.location = location;
	}
	
	public String getLocation() {
		return location;
	}
	
	public String getName() {
		return name;
	}
    
	@Override
	public String toString() {
		return "(" + name + ")" + location;
	}
}
