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

package com.github.dnvriend.validation

import com.github.dnvriend.TestSpec
import org.typelevel.scalatest.DisjunctionMatchers

import scala.util.Try
import scalaz.Scalaz._
import scalaz._

// a quick domain (don't you just love Scala?)
case class Address(city: String)
case class User(first: String, last: String, address: Option[Address])
case class DBObject(id: Long, user: Option[User])
case class Person(name: String, age: Int)

class ValidationTest extends TestSpec with DisjunctionMatchers {

  /**
   * Take a look at: https://www.parleys.com/tutorial/a-skeptics-look-scalaz-gateway-drugs
   */

  /**
   * Validation:
   * - it looks similar to Disjunction \/ and you can convert between them
   * - it's subtypes are scalaz.Success and scalaz.Failure
   * - it's *not* a Monad!!
   * - it's an Applicative Functor (it adds stuff together, it chains operations)
   * - If any failure in the chain, failure wins: All errors get mashed together
   *
   * Validation will be used in a chain of Validations, if any failure happens, the failure will win,
   * all the "errors will be mashed together".
   */

  val brendan = DBObject(1, Option(User("Brendan", "McAdams", None)))
  val someOtherGuy = DBObject(2, None)
  val brendanCA = DBObject(4, Option(User("Brendan", "McAdams", Option(Address("Sunnyvale")))))
  val cthulu = DBObject(5, Option(User("Cthulu", "Old One", Option(Address("R'lyeh")))))
  val noSuchPerson = DBObject(6, None)
  val jonPretty = DBObject(7, Option(User("Jon", "Pretty", None)))

  /**
   * We can now validate these users:
   *  - Is there a user object?
   *  - Is there an address object?
   */

  def validDBUser(dbObj: DBObject): Validation[String, User] = dbObj.user match {
    case Some(user) => Success(user)
    case _          => Failure(s"DBObject $dbObj does not contain a user object")
  }

  "Validate DB Object" should "be able to validate on user" in {
    validDBUser(brendanCA) should be a 'success
    validDBUser(cthulu) should be a 'success
    validDBUser(noSuchPerson) should be a 'failure
    validDBUser(jonPretty) should be a 'success
  }

  def postOfficeValid(address: Address): Boolean = address match {
    case Address(city) if city == "R'lyeh" => false
    case _                                 => true
  }

  def validAddress(user: Option[User]): Validation[String, Address] = user match {
    case Some(User(_, _, Some(address))) if postOfficeValid(address) => address.success
    case Some(User(_, _, Some(address)))                             => s"Invalid address: address: ${address.city} not recognized by postal service".failure
    case Some(user @ User(_, _, None))                               => s"User: ${user.first} ${user.last} has no defined address".failure
    case None                                                        => "No such user".failure
  }

  "Validate User" should "be able to validate on address" in {
    validAddress(brendanCA.user) should be a 'success
    validAddress(cthulu.user) should be a 'failure
    validAddress(noSuchPerson.user) should be a 'failure
    validAddress(jonPretty.user) should be a 'failure
  }

  /**
   * Applicative operators: add stuff together
   *
   * Scalaz has a number of applicative operators that combine results:
   *
   * "*>" and "<*":
   * - "*>" takes the right hand value and discards the left,
   * - "<*" takes the left hand value and discards the right.
   * - Errors always win
   */

  "applicative *>" should "combine left + right but discard left" in {
    1.some *> 2.some shouldBe Some(2)
    None *> 2.some should not be 'defined
  }

  "applicative <*" should "combine left + right but discard right" in {
    1.some <* 2.some shouldBe Some(1)
    1.some <* None should not be 'defined
  }

  def validateUser(dbuser: DBObject): Validation[String, Address] =
    validDBUser(dbuser) *> validAddress(dbuser.user)

  "validating DB User" should "brendanCA to success" in {
    validateUser(brendanCA) should be a 'success
  }

  it should "cthulu to failure" in {
    validateUser(cthulu) should be a 'failure
    validateUser(cthulu) should matchPattern {
      case Failure("Invalid address: address: R'lyeh not recognized by postal service") =>
    }
  }

  it should "Jon Pretty to failure" in {
    validateUser(jonPretty) should be a 'failure
    validateUser(jonPretty) should matchPattern {
      case Failure("User: Jon Pretty has no defined address") =>
    }
  }

  it should "no such person to failure" in {
    validateUser(noSuchPerson) should be a 'failure
    validateUser(noSuchPerson) should matchPattern {
      case Failure("DBObject DBObject(6,None) does not contain a user objectNo such user") =>
    }

    // !! Note, the "*>" on Validation appends all the errors together
  }

