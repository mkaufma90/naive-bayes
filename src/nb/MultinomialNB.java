package nb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import nb.util.AccuracyResult;
import nb.util.Feature;
import nb.util.Instance;
import nb.util.Serializer;
import nb.util.ValueComparator;

import java.util.Set;
import java.util.TreeMap;


public class MultinomialNB implements Serializable {

	// **************CONFIG**************
	// These are configurable through calling MultinomialNB(String[] args)
	private transient String TRAINING_FILE, TESTING_FILE, MODEL_FILE, SYS_FILE;
	private double CLASS_PRIOR_DELTA, COND_PROB_DELTA;
	private static final long serialVersionUID = 1L;

	// **************END CONFIG**************

	// **************CLASS VARIABLES**************
	private transient List<Instance> trainingInstances = new ArrayList<Instance>();
	private transient List<Instance> testingInstances = new ArrayList<Instance>();
	private transient Set<String> allFeatures = new HashSet<String>();
	private transient Set<String> allLabels = new HashSet<String>();
	private Integer vocabSize = 0;
	private transient HashMap<String, HashMap<String, Integer>> wordCountsForClasses = new HashMap<String, HashMap<String, Integer>>();
	private transient HashMap<String, Integer> wordsInClasses = new HashMap<String, Integer>();
	private  HashMap<String, HashMap<String, Double>> wordProbsForClasses = new HashMap<String, HashMap<String, Double>>();
	private  HashMap<String, Integer> classCounts = new HashMap<String, Integer>();
	private  HashMap<String, Double> classProbs = new HashMap<String, Double>();
	private  HashMap<String, Double> missingWordProbs = new HashMap<String, Double>();
	private transient final DecimalFormat df = new DecimalFormat("#.####");

	// **************END CLASS VARIABLES***********

	/**
	 * For use when calling from the command line. Does not actually train or
	 * evaluate a model, just loads in training and testing data. To use,
	 * manually call either trainMultinomial() or trainBernoulli() You can also
	 * use computeAccuracy() to evaluate on a set of testing instances.
	 */
	public MultinomialNB(String[] args) {
		TRAINING_FILE = args[0];
		TESTING_FILE = args[1];
		CLASS_PRIOR_DELTA = Double.parseDouble(args[2]);
		COND_PROB_DELTA = Double.parseDouble(args[3]);
		MODEL_FILE = args[4];
		SYS_FILE = args[5];
		load();
	}

	/**
	 * For programmatic use. You will need to manually call
	 * setTrainingInstances() and either trainMultinomial() or trainBernoulli()
	 */
	public MultinomialNB() {
		CLASS_PRIOR_DELTA = 0;
		COND_PROB_DELTA = 1;
	}

	public List<Instance> getTrainingInstances() {
		return trainingInstances;
	}

	public void setTrainingInstances(List<Instance> trainingInstances) {
		for (Instance inst : trainingInstances) {
			allLabels.add(inst.getLabel());
			for (Feature f : inst.getFeatures()) {
				allFeatures.add(f.getWord());
			}
		}
		this.trainingInstances = trainingInstances;

	}

	public List<Instance> getTestingInstances() {
		return testingInstances;
	}

	public void setTestingInstances(List<Instance> testingInstances) {
		this.testingInstances = testingInstances;
	}

