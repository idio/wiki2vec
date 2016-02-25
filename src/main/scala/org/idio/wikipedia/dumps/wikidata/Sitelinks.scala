package org.idio.wikipedia.dumps.wikidata

import java.net.URLDecoder


object Sitelinks {


  val dbpediaPatttern = "<https://([a-z-]+).wikipedia.org/wiki/([^>]+)>".r
  val qidPattern = "<http://www.wikidata.org/entity/([^>]+)>".r


  def unquote(uri: String ): String ={
    URLDecoder.decode(uri.replace("+", "%2B"))
  }

  def extractQid(uri: String): String= {
    val qidPattern(qid) = uri
    qid
  }

  def extractDbpediaId(uri: String): (String, String)={

   // <https://kk.wikipedia.org/wiki/%D0%9A%D0%B0%D0%BB%D0%B8%D1%84%D0%BE%D1%80%D0%BD%D0%B8%D1%8F>
    val dbpediaPatttern(language, dbpediaID) = uri
    (language, dbpediaID)
  }



  def getSiteLinkMap(path:String, language: String):Map[String, String]={


    // <https://kk.wikipedia.org/wiki/%D0%9A%D0%B0%D0%BB%D0%B8%D1%84%D0%BE%D1%80%D0%BD%D0%B8%D1%8F> <http://schema.org/about> <http://www.wikidata.org/entity/Q99>
    val dbpediaQidPairs = scala.io.Source.fromFile(path, "UTF-8").getLines().flatMap{line =>

      val splitLine = line.split(" ")
      val subject = splitLine(0)
      val obj = splitLine(2)
      val predicate = splitLine(1)

      if (predicate=="<http://schema.org/about>"){

        val (identifierLang, dbpedia) = extractDbpediaId(unquote(subject))

        if (language==identifierLang){
          val qid = extractQid(obj)
          Some( (dbpedia, qid))
        }else{
          None
        }

      }else{
        None
      }


    }
    dbpediaQidPairs.toMap
  }

}