  /**
   * Enter NonEmptyList (NEL)
   *
   * - NonEmptyList (NEL) is just a List which is guaranteed to have at least one element.
   * - You cannot instantiate a NEL without an element!
   * - It is commonly used with Validation to allow accrual of multiple error messages
   * - It is so commonly used in fact, that there exists a simple type alias for a Validation that contains a NonEmptyList
   *   - Validation[NonEmptyList[L], R] => ValidationNEL[L, R] which is nice :)
   * - Append on an NEL will add each element separately
   */

  /**
   * We can be explicit and construct a NonEmptyList, and also declare the types explicitly
   */
  def validDBUserNELExplicit(dbObj: DBObject): Validation[NonEmptyList[String], User] = dbObj.user match {
    case Some(user) => Success(user)
    case _          => Failure(NonEmptyList(s"DBObject $dbObj does not contain a user object"))
  }

  /**
   * Or we can use some helpful shortcuts, call .failureNel and declare the ValidationNel type
   */
  def validDBUserNel(dbObj: DBObject): ValidationNel[String, User] = dbObj.user match {
    case Some(user) => Success(user)
    case _          => s"DBObject $dbObj does not contain a user object".failureNel
  }

  def validAddressNel(user: Option[User]): ValidationNel[String, Address] = user match {
    case Some(User(_, _, Some(address))) if postOfficeValid(address) => address.success
    case Some(User(_, _, Some(address)))                             => s"Invalid address: address: ${address.city} not recognized by postal service".failureNel
    case Some(user @ User(_, _, None))                               => s"User: ${user.first} ${user.last} has no defined address".failureNel
    case None                                                        => "No such user".failureNel
  }

  def validateUserNel(dbObj: DBObject): ValidationNel[String, Address] =
    validDBUserNel(dbObj) *> validAddressNel(dbObj.user)

  "validating using NEL" should "no such person to failure" in {
    validateUserNel(noSuchPerson) should matchPattern {
      case Failure(NonEmptyList(_, _)) =>
    }
  }

  /**
   * A new applicative operator, the |@| or 'Admiral Ackbar'
   *
   * 'Admiral Ackbar' combines all of the success and all of the failure conditions,
   * but we must provide a PartialFunction to combine them
   */

  "combining two validations" should "produce a new output" in {
    val result: ValidationNel[String, String] = (validDBUserNel(brendanCA) |@| validAddressNel(brendanCA.user)) {
      case (user, address) =>
        s"User ${user.first} ${user.last} lives in ${address.city}"
    }
    result should be a 'success
    result should matchPattern {
      case Success("User Brendan McAdams lives in Sunnyvale") =>
    }
  }

  it should "combine three validations" in {
    val result: ValidationNel[String, String] = (validDBUserNel(brendanCA) |@| validDBUserNel(brendanCA) |@| validDBUserNel(brendanCA)) {
      case (user1, user2, user3) => s"${user1.first},${user2.first},${user3.first}"
    }
    result should be a 'success
    result should matchPattern {
      case Success("Brendan,Brendan,Brendan") =>
    }
  }

  /**
   * For example, we want to validate whether a List[String] can be converted to Ints
   */

  "List of String" should "be converted to Int" in {
    def toInt(mayBeInts: List[String]): List[Int] = mayBeInts map (_.toInt)

    intercept[NumberFormatException] {
      toInt(List("x", "y", "1"))
    }

    toInt(List("1", "2", "3")) shouldBe List(1, 2, 3)
  }

  it should "be converted to Int using Try for each element" in {
    def toInt(mayBeInts: List[String]): List[Try[Int]] = mayBeInts.map(x => Try(x.toInt))

    toInt(List("x", "y", "1")) should matchPattern {
      case List(scala.util.Failure(_), scala.util.Failure(_), scala.util.Success(_)) =>
    }

    toInt(List("x", "y", "1")).exists(_.isFailure) should be(true)
    toInt(List("x", "y", "1")).exists(_.isSuccess) should be(true)

    // this is better, we can now accrue the errors, and we have one success!
  }

  it should "be converted as a whole to failure or success using Try" in {
    def toInt(mayBeInts: List[String]): Try[List[Int]] = Try(mayBeInts map (_.toInt))

    toInt(List("x", "y", "1")) should matchPattern {
      case scala.util.Failure(_) =>
    }

    // it only gave us one failure and failed fast. Not able to accrue errors
  }

  it should "be converted and errors accrued using ValidationNel" in {
    def toInts(maybeInts: List[String]): ValidationNel[Throwable, List[Int]] = {
      val validationList = maybeInts map { s =>
        Validation.fromTryCatchNonFatal(s.toInt :: Nil).toValidationNel
      }
      validationList reduce (_ +++ _)
    }

    // validate all cases and reduce the list of validations into
    // a single result where we can access either the successful value
    // or all errors found when parsing
    toInts(List("1", "2", "3")) should matchPattern {
      case Success(List(1, 2, 3)) =>
    }

    toInts(List("1", "2", "3", "x", "z")) should matchPattern {
      case Failure(NonEmptyList(_, _)) =>
    }
  }