	/**
	 * Loads testing instances, assuming TESTING_FILE is not null. Assumes that
	 * TESTING_FILE is formatted for mallet.
	 */
	private void loadTesting() {
		try {
			BufferedReader b = new BufferedReader(new FileReader(TESTING_FILE));
			String line = "";
			while ((line = b.readLine()) != null) {
				String[] sects = line.split(" ");
				String label = null;
				Set<Feature> feats = new HashSet<Feature>();
				for (String feat : sects) {
					String[] featVal = feat.split(":");
					if (featVal.length == 2) {
						String feature = featVal[0];
						Integer val = Integer.parseInt(featVal[1]);
						feats.add(new Feature(feature, val));
					} else if (featVal.length == 1) {
						label = featVal[0];
					}
				}
				if (label != null && !feats.isEmpty()) {
					testingInstances.add(new Instance(feats, label));
				}

			}
			b.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void computeRightHandSideBernoulli() {
		for (String label : allLabels) {
			HashMap<String, Double> wordProbsForClass = wordProbsForClasses
					.get(label);
			double totalProb = 1;
			for (String word : allFeatures) {
				totalProb = totalProb * (1 - wordProbsForClass.get(word));
			}
			missingWordProbs.put(label, totalProb);
		}
	}

	public HashMap<String, Double> evaluateBernoulli(Instance i) {
		HashMap<String, Double> probs = new HashMap<String, Double>();
		double cumulativeProb = 0;

		for (Entry<String, Double> e : classProbs.entrySet()) {
			String label = e.getKey();

			// Constant for each class
			Double classProb = Math.log10(e.getValue());
			Double rhs = missingWordProbs.get(label);

			HashMap<String, Double> wordProbs = wordProbsForClasses.get(label);

			double cumulativeWordProbs = 0;
			for (Feature wordWithCount : i.getFeatures()) {
				String testWord = wordWithCount.getWord();
				Double wordProb = wordProbs.get(testWord);
				if (wordProb != null) {
					Double numerator = Math.log10(wordProb);
					Double denominator = Math.log10(1 - wordProb);
					Double finalWordProb = numerator / denominator;

					cumulativeWordProbs += finalWordProb;
				}
			}
			double finalClassProb = classProb + rhs + cumulativeWordProbs;
			cumulativeProb += finalClassProb;
			probs.put(label, finalClassProb);
		}

		HashMap<String, Double> newProbs = new HashMap<String, Double>();
		for (Entry<String, Double> e : probs.entrySet()) {
			newProbs.put(e.getKey(), (e.getValue() / cumulativeProb));
		}
		return newProbs;
	}

	public HashMap<String, Double> evaluateMultinomial(Instance i) {
		HashMap<String, Double> probs = new HashMap<String, Double>();
		double cumulativeProb = 0;
		for (Entry<String, Double> e : classProbs.entrySet()) {
			String label = e.getKey();
			Double classProb = Math.log10(e.getValue());

			HashMap<String, Double> wordProbs = wordProbsForClasses.get(label);

			double cumulativeWordProbs = 0;
			for (Feature wordWithCount : i.getFeatures()) {
				String testWord = wordWithCount.getWord();
				Integer testWordCount = wordWithCount.getValue();
				Double wordProb = wordProbs.get(testWord);
				if (wordProb != null) {
					wordProb = Math.log10(wordProb);
					cumulativeWordProbs += (double) testWordCount * wordProb;
				}
			}
			double finalClassProb = classProb + cumulativeWordProbs;
			cumulativeProb += finalClassProb;
			probs.put(label, finalClassProb);

		}

		HashMap<String, Double> newProbs = new HashMap<String, Double>();
		for (Entry<String, Double> e : probs.entrySet()) {
			newProbs.put(e.getKey(), (e.getValue() / cumulativeProb));
		}
		return newProbs;
	}

	/**
	 * Loads training instances, assuming TRAINING_FILE is not null. Assumes
	 * that TRAINING_FILE is formatted for mallet.
	 */
	private void loadTraining() {
		try {
			BufferedReader b = new BufferedReader(new FileReader(TRAINING_FILE));
			String line = "";
			while ((line = b.readLine()) != null) {
				String[] sects = line.split(" ");
				String label = null;

				Set<Feature> feats = new HashSet<Feature>();
				for (String feat : sects) {
					String[] featVal = feat.split(":");
					if (featVal.length == 2) {
						String feature = featVal[0];
						Integer val = Integer.parseInt(featVal[1]);
						feats.add(new Feature(feature, val));
						allFeatures.add(feature);
					} else if (featVal.length == 1) {
						label = featVal[0];
						allLabels.add(label);
					}
				}
				trainingInstances.add(new Instance(feats, label));
			}
			vocabSize = allFeatures.size();
b.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Wrapper to load all data
	 */
	private void load() {
		loadTraining();
		loadTesting();
	}

	/**
	 * Updates the word count for a given class
	 * 
	 * @param label
	 *            , the class
	 * @param word
	 *            , the word
	 * @param countToAdd
	 *            , the amount to *add* to the current count for (word|class)
	 */
	private void updateWordCounts(String label, String word, Integer countToAdd) {
		HashMap<String, Integer> wordCounts = wordCountsForClasses.get(label);
		if (wordCounts == null) {
			wordCounts = new HashMap<String, Integer>();
		}
		Integer oldCount = wordCounts.get(word);
		if (oldCount == null) {
			wordCounts.put(word, countToAdd);
		} else {
			wordCounts.put(word, oldCount + countToAdd);
		}
		wordCountsForClasses.put(label, wordCounts);
	}

	/**
	 * Updates wordsInClasses, which keeps track of the vocab size of each class
	 * 
	 * @param label
	 *            , the class
	 * @param countToAdd
	 *            , the amount to *add* to the current count for label
	 */
	private void updateWordsInClass(String label, Integer countToAdd) {
		Integer oldCount = wordsInClasses.get(label);
		if (oldCount == null) {
			wordsInClasses.put(label, countToAdd);
		} else {
			wordsInClasses.put(label, oldCount + countToAdd);
		}
	}

	/**
	 * Computes the probability of each class. Uses CLASS_PRIOR_DELTA for
	 * smoothing
	 */
	private void computeClassProbs() {
		int numInstances = trainingInstances.size();
		for (Entry<String, Integer> e : classCounts.entrySet()) {
			double rawProb = (double) ((double) e.getValue() / (double) numInstances);
			classProbs.put(e.getKey(), CLASS_PRIOR_DELTA + rawProb);
		}
	}

	/**
	 * Updates the number of instances for each class
	 * 
	 * @param label
	 *            , the class
	 * @param countToAdd
	 *            , the amount to *add* to the current count for the class
	 */
	private void updateClassCounts(String label, Integer countToAdd) {
		Integer oldCount = classCounts.get(label);
		if (oldCount == null) {
			classCounts.put(label, countToAdd);
		} else {
			classCounts.put(label, oldCount + countToAdd);
		}
	}

	/**
	 * Computes the probabilty for a every word seen in training data, assuming
	 * you are training a multinomial NB Uses COND_PROB_DELTA for smoothing
	 */
	private void computeWordProbsMultinomial() {
		for (String label : allLabels) {
			HashMap<String, Double> wordProbsForClass = new HashMap<String, Double>();
			HashMap<String, Integer> wordCountsForClass = wordCountsForClasses
					.get(label);

			// Used to normalize
			double denominator = vocabSize + wordsInClasses.get(label);

			// loop over every word
			for (String word : allFeatures) {
				Integer wordCount = wordCountsForClass.get(word);
				if (wordCount == null) {
					// this means we haven't seen the word
					wordCount = 0;
				}
				double numerator = COND_PROB_DELTA + wordCount;
				double prob = numerator / denominator;
				wordProbsForClass.put(word, prob);
			}

			wordProbsForClasses.put(label, wordProbsForClass);
		}
	}

	/**
	 * Computes the probabilty for a every word seen in training data, assuming
	 * you are training a Bernoulli NB Uses COND_PROB_DELTA for smoothing
	 */
	private void computeWordProbsBernoulli() {
		for (Entry<String, HashMap<String, Integer>> wordCountForClass : wordCountsForClasses
				.entrySet()) {
			String label = wordCountForClass.getKey();
			Integer numDocsForClass = classCounts.get(label);
			HashMap<String, Integer> wordCounts = wordCountForClass.getValue();
			HashMap<String, Double> wordProbs = new HashMap<String, Double>();

			for (String word : allFeatures) {
				Integer countForClass = wordCounts.get(word);
				if (countForClass == null)
					countForClass = 0;
				double numerator = (double) countForClass + COND_PROB_DELTA;
				double denominator = (double) numDocsForClass
						+ (2 * COND_PROB_DELTA);
				double prob = (double) (numerator / denominator);
				wordProbs.put(word, prob);
			}
			wordProbsForClasses.put(label, wordProbs);

		}
	}

	/**
	 * Trains a multinomial NB, assuming trainingInstances is not null
	 */
	public void trainMultinomial() {
		for (Instance inst : trainingInstances) {
			String label = inst.getLabel();
			Set<Feature> words = inst.getFeatures();
			updateClassCounts(label, 1);
			for (Feature wordWithCount : words) {
				String word = wordWithCount.getWord();
				Integer count = wordWithCount.getValue();
				updateWordCounts(label, word, count);
				updateWordsInClass(label, count);
			}
		}
		computeClassProbs();
		computeWordProbsMultinomial();
	}

	/**
	 * Trains a Bernoulli NB, assuming trainingInstances is not null
	 */
	public void trainBernoulli() {
		for (Instance inst : trainingInstances) {
			String label = inst.getLabel();
			Set<Feature> words = inst.getFeatures();
			updateClassCounts(label, 1);
			for (Feature wordWithCount : words) {
				String word = wordWithCount.getWord();
				Integer count = wordWithCount.getValue();
				updateWordCounts(label, word, 1);
				updateWordsInClass(label, count);
			}
		}

		// precompute various values
		computeClassProbs();
		computeWordProbsBernoulli();
		computeRightHandSideBernoulli();

	}

	/**
	 * Finds the best class for a given word
	 * 
	 * @param probs
	 *            , a map where the keys are classes, and the values are the
	 *            probability of (class|word)
	 * @return The most probable class
	 */
	public String findBestClass(HashMap<String, Double> probs) {
		Double highest = null;
		String bestClass = null;
		for (Entry<String, Double> e : probs.entrySet()) {
			if (highest == null) {
				highest = e.getValue();
				bestClass = e.getKey();
			} else if (e.getValue() < highest) {
				highest = e.getValue();
				bestClass = e.getKey();
			}
		}
		return bestClass;
	}

	public Entry<String, Double> findBestClass(Instance i, boolean bernoulli) {
		HashMap<String, Double> probs;
		if (bernoulli) {
			probs = evaluateBernoulli(i);
		} else {
			probs = evaluateMultinomial(i);
		}
		Double highest = null;
		Entry<String, Double> bestClass = null;
		for (Entry<String, Double> e : probs.entrySet()) {
			if (highest == null) {
				highest = e.getValue();
				bestClass = e;
			} else if (e.getValue() < highest) {
				highest = e.getValue();
				bestClass = e;
			}
		}
		return bestClass;
	}

	/**
	 * Evaluates the trained model on a set of instances, and prints the results
	 * to stdout
	 * 
	 * @param instances
	 *            the instances to evaluate on
	 * @param printTable
	 *            whether to print a confusion matrix to stdout
	 * @param bernoulli
	 *            . If true, will evaluate assuming that a bernoulli model was
	 *            trained (aka trainBernoulli() was called) If false, will
	 *            evaluate assuming that a multinomial model was trained (aka
	 *            trainMultinomial() was called)
	 * @return a double representing the accuracy of the classification results
	 */
	public double computeAccuracy(List<Instance> instances, boolean printTable,
			boolean bernoulli) {
		HashMap<AccuracyResult, Integer> results = new HashMap<AccuracyResult, Integer>();
		List<String> allLabels = new ArrayList<String>();
		for (Instance instance : instances) {
			String actualLabel = instance.getLabel();
			String guessedLabel;
			if (bernoulli)
				guessedLabel = findBestClass(evaluateBernoulli(instance));
			else
				guessedLabel = findBestClass(evaluateMultinomial(instance));
			if (!allLabels.contains(actualLabel))
				allLabels.add(actualLabel);

			AccuracyResult key = new AccuracyResult(actualLabel, guessedLabel);
			Integer count = results.get(key);
			if (count == null) {
				results.put(key, 1);
			} else {
				results.put(key, count + 1);

			}
		}
		double acc = AccuracyResult.computeAccuracy(results);
		if (printTable) {
			System.out.print("\t\t\t");
			for (String s : allLabels) {
				System.out.print(s + "\t");
			}
			System.out.println();
			for (String a : allLabels) {
				System.out.print(a + "\t");
				for (String g : allLabels) {
					Integer count = results.get(new AccuracyResult(a, g));
					if (count == null)
						count = 0;
					System.out.print(count + "\t\t\t");
				}
				System.out.println();
			}
			System.out.println("Accuracy:" + df.format(acc));
		}
		return acc;

	}

	/**
	 * Prints out the model file, assuming MODEL_FILE is not null, and that a
	 * model was trained. The model file contains the probability of each class,
	 * and the probability of each training AND testing instance.
	 * 
	 * @param bernoulli
	 *            . If true, will evaluate assuming that a bernoulli model was
	 *            trained (aka trainBernoulli() was called) If false, will
	 *            evaluate assuming that a multinomial model was trained (aka
	 *            trainMultinomial() was called)
	 */
	public void printModel(boolean bernoulli) {
		try {
			PrintWriter w = new PrintWriter(
					new FileWriter(new File(MODEL_FILE)));
			w.println("%%%%% prior prob P(c) %%%%%");
			for (Entry<String, Double> classProb : classProbs.entrySet()) {
				w.println(classProb.getKey() + " " + classProb.getValue() + " "
						+ Math.log10(classProb.getValue()));
			}

			w.println("%%%%% conditional prob P(f|c) %%%%%");

			for (Entry<String, HashMap<String, Double>> e : wordProbsForClasses
					.entrySet()) {
				String label = e.getKey();
				w.println("%%%%% conditional prob P(f|c) c=" + label + " %%%%%");
				for (Entry<String, Double> wordProb : e.getValue().entrySet()) {
					String word = wordProb.getKey();
					double rawProb = wordProb.getValue();
					w.println(word + " " + label + " " + rawProb + " "
							+ Math.log10(rawProb));
				}
			}
			w.flush();
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Prints out the sys file, assuming SYS_FILE is not null, and that a model
	 * was trained. The sys file contains the probability of each class for each
	 * training and testing instance.
	 * 
	 * @param bernoulli
	 *            . If true, will evaluate assuming that a bernoulli model was
	 *            trained (aka trainBernoulli() was called) If false, will
	 *            evaluate assuming that a multinomial model was trained (aka
	 *            trainMultinomial() was called)
	 */
	public void printSys(boolean bernoulli) {
		try {
			PrintWriter w = new PrintWriter(new FileWriter(new File(SYS_FILE)));

			w.println("%%%%% training data:");
			for (int i = 0; i < trainingInstances.size(); i++) {
				Instance instance = trainingInstances.get(i);
				HashMap<String, Double> results;
				if (bernoulli)
					results = evaluateBernoulli(instance);
				else
					results = evaluateMultinomial(instance);
				String bestClass = findBestClass(results);
				w.print("array:" + i + " " + bestClass + " ");

				ValueComparator bvc = new ValueComparator(results);
				TreeMap<String, Double> sorted_results = new TreeMap<String, Double>(
						bvc);
				sorted_results.putAll(results);
				for (Entry<String, Double> result : sorted_results.entrySet()) {
					w.print(result.getKey() + " " + result.getValue() + " ");
				}
				w.println();
			}

			w.println("%%%%% testing data:");
			for (int i = 0; i < testingInstances.size(); i++) {
				Instance instance = testingInstances.get(i);
				HashMap<String, Double> results;
				if (bernoulli)
					results = evaluateBernoulli(instance);
				else
					results = evaluateMultinomial(instance);
				String bestClass = findBestClass(results);
				w.print("array:" + i + " " + bestClass + " ");

				ValueComparator bvc = new ValueComparator(results);
				TreeMap<String, Double> sorted_results = new TreeMap<String, Double>(
						bvc);
				sorted_results.putAll(results);

				for (Entry<String, Double> result : sorted_results.entrySet()) {
					w.print(result.getKey() + " " + result.getValue() + " ");
				}
				w.println();
			}
			w.flush();
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static MultinomialNB load(String modelFile) {
		Serializer<MultinomialNB> loader = new Serializer<MultinomialNB>();
		return loader.load(modelFile);
	}

	public void save(String modelFile) {
		Serializer<MultinomialNB> save = new Serializer<MultinomialNB>();
		save.save(modelFile, this);
	}

}
