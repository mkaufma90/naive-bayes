----README----

To solve this without any external dependencies, I implemented a multinomial Naive Bayes classifier (I actually adapted/updated a bernoulli NB I wrote earlier). I don't know where the idea for using NB for language identification originally came from, but an example paper that discusses it is http://aclweb.org/anthology//E/E12/E12-3006.pdf, albeit as a baseline to compare to other approaches. 

I trained the classifier using the Europarl corpus (available at: http://www.statmt.org/europarl/v7/europarl.tgz). There are 21 languages in the Europarl corpus, and each one has hundreds of documents. I only used 100 documents for training on each languages, and 100 for testing, and achieved an average accuracy of 0.944. The confusion matrix (where columns are actual labels, and rows are classified labels) for this training is in cmatrix.txt, but is hard to read without a really wide monitor. I didn't use any complex features, just the counts for each unigram in a document. I experimented with just binary features (e.g., whether the word appears or not). This didn't speed up training time significantly, and  dropped the accuracy by 1 or 2 points, probably because some words exist in multiple languages.  

To actually use this, you can either retrain the model, or download mine.

----Training a model----
If you want to download my model (trained on all 21 languages in europarl, using 100 docs per language), it is available at: https://drive.google.com/a/uw.edu/file/d/0B7shg__lfRScaXBaX1pBUFBycDQ/edit?usp=sharing

If you want to train your own, that is possible too. There are instructions near the top of EuroparlTrainer that explain how to do it with non-Europarl corpora. The only downside is that I didn't optimize how the model is serialized, so it can take a few minutes. But if you want to train your own, you can do:

java -cp nb.jar lang.EuroparlTrainer /path/to/corpus model_file
	path/to/corpus must correspond to the "txt/" directory in Europarl.
	model_file is where the model will be saved. It is 300ish meg if you use 100 training documents, since there are a lot unique words in 21 languages, and we need to keep track of p(w|c) for each class...

If you do use this method, it will reproduce the confusion matrix, using 100 docs for training, and 100 docs for testing . You can alter how many documents are used for training/testing by: 

java -cp nb.jar lang.EuroparlTrainer /path/to/corpus model_file num_training_docs num_testing_docs


---Running---
Once you have a model, you can run it with:

java -cp nb.jar lang.LanguageGuesser model_file input_doc
	model_file is the model_file output from the previous step
	input_doc is a text file that you want to evaluate
The resulting best language will be printed to stdout, with it's probability.

Obviously this could have been faster if I just kept the model in memory the whole time. I actually originally wrote it that way, and it took about 2 minutes from start to finish. But this way seemed more and would be faster if the training time of the model ever became more significant than the time it took to do the disk I/O (which doesn't seem like a very unlikely scenario)

If you have any questions about these instructions, or my literature review, feel free to contact me. I hope to here from you soon!




