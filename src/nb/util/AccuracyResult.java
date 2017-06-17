package nb.util;

import java.util.HashMap;
import java.util.Map.Entry;

/**
 * This class makes it easier to print out a confusion matrix and accuracy if you have testing data (in the form of List<Instance>)
 */
public class AccuracyResult {


	public AccuracyResult(String actualLabel, String guessedLabel) {
		this.actualLabel = actualLabel;
		this.guessedLabel = guessedLabel;
	}
	
	private String actualLabel, guessedLabel;


	public String getActualLabel() {
		return actualLabel;
	}

	public void setActualLabel(String actualLabel) {
		this.actualLabel = actualLabel;
	}

	public String getGuessedLabel() {
		return guessedLabel;
	}

	public void setGuessedLabel(String guessedLabel) {
		this.guessedLabel = guessedLabel;
	}

	public boolean isCorrect() {
		if (actualLabel.equals(guessedLabel))
			return true;
		else
			return false;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((actualLabel == null) ? 0 : actualLabel.hashCode());
		result = prime * result
				+ ((guessedLabel == null) ? 0 : guessedLabel.hashCode());
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
		AccuracyResult other = (AccuracyResult) obj;
		if (actualLabel == null) {
			if (other.actualLabel != null)
				return false;
		} else if (!actualLabel.equals(other.actualLabel))
			return false;
		if (guessedLabel == null) {
			if (other.guessedLabel != null)
				return false;
		} else if (!guessedLabel.equals(other.guessedLabel))
			return false;
		return true;
	}

	public static double computeAccuracy(HashMap<AccuracyResult, Integer> results) {
		double correctAnswers = 0;
		double totalAnswers = 0;
		for (Entry<AccuracyResult, Integer> resultSet : results.entrySet()) {
			boolean correct = resultSet.getKey().isCorrect();
			int numAnswers = resultSet.getValue();
			totalAnswers += numAnswers;
			if (correct)
				correctAnswers += numAnswers;
		}
		return correctAnswers / totalAnswers;
	}
}
