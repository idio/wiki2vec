package org.idio.wikipedia.word2vec

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkContext, SparkConf}
import info.bliki.wiki.filter.PlainTextConverter
import info.bliki.wiki.model.WikiModel
import org.idio.wikipedia.dumps.{WikipediaPage, EnglishWikipediaPage}
import org.idio.wikipedia.redirects.{RedisRedirectStore, MapRedirectStore, EmptyRedirectStore, RedirectStore}
import org.idio.wikipedia.utils.{NoStemmer, Stemmer, SnowballStemmer}
import java.util.Locale
/**
 * Creates a corpus which can feed to word2vec
 * to extract vectors for each wikipedia Topic.
 *
 * It assumes a "ReadableWikipedia " dump is fed.
 * A Readable Wikipedia dump is defined as one in which every line in hte file follows:
 * [Article Title] [Tab] [Article Text]
 */
class Word2VecCorpus(pathToReadableWiki:String, redirectStore:RedirectStore, pathToOutput:String, language:String)(implicit val sc: SparkContext){

  private val PREFIX = "DBPEDIA_ID/"

  // RDD of a readableWikipedia where each line follows the format :
  //  article Title <tab> article text
  private val readableWikipedia = sc.textFile(pathToReadableWiki)

  // RDD (WikiTitle, Article Text)
  private val wikiTitleTexts = getPairRDD(readableWikipedia)

  private val redirectStoreBC = sc.broadcast(redirectStore)

  /*
  * Returns a PairRDD (WikiTitle, ArticleText)
  * Out of a readable wikipedia
  * */
  private def getPairRDD(articlesLines:RDD[String])={
    articlesLines.map{ line =>
      val splitLine = line.split("\t")
      try {
        val wikiTitle = splitLine(0)
        val articleText = splitLine(1)
        (wikiTitle, articleText)
      }catch{
        case _ => ("", "")
      }
    }
  }


  /*
  *  Clean the articles (wikimedia markup)
  * */
  private def cleanArticles(titleArticleText:RDD[(String, String)]) ={


    titleArticleText.map{
      case (title, text) =>
        // Removes all of the Wikimedia boilerplate, so we can get only the article's text
        val wikiModel = new EnglishWikipediaPage()

        // cleans wikimedia markup
        val pageContent = WikipediaPage.readPage(wikiModel, text)

        // cleans further Style tags {| some CSS inside |}
        val markupClean = ArticleCleaner.cleanStyle(pageContent)

        // clean brackets i.e: {{cite}}
        val cleanedText = ArticleCleaner.cleanCurlyBraces(markupClean)

        // cleans "==" out of "==Title=="
        val noTitleMarkers = cleanedText.replace("="," ").replace("*", " ")

        (title, noTitleMarkers)
    }

  }

  /*
 * Replaces links to wikipedia  articles following the format:
 *   [[Wikipedia Title]] and [[Wikipedia Title | anchor]]
 *
 * for:
 *  DBPEDIA_ID/Wikipedia_Title <Space> Wikipedia Title
 *
 *  or:
 *
 *  DBPEDIA_ID/Wikipedia_Title <Space> anchor
 *
 * */
  private def replaceLinksForIds(titleArticleText:RDD[(String, String)], redirectStore: RedirectStore)={

    // avoiding to serialize this class for Spark
    val prefix = PREFIX.toString

    val replace = { (anchorText:String, dbpdiaId:String) =>
      // Avoiding serializing this for spark..
      " " + prefix + dbpdiaId + " " + anchorText + " "
    }
     // replaces {{linkToWikipediaARticle}} =>  DBPEDIA_ID/wikiPediaTitle
    val redirectStore_local = redirectStoreBC.value
    titleArticleText.map{ case(title,text) =>
      (title, ArticleCleaner.replaceLinks(text, replace, redirectStore_local))
    }
  }


  /*
  * Dumb Tokenization.
  * Replace this for something smarter
  * */
  private def tokenize(stringRDD:RDD[(String,String)]): RDD[String] ={

    val prefix = PREFIX
    val language_local = language

    val tokenizedLines = stringRDD.map{
      case (dbpedia, line) =>
        val stemmer = try{
                          new SnowballStemmer(language_local)
                    }catch{
                      case _=> new NoStemmer()
                }
         line.split("\\s").map{
            word =>
               word match{
                 case w if w.startsWith(prefix) => w
                 case _ => stemmer.stem(word.replace(",","").replace(".","").replace("“","").replace("\\","").replace("[","").replace("]","").replace("‘",""))
               }
         }.mkString(" ")
    }
    tokenizedLines
  }


  def getWord2vecCorpus(): Unit ={
    val replacedLinks = replaceLinksForIds(wikiTitleTexts, redirectStore)
    val cleanedArticles = cleanArticles(replacedLinks)
    val tokenizedArticles = tokenize(cleanedArticles)
    tokenizedArticles.saveAsTextFile(pathToOutput)
  }

}


object Word2VecCorpus{

  def main(args:Array[String]): Unit ={
    val pathToReadableWikipedia = "file://" + args(0)
    val pathToRedirects =  args(1)
    val pathToOutput = "file://" + args(2)
    val language = try { args(3) }catch{
         case _ => {
           println("Warning: Stemming is deactivated..")
           "NoStemmer"
         }
    }


    println("Path to Readable Wikipedia: "+ pathToReadableWikipedia)
    println("Path to Wikipedia Redirects: " + pathToRedirects)
    println("Path to Output Corpus : " + pathToOutput)

    val conf = new SparkConf()
                   .setMaster("local[8]")
                   .setAppName("Wiki2Vec corpus creator")
                   .set("spark.executor.memory", "11G")

    implicit val sc: SparkContext = new SparkContext(conf)


    val redirectStore = try{
      val redirects = new MapRedirectStore(pathToRedirects)
      redirects
    }catch{
      case e:Exception=> {
         e.printStackTrace()
         println("using empty redirect store..")
         new EmptyRedirectStore(pathToRedirects)
      }
    }



    val word2vecCorpusCreator = new Word2VecCorpus(pathToReadableWikipedia, redirectStore, pathToOutput, language)
    word2vecCorpusCreator.getWord2vecCorpus()
  }
}
