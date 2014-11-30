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

import de.kp.spark.core.model._
import de.kp.spark.core.elastic.{ElasticBuilderFactory => EBF}

import de.kp.spark.core.io.ElasticIndexer

import de.kp.spark.recom.model._

class RecomIndexer extends BaseActor {
  
  def receive = {
    
    case req:ServiceRequest => {

      val uid = req.data("uid")
      val origin = sender

      try {

        val index   = req.data("index")
        val mapping = req.data("type")
    
        val topic = req.task.split(":")(1) match {
          
          case "event" => "event"
          
          case "item" => "item"
          
          case _ => {
            
            val msg = Messages.TASK_IS_UNKNOWN(uid,req.task)
            throw new Exception(msg)
            
          }
        
        }
        
        val builder = EBF.getBuilder(topic,mapping)
        val indexer = new ElasticIndexer()
    
        indexer.create(index,mapping,builder)
        indexer.close()
      
        val data = Map("uid" -> uid, "message" -> Messages.SEARCH_INDEX_CREATED(uid))
        val response = new ServiceResponse(req.service,req.task,data,ResponseStatus.SUCCESS)	
      
        origin ! response
      
      } catch {
        
        case e:Exception => {
          
          log.error(e, e.getMessage())
      
          val data = Map("uid" -> uid, "message" -> e.getMessage())
          val response = new ServiceResponse(req.service,req.task,data,ResponseStatus.FAILURE)	
      
          origin ! response
          
        }
      
      } finally {
        
        context.stop(self)

      }
    }
    
  }

}