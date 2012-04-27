package net.praqma.ava.persistence;

import java.util.Date;

import net.praqma.ava.model.AbstractBranch;

public interface PersistenceStrategy {
	public void setLastCommitDate( AbstractBranch branch, Date date );
	public Date getLastCommitDate( AbstractBranch branch );
	public void save();
}
