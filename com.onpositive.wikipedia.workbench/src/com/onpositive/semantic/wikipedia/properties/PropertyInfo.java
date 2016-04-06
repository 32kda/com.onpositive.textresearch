package com.onpositive.semantic.wikipedia.properties;

public class PropertyInfo {
	public final String templateName;
	public final String propertyName;
	public final Object source;

	public String getKey(){
		return templateName+"/"+propertyName;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isSimple ? 1231 : 1237);
		result = prime * result
				+ ((propertyName == null) ? 0 : propertyName.hashCode());
		result = prime * result
				+ ((propertyValue == null) ? 0 : propertyValue.hashCode());
		result = prime * result
				+ ((templateName == null) ? 0 : templateName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PropertyInfo other = (PropertyInfo) obj;
		if (isSimple != other.isSimple)
			return false;
		if (propertyName == null) {
			if (other.propertyName != null)
				return false;
		} else if (!propertyName.equals(other.propertyName))
			return false;
		if (propertyValue == null) {
			if (other.propertyValue != null)
				return false;
		} else if (!propertyValue.equals(other.propertyValue))
			return false;
		if (templateName == null) {
			if (other.templateName != null)
				return false;
		} else if (!templateName.equals(other.templateName))
			return false;
		return true;
	}

	public PropertyInfo(String templateName, String propertyName,
			String proprtyValue, boolean isSimple,Object source) {
		super();
		this.templateName = templateName.trim().toLowerCase();
		this.propertyName = propertyName.trim().toLowerCase();
		this.propertyValue = proprtyValue.trim();
		this.isSimple = isSimple;
		this.source=source;
	}

	public String propertyValue;
	public boolean isSimple;

	@Override
	public String toString() {
		return templateName + "/" + propertyName + "=" + propertyValue
				+ (!isSimple ? "*" : "");
	}
}