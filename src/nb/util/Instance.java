package nb.util;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a label associated with data. Very similar to a mallet training
 * instance
 */
public class Instance implements Serializable {
	
	private static final long serialVersionUID = 1L;

	/**
	 * The data
	 */
	private Set<Feature> features = new HashSet<Feature>();

	/**
	 * The class to which the data belongs
	 */
	private String label;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Set<Feature> getFeatures() {
		return features;
	}

	public void setFeatures(Set<Feature> features) {
		this.features = features;
	}

	public Instance(Set<Feature> features, String label) {
		this.features = features;
		this.label = label;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((features == null) ? 0 : features.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Instance other = (Instance) obj;
		if (features == null) {
			if (other.features != null)
				return false;
		} else if (!features.equals(other.features))
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		return true;
	}

}