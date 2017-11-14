"""
This can be used to convert gensim's output Word2Vec model to the two files we need in DBpedia Spotlight:
 the dictionary and the weights matrix.

They're saved in csv format, which can be fed into Spotlight's model creation, where it's used to generate the vectors.mem store.
"""
__author__ = 'dowling'

import sys

import logging
import datetime

format_ = "%(asctime)s %(levelname)-8s %(name)-18s: %(message)s"
logging.basicConfig(format=format_)
ln = logging.getLogger()
ln.setLevel(logging.DEBUG)

from gensim.models.word2vec import Word2Vec
import numpy as np

def convert_model(prefix):
    ln.info("loading model")
    w2v = Word2Vec.load(prefix)

    ln.info("saving dict...")
    dict_file = prefix + ".wordids.txt"
    with open(dict_file, "w") as f:
        for word, voc_obj in w2v.vocab.items():
            f.write((u"%s\t%s\n" % (word, voc_obj.index)).encode("UTF-8"))

    ln.info("saving weights as csv...")
    weights_file = prefix+".syn0.csv"
    np.savetxt(weights_file, w2v.syn0, delimiter=",", header="%s\n%s" % w2v.syn0.shape)

    ln.info("all done. Saved converted model files: %s and %s." % (weights_file, dict_file))


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print "Usage: python convert_model.py <path-to-word2vec-model>"
    convert_model(sys.argv[1])
