package de.kp.spark.recom.api
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
import akka.actor.{ActorSystem,Props}

import de.kp.spark.recom.actor.RecomMaster
/**
 * The Akka API is used to receive messages from Akka engines
 * that are used in collaboration by the Recommender System.
 * 
 * These services are:
 * 
 * 1) Association Analysis Engine
 * 
 * 2) Context-Aware Analysis Engine
 * 
 * 3) User Preference Engine
 * 
 */
class AkkaApi(system:ActorSystem,@transient sc:SparkContext) {

  val master = system.actorOf(Props(new RecomMaster(sc)), name="recom-master")

  def start() {
     while (true) {}   
  }
}