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
import akka.actor.{ActorRef,Props}

import akka.pattern.ask
import akka.util.Timeout

import akka.actor.{OneForOneStrategy, SupervisorStrategy}
import de.kp.spark.core.model._

import de.kp.spark.recom.Configuration
import de.kp.spark.recom.model._

import scala.concurrent.duration.DurationInt
import scala.concurrent.Future

class RecomMaster(@transient val sc:SparkContext) extends BaseActor {
  
  val (duration,retries,time) = Configuration.actor   
      
  implicit val ec = context.dispatcher
  implicit val timeout:Timeout = DurationInt(time).second

  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries=retries,withinTimeRange = DurationInt(time).minutes) {
    case _ : Exception => SupervisorStrategy.Restart
  }
  
  def receive = {
    
    case req:String => {
	  	    
	  val origin = sender

	  val deser = Serializer.deserializeRequest(req)
	  val response = execute(deser)
	  
      response.onSuccess {
        case result => origin ! Serializer.serializeResponse(result)
      }
      response.onFailure {
        case result => origin ! failure(deser,Messages.GENERAL_ERROR(deser.data("uid")))	      
	  }
      
    }
    
    case req:ServiceRequest => {
	  	    
	  val origin = sender

	  val response = execute(req)
      response.onSuccess {
        case result => origin ! Serializer.serializeResponse(result)
      }
      response.onFailure {
        case result => origin ! failure(req,Messages.GENERAL_ERROR(req.data("uid")))	      
	  }
      
    }
  
    case _ => {

      val origin = sender               
      val msg = Messages.REQUEST_IS_UNKNOWN()          
          
      origin ! Serializer.serializeResponse(failure(null,msg))

    }
    
  }

  private def execute(req:ServiceRequest):Future[ServiceResponse] = {
	  
    req.task.split(":")(0) match {

      case "get" => ask(actor("questor"),req).mapTo[ServiceResponse]
      case "index" => ask(actor("indexer"),req).mapTo[ServiceResponse]

      case "register"  => ask(actor("registrar"),req).mapTo[ServiceResponse]
      case "status" => ask(actor("miner"),req).mapTo[ServiceResponse]

      case "train"  => ask(actor("miner"),req).mapTo[ServiceResponse]
      case "track"  => ask(actor("tracker"),req).mapTo[ServiceResponse]
       
      case _ => Future {     
        failure(req,Messages.TASK_IS_UNKNOWN(req.data("uid"),req.task))
      }
      
    }
    
  }
  
  private def actor(worker:String):ActorRef = {
    
    worker match {
      // TODO
      case _ => null
      
    }
  
  }

}