#!/bin/bash
#+------------------------------------------------------------------------------------------------------------------------------+
#| Idio Wiki2Vec                                                                      |                                                                                                     |
#+------------------------------------------------------------------------------------------------------------------------------+

# Creates Wiki2Vec corpora out of a wikipedia dump

# $1 Locale (en_US)
# $2 Target Folder( Output Folder)
# $3 Stemmer

WIKI2VEC_VERSION="1.0"

usage ()
{
     echo "prepare.sh"
     echo "usage: ./prepare.sh en_US /data/word2vec/ [StemmerLanguage]"
     echo "Creates a wikipedia corpus which can be fed into word2vec creation tools"
}

shift $((OPTIND - 1))

if [ $# < 2 ]
then
    usage
    exit
fi

BASE_DIR=$(pwd)
TARGET_DIR="$2"
LANGUAGE=`echo $1 | sed "s/_.*//g"`
WDIR="$BASE_DIR/working"
SPARK_PATH="$WDIR/spark-1.2.0-bin-hadoop2.4"
JAR_PATH="$BASE_DIR/target/scala-2.10/wiki2vec-assembly-${WIKI2VEC_VERSION}.jar"
READABLEWIKI="$TARGET_DIR/${LANGUAGE}wiki-latest.lines"
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
$SPARK_PATH/bin/spark-submit --driver-memory 15g --num-executors 4 --class org.idio.wikipedia.word2vec.Word2VecCorpus $JAR_PATH $READABLEWIKI $BASE_DIR/fakePathToRedirect/file.nt $SPLIT_OUTPUT_CORPUS $STEMMERNAME

# joining split files
echo "Joining corpus.."
cd $SPLIT_OUTPUT_CORPUS
cat part* >> $OUTPUTCORPUS


echo " ^___^ corpus : $OUTPUTCORPUS"

