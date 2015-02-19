package org.idio.wikipedia.utils
import java.util.Locale

trait Stemmer{
  def stem(token:String):String
}

class NoStemmer() extends Stemmer{
  def stem(token:String):String = token
}

class SnowballStemmer(locale:Locale) extends Stemmer{

  private val stemmerName = locale.getDisplayLanguage().toLowerCase() +"Stemmer"
  val stemmer = Class.forName("org.tartarus.snowball.ext.%s".format(stemmerName)).newInstance().asInstanceOf[org.tartarus.snowball.SnowballStemmer]

  def this(language: String) =  this(new Locale(language))

  def stem(token: String): String = this.synchronized {
    stemmer.setCurrent(token.toLowerCase)
    stemmer.stem()
    stemmer.getCurrent
  }

}