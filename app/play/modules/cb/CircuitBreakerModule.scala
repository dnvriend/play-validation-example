/*
 * Copyright 2016 Dennis Vriend
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package play.modules.cb

import akka.actor.ActorSystem
import akka.pattern.CircuitBreaker
import com.google.inject.{ AbstractModule, Provides }

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class CircuitBreakerModule extends AbstractModule {
  override def configure(): Unit = {
    @Provides
    def circuitBreakerProvider(system: ActorSystem)(implicit ec: ExecutionContext): CircuitBreaker = {
      val maxFailures: Int = 3
      val callTimeout: FiniteDuration = 1.seconds
      val resetTimeout: FiniteDuration = 10.seconds
      new CircuitBreaker(system.scheduler, maxFailures, callTimeout, resetTimeout)
    }
  }
}

