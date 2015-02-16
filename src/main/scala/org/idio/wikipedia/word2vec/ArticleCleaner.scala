package org.idio.wikipedia.word2vec
import java.util.regex.Matcher.quoteReplacement
import scala.util.matching.Regex

object ArticleCleaner {

  /*
  *  Parses a WikiLink in MediaWiki's format
  *  Returns a pair (Surface Form, DbpediaID)
  * */
  def parseWikimediaLink(matchedLink:String): (String,String) ={

    val split = matchedLink.replace("""[[""", "").replace("""]]""","").split("""\|""").toList

    // [[ Dbpedia Title]]
    if (split.length == 1) {
      val surfaceForm = split(0).trim()
      val dbpediaId = surfaceForm.replace(" ", "_")
      (surfaceForm, dbpediaId)
    }
    else{
      // [[Dbpedia Title | anchor]]
      val surfaceForm = split.toList.last.trim()
      val dbpediaId = split(0).trim().replace(" ", "_")
      (surfaceForm, dbpediaId)
    }

  }

  /*
  * Given a wikimedia article's text
  * It replaces all intra-wiki links ( [[Dbpedia Title]] and [[Dbpedia Title|Anchor]]) in Text
  * for :  processLink(processedDbpediaTitle, AnchorText)
  * */
  def replaceLinks(text:String, processLink: (String, String) => String): String ={

    // regex to find links to other wiki articles
    val linksRegex = """\[\[([^:\[\]])+\]\]""".r

      linksRegex.replaceAllIn(quoteReplacement(text), linkMatch => {
        try {
          val (surfaceForm, dbpediaId) = parseWikimediaLink(linkMatch.toString())
          processLink(surfaceForm, dbpediaId)
        } catch {
          case _ => {
            println("error with link: " + linkMatch.toString())
            ""
          }
        }
      })

  }

  /*
  * Given a wikimedia article's text
  * Cleans links to external sources
  * */
  def cleanCurlyBraces(text:String): String ={
    // regex to find external links
    val curlyBracesRegex = """\{\{([^:\[\]])+\}\}""".r
    curlyBracesRegex.replaceAllIn(text, "")
  }

  def cleanStyle(text:String):String ={
    // clean css style encoded as: {| class "wikitable" style "float: right; margin-left: 1em;"
    val styleRegex = """\{\|.*\|\}""".r
    styleRegex.replaceAllIn(text, "")
  }

}