  /**
   * Surely it must be able to generalize further than the simple String => Int conversion.
   */

  it should "convert using a general validate method" in {

    // we have the following properties in the method 'validate':
    //
    // 1. F which is a Higher Kinded Type (types that have a type constructor) like eg. List, Option, etc
    // 2. Foldable of F[_] which means that F[_] has the property Foldable, which means that it can be folded
    // 3. A which is a simple type
    // 4. B which is a simple type
    // 5. Monoid of B which means there is proof/logic/implementation that B's can be 'appended/combined'
    //
    // 6. in: which is an F[A] so we have defined that the HKT is an F[A]
    //    which means that we can Fold F[A]'s
    //
    // 7. out: which is a function of A => B. These functions are often used in higher-order-functions
    //      which is a fancy word for functions that accept a function like .map()
    //
    // 8. The return type: ValidationNel[Throwable, B]
    //
    // 9. It uses the foldMap operation that needs a Monoid instance as we don't need (or have a way)
    //    to define an explicit zero because the monoid definition already contains the zero definition
    //    that would be absolutely redundant.
    //
    // phew that is a lot of logic in one line of code!
    def validate[F[_]: Foldable, A, B: Monoid](in: F[A])(out: A => B): ValidationNel[Throwable, B] = {
      in.foldMap(a => Validation.fromTryCatchNonFatal[B](out(a)).toValidationNel)
    }

    // toInts uses the generalized (general/parameterized) method 'validate' to do validation
    // of F[_] types, here a List[String], so F=List and A=String.
    def toInts(maybeInts: List[String]): ValidationNel[Throwable, List[Int]] =
      validate(maybeInts)(_.toInt :: Nil)

    toInts(List("1", "2", "3")) should matchPattern {
      case Success(List(1, 2, 3)) =>
    }

    validate(Option("x"))(_.toInt) should matchPattern {
      case Failure(NonEmptyList(_)) =>
    }

    validate(Option("1"))(_.toInt) should matchPattern {
      case Success(1) =>
    }

    validate(Vector("1"))(_.toInt) should matchPattern {
      case Success(1) =>
    }
  }

  /**
   * Validation is similar (isomorphic) to Either or \/ (Disjunction), but unlike those it has the advantage of
   * allowing error accumulation instead of the *default* fail fast strategy of other types commonly used for
   * error handling.
   */

  it should "validate Person" in {
    def validateName(name: String): ValidationNel[String, String] =
      Option(name).map(_.trim).filter(_.nonEmpty).toSuccessNel("Field name must not be empty")

    def validateAge(age: Int): ValidationNel[String, Int] =
      Option(age).filter(_ >= 0).filter(_ < 110).toSuccessNel(s"Field age must be gt 0 and lt 110 but is '$age'")

    (validateName("") |@| validateAge(42))(Person.apply) should haveFailure("Field name must not be empty")
  }

  it should "parse values and return validations which is part of 'scalaz.syntax.std.StringOps'" in {
    "true".parseBoolean shouldBe Success(true)
    "false".parseBoolean shouldBe Success(false)
    "TRUE".parseBoolean shouldBe Success(true)
    "FALSE".parseBoolean shouldBe Success(false)
    "FOO".parseBoolean should matchPattern { case Failure(t: IllegalArgumentException) => }
    "6".parseInt shouldBe 6.success
    "6".parseLong shouldBe 6L.success
    "6".parseDouble shouldBe 6.0.success
    "6".parseBigInt shouldBe BigInt(6).success
    "6".parseBigDecimal shouldBe BigDecimal(6.0).success

    "foo".parseInt should matchPattern { case Failure(t: NumberFormatException) => }
    "foo".parseLong should matchPattern { case Failure(t: NumberFormatException) => }
  }

  it should "success" in {
    "".successNel shouldBe
      Success("")
  }

  it should "twoSuccess" in {
    ("".successNel[String] *> "".successNel) shouldBe
      Success("")
  }

  it should "threeSuccess" in {
    ("".successNel[String] *> "".successNel *> "".successNel) shouldBe
      Success("")
  }

  it should "oneFailure" in {
    "failure a".failureNel shouldBe
      Failure(NonEmptyList("failure a"))
  }

  it should "twoFailures" in {
    ("failure a".failureNel *> "failure b".failureNel) shouldBe
      Failure(NonEmptyList("failure a", "failure b"))
  }

  it should "threeFailures" in {
    ("failure a".failureNel *> "failure b".failureNel *> "failure c".failureNel) shouldBe
      Failure(NonEmptyList("failure a", "failure b", "failure c"))
  }

  it should "failSuccessFail1" in {
    ("".successNel[String] *> "failure b".failureNel *> "failure c".failureNel) shouldBe
      Failure(NonEmptyList("failure b", "failure c"))
  }

