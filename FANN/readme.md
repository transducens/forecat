To compile train, put train in FANN examples folder and execute

gcc -I ../src/include -L ../src/ -O3 train.c -o train -lfann -lm

Be sure to have a working installation of FANN in your computer prior this.