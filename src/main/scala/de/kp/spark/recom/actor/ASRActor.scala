package de.kp.spark.recom.actor
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

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

import de.kp.spark.core.Names

import de.kp.spark.core.model._
import de.kp.spark.recom.model._

import de.kp.spark.recom.RemoteContext

import scala.concurrent.Future
import scala.util.control.Breaks._

import scala.collection.mutable.ArrayBuffer

/**
 * ASRActor is responsible for interaction with the Association
 * Analysis engine to build recommendations from association rules
 */
class ASRActor(@transient sc:SparkContext,rtx:RemoteContext) extends RecomWorker(sc) {
  /**
   * Recommendations based on association rules do not need to
   * build user preferences first; therefore, the request is
   * delegated to mining the respective association rules
   */
  def doBuildRequest(req:ServiceRequest) {

    val service = "association"    
    doTrainRequest(new ServiceRequest(service,"train",req.data))
    
  }
  /**
   * In case of association rule based recommendation models, the 
   * term 'model' is equivalent to the respective association rules
   */
  def doTrainRequest(req:ServiceRequest) {
      
    val service = req.service
    val message = Serializer.serializeRequest(req)
    /*
     * Mining association rules is a fire-and-forget task
     * from the recommendation service prespective
     */
    rtx.send(service,message)
    
  }
  
  def doGetRequest(req:ServiceRequest):Future[Any] = {

    val service = "association"
    val message = Serializer.serializeRequest(new ServiceRequest(service,"get:recommendation",req.data))
    
    rtx.send(service,message)
    
  }

  /**
   * A recommendation request is dedicated to a certain 'site' and a list of users, 
   * and the result is a list of rules assigned to this input
   */
  override def buildGetResponse(req:ServiceRequest,intermediate:ServiceResponse):Any = {
    
    if (intermediate.status == ResponseStatus.SUCCESS) {
    
      val site  = req.data(Names.REQ_SITE)
      val users = req.data(Names.REQ_USERS).split(",").toList
      /* 
       * The total number of items returned as recommendations 
       * for each user
       */
      val total = req.data(Names.REQ_TOTAL).toInt
    
      val rules = Serializer.deserializeMultiUserRules(req.data("recommendation")).items
      val preferences = rules.filter(x => x.site == site && users.contains(x.user)).flatMap(entry => {
              
        val (site,user) = (entry.site,entry.user)
        /*
         * Note, that weighted rules are determined by providing a certain 
         * threshold; to determine the respective items, we first take those 
         * items with the heighest weight, highest confidence and finally highest 
         * support
         */
        getPreferences(site,user,entry.items,total)
              
      })
            
      Preferences(preferences)
    
    } else {
      /*
       * In case of an error, send the intermediate message as it contains
       * the specification of the respective failure
       */
      intermediate
      
    }    
    
  }
  
  /**
   * This private method returns preferences from a list of weighted association 
   * rules; the weight is used to specify the intersection of rule based antecedent 
   * and last customer transaction items.
   * 
   * The weighted rules are sorted by weight, confidence and support; the score for
   * each item is computed by multiplying these factors
   */
  private def getPreferences(site:String,user:String,rules:List[WeightedRule],total:Int):List[Preference] = {
    
    val dataset = rules.map(rule => {
      (rule.weight,rule.confidence,rule.support,rule.consequent)
    })
    
    val sorted = dataset.sortBy(x => (-x._1, -x._2, -x._3))
    val len = sorted.length
    
    if (len == 0) return List.empty[Preference]
    
    val preferences = ArrayBuffer.empty[Preference]
    breakable {
      
      (0 until len).foreach( i => {
        
        val (weight,confidence,support,items) = sorted(i)
        val score = weight * confidence * support
        
        items.foreach(item => preferences += Preference(site,user,item,score))
        if (preferences.length >= total) break
      
      })
      
    }

    preferences.toList

  }
  
}