package net.praqma.vcs.util.configuration;

public class Configuration {
	private AbstractConfiguration sourceConfiguration;
	private AbstractConfiguration targetConfiguration;
	
	public AbstractConfiguration getSourceConfiguration() {
		return sourceConfiguration;
	}
	
	public void setSourceConfiguration( AbstractConfiguration tourceConfiguration ) {
		this.sourceConfiguration = tourceConfiguration;
	}
	
	public AbstractConfiguration getTargetConfiguration() {
		return targetConfiguration;
	}
	
	public void setTargetConfiguration( AbstractConfiguration targetConfiguration ) {
		this.targetConfiguration = targetConfiguration;
	}
	
	public String toString() {
		return "Source configuration: " + sourceConfiguration.toString() + "\nTarget configuration: " + targetConfiguration.toString();
	}
}
