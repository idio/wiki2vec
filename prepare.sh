#!/bin/bash
#+------------------------------------------------------------------------------------------------------------------------------+
#| Idio Wiki2Vec                                                                      |                                                                                                     |
#+------------------------------------------------------------------------------------------------------------------------------+

# $1 Locale (en_US)
# $2 Target Folder( Output Folder)

WIKI2VEC_VERSION="1.0"

usage ()
{
     echo "prepare.sh"
     echo "usage: ./prepare.sh en_US /data/word2vec/"
     echo "Creates a wikipedia corpus which can be fed into word2vec creation tools"
}

shift $((OPTIND - 1))

if [ $# != 2 ]
then
    usage
    exit
fi

BASE_DIR=$(pwd)

if [[ "$2" = /* ]]
then
   TARGET_DIR="$2"
else
   TARGET_DIR="$BASE_DIR/$2"
fi

WDIR="$BASE_WDIR/$2"
SPARK_PATH="$WDIR/spark-1.2.0-bin-hadoop2.4"
JAR_PATH="$BASE_DIR/target/scala-2.10/wiki2vec-assembly-${WIKI2VEC_VERSION}.jar"
READABLEWIKI="$WDIR/${LANGUAGE}wiki-latest.lines"
SPLIT_OUTPUT_CORPUS="$WDIR/${LANGUAGE}wiki"
OUTPUTCORPUS="$WDIR/${LANGUAGE}wiki.corpus"
LANGUAGE=`echo $1 | sed "s/_.*//g"`

echo "Language: $LANGUAGE"
echo "Working directory: $WDIR"

mkdir -p $WDIR
cd $WDIR

echo "Downloading Wikipedia Dump"
curl -O "http://dumps.wikimedia.org/${LANGUAGE}wiki/latest/${LANGUAGE}wiki-latest-pages-articles.xml.bz2"
WIKIPEDIA_PATH="$WDIR/${LANGUAGE}wiki-latest-pages-articles.xml.bz2"

echo "Downloading Apache Spark"
curl "http://d3kbcqa49mib13.cloudfront.net/spark-1.2.0-bin-hadoop2.4.tgz" | tar xvz

# Installing Java
add-apt-repository ppa:webupd8team/java

# Installing SBT
echo "deb http://dl.bintray.com/sbt/debian /" | tee -a /etc/apt/sources.list.d/sbt.list

apt-get update
apt-get install unzip oracle-java7-installer sbt

# Compiling
echo "Compiling wiki2vec..."
cd $BASE_DIR
sbt assembly


# Process Wiki
echo "Creating Readable Wiki.."
java -Xmx10G -Xms10G -cp org.idio.wikipedia.dumps.ReadableWiki $JAR_PATH $WIKIPEDIA_PATH $READABLEWIKI

# Create Wiki2Vec Corpus
echo "Creating Word2vec Corpus"
$SPARK_PATH/bin/spark-submit --class org.idio.wikipedia.word2vec.Word2VecCorpus $JAR_PATH $READABLEWIKI fakePathToRedirect/file.nt $SPLIT_OUTPUT_CORPUS

# joining split files
echo "Joining corpus.."
cd $SPLIT_OUTPUT_CORPUS
cat part* >> $OUTPUTCORPUS


echo "Done..{$OUTPUTCORPUS}"

