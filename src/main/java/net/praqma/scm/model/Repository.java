package net.praqma.scm.model;

public class Repository {
	private String location;
	private String name = "";
	
	public Repository( String location, String name ) {
		this.name = name;
		this.location = location;
	}
	
	public String getLocation() {
		return location;
	}
	
	public String toString() {
		return location;
	}
}
