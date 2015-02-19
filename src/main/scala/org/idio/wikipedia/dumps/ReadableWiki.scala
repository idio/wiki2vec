package org.idio.wikipedia.dumps

import java.io._

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream
import com.stratio.parsers.XMLDumpParser
import com.stratio.callbacks.{RevisionCallback, PageCallback}
import com.stratio.data.{Revision, Page}

class ReadableWiki(pathToWikipediaDump: String, pathToOutFile: String){

    /*
    * Wikipedia dumps are compressed as bz2 files.
    * This reads directly from a compressed wikipedia dump and returns a stream
    **/
    private def getWikipediaStream(wikipediaXmlDump: String) ={
      val fin = new FileInputStream(wikipediaXmlDump);
      val bis = new BufferedInputStream(fin);
      new BZip2CompressorInputStream(bis, true)
    }

    /*
    * Generates a WikiDump.
    * Given the following format:
    *  title <tab> article text...
    *  title <tab> article text...
    * */
    def createReadableWiki(): Unit ={

      val writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pathToOutFile), "UTF-8"))
      val parser = new XMLDumpParser(getWikipediaStream(pathToWikipediaDump))
      var counter = 0

      parser.getContentHandler.setRevisionCallback(new RevisionCallback {
        override def callback(revision: Revision): Unit = {
          val page = revision.getPage
          val title = page.getTitle
          val articleText =  revision.getText.replace("\t", " ").replace("\n"," ")
          val line = List[String](title, articleText).mkString("\t") + "\n"
          writer.write(line)
          counter = counter + 1
          println(counter)
        }
      })
      parser.parse()
      writer.close()
    }

}

object CreateReadableWiki{
    def main(args:Array[String])={
      val wikiPediaXmlDump = args(0)
      val outputFile = args(1)
      new ReadableWiki(wikiPediaXmlDump, outputFile).createReadableWiki()
    }
}
