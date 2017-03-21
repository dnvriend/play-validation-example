# play-validation-example
A small study project on [Scalaz](https://github.com/scalaz/scalaz) [Disjunction](https://github.com/scalaz/scalaz/blob/v7.2.8/tests/src/test/scala/scalaz/DisjunctionTest.scala), Scalaz [Validation](https://github.com/scalaz/scalaz/blob/v7.2.8/tests/src/test/scala/scalaz/ValidationTest.scala) and [Play's Form validation framework](https://www.playframework.com/documentation/2.5.x/ScalaForms).

## How to use
Clone the repository:

```bash
git clone git@github.com:dnvriend/play-validation-example.git
```

Next enter the directory:

```bash
cd play-validation-example
```

Launch the test:

```bash
sbt test
```

You can also launch a single test:

```bash
sbt "testOnly *ValidationTest"

sbt "testOnly *FormValidationTest"

sbt "testOnly *DisjunctionTest"
```

## Test library
The projects uses the [ScalaTest](http://www.scalatest.org/) library in combination with the [scalaz-scalatest](https://github.com/typelevel/scalaz-scalatest)
library to get a nice DSL in which to define the specification. We use the [FlatSpec](http://doc.scalatest.org/3.0.1/#org.scalatest.FlatSpec) with [Matchers](http://doc.scalatest.org/3.0.1/#org.scalatest.Matchers)
mixins to define an abstract [TestSpec class](https://github.com/dnvriend/play-validation-example/blob/master/test/com/github/dnvriend/TestSpec.scala) that defines the language that we use to define our [TestSuites](http://www.scalatest.org/user_guide) with.

## Documentation
- [Eugene Yokota Blogs](http://eed3si9n.com/)
- [Learning Scalaz - Eugene Yokota (eed3si9n)](http://eed3si9n.com/learning-scalaz/)
- [Scalaz to Cats](http://underscore.io/blog/posts/2016/02/02/advanced-scala-scalaz-to-cats.html)
- [Typelevel blogs](http://typelevel.org/blog/)
- [Typelevel Cats](https://github.com/typelevel/cats)
- [Herding Cats - Eugene Yokota](http://eed3si9n.com/herding-cats/)
- [Scala Hamsters](https://github.com/scala-hamsters/hamsters)

## Video
- [(0'45 hrs) Comparing Functional Error Handling in Scalaz and Scalactic - Bill Venners](https://www.youtube.com/watch?v=2kFigGFqML0)