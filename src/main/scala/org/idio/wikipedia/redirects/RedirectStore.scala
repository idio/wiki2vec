package org.idio.wikipedia.redirects

import java.net.URLDecoder

import com.redis.RedisClient

import scala.io.Source

@SerialVersionUID(123L)
trait RedirectStore extends Serializable {
  // checks the store looking for a redirect
  def getRedirect(dbpediaId:String) : Option[String]

  // If it finds a redirect returns it, otherwise returns the same string given as input
  def getCanonicalId(dbpediaId:String): String ={
     this.getRedirect(dbpediaId) match {
       case None => dbpediaId
       case Some(redirect) => redirect
     }
  }

}

@SerialVersionUID(123L)
class EmptyRedirectStore() extends Serializable with RedirectStore{
  def getRedirect(dbpediaId:String):Option[String] = Some(dbpediaId)
  def this(pathToFile: String) = this()
}

@SerialVersionUID(123L)
class MapRedirectStore(map:Map[String, String])  extends Serializable with RedirectStore{
  def getRedirect(dbpediaId:String):Option[String] = map.get(dbpediaId)

  def this(pathToFile: String) = this(RedirectStore.readFile(pathToFile).toMap)

}

@SerialVersionUID(123L)
class RedisRedirectStore(redisHost:String, redisPort:Int, redirects:Iterator[(String, String)])  extends Serializable with RedirectStore{
  @transient val redis = new RedisClient(redisHost, redisPort)

  // filling redis DB
  redirects.foreach{
    case (source:String, target:String) =>
         redis.set(source, target)
  }

  def getRedirect(dbpediaId:String) : Option[String]={
    redis.get(dbpediaId)
  }

  def this(pathToFile: String, redisHost:String = "localhost", redisPort:Int = 6379) = this(redisHost, redisPort, RedirectStore.readFile(pathToFile))
}

object RedirectStore{

  def readFile(pathToRedirectFile: String) = {
     Source.fromFile(pathToRedirectFile, "UTF-8").getLines().map{
          line =>
            val entityRegex = "<http://dbpedia.org/resource/([^ >]+)>".r
            val matches = entityRegex.findAllIn(line).matchData
            if(matches.hasNext){
                val origin = URLDecoder.decode(matches.next().group(1), "utf-8")
                if(matches.hasNext){
                    val destination = URLDecoder.decode(matches.next().group(1), "utf-8")
                    //print(origin + "-->" + destination)
                    (origin, destination)
                }else{
                    ("nothing", "nothing")
                }
            }else{
                ("nothing", "nothing")
            }

     }
  }

}
