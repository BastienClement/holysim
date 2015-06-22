name := "holysim"

version := "1.0"

scalaVersion := "2.11.6"

scalaSource in Compile <<= (baseDirectory in Compile)(_ / "src")

scalaSource in Test <<= (baseDirectory in Test)(_ / "test")

libraryDependencies += "org.scala-lang" % "scala-reflect" % "2.11.6"
