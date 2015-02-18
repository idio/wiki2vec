package org.idio.wikipedia.utils

import org.tartarus.snowball.SnowballProgram

trait Stemmer{
  def stem(token:String):String
}

class NoStemmer() extends Stemmer{
  def stem(token:String):String = token
}

class SnowballStemmer(stemmer: SnowballProgram) extends Stemmer{

  def this(s: String) =
    this(Class.forName("org.tartarus.snowball.ext.%s".format(s)).newInstance().asInstanceOf[SnowballProgram])


  def stem(token: String): String = this.synchronized {
    stemmer.setCurrent(token.toLowerCase)
    stemmer.getCurrent
  }

}