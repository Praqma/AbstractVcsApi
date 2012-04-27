package net.praqma.ava.model;

import java.io.Serializable;

public class Repository implements Serializable {

	private static final long serialVersionUID = 1610469854659236534L;

	private String location;
	
	/**
	 * Sometimes the name equals the branch name. This is the case with Git
	 */
	private String name = "";
	
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
	
	public String toString() {
		return "(" + name + ")" + location;
	}
}
