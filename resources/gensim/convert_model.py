__author__ = 'dowling'

import sys
from gensim.models.word2vec import Word2Vec
import numpy as np


def convert_model(prefix):
    print "loading model"
    w2v = Word2Vec.load(prefix)

    print "saving weights as csv..."
    weights_file = prefix+".syn0.csv"
    np.savetxt(weights_file, w2v.syn0, delimiter=",")

    print "saving dict..."
    dict_file = prefix + ".wordids.txt"
    with open(dict_file, "w") as f:
        for word, obj in sorted(w2v.vocab.items(), key=lambda (w, o): w2v.vocab[w].index):
            idx = w2v.vocab[word].index
            f.write("%s\t%s\n" % (word, idx))

    print "all done. Saved converted model files: %s and %s." % (weights_file, dict_file)


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print "Usage: python convert_model.py <path-to-word2vec-model>"
    convert_model(sys.argv[1])