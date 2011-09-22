package net.praqma.vcs.persistence;

import java.util.Date;

public interface PersistenceStrategy {
	public void setLastCommitDate( Date date );
	public Date getLastCommitDate();
	public void save();
}
