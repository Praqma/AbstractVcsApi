package net.praqma.vcs.model;

public class Repository {
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
