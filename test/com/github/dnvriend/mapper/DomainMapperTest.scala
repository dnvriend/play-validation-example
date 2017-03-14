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

package com.github.dnvriend.mapper

import com.github.dnvriend.TestSpec

class DomainMapperTest extends TestSpec {

  object Converter {
    def apply[A, B](implicit conv: Converter[A, B]): Converter[A, B] = conv
  }

  trait Converter[A, B] {
    def convert(value: A): B
  }

  case class DomainA(x: Int) // rest (DTO)
  case class DomainB(x: Int) // application
  case class DomainC(x: Int) // domain

  object DomainB {
    implicit val ab = new Converter[DomainA, DomainB] {
      override def convert(value: DomainA): DomainB =
        DomainB(value.x)
    }

    implicit val bc = new Converter[DomainB, DomainC] {
      override def convert(value: DomainB): DomainC =
        DomainC(value.x)
    }

    implicit val cb = new Converter[DomainC, DomainB] {
      override def convert(value: DomainC): DomainB =
        DomainB(value.x)
    }

    implicit val ba = new Converter[DomainB, DomainA] {
      override def convert(value: DomainB): DomainA =
        DomainA(value.x)
    }
  }

  val aToB = Converter[DomainA, DomainB].convert(_)
  val bToC = Converter[DomainB, DomainC].convert(_)
  val aToC = aToB andThen bToC

  val cToB = Converter[DomainC, DomainB].convert(_)
  val bToA = Converter[DomainB, DomainA].convert(_)
  val cToA = cToB andThen bToA

  val echo = aToC andThen cToA

  it should "map domain A to C where all knowledge is in DomainB" in {
    val a = DomainA(1)
    aToC.apply(a) shouldBe DomainC(1)
  }

  it should "map domain C to B where all knowledge is in DomainB" in {
    val c = DomainC(1)
    cToA.apply(c) shouldBe DomainA(1)
  }

  it should "map domain A to C and back to A where all knowledge is in DomainB" in {
    val a = DomainA(1)
    echo.apply(a) shouldBe a
  }
}
