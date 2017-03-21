name := "play-validation-example"

organization := "com.github.dnvriend"

version := "1.0.0"

scalaVersion := "2.11.8"

val akkaVersion = "2.4.17"

libraryDependencies += ws
libraryDependencies += "com.github.nscala-time" %% "nscala-time" % "2.16.0"
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.5.13"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-persistence" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-persistence-query-experimental" % akkaVersion

// fp
libraryDependencies += "com.github.mpilquist" %% "simulacrum" % "0.10.0"
libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.2.10"
libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.1"

// database support
libraryDependencies += jdbc
libraryDependencies += evolutions
libraryDependencies += "com.zaxxer" % "HikariCP" % "2.6.1"
libraryDependencies += "com.typesafe.play" %% "anorm" % "2.5.3"
// database driver
libraryDependencies += "com.h2database" % "h2" % "1.4.193"

libraryDependencies += "io.swagger" %% "swagger-play2" % "1.5.3"

// test
libraryDependencies += "org.typelevel" %% "scalaz-scalatest" % "1.1.2" % Test
libraryDependencies += "org.mockito" % "mockito-core" % "2.7.17" % Test
libraryDependencies += "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.0.0-M1" % Test

fork in Test := true

parallelExecution := false

enablePlugins(PlayScala)


enablePlugins(BuildInfoPlugin)

buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion)

buildInfoOptions += BuildInfoOption.ToMap

buildInfoOptions += BuildInfoOption.ToJson

buildInfoOptions += BuildInfoOption.BuildTime

buildInfoPackage := organization.value



enablePlugins(AutomateHeaderPlugin)

licenses +=("Apache-2.0", url("http://opensource.org/licenses/apache2.0.php"))

import de.heikoseeberger.sbtheader.license.Apache2_0

headers := Map(
  "scala" -> Apache2_0("2016", "Dennis Vriend"),
  "conf" -> Apache2_0("2016", "Dennis Vriend", "#")
)

import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform

SbtScalariform.autoImport.scalariformPreferences := SbtScalariform.autoImport.scalariformPreferences.value
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
  .setPreference(DoubleIndentClassDeclaration, true)
