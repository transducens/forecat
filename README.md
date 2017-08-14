Forecat
=======

Forecat is a web tool for interactive translation prediction.

See "[Black-box integration of heterogeneous bilingual resources into an interactive translation system](https://sites.google.com/site/hacat2014/program/papers/HaCaT09.pdf?attredirects=0)"; Juan Antonio Pérez-Ortiz, Daniel Torregrosa and Mikel Forcada; EACL 2014 Workshop on Humans and Computer-Assisted Translation. 

"[Ranking suggestions for blackbox interactive translation prediction systems with multilayer perceptrons](https://scholar.google.es/citations?view_op=view_citation&citation_for_view=ZHU6W5oAAAAJ:3fE2CSJIrl8C)"; Daniel Torregrosa, Juan Antonio Pérez-Ortiz and Mikel Forcada, AMTA 2016

"[Comparative Human and Automatic Evaluation of Glass-Box and Black-Box Approaches to Interactive Translation Prediction](https://scholar.google.es/citations?view_op=view_citation&citation_for_view=ZHU6W5oAAAAJ:_kc_bZDykSQC)"; Daniel Torregrosa, Juan Antonio Pérez-Ortiz and Mikel Forcada, EAMT 2017

## Train your own neural network for Forecat-OmegaT


This section describes a simplified step-by-step guide to train the neural network to be used in `Forecat-OmegaT`:
1. Run Forecat using the following parameters:
```
java -jar forecat.jar -l -e A -S pn -d n -sl 4 -s eng -t kaz -is eng.txt -it kaz.txt -o out.dat -featuresWinning features.fann -FANNstyle
```
  * `-l`: to use basic selection 
  * `-e A`: to use a local instalation of apertium.
  * `-S cp`: sorting method; the sorting method is irrelevant as the order of the suggestions is not used during the training and all the compatible ones will be shown. 
  * `-sl 4`: use subsegments up to length 4,.
  * `-s SL`: source language.
  * `-t TL`: target language.
  * `-is /path/to/source\_corpus`: path to a file containing segments in source language.
  * `-it /path/to/target\_corpus`: path to a file containing segments in target language; the system will use this sentences as reference for classifying the suggestions.
  * `-o /output/file`: a file with different data and stats of the automatic evaluation. This file will be overwritten if it exists. The last lines of this file have the average and standard deviation values needed to correctly configure the plugin.
  * `-featuresWinning /output/winningFeatures`: events file with all the events; the winning events get classified as 1, the rest as 0.
  * `-featuresViable /output/viableFeatures`: events file with all the events; the viable events get classified as 1, the rest as 0. 
  * `-FANNstyle`: This parameter makes the events file follow the [FANN format](\url{http://leenissen.dk/fann/wp/help/getting-started/}). If not included, the output files will have [Weka's ARFF file format](\url{http://www.cs.waikato.ac.nz/ml/weka/arff.html).

2. Using the events file, train a FANN neural network. 
  * Download, compile and install [FANN](\url{http://leenissen.dk/fann/wp/)
  * Follow the instructions in FANN folder to compile `train.c`. 
  * Extract a training and development set from each event file following FANN's format. For example, use the first 90% events as training and the remaining 10% as development.
  * Train the neural network using either the viable or winning events file; during the experiments carried out by the authors, it was found that there is little difference between the performance of the networks; therefore, it is recommended, but not mandatory, to train different networks with each and keep the one that minimizes the mean squared error (MSE). To train the network, use the following parameters:

```
./train trainingData devData netOut inputN hiddenN learningRate maxDrift seed maxTime}
```

  * `trainingData`: a FANN events file with the training data.
  * `devData`: a FANN events file with the development data. 
  * `netFile`: the file where the neural network will be written to.
  * `inputN`: number of input units. It has to match the number of features in each event: in this case, 79.
  * `hiddenN`: number of hidden units; the authors found 128 is a good value for this parameter.
  * `learningRate`: the learning rate; the authors found 0.001 is a good value for this parameter.
  * `maxDrift`: if more than `maxDrift` sequential epochs fail to improve the MSR, then the training procedure ends, and the weights of the network with the minimum MSR are printed to `netFile`.
  * `seed`: a seed to randomly initialize the network weights; it is recommended to train different neural networks with different seeds and keep the one that obtains the minimum MSR, but this could be an expensive procedure, and the differences are minute. Therefore, it is recommended to train at least 2 networks with different seeds in case one gets trapped in local minima.
  * `maxTime`: if the training procedure takes longer than `maxTime` seconds, the training procedure will finalize as soon as the current epoch is computed.

3. The neural network obtained in step 2 (in the file `netFile`) can be used for `Forecat-OmegaT`, along with the average and standard deviation values at the end of `output/file`, obtained during the step 1.
