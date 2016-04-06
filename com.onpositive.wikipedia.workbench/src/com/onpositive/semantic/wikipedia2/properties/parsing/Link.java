package com.onpositive.semantic.wikipedia2.properties.parsing;

import com.onpositive.semantic.wikipedia2.WikiDoc;

public class Link {

	public final String caption;
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((caption == null) ? 0 : caption.hashCode());
		result = prime * result
				+ ((document == null) ? 0 : document.hashCode());
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
		Link other = (Link) obj;
		if (caption == null) {
			if (other.caption != null)
				return false;
		} else if (!caption.equals(other.caption))
			return false;
		if (document == null) {
			if (other.document != null)
				return false;
		} else if (!document.equals(other.document))
			return false;
		return true;
	}
	public final WikiDoc document;
	public Link(String caption, WikiDoc document) {
		super();
		this.caption = caption;
		this.document = document;
	}

	@Override
	public String toString() {
		return caption.toString();
	}
}
