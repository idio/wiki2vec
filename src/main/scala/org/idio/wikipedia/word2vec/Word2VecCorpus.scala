package org.idio.wikipedia.word2vec

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkContext, SparkConf}
import info.bliki.wiki.filter.PlainTextConverter
import info.bliki.wiki.model.WikiModel
import org.idio.wikipedia.dumps.{WikipediaPage, EnglishWikipediaPage}

/**
 * Creates a corpus which can feed to word2vec
 * to extract vectors for each wikipedia Topic.
 *
 * It assumes a "ReadableWikipedia " dump is fed.
 * A Readable Wikipedia dump is defined as one in which every line in hte file follows:
 * [Article Title] [Tab] [Article Text]
 */
class Word2VecCorpus(pathToReadableWiki:String, pathToRedirects:String, pathToOutput:String)(implicit val sc: SparkContext){

  private val PREFIX = "DBPEDIA_ID/"

  // RDD of a readableWikipedia where each line follows the format :
  //  article Title <tab> article text
  private val readableWikipedia = sc.textFile(pathToReadableWiki)

  // RDD (WikiTitle, Article Text)
  private val wikiTitleTexts = getPairRDD(readableWikipedia)

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

        WikipediaPage.readPage(wikiModel, text)

        // cleans wikimedia markup
        val markupClean = wikiModel.getTitle()+" "+wikiModel.getContent()

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
  private def replaceLinksForIds(titleArticleText:RDD[(String, String)])={

    /*
    ** ToDo: Redirections should be taken into account..
    ** Dbpedia has to be checked for redirections and replaced accordingly..
    **/
    def replace(dbpdiaId:String, anchorText:String): String = {
      // Avoiding serializing this for spark..
      val prefix = PREFIX
      " " + prefix + dbpdiaId + " " + anchorText + " "
    }
     // replaces {{linkToWikipediaARticle}} =>  DBPEDIA_ID/wikiPediaTitle
    titleArticleText.map{ case(title,text) =>
      (title, ArticleCleaner.replaceLinks(text, replace))
    }
  }


  /*
  * Dumb Tokenization.
  * Replace this for something smarter
  * */
  private def tokenize(stringRDD:RDD[(String,String)]): RDD[String] ={

    val tokenizedLines = stringRDD.map{
      case (dbedpia, line) =>
        line.replace(",","").replace(".","").replace("“","").replace("\\","").replace("[","").replace("]","").
          replace("‘","").toLowerCase.split("\\s").mkString(" ")
    }
    tokenizedLines
  }


  def getWord2vecCorpus(): Unit ={
    val replacedLinks = replaceLinksForIds(wikiTitleTexts)
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


    println("Path to Readable Wikipedia: "+ pathToReadableWikipedia)
    println("Path to Wikipedia Redirects: " + pathToRedirects)
    println("Path to Output Corpus : " + pathToOutput)

    val conf = new SparkConf()
                   .setMaster("local[8]")
                   .setAppName("Wiki2Vec corpus creator")
                   .set("spark.executor.memory", "11G")

    implicit val sc: SparkContext = new SparkContext(conf)

    val word2vecCorpusCreator = new Word2VecCorpus(pathToReadableWikipedia, pathToRedirects,pathToOutput)
    word2vecCorpusCreator.getWord2vecCorpus()
  }
}
