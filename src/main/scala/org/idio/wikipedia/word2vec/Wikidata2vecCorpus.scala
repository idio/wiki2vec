package org.idio.wikipedia.word2vec

import org.apache.spark.{SparkContext, SparkConf}
import org.idio.wikipedia.dumps.wikidata.Sitelinks


object Wikidata2vecCorpus{

  def main(args:Array[String]) ={
    val pathToWiki2vecCorpus = args(0)
    val pathToWikidataSitelinks = args(1)
    val language = args(2)
    val outputPath = args(3)
    val conf = new SparkConf().setAppName("Wiki2Vec corpus creator")
    implicit val sc: SparkContext = new SparkContext(conf)
    val wikipediaToQid = Sitelinks.getSiteLinkMap(pathToWikidataSitelinks, language)
    val  wikidataCorpusRDD  = new Wikidata2vecCorpus(pathToWiki2vecCorpus, wikipediaToQid).getWikidataCorpus()
    wikidataCorpusRDD.saveAsTextFile(outputPath)
  }

}

class Wikidata2vecCorpus(pathToWiki2vecCorpus: String, wikipediaToQid:Map[String, String])(implicit val sc: SparkContext){



  def getWikidataCorpus() ={
    val corpusRDD = sc.textFile(pathToWiki2vecCorpus)

    val annotationPattern = "DBPEDIA_ID/([^ ]+)".r

    corpusRDD.map {
      article =>

        // search for DBpedia/XX
        // replace for wikidata identifiers

        val annotations = annotationPattern.findAllIn(article)

        val qidAnnotations = annotations.flatMap {
          annotation =>
            val wikipediaId = annotation.replace("DBPEDIA_ID/", "")
            wikipediaToQid.get(wikipediaId) match {
              case Some(qid) => Some(annotation, "DBPEDIA_ID/"+qid)
              case None => None
            }
        }

        qidAnnotations.foldLeft(article) {
          (text: String, pair: (String, String)) =>
            text.replace(pair._1, pair._2)
        }

    }

  }



}
