/*
Fast Artificial Neural Network Library (fann)
Copyright (C) 2003-2016 Steffen Nissen (steffen.fann@gmail.com)

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

#include "fann.h"
#include "float.h"
#include "time.h"
#include "string.h"

int main(int narg, char** args)
{
	if (narg < 4)
	{
		printf("Not enough arguments");
		return -1;
	}

	time_t startTime = time(0);
	
	const unsigned int num_output = 1;
	const unsigned int num_layers = 3;	
	
	struct fann_train_data *trainData = fann_read_train_from_file(args[1]);
	struct fann_train_data *devData = fann_read_train_from_file(args[2]);
	const char* outputNet = args[3];
	const unsigned int num_input = atoi(args[4]);
	const unsigned int num_neurons_hidden = atoi(args[5]);
	const float learning_rate = atof(args[6]);
	const unsigned int iterationsWithoutImprovement = atoi(args[7]);
	const int seed = atoi(args[8]);
	const float maxTime = atof(args[9]);
	
	const float desired_error = (const float) 0.001;
	const unsigned int max_epochs = 20001;
	const unsigned int epochs_between_reports = 100;
	int i, besti, noImprov, bitFail;
	
	double testResult = 0, trainResult = 0, bestTestResult = DBL_MAX;
	struct fann *ann = fann_create_standard(num_layers, num_input, num_neurons_hidden, num_output), *annCopy = NULL;
	
	printf("Data loaded, training in progress...\n");
	printf("#epoch:trainMSE:devMSE:bitfail:noImprov:devMSE/bestdevMSE:ellapsedTime\n");

	srand(seed);

	fann_set_learning_rate(ann, learning_rate);
	ann->training_algorithm = FANN_TRAIN_INCREMENTAL;

	fann_set_activation_function_hidden(ann, FANN_SIGMOID);
	fann_set_activation_function_output(ann, FANN_SIGMOID);

	for(i = 1; i < max_epochs; i++) {
		fann_train_on_data(ann, trainData, 1, 0, desired_error);
		testResult = fann_get_MSE(ann);
		bitFail = fann_get_bit_fail(ann);
		fann_reset_MSE(ann);
		fann_test_data(ann, devData);
		testResult = fann_get_MSE(ann);
		printf("%i:%f:%f:%i:%i:%f:%.0f\n", i,trainResult, testResult, bitFail,noImprov,testResult/bestTestResult,difftime(time(0),startTime));		
		if(testResult / bestTestResult > 1.00001) {
			noImprov ++;
			//~ printf("%i epoch without improvement", noImprov);
			if (noImprov == iterationsWithoutImprovement)
			{
				break;
			}
		} else {
			bestTestResult = testResult;
			besti = i;
			noImprov = 0;
			if (annCopy)
				fann_destroy(annCopy);
			annCopy = fann_copy(ann);
			//~ fann_save(ann, outputNet);
		}
		if (difftime(time(0),startTime) > maxTime)
		{
			char aux[1024];
			strcpy(aux, outputNet);
			strcat(aux, ".tmp");
			printf("Over max time, saving in %s\n", aux);
			fann_save(annCopy, aux);
			break;
		} 
	}
	
	printf("%i:%f\n", besti, bestTestResult);
	fann_save(annCopy, outputNet);
	
	fann_destroy(ann);
	if (annCopy)
		fann_destroy(annCopy);
	fann_destroy_train(trainData);
	fann_destroy_train(devData);

	return 0;
}
