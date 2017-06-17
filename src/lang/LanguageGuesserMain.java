package lang;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

import nb.MultinomialNB;
import nb.util.Instance;
import nb.util.WordCountUtils;

/**
 * This class is responsible for guessing the language of an input text file
 * 
 */
public class LanguageGuesserMain {

	public static void main(String[] args) {
		// String modelFile = "/home/max/temp/assignment/model.nb";
		// String docFile =
		// "/local/corpora/europarl_raw/et/ep-11-11-16-006-07.txt";
		if (args.length == 2) {
			String modelFile = args[0];
			String docFile = args[1];
			System.out.println("Loading model....");
			MultinomialNB nb = MultinomialNB.load(modelFile);
			System.out.println("Evaluating...");
			Entry<String, Double> bestLang = evaluate(nb, new File(docFile));
			System.out.println(
					"The best language is: " + bestLang.getKey() + " with probability: " + bestLang.getValue());
		} else {
			System.err.println(
					"Improper number or arguments. First argument must be a model file trained by EuroparlTrainer, and second must be a text file containing text in one language");
			System.exit(0);
		}

	}

	/**
	 * Returns the best language for a given document
	 * 
	 * @param trainedNB
	 *            , a trained Multinomial Nb
	 * @param document
	 *            , the document to be evaluated
	 * @return the best language for the document, with its probability
	 */
	public static Entry<String, Double> evaluate(MultinomialNB trainedNB, File document) {
		Map<String, Integer> wordCounts = WordCountUtils.doc2WordCount(document);
		Instance instance = WordCountUtils.wordCounts2Instance(wordCounts, "");
		Entry<String, Double> bestLang = trainedNB.findBestClass(instance, false);
		return bestLang;
	}
}
