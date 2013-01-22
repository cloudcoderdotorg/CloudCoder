package org.cloudcoder.app.shared.model;

import java.util.Date;

/**
 * Adapter class for editing an {@link IProblem}.
 * Subclasses must implement the {@link #onChange()} method,
 * which is called whenever the underlying IProblem object is
 * modified with a setter method.
 */
public abstract class EditProblemAdapter implements IProblem {
	private final IProblem delegate;

	/**
	 * Constructor.
	 * 
	 * @param delegate an IProblem object to which getter/setter calls will be delegated
	 */
	public EditProblemAdapter(IProblem delegate) {
		this.delegate = delegate;
	}

	/**
	 * Downcall method: is called whenever a setter method has been called
	 * and the IProblem object to which the adapter is delegating
	 * might have changed.
	 */
	protected abstract void onChange();

	@Override
	public void setProblemType(ProblemType problemType) {
		delegate.setProblemType(problemType);
		onChange();
	}

	@Override
	public void setProblemType(int problemType) {
		delegate.setProblemType(problemType);
		onChange();
	}

	@Override
	public ProblemType getProblemType() {
		return delegate.getProblemType();
	}

	@Override
	public String getTestname() {
		return delegate.getTestname();
	}

	@Override
	public void setTestname(String testName) {
		delegate.setTestname(testName);
		onChange();
	}

	@Override
	public void setBriefDescription(String briefDescription) {
		delegate.setBriefDescription(briefDescription);
		onChange();
	}

	@Override
	public String getBriefDescription() {
		return delegate.getBriefDescription();
	}

	@Override
	public String getDescription() {
		return delegate.getDescription();
	}

	@Override
	public void setDescription(String description) {
		delegate.setDescription(description);
		onChange();
	}

	@Override
	public void setSkeleton(String skeleton) {
		delegate.setSkeleton(skeleton);
		onChange();
	}

	@Override
	public String getSkeleton() {
		return delegate.getSkeleton();
	}

	@Override
	public void setSchemaVersion(int schemaVersion) {
		delegate.setSchemaVersion(schemaVersion);
		onChange();
	}

	@Override
	public int getSchemaVersion() {
		return delegate.getSchemaVersion();
	}

	@Override
	public void setAuthorName(String authorName) {
		delegate.setAuthorName(authorName);
		onChange();
	}

	@Override
	public String getAuthorName() {
		return delegate.getAuthorName();
	}

	@Override
	public void setAuthorEmail(String authorEmail) {
		delegate.setAuthorEmail(authorEmail);
		onChange();
	}

	@Override
	public String getAuthorEmail() {
		return delegate.getAuthorEmail();
	}

	@Override
	public void setAuthorWebsite(String authorWebsite) {
		delegate.setAuthorWebsite(authorWebsite);
		onChange();
	}

	@Override
	public String getAuthorWebsite() {
		return delegate.getAuthorWebsite();
	}

	@Override
	public void setTimestampUtc(long timestampUTC) {
		delegate.setTimestampUtc(timestampUTC);
		onChange();
	}

	@Override
	public long getTimestampUtc() {
		return delegate.getTimestampUtc();
	}

	@Override
	public void setLicense(ProblemLicense license) {
		delegate.setLicense(license);
		onChange();
	}

	@Override
	public ProblemLicense getLicense() {
		return delegate.getLicense();
	}
	
	@Override
	public void setParentHash(String parentHash) {
		delegate.setParentHash(parentHash);
		onChange();
	}
	
	@Override
	public String getParentHash() {
		return delegate.getParentHash();
	}

	@Override
	public Integer getProblemId() {
		return delegate.getProblemId();
	}

	@Override
	public void setProblemId(Integer id) {
		delegate.setProblemId(id);
		onChange();
	}

	@Override
	public Integer getCourseId() {
		return delegate.getCourseId();
	}

	@Override
	public void setCourseId(Integer courseId) {
		delegate.setCourseId(courseId);
		onChange();
	}

	@Override
	public long getWhenAssigned() {
		return delegate.getWhenAssigned();
	}

	@Override
	public Date getWhenAssignedAsDate() {
		return delegate.getWhenAssignedAsDate();
	}

	@Override
	public void setWhenAssigned(long whenAssigned) {
		delegate.setWhenAssigned(whenAssigned);
		onChange();
	}

	@Override
	public long getWhenDue() {
		return delegate.getWhenDue();
	}

	@Override
	public Date getWhenDueAsDate() {
		return delegate.getWhenDueAsDate();
	}

	@Override
	public void setWhenDue(long whenDue) {
		delegate.setWhenDue(whenDue);
		onChange();
	}

	@Override
	public void setVisible(boolean visible) {
		delegate.setVisible(visible);
		onChange();
	}

	@Override
	public boolean isVisible() {
		return delegate.isVisible();
	}
	
	@Override
	public void setProblemAuthorship(ProblemAuthorship problemAuthorship) {
		delegate.setProblemAuthorship(problemAuthorship);
		onChange();
	}
	
	@Override
	public ProblemAuthorship getProblemAuthorship() {
		return delegate.getProblemAuthorship();
	}
	
	@Override
	public void setDeleted(boolean deleted) {
		delegate.setDeleted(deleted);
		onChange();
	}
	
	@Override
	public boolean isDeleted() {
		return delegate.isDeleted();
	}
	
	@Override
	public void setModuleId(int moduleId) {
		delegate.setModuleId(moduleId);
		onChange();
	}
	
	@Override
	public int getModuleId() {
		return delegate.getModuleId();
	}

}
