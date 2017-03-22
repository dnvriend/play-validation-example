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
import com.github.dnvriend.scalactic.ScalacticOptionTest._
import org.scalactic.OptionSugar._
import org.scalactic._

object ScalacticOptionTest {
  case class Person(name: String, age: Option[Int])

  def ageOfOpt(maybePerson: Option[Person]): Option[Int] = for {
    person <- maybePerson
    age <- person.age
  } yield age

  def ageOf(maybePerson: Option[Person]): Or[Int, String] = for {
    person <- maybePerson.toOr("no person here")
    age <- person.age.toOr("ageless person")
  } yield age
}

class ScalacticOptionTest extends SimpleSpec {
  it should "determine the age of a person" in {
    ageOfOpt(Option(Person("Ralph", Option(42)))).value shouldBe 42
  }

  it should "monadic composition should shortcut with no usable value" in {
    ageOfOpt(Option(Person("Curt", Option.empty[Int]))) should not be 'defined
  }

  it should "determine the age of a person and return an Or" in {
    ageOf(Option(Person("Ralph", Option(42)))) shouldBe Good(42)
  }

  it should "return a usable error message when no age is available" in {
    ageOf(Option(Person("Curt", Option.empty[Int]))) shouldBe Bad("ageless person")
  }

  it should "return a usable error message when no person is available" in {
    ageOf(Option.empty[Person]) shouldBe Bad("no person here")
  }
}
