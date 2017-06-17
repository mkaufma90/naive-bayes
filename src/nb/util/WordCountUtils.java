package nb.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * 
 * Various utilities to help count words
 *
 */
public class WordCountUtils {

	
	/**
	 * Converts raw word counts into an Instance, so that it can be used by the
	 * NB classifier
	 * 
	 * @param wordCounts
	 *            , a map where keys are words and the values are counts
	 * @param label
	 *            , the label of the class that the word counts correspond to
	 * @return An Instance
	 */
	public static Instance wordCounts2Instance(Map<String, Integer> wordCounts,
			String label) {
		Set<Feature> feats = new HashSet<Feature>();
		for (Entry<String, Integer> wordCount : wordCounts.entrySet()) {
			feats.add(new Feature(wordCount.getKey(), wordCount.getValue()));
		}
		return new Instance(feats, label);
	}

	/**
	 * Converts a document into a map of word counts. This is necessary to
	 * compute unigram features for the NB classifier Note that a "word" is
	 * defined by splitting on spaces. This isn't a very good method of
	 * tokenization, but as long as the splitting method is consistent between
	 * training and testing, it shouldn't effect results too much, with the
	 * exception of languages in which words are not space-delimited (e.g.,
	 * Chinese)
	 * 
	 * @param doc
	 *            , the document to be processed
	 * @return A map containing the count of every word in the document
	 */
	public static Map<String, Integer> doc2WordCount(File doc) {
		Map<String, Integer> wordCounts = new HashMap<String, Integer>();
		try {
			BufferedReader b = new BufferedReader(new FileReader(doc));
			String line = "";
			while ((line = b.readLine()) != null) {
				String[] splitWords = line.split(" ");
				for (String word : splitWords) {

					// This is faster than checking if the map contains the
					// count, and then calling get.
					Integer count = wordCounts.get(word);
					if (count == null) {
						// This means that the map didn't contain the key
						count = 1;
					} else {
						count = count + 1;
					}
					wordCounts.put(word, count);
				}
			}
			b.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return wordCounts;
	}
}
