#!/bin/bash
#+------------------------------------------------------------------------------------------------------------------------------+
#| Idio Wiki2Vec                                                                      |                                                                                                     |
#+------------------------------------------------------------------------------------------------------------------------------+

# Creates Wiki2Vec vectors out of massaged wikipedia corpus
# It uses gensim

CORPUS=$1
OUPUTFOLDER=$2
MIN_COUNT=$3
VECTOR_SIZE=$4
WINDOW_SIZE=$5

# Install all the python dependencies
apt-get install python-pip build-essential liblapack-dev gfortran zlib1g-dev python-dev
pip install cython
apt-get install python-numpy python-scipy
pip install gensim

# Call python script
python gensim_word2vec.py $CORPUS $OUPUTFOLDER -m $MIN_COUNT -s $VECTOR_SIZE -w $WINDOW_SIZE
