__author__ = 'dowling'
import datetime
import logging

logFormatter = logging.Formatter("%(asctime)s %(levelname)-8s %(name)-18s: %(message)s")

ln = logging.getLogger()

fileHandler = logging.FileHandler("fix_corpus_log%s.txt" % datetime.datetime.now().isoformat())

fileHandler.setFormatter(logFormatter)
ln.addHandler(fileHandler)

consoleHandler = logging.StreamHandler()
consoleHandler.setFormatter(logFormatter)
ln.addHandler(consoleHandler)

ln.setLevel(logging.DEBUG)

import re
import string
from gensim import utils

replace_punctuation = string.maketrans(string.punctuation, ' '*len(string.punctuation))

class PreprocessingLineSentence():
    def __init__(self, path_to_corpus):
        self.path = path_to_corpus

    def __iter__(self):
        with utils.smart_open(self.path) as fin:
            for line_no, line in enumerate(fin):
                if line_no % 10000 == 0:
                    ln.debug("Processed %s lines" % line_no)

                # replace all punctuation with a space, unless it's inside a DBPEDIA_ID
                line_parts = []
                start_at = 0
                for list_idx, match in enumerate(re.finditer(r"DBPEDIA_ID/\S+", line)):

                    edited = line[start_at: match.start()].translate(replace_punctuation)

                    line_parts.append(edited)
                    line_parts.append(match.group(0))
                    start_at = match.end()

                edited = line[start_at: -1].translate(replace_punctuation)
                line_parts.append(edited)

                line = "".join(line_parts)

                line = utils.to_unicode(line)
                yield line.split()


def fix_corpus(path_to_corpus):
    fixed = PreprocessingLineSentence(path_to_corpus)
    with open(path_to_corpus + "_fixed", "w") as f:
        for line in fixed:
            f.write(line)