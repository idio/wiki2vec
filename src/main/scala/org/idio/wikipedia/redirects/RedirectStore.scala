package org.idio.wikipedia.redirects

import com.redis.RedisClient


trait RedirectStore {
  def getRedirect(dbpediaId:String) : Option[String]
}

class MapRedirectStore(map:Map[String, String]) extends RedirectStore{
  def getRedirect(dbpediaId:String):Option[String] = map.get(dbpediaId)
}

class RedisRedirectStore(redisHost:String, redisPort:Int = 6379) extends RedirectStore{
  val redis = new RedisClient(redisHost, redisPort)

  def getRedirect(dbpediaId:String) : Option[String]={
    redis.get(dbpediaId)
  }
}
