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

package com.github.dnvriend.scalactic

import com.github.dnvriend.SimpleSpec
import ScalacticAccumulationTest._
import org.scalactic._
import OptionSugar._
import Accumulation._

import scala.util.Try

object ScalacticAccumulationTest {
  case class Person(name: String, age: Int)
  def parseName(name: String): Or[String, One[ErrorMessage]] = {
    Option(name).map(_.trim).filterNot(_.isEmpty).toOr(One("Field 'name' is empty"))
  }

  def ageNonEmpty(age: String): Or[String, One[ErrorMessage]] = {
    Option(age).map(_.trim).filterNot(_.isEmpty).toOr(One("Field 'age' is empty"))
  }

  def ageGtZero(age: Int): Or[Int, One[ErrorMessage]] = {
    Option(age).filterNot(_ <= 0).toOr(One(s"$age is not a valid age"))
  }

  def parseAge(age: String): Or[Int, One[ErrorMessage]] = for {
    nonEmptyAge <- ageNonEmpty(age)
    numericAge <- Or.from(Try(nonEmptyAge.toInt)).badMap(t => One(t.toString))
    validAge <- ageGtZero(numericAge)
  } yield validAge

  def parsePerson(name: String, age: String): Or[Person, Every[ErrorMessage]] = {
    val validName = parseName(name)
    val validAge = parseAge(age)
    withGood(validName, validAge)(Person.apply)
  }
}

class ScalacticAccumulationTest extends SimpleSpec {
  it should "parse a person" in {
    parsePerson("Dennis", "42") shouldBe Good(Person("Dennis", 42))
  }

  it should "parse with empty name" in {
    parsePerson("", "42") shouldBe Bad(One("Field 'name' is empty"))
  }

  it should "parse with empty age" in {
    parsePerson("", "") shouldBe Bad(Many("Field 'name' is empty", "Field 'age' is empty"))
  }

  it should "parse with invalid age" in {
    parsePerson("", "0") shouldBe Bad(Many("Field 'name' is empty", "0 is not a valid age"))
  }

  it should "parse with invalid alpha age" in {
    parsePerson("", "abcd") shouldBe Bad(Many("Field 'name' is empty", """java.lang.NumberFormatException: For input string: "abcd""""))
  }
}
