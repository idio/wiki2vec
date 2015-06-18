# Wiki2Vec

Utilities for creating Word2Vec vectors for Dbpedia Entities via a Wikipedia Dump.

Within the release of [Word2Vec](http://code.google.com/p/word2vec/) the Google team released vectors for freebase entities trained on the Wikipedia. These vectors are useful for a variety of tasks.

This Tool will allow you to generate those vectors. Instead of `mids` entities will be addressed via `DbpediaIds` which correspond to wikipedia article's titles.
Vectors are generated for (i) words appearing inside wikipedia (ii) vectors for topics i.e: `dbpedia/Barack_Obama`.


## Prebuilt models

You can download via torrent one of the prebuilt word2vec models:

- [English Wikipedia 1000 dimension - No stemming - 10skipgram](https://github.com/idio/wiki2vec/raw/master/torrents/enwiki-gensim-word2vec-1000-nostem-10cbow.torrent)


#### Using a prebuilt model

 - Get python 2.7
 - Install gensim: `pip install gensim`
 - uncompress downloaded model: `tar -xvf model.tar.gz`
 - Load model in gensim:

 ```python
 from gensim.models import Word2Vec
 model = Word2Vec.load("path/to/word2vec/en.model")
 model.similarity('woman', 'man')
 ```


## Quick usage:

- The automated Script set up and runs everything on Ubuntu 14.04. For other Platforms check `Going the long way`
- Run `sudo sh prepare.sh <Locale> PathToOutputFolder`. i.e: 
   - `sudo sh prepare.sh es_ES /mnt/data/`  will work on the spanish wikipedia
   - `sudo sh prepare.sh en_US /mnt/data/`  will work on the english wikipedia
   - `sudo sh prepare.sh da_DA /mnt/data/`  will work on the danish wikipedia
  
- Running `prepare` will:
   - Download the latest wikipedia dump for the given language
   - Clean the dump, stem it and tokenize it
   - Create a `language.corpus` file in `outputFolder`, this corpus can be fed to any word2vec tool to generate vectors.

- Once you get `language.corpus` go to `resources/gensim` and do:

  `wiki2vec.sh pathToCorpus pathToOutputFile <MIN_WORD_COUNT> <VECTOR_SIZE> <WINDOW_SIZE>`

this will install all requiered dependencies for Gensim and build word2vec vectors.

i.e:

`wiki2vec.sh corpus output/model.w2c 50 500 10`

- Discards words below 50 counts, generate vectors of size 500, and the window size for building the counts of each occurence is 10 words.

------

`prepare.sh` script installs:
 - Java 7
 - Sbt
 - Apache Spark

`wiki2vec.sh` script installs:
 - python-pip
 - build-essential
 - liblapack-dev
 - gfortran
 - zlib1g-dev
 - python-dev
 - cython
 - numpy
 - scipy
 - gensim

## Going the long way

### Compile

 - Get sbt
 - make sure `JAVA_HOME` is pointing to Java 7 
 - do `sbt assembly`

### Readable Wikipedia

Wikipedia dumps are stored in xml format. This is a difficult format to process in parallel because the  xml file has to be streamed getting the articles on the go.
A Readable wikipedia Dump is a transformation of the dump such that it is easy to pipeline into tools such as Spark or Hadoop.

Every line in a readable wikipedia dump follows the format:
`Dbpedia Title` `<tab>` `Article's Text`

The class `org.idio.wikipedia.dumps.ReadableWiki` gets a `multistreaming-xml.bz2`wikipedia dump and outputs a readable wikipedia.

params:
 - path to wikipedia dump
 - path to output readable wikipedia
i.e:

`java -Xmx10G -Xms10G -cp org.idio.wikipedia.dumps.ReadableWiki wiki2vec-assembly-1.0.jar path-to-wiki-dump/eswiki-20150105-pages-articles-multistream.xml.bz2 pathTo/output/ReadableWikipedia`


### Word2Vec Corpus

Creates a Tokenized corpus which can be fed into tools such as Gensim to create Word2Vec vectors for Dbpedia entities.

- Every Wikipedia link to an article within wiki is replaced by : `DbpediaId/DbpediaIDToLink`. i.e: 

if an article's text contains: 
```
[[ Barack Obama | B.O ]] is the president of [[USA]]
```

is transformed into:

```
DbpediaID/Barack_Obama B.O is the president of DbpediaID/USA
```

- Articles are tokenized (At the moment in a very naive way)


#### Getting a Word2Vec Corpus

1. Make sure you got a `Readable Wikipedia`
2. Download Spark : http://d3kbcqa49mib13.cloudfront.net/spark-1.2.0-bin-hadoop2.4.tgz
3. In your Spark folder do:
  ```
  bin/spark-submit --class "org.idio.wikipedia.word2vec.Word2VecCorpus"  target/scala-2.10/wiki2vec-assembly-1.0.jar   /PathToYourReadableWiki/readableWiki.lines /Path/To/RedirectsFile /PathToOut/Word2vecReadyWikipediaCorpus
  ```
4. Feed your corpus to a word2vec tool

### Stemming

By default the word2vec corpus is always stemmed. If you don't want that to happen: 


#### If using the automated scripts..
pass None as an extra argument

`sudo sh prepare.sh es_ES /mnt/data/ None`  will work on the spanish wikipedia and won't stem words

#### If you are manually running the tools:
Pass None as an extra argument when calling spark
 ```
 bin/spark-submit --class "org.idio.wikipedia.word2vec.Word2VecCorpus"  target/scala-2.10/wiki2vec-assembly-1.0.jar   /PathToYourReadableWiki/readableWiki.lines /Path/To/RedirectsFile /PathToOut/Word2vecReadyWikipediaCorpus None
 ```


## Word2Vec tools:

- [Gensim](https://radimrehurek.com/gensim/)
- [DeepLearning4j](https://github.com/SkymindIO/deeplearning4j): Feb 2014, Gets stuck in infinite loops on a big corpus
- [Spark's word2vec](https://github.com/apache/spark/blob/master/mllib/src/main/scala/org/apache/spark/mllib/feature/Word2Vec.scala): Feb 2014, `number of dimensions` * `vocabulary size` has to be less than a certain value otherwise an exception is thrown. [issue](http://mail-archives.apache.org/mod_mbox/spark-issues/201412.mbox/%3CJIRA.12761684.1418621192000.36769.1418759475999@Atlassian.JIRA%3E)



## ToDo:
- Remove hard coded spark params
- Handle Wikipedia Redirections
- Intra Article co-reference resolution
