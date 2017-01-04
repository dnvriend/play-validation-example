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

package com.github.dnvriend.mymonad

import com.github.dnvriend.TestSpec

case class MyMonad[A](a: A) {
  def map[B](f: A => B): MyMonad[B] = MyMonad(f(a))
  def flatMap[B](f: A => MyMonad[B]): MyMonad[B] = f(a)
}

class MyMonadTest extends TestSpec {
  it should "be composed" in {
    val result: MyMonad[Int] = for {
      res1 <- MyMonad(1)
      res2 <- MyMonad(2)
      res3 <- MyMonad(3)
    } yield res1 + res2 + res3

    result shouldBe MyMonad(6)
  }
}

object MyOtherMonad {
  def apply[A](a: A): MyOtherMonad[A] =
    if (a == null) MyLeftMonad else MyRightMonad(a)
  def left[A]: MyOtherMonad[A] = MyLeftMonad
  def empty[A]: MyOtherMonad[A] = left
}
abstract class MyOtherMonad[+A] {
  def isLeft: Boolean
  def get: A
  def map[B](f: A => B): MyOtherMonad[B] =
    if (isLeft) MyLeftMonad else MyRightMonad(f(get))
  def flatMap[B](f: A => MyOtherMonad[B]): MyOtherMonad[B] =
    if (isLeft) MyLeftMonad else f(get)
}

final case class MyRightMonad[+A](a: A) extends MyOtherMonad[A] {
  override def isLeft: Boolean = false
  override def get: A = a
}

case object MyLeftMonad extends MyOtherMonad[Nothing] {
  override def isLeft: Boolean = true
  override def get = throw new NoSuchElementException("MyLeftMonad.get")
}

class MyOtherMonadTest extends TestSpec {
  it should "compose MyOtherMonad" in {
    val result: MyOtherMonad[Int] = for {
      res1 <- MyOtherMonad(2)
      res2 <- MyOtherMonad(3)
      res3 <- MyOtherMonad(4)
    } yield res1 + res2 + res3

    result shouldBe MyRightMonad(9)
  }

  it should "have a failure situation" in {
    val result: MyOtherMonad[Int] = for {
      res1 <- MyOtherMonad(1)
      res2 <- MyOtherMonad.empty[Int] // explicit stop computation here
      res3 <- MyOtherMonad(2)
    } yield res1 + res2 + res3

    result shouldBe MyLeftMonad
  }
}