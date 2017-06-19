### Intro
The goal of this project was to perform language detection without using any external dependencies. To do this, I implemented a multinomial Naive Bayes classifier. I don't know where the idea for using NB for language identification originally came from, but an example paper that discusses it is http://aclweb.org/anthology//E/E12/E12-3006.pdf, albeit as a baseline to compare to other approaches. 

I trained the classifier using the Europarl corpus (available at: http://www.statmt.org/europarl/v7/europarl.tgz). There are 21 languages in the Europarl corpus, and each one has hundreds of documents. I only used 100 documents for training on each languages, and 100 for testing, and achieved an average accuracy of 0.944. You can download it from https://drive.google.com/a/uw.edu/uc?id=0B7shg__lfRScaXBaX1pBUFBycDQ&export=download. The confusion matrix (where columns are actual labels, and rows are classified labels) for this training is in results/cmatrix.txt, but is hard to read without a really wide monitor. I didn't use any complex features, just the counts for each unigram in a document. I experimented with just binary features (e.g., whether the word appears or not), but this didn't speed up training time significantly, and  dropped the accuracy by 2%, probably because some words exist in multiple languages.


### Building
Running `./build.sh` from the root directory will produce an executable in the root directory called 'nb.jar', with the main class being the LanguageGuesserMain, which allows you to guess the language of a document. 


### Running
Once you have built or downloaded a model (download mine: https://drive.google.com/a/uw.edu/uc?id=0B7shg__lfRScaXBaX1pBUFBycDQ&export=download), you can run it with:

`java -cp nb.jar lang.LanguageGuesserMain model_file input_doc`
	
* `model_file` is the model_file output from the previous step

* `input_doc` is a text file that you want to evaluate

The resulting best language will be printed to stdout, with it's probability. The model serialization is very slow (this was originally designed to run on a server that would only have to load it once). 


### Training a model
If you want to download my model (trained on all 21 languages in europarl, using 100 docs per language), it is available at: https://drive.google.com/a/uw.edu/uc?id=0B7shg__lfRScaXBaX1pBUFBycDQ&export=download.		

But if you want to train your own, that is possible too. There are instructions near the top of `EuroparlTrainerMain` that explain how to do it with non-Europarl corpora. The only downside is that I didn't optimize how the model is serialized, so it can take a few minutes to train+save. But if you want to train your own, you can do:

`java -cp nb.jar lang.EuroparlTrainerMain /path/to/corpus model_file`
* `path/to/corpus` must correspond to the "txt/" directory in Europarl.
* `model_file` is where the model will be saved. It is 300ish meg if you use 100 training documents, since there are a lot unique words in 21 languages, and we need to keep track of p(w|c) for each class...

If you do use this method, it will reproduce the confusion matrix, using 100 docs for training, and 100 docs for testing. You can alter how many documents are used for training/testing by: 

`java -cp nb.jar lang.EuroparlTrainerMain /path/to/corpus model_file num_training_docs num_testing_docs`

