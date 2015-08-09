#!/bin/bash
#+------------------------------------------------------------------------------------------------------------------------------+
#| Idio Wiki2Vec                                                                      |                                                                                                     |
SPLIT_OUTPUT_CORPUS="$WDIR/${LANGUAGE}wiki"
OUTPUTCORPUS="$TARGET_DIR/${LANGUAGE}wiki.corpus"

if [ ! -z "$3" ]; then 
        STEMMERNAME="$3"
else 
        STEMMERNAME="$LANGUAGE"
fi

echo "Language: $LANGUAGE"
echo "Working directory: $WDIR"
echo "Language stemmer: $STEMMERNAME"


#apt-get update

# Installing Java
#add-apt-repository ppa:webupd8team/java


# Installing SBT
#echo "deb http://dl.bintray.com/sbt/debian /" | tee -a /etc/apt/sources.list.d/sbt.list

#apt-get update
#apt-get install unzip oracle-java7-installer sbt


mkdir -p $WDIR
cd $WDIR

echo "Downloading Wikipedia Dump"
# curl -O "http://dumps.wikimedia.org/${LANGUAGE}wiki/latest/${LANGUAGE}wiki-latest-pages-articles-multistream.xml.bz2"
WIKIPEDIA_PATH="$WDIR/${LANGUAGE}wiki-latest-pages-articles-multistream.xml.bz2"

echo "Downloading Apache Spark"
#curl "http://d3kbcqa49mib13.cloudfront.net/spark-1.2.0-bin-hadoop2.4.tgz" | tar xvz


# Compiling
echo "Compiling wiki2vec..."
cd $BASE_DIR
#sbt assembly


# Process Wiki
echo "Creating Readable Wiki.."
#java -Xmx10G -Xms10G -cp $JAR_PATH org.idio.wikipedia.dumps.CreateReadableWiki $WIKIPEDIA_PATH $READABLEWIKI

# Create Wiki2Vec Corpus
echo "Creating Word2vec Corpus"
$SPARK_PATH/bin/spark-submit --driver-memory 20g --class org.idio.wikipedia.word2vec.Word2VecCorpus $JAR_PATH $READABLEWIKI $BASE_DIR/fakePathToRedirect/file.nt $SPLIT_OUTPUT_CORPUS $STEMMERNAME

# joining split files
echo "Joining corpus.."
cd $SPLIT_OUTPUT_CORPUS
cat part* >> $OUTPUTCORPUS


echo " ^___^ corpus : $OUTPUTCORPUS"

