package org.specs2.scalaz

import org.specs2.matcher._
import org.specs2.mutable.FragmentsBuilder
import org.specs2.data.NoTuplesToSeq
import org.specs2.specification.{Example, Fragments, BaseSpecification, SpecificationStructure}
import org.specs2.main.{ArgumentsShortcuts, ArgumentsArgs}

import org.scalacheck.{Gen, Arbitrary, Prop, Properties}

// Lazy code duplication from scalaz-tests

/** A minimal version of the Specs2 mutable base class */
trait Spec
  extends BaseSpecification with FragmentsBuilder with MustExpectations
  with MustThrownExpectations with ShouldThrownExpectations with ScalaCheckMatchers
  with MatchersImplicits with StandardMatchResults
  with ArgumentsShortcuts with ArgumentsArgs
  with NoTuplesToSeq {

  addArguments(fullStackTrace)

  def is = fragments

  def checkAll(name: String, props: Properties)(implicit p: Parameters) {
    addFragments(name + " " + props.name,
      for ((name, prop) <- props.properties) yield { name in check(prop)(p)}
      , "must satisfy"
    )
  }

  def checkAll(props: Properties)(implicit p: Parameters) {
    addFragments(props.name,
      for ((name, prop) <- props.properties) yield { name in check(prop)(p)}
      , "must satisfy"
    )
  }

  import scala.language.implicitConversions
  implicit def enrichProperties(props: Properties) = new {
    def withProp(propName: String, prop: Prop) = new Properties(props.name) {
      for {(name, p) <- props.properties} property(name) = p
      property(propName) = prop
    }
  }

  /**
   * Most of our scalacheck tests use (Int => Int). This generator includes non-constant
   * functions (id, inc), to have a better chance at catching bugs.
   */
  implicit def Function1IntInt[A](implicit A: Arbitrary[Int]): Arbitrary[Int => Int] =
    Arbitrary(Gen.frequency[Int => Int](
      (1, Gen.const((x: Int) => x)),
      (1, Gen.const((x: Int) => x + 1)),
      (3, A.arbitrary.map(a => (_: Int) => a))
    ))
}

// vim: expandtab:ts=2:sw=2
