package com.ontimize.jee.common.services.reportstore;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * The Class BasicReportDefinition.
 */
public class BasicReportDefinition implements IReportDefinition, Serializable {

	/** The id. */
	private Object				id;

	/** The name. */
	private String				name;

	/** The description. */
	private String				description;

	/** The type. */
	private String				type;

	/** The main report file name. */
	private String				mainReportFileName;

	/** The other info. */
	private Map<String, String>	otherInfo;

	/**
	 * Instantiates a new basic report definition.
	 *
	 * @param id
	 *            the id
	 * @param name
	 *            the name
	 * @param description
	 *            the description
	 * @param type
	 *            the type
	 */
	public BasicReportDefinition(Object id, String name, String description, String type) {
		super();
		this.id = id;
		this.name = name;
		this.description = description;
		this.type = type;
		this.otherInfo = new HashMap<>();
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	@Override
	public Object getId() {
		return this.id;
	}

	/**
	 * Sets the id.
	 *
	 * @param id
	 *            the id to set
	 */
	public void setId(Object id) {
		this.id = id;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	@Override
	public String getName() {
		return this.name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	@Override
	public String getDescription() {
		return this.description;
	}

	/**
	 * Sets the description.
	 *
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	@Override
	public String getType() {
		return this.type;
	}

	/**
	 * Sets the type.
	 *
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/*
	 * (non-Javadoc)
	 * @see com.ontimize.jee.common.services.reportstore.IReportDefinition#getMainReportFileName()
	 */
	@Override
	public String getMainReportFileName() {
		return this.mainReportFileName;
	}

	/**
	 * Sets the main report file name.
	 *
	 * @param mainReportFileName
	 *            the new main report file name
	 */
	public void setMainReportFileName(String mainReportFileName) {
		this.mainReportFileName = mainReportFileName;
	}

	/**
	 * Gets the other info.
	 *
	 * @return the otherInfo
	 */
	@Override
	public Map<String, String> getOtherInfo() {
		return this.otherInfo;
	}

	/**
	 * Sets the other info.
	 *
	 * @param otherInfo
	 *            the otherInfo to set
	 */
	public void setOtherInfo(Map<String, String> otherInfo) {
		this.otherInfo = otherInfo;
	}

	/**
	 * Adds the other info.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	public void addOtherInfo(String key, String value) {
		if (this.otherInfo == null) {
			this.otherInfo = new HashMap<>();
		}
		this.otherInfo.put(key, value);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("id: %s, name: %s, description: %s", this.id, this.name, this.description);
	}

}
