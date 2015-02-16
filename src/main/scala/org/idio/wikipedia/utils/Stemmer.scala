package org.idio.wikipedia.utils

import org.tartarus.snowball.SnowballProgram

class Stemmer(stemmer: SnowballProgram) {

  def this(s: String) =
    this(Class.forName("org.tartarus.snowball.ext.%s".format(s)).newInstance().asInstanceOf[SnowballProgram])


  def stem(token: String): String = this.synchronized {
    stemmer.setCurrent(token.toLowerCase)
    stemmer.getCurrent
  }

}