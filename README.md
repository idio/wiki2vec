# Wiki2Vec

Utilities for creating Word2Vec vectors for Dbpedia Entities via a Wikipedia Dump.

Within the release of [Word2Vec](http://code.google.com/p/word2vec/) the Google team released vectors for freebase entities trained on the Wikipedia. These vectors are useful for a variety of tasks.

This Tool will allow you to do generate those vectors. Instead of `mids` entities will be addressed via `DbpediaIds` which correspond to wikipedia article's titles.

## Compile

 - Get sbt
 - make sure `JAVA_HOME` is pointing to Java 7 
 - do `sbt assembly`

## Readable Wikipedia

Wikipedia dumps are stored in xml format. This is a difficult format to process in parallel because the  xml file has to be streamed getting the articles on the go.
A Readable wikipedia Dump is a transformation of the dump such that it is easy to pipeline into tools such as Spark or Hadoop.

Every line in a readable wikipedia dump follows the format:
`Dbpedia Title` `<tab>` `Article's Text`

The class `org.idio.wikipedia.dumps.ReadableWiki` gets a `multistreaming-xml.bz2`wikipedia dump and outputs a readable wikipedia.
i.e

`java -Xmx10G -Xms10G -cp org.idio.wikipedia.dumps.ReadableWiki wiki2vec-assembly-1.0.jar path-to-wiki-dump/eswiki-20150105-pages-articles-multistream.xml.bz2 pathToReadableWiki/eswiki-readable-20150105.lines`


## Word2Vec Corpus

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


### Getting a Word2Vec Corpus

1. Make sure you got a `Readable Wikipedia`
2. Download Spark : http://d3kbcqa49mib13.cloudfront.net/spark-1.2.0-bin-hadoop2.4.tgz
3. In your Spark folder do:
  ```
  bin/spark-submit --class "org.idio.wikipedia.word2vec.Word2VecCorpus"  target/scala-2.10/wiki2vec-assembly-1.0.jar   /PathToYourReadableWiki/readableWiki.lines /Path/To/RedirectsFile /PathToOut/Word2vecReadyWikipediaCorpus
  ```
4. Feed your corpus to a word2vec tool like: 
   - [Gensim](https://radimrehurek.com/gensim/)
   - [DeepLearning4j](https://github.com/SkymindIO/deeplearning4j): Feb 2014, Gets stuck in infinite loops on a big corpus
   - [Spark's word2vec](https://github.com/apache/spark/blob/master/mllib/src/main/scala/org/apache/spark/mllib/feature/Word2Vec.scala): Feb 2014, `number of dimensions` * `vocabulary size` has to be less than a certain value otherwise an exception is thrown. [issue](http://mail-archives.apache.org/mod_mbox/spark-issues/201412.mbox/%3CJIRA.12761684.1418621192000.36769.1418759475999@Atlassian.JIRA%3E)


## ToDo: 
- Remove hard coded spark params
- Better Tokenization
- Handle Wikipedia Redirections
- Intra Article co-reference resolution
