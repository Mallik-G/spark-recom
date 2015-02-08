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

import com.typesafe.config.ConfigFactory
import org.apache.hadoop.conf.{Configuration => HConf}

import de.kp.spark.core.{Configuration => CoreConf}

object Configuration extends Serializable with CoreConf {

    /* Load configuration for router */
  val path = "application.conf"
  val config = ConfigFactory.load(path)

  override def actor:(Int,Int,Int) = {
  
    val cfg = config.getConfig("actor")

    val duration = cfg.getInt("duration")
    val retries = cfg.getInt("retries")  
    val timeout = cfg.getInt("timeout")
    
    (duration,retries,timeout)
    
  }
   
  def cache:Int = {
  
    val cfg = config.getConfig("cache")
    cfg.getInt("size")   
    
  }
 
  override def cassandra:Map[String,String] = {
   
    val cfg = config.getConfig("cassandra")
    val conf = Map(
      "spark.cassandra.connection.host" -> cfg.getString("spark.cassandra.connection.host")
    )                          

    conf
     
  }

  override def elastic:HConf = {
  
    val cfg = config.getConfig("elastic")
    val conf = new HConf()                          

    conf.set("es.nodes",cfg.getString("es.nodes"))
    conf.set("es.port",cfg.getString("es.port"))

    conf
    
  }

  override def hbase:Map[String,String] = {
   
    val cfg = config.getConfig("hbase")
    val conf = Map(
      "spark.hbase.host" -> cfg.getString("spark.hbase.host")
    )                          

    conf
     
  }
    
  override def input:List[String] = {
  
    val cfg = config.getConfig("file")
    
    val event = cfg.getString("event")   
    val item  = cfg.getString("item")   
    
    List(event,item)
    
  }
   
  def model:String = {
  
    val cfg = config.getConfig("model")
    cfg.getString("path")   
    
  }
 
  override def mongo:HConf = {
   
    val cfg = config.getConfig("mongo")
    val conf = new HConf()                          

    conf.set("mongo.input.uri",cfg.getString("mongo.input.uri"))
    conf
     
  }
 
  override def mysql:(String,String,String,String) = {

   val cfg = config.getConfig("mysql")
  
   val url = cfg.getString("url")
   val db  = cfg.getString("database")
  
   val user = cfg.getString("user")
   val password = cfg.getString("password")
    
   (url,db,user,password)
   
  }
  
  override def output:List[String] = null
  
  override def redis:(String,String) = {
  
    val cfg = config.getConfig("redis")
    
    val host = cfg.getString("host")
    val port = cfg.getString("port")
    
    (host,port)
    
  }

  override def rest:(String,Int) = {
      
    val cfg = config.getConfig("rest")
      
    val host = cfg.getString("host")
    val port = cfg.getInt("port")

    (host,port)
    
  }
  
  override def spark:Map[String,String] = {
  
    val cfg = config.getConfig("spark")
    
    Map(
      "spark.executor.memory"          -> cfg.getString("spark.executor.memory"),
	  "spark.kryoserializer.buffer.mb" -> cfg.getString("spark.kryoserializer.buffer.mb")
    )

  }
  
}