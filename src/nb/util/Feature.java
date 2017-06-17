package nb.util;

import java.io.Serializable;


/**
 * Simply represents a feature and an associated value.
 * Values are hardcoded to be integers, since this class was designed to represent word counts in a document
 * @TODO: Rewrite with generic values
 */
public class Feature implements Serializable{

	private static final long serialVersionUID = 1L;

	public Feature(String word, Integer value) {
		this.word = word;
		this.value = value;
	}
	
	/**
	 * A feature
	 */
	private String word;
	
	/**
	 * A value associated with the feature
	 */
	private Integer value;
	
	
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public Integer getValue() {
		return value;
	}
	public void setValue(Integer value) {
		this.value = value;
	}
	
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		result = prime * result + ((word == null) ? 0 : word.hashCode());
		return result;
	}
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Feature other = (Feature) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		if (word == null) {
			if (other.word != null)
				return false;
		} else if (!word.equals(other.word))
			return false;
		return true;
	}
	
}
