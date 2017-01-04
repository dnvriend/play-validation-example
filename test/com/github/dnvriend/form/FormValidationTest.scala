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

package com.github.dnvriend.form

import com.github.dnvriend.TestSpec
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.{ JsValue, Json }
import scalaz._
import Scalaz._

object Person {
  implicit val format = Json.format[Person]
}
final case class Person(name: String, age: Int, email: String)

class FormValidationTest extends TestSpec {
  //
  // Play’s form handling approach is based around the concept of binding data.
  // When data comes in from a POST request, Play will look for formatted values
  // and bind them to a Form object.
  //
  // From there, Play can use the bound form to value a case class with data,
  // call custom validations, and so on.
  //

  def toValidation[A](form: Form[A]): ValidationNel[String, A] = {
    def fromFormError(formError: FormError): String = {
      val key: String = formError.key
      val messages: String = formError.messages.mkString(",")
      val args: Option[String] = if (formError.args.isEmpty) Option.empty[String] else Option(formError.args.map(_.toString).mkString(","))
      val argsMsg: String = args.map(msg => s" -> $msg") | ""
      s"$key -> $messages$argsMsg"
    }
    if (form.errors.isEmpty) form.value.toSuccessNel("")
    else (form.errors.map(fromFormError).toList.toNel | NonEmptyList("")).failure[A]
  }

  def toDisjunction[A](form: Form[A]): Disjunction[String, A] =
    toValidation(form).leftMap(_.toList.mkString(",")).disjunction

  it should "validate with no constraint" in {
    val form: Form[Person] = Form(mapping("name" -> text, "age" -> number, "email" -> text)(Person.apply)(Person.unapply))
    val person = Person("", -1, "")
    val personAsJson: JsValue = Json.toJson(person)
    val boundedForm: Form[Person] = form.bind(personAsJson)
    boundedForm.hasErrors shouldBe false
    boundedForm.errors shouldBe empty

    val validation: Validation[NonEmptyList[String], Person] = toValidation(boundedForm)
    val disjunction: Disjunction[String, Person] = toDisjunction(boundedForm)

    validation should beSuccess(person)
    disjunction should beRight(person)
  }

  //
  // The constraints that are available are:
  //
  // text: maps to scala.String, optionally takes minLength and maxLength.
  // nonEmptyText: maps to scala.String, optionally takes minLength and maxLength.
  // number: maps to scala.Int, optionally takes min, max, and strict.
  // longNumber: maps to scala.Long, optionally takes min, max, and strict.
  // bigDecimal: takes precision and scale.
  // date, sqlDate, jodaDate: maps to java.util.Date, java.sql.Date and org.joda.time.DateTime, optionally takes pattern and timeZone.
  // jodaLocalDate: maps to org.joda.time.LocalDate, optionally takes pattern.
  // email: maps to scala.String, using an email regular expression.
  // boolean: maps to scala.Boolean.
  // checked: maps to scala.Boolean.
  // optional: maps to scala.Option.

  // The 'text' constraint considers empty strings to be valid.
  // This means that name could be empty here without an error,
  // which is not what we want.
  //
  // A way to ensure that name has the appropriate value is to use the nonEmptyText constraint.
  //

  it should "validate with constraints with nonempty name and age min=0 and max = 110" in {
    val form: Form[Person] = Form(mapping("name" -> nonEmptyText, "age" -> number(min = 0, max = 110), "email" -> text)(Person.apply)(Person.unapply))
    val person = Person("", -1, "")

    val personAsJson: JsValue = Json.toJson(person)
    val boundedForm: Form[Person] = form.bind(personAsJson)
    boundedForm.hasErrors shouldBe true
    boundedForm.errors should not be empty

    val validation: Validation[NonEmptyList[String], Person] = toValidation(boundedForm)
    val disjunction: Disjunction[String, Person] = toDisjunction(boundedForm)

    validation should (haveFailure("name -> error.required") and haveFailure("age -> error.min -> 0"))
    disjunction should beLeft("name -> error.required,age -> error.min -> 0")
  }

  it should "validate with email constraint and invalid mail adres" in {
    val form: Form[Person] = Form(mapping("name" -> text, "age" -> number, "email" -> email)(Person.apply)(Person.unapply))
    val person = Person("", -1, "")

    val personAsJson: JsValue = Json.toJson(person)
    val boundedForm: Form[Person] = form.bind(personAsJson)
    boundedForm.hasErrors shouldBe true
    boundedForm.errors should not be empty

    val validation: Validation[NonEmptyList[String], Person] = toValidation(boundedForm)
    val disjunction: Disjunction[String, Person] = toDisjunction(boundedForm)

    validation should haveFailure("email -> error.email")
    disjunction should beLeft("email -> error.email")
  }

  it should "validate with email constraint and valid mail adres" in {
    val form: Form[Person] = Form(mapping("name" -> text, "age" -> number, "email" -> email)(Person.apply)(Person.unapply))
    val person = Person("", -1, "dnvriend@gmail.com")

    val personAsJson: JsValue = Json.toJson(person)
    val boundedForm: Form[Person] = form.bind(personAsJson)
    boundedForm.hasErrors shouldBe false
    boundedForm.errors shouldBe empty

    val validation: Validation[NonEmptyList[String], Person] = toValidation(boundedForm)
    val disjunction: Disjunction[String, Person] = toDisjunction(boundedForm)

    validation should beSuccess(person)
    disjunction should beRight(person)
  }
}