  it should "failSuccessFail2" in {
    ("failure a".failureNel *> "".successNel *> "failure c".failureNel) shouldBe
      Failure(NonEmptyList("failure a", "failure c"))
  }

  it should "failSuccessFail3" in {
    ("failure a".failureNel *> "failure b".failureNel *> "".successNel) shouldBe
      Failure(NonEmptyList("failure a", "failure b"))
  }

  it should "fromTryCatchNonFatal" in {
    (Validation.fromTryCatchNonFatal[Int](1 / 0).leftMap(t ⇒ t.getMessage).toValidationNel[String, Int] *>
      Validation.fromTryCatchNonFatal[Int](1).leftMap(t ⇒ t.getMessage).toValidationNel[String, Int]) shouldBe
      Failure(NonEmptyList("/ by zero"))
  }

  it should "construct a validation from a scala.util.Try success path" in {
    scala.util.Try(1).toValidationNel shouldBe Success(1)
  }

  it should "construct a validation from a scala.util.Try failure path" in {
    val err: Throwable = new RuntimeException("foo")
    scala.util.Try(throw err).toValidationNel *> scala.util.Try(throw err).toValidationNel shouldBe
      Failure(NonEmptyList(err, err))
  }

  it should "validation should accumulate errors on the left side and create a disjunction containing all errors on the left side" in {
    ("err1".failureNel[Unit] *> "err2".failureNel[Unit] *> "err3".failureNel[Unit]).disjunction should beLeft(NonEmptyList("err1", "err2", "err3"))
  }

  it should "validate case class success flow" in {
    case class Foo(x: String)
    Foo("x").successNel[String] *>
      Foo("y").successNel[String] *>
      Foo("z").successNel[String] shouldBe Success(Foo("z"))
  }

  it should "validate case class failure flow" in {
    case class Foo(x: String)
    Foo("x").successNel[String] *>
      "wtf".failureNel *>
      Foo("z").successNel[String] *>
      "rtfm".failureNel shouldBe Failure(NonEmptyList("wtf", "rtfm"))
  }

  it should "fold a list of validation case classes failure flow" in {
    case class Foo(x: String)
    NonEmptyList(
      Foo("x").successNel[String],
      "wtf".failureNel,
      Foo("z").successNel[String],
      "rtfm".failureNel
    ).foldLeft(Foo("x").successNel[String]) {
      case (acc, v) => acc *> v
    } shouldBe Failure(NonEmptyList("wtf", "rtfm"))
  }

  it should "fold a list of validation case classes success flow" in {
    case class Foo(x: String)
    NonEmptyList(
      Foo("x").successNel[String],
      Foo("y").successNel[String],
      Foo("z").successNel[String]
    ).foldLeft(Foo("x").successNel[String]) {
      case (acc, v) => acc *> v
    } shouldBe Success(Foo("z"))
  }

  it should "Fold List Of Failures" in {
    NonEmptyList(
      "failure a".failureNel[String],
      "failure b".failureNel[String],
      "failure c".failureNel[String]
    ).foldLeft(List.empty[String].successNel[String]) {
      case (acc, v) ⇒ (acc |@| v)(_ :+ _)
    } shouldBe Failure(NonEmptyList("failure a", "failure b", "failure c"))
  }

  it should "accumulate validations using traverseU which is map+sequence" in {
    def validate(msg: String): ValidationNel[String, String] = s"failure $msg".failureNel[String]
    val listToValidate = NonEmptyList("a", "b", "c")
    listToValidate.traverseU(validate) shouldBe
      Failure(NonEmptyList("failure a", "failure b", "failure c"))
  }

  it should "accumulate validations using sequenceU turining a List[ValidationNel[String, A]] into a ValidationNel[String, List[A]] failure case" in {
    List("failure a".failureNel, "failure b".failureNel, "failure c".failureNel).sequenceU shouldBe
      Failure(NonEmptyList("failure a", "failure b", "failure c"))
  }

  it should "accumulate validations using sequenceU turining a List[ValidationNel[String, A]] into a ValidationNel[String, List[A]] success case" in {
    List("success a".successNel[String], "success b".successNel[String], "success c".successNel[String]).sequenceU shouldBe
      Success(List("success a", "success b", "success c"))
  }

  it should "fromTryCatchNonFatal in for comprehension must be a disjunction as Validation is not a Monad but an Applicative" in {
    def throwException: Int = throw new RuntimeException("test")
    (for {
      a ← Validation.fromTryCatchNonFatal(1).disjunction.leftMap(_.getMessage)
      b ← Validation.fromTryCatchNonFatal[Int](throwException).disjunction.leftMap(_.getMessage)
    } yield a + b).validationNel shouldBe Failure(NonEmptyList("test"))
  }
}
