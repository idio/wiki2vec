package org.idio.wikipedia.redirects

import com.bizo.mighty.csv.CSVDictReader
import com.redis.RedisClient


object Redirect {

  type Redirect = (String, String)

  /*
  *
  * Reads a Redirect map.
  * */
  def loadRedirects[T](pathToCSVFile:String, callBack: (Iterator[Redirect]) => T): T ={
    val rows = CSVDictReader(pathToCSVFile)
    val tuples = rows.map{
      row:Map[String, String] =>
        (row.get("page_id").get, row.get("page_title").get)
    }
    callBack(tuples)

  }

  /*
  * Reads a redirect csv file and loads it into a Scala map
  * */
  def loadRedirectsToMap(pathToCSVFile:String): RedirectStore ={

    def loadToMap(redirects:Iterator[Redirect]): Map[String, String] ={
      redirects.toMap
    }

    val redirectsMap = loadRedirects(pathToCSVFile, loadToMap)
    new MapRedirectStore(redirectsMap)

  }

  /*
  * Reads a redirect csv file  and loads it into a Redis
  * */
  def loadToRedis(pathToCSVFile:String, redisHost: String, redisPort:Int = 6379): RedirectStore={
    val redis = new RedisClient(redisHost, redisPort)
    def loadToRedis(redirects:Iterator[Redirect]): Unit ={

      redirects.foreach{
        redirect =>
          redis.set(redirect._1, redirect._2)
      }

    }
    loadRedirects(pathToCSVFile, loadToRedis)

    new RedisRedirectStore(redisHost, redisPort)
  }

}