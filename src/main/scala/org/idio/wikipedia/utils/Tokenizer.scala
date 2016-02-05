package org.idio.wikipedia.utils

import java.io.StringReader
import java.util.Locale

import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer
import org.apache.lucene.analysis.tokenattributes.{CharTermAttribute, CharTermAttributeImpl}
import collection.JavaConversions._
import scala.collection.mutable.ListBuffer

/**
 * Created by dav009 on 13/02/2015.
 */
object Tokenizer{


  def tokenize(sentence: String, language:String) ={

    val l = new Locale(language)
    l match{

      case Locale.CHINESE => tokenizeChinese(sentence)
      case _ => tokenizeAny(sentence)
    }

  }

  def tokenizeChinese(sentence: String): List[String] ={

    val index = sentence.indexOfSlice(" DBPEDIA_ID/")

    if (index > -1){
      val rightHandSideSentence = sentence.slice(index+1, sentence.length)
      val spaceIndex = rightHandSideSentence.indexOfSlice(" ")
      val dbpediaID = rightHandSideSentence.slice(0, spaceIndex)


      val rest = sentence.slice(1 + index + dbpediaID.length, sentence.length)
      val tokenizedSentence = tokenizeZH(sentence.slice(0, index)) ++ List[String](dbpediaID)

      println("DBPEDIA: "+ dbpediaID)
      println("rest:" + rest)
      println("tokenizedSentence:" + tokenizedSentence)

      tokenizedSentence ++ tokenizeChinese(rest)
    }else{
      tokenizeZH(sentence)
    }


  }

   private def tokenizeZH(sentence: String) ={
    val tokenizer = new SmartChineseAnalyzer()
    val tokenStream  = tokenizer.tokenStream("content", new StringReader(sentence))
    val termAttribute = tokenStream.addAttribute(classOf[CharTermAttribute])
    val words = new ListBuffer[String]();

     try {
       tokenStream.reset(); // Resets this stream to the beginning. (Required)
       while (tokenStream.incrementToken()) {
         words.add(termAttribute.toString());
       }
       tokenStream.end(); // Perform end-of-stream operations, e.g. set the final offset.
     }
     finally {
       tokenStream.close(); // Release resources associated with this stream.
     }

     words.toList

   }
   def tokenizeAny(sentence: String): List[String] ={
    sentence.split("\\s").toList
  }
}