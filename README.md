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

## Documentation
- [Eugene Yokota Blogs](http://eed3si9n.com/)
- [Learning Scalaz - Eugene Yokota (eed3si9n)](http://eed3si9n.com/learning-scalaz/)
- [Scalaz to Cats](http://underscore.io/blog/posts/2016/02/02/advanced-scala-scalaz-to-cats.html)
- [Typelevel blogs](http://typelevel.org/blog/)
- [Typelevel Cats](https://github.com/typelevel/cats)
- [Herding Cats - Eugene Yokota](http://eed3si9n.com/herding-cats/)
- [Scala Hamsters](https://github.com/scala-hamsters/hamsters)