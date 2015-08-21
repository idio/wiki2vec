#!/bin/bash
#+------------------------------------------------------------------------------------------------------------------------------+
#| Idio Wiki2Vec                                                                      |                                                                                                     |
#+------------------------------------------------------------------------------------------------------------------------------+

# Creates Wiki2Vec vectors out of massaged wikipedia corpus
# It uses gensim

CORPUS=$1
OUTPUTFOLDER=$2
MIN_COUNT=$3
ENTITY_MIN_COUNT=$4
VECTOR_SIZE=$5
WINDOW_SIZE=$6

# Install all the python dependencies
apt-get install python-pip build-essential liblapack-dev gfortran zlib1g-dev python-dev
pip install cython
apt-get install python-numpy python-scipy
pip install gensim

# Call python script
python gensim_word2vec.py $CORPUS $OUTPUTFOLDER -m $MIN_COUNT -e $ENTITY_MIN_COUNT -s $VECTOR_SIZE -w $WINDOW_SIZE
