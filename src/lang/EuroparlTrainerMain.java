package lang;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nb.MultinomialNB;
import nb.util.Instance;
import nb.util.WordCountUtils;

public class EuroparlTrainerMain  {

	// **************CONFIG**************

	// This should corresponded to the content of the txt/ directory of the
	// Europarl corpus, downloadable from
	// http://www.statmt.org/europarl/v7/europarl.tgz
	// If you want to use another corpus, just make sure it is in the following
	// format:
	// -ROOT_DIR/
	// --lang1/
	// ---doc1.txt
	// ---doc2.txt
	// --lang2/
	// ---doc1.txt
	// ---doc2.txt
	private static String ROOT_DIR;

	// The europarl corpora has more training data than we need to achieve
	// reasonable classification results.
	// Assuming that 1 document = 1 training instance, these variables allow you
	// to control how many documents are used for
	// training and testing
	private static Integer MAX_TRAIN_INSTANCES = 100;
	private static Integer MAX_TEST_INSTANCES = 100;
	// **************END CONFIG**************

	// **************CLASS VARIABLES**************
	private List<Instance> trainingInstances = new ArrayList<Instance>();
	private List<Instance> testingInstances = new ArrayList<Instance>();
	private MultinomialNB nb;
	// **************END CLASS VARIABLES***********

	/**
	 * Loads in the data from a corpus (@see {@link #ROOT_DIR} Converts that data into
	 * training and testing instances (@see {@link #MAX_TRAIN_INSTANCES}  and @see {@link #MAX_TEST_INSTANCES})
	 * 
	 * @see {@link #MAX_TRAIN_INSTANCES} Runs a multinomial NB classifier and prints out
	 *                         the confusion matrix and accuracy
	 */
	public static void main(String[] args) {
		String rootDir = args[0];
		String modelFile = args[1];
		EuroparlTrainerMain trainer = null;

		// default constructor
		if (args.length == 2) {
			trainer = new EuroparlTrainerMain(rootDir);
		}
		// If we pass in max training/testing values, use them
		else if (args.length ==4 ){
			Integer maxTrain = Integer.parseInt(args[2]);
			Integer maxTest = Integer.parseInt(args[3]);
			trainer = new EuroparlTrainerMain(rootDir, maxTrain, maxTest);
		}
		//this probably should do more sophisticated argument checking 
		else{
			System.err.println("Improper number or arguments. Must be 2 (root_dir, model_file) or 4 (root_dir, model_file, max_testing, min_testing)");
			System.exit(0);
		}

		//If we have initialized a EuroparlTrainer properly, then train it,print out the confusion matrices given a test set, and save it
		try {
			System.out.println("Loading files...");
			trainer.loadFiles();
			System.out.println("Training...");
			trainer.trainAndEvaluate();
			System.out.println("Saving...");
			trainer.saveModel(modelFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * For use if you are training with default MAX_TRAIN_INSTANCES and
	 * MAX_TEST_INSTANCES
	 */
	public EuroparlTrainerMain(String root) {
		ROOT_DIR = root;
	}

	/**
	 * For use if you are specifying a number of train/testing docs
	 */
	public EuroparlTrainerMain(String root, int maxTrain, int maxTest) {
		ROOT_DIR = root;
		MAX_TRAIN_INSTANCES = maxTrain;
		MAX_TEST_INSTANCES = maxTest;
	}

	/**
	 * Saves the NB model so that we can evaluate easily
	 */
	public void saveModel(String modelFile) {
		nb.save(modelFile);
	}

	/**
	 * Converts files from ROOT_DIRECTORY into training and testing instances
	 * 
	 * @throws IOException
	 */
	public void loadFiles() throws IOException {
		File root = new File(ROOT_DIR);
		for (File langRootDir : root.listFiles()) {
			// Iterate over all languages
			if (langRootDir.isDirectory()) {
				String label = langRootDir.getName();

				int numFiles = langRootDir.listFiles().length;

				// If there are not enough files for the language, throw an
				// error
				if (numFiles <= MAX_TRAIN_INSTANCES + MAX_TEST_INSTANCES) {
					throw new IOException(
							langRootDir
									+ " does not contain enough files to train and test with the values specified");
				}
				int numFilesLoaded = 0;
				boolean currentlyLoadingTraining = true;

				// This iterates over the documents for each languages
				// It first loads all the training instances, then testing
				// instances
				for (File langFile : langRootDir.listFiles()) {

					// Did we load all the training instances. If so, switch to
					// loading testing instances
					if (numFilesLoaded > MAX_TRAIN_INSTANCES
							&& currentlyLoadingTraining) {
						currentlyLoadingTraining = false;
						numFilesLoaded = 0;
					}

					// Did we load all the testing instances? If so, we're done.
					else if (numFilesLoaded > MAX_TEST_INSTANCES
							&& !currentlyLoadingTraining) {
						break;
					}
					// Otherwise, load the document into the appropriate
					// category
					else {
						Map<String, Integer> wordCounts = WordCountUtils
								.doc2WordCount(langFile);
						Instance instance = WordCountUtils.wordCounts2Instance(
								wordCounts, label);
						if (currentlyLoadingTraining) {
							trainingInstances.add(instance);
						} else {
							testingInstances.add(instance);
						}
						numFilesLoaded++;
					}
				}
			}
		}
	}

	/**
	 * Wrapper for training the multinomial naive bayes, and printing the
	 * confusion matrix and accuracy
	 */
	public void trainAndEvaluate() {
		nb = new MultinomialNB();
		nb.setTrainingInstances(trainingInstances);
		nb.trainMultinomial();
		nb.computeAccuracy(testingInstances, true, false);
	}

}
