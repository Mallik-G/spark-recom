package de.kp.spark.recom
/* Copyright (c) 2014 Dr. Krusche & Partner PartG
* 
* This file is part of the Spark-Recom project
* (https://github.com/skrusche63/spark-recom).
* 
* Spark-Recom is free software: you can redistribute it and/or modify it under the
* terms of the GNU General Public License as published by the Free Software
* Foundation, either version 3 of the License, or (at your option) any later
* version.
* 
* Spark-Recom is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU General Public License for more details.
* You should have received a copy of the GNU General Public License along with
* Spark-Recom. 
* 
* If not, see <http://www.gnu.org/licenses/>.
*/

import akka.actor.{ActorSystem,Props}
import com.typesafe.config.ConfigFactory

import de.kp.spark.core.SparkService
import de.kp.spark.recom.api.{AkkaApi,RestApi}

object RecomServer extends SparkService {
  
  private val sc = createCtxLocal("RecomContext",Configuration.spark)      

  def main(args: Array[String]) {
    
    /**
     * REST API 
     */
    val httpSystem = ActorSystem("http-recommender")
    sys.addShutdownHook(httpSystem.shutdown)
    
    val (host,port) = Configuration.rest
    new RestApi(host,port,httpSystem,sc).start()
 
    println("REST API activated.")
    
    /**
     * AKKA API 
     */
    val conf:String = "server.conf"

    val akkaSystem = ActorSystem("akka-recommender",ConfigFactory.load(conf))
    sys.addShutdownHook(akkaSystem.shutdown)
    
    new AkkaApi(akkaSystem,sc).start()
 
    println("AKKA API activated.")
      
  }

}