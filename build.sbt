name := "holysim"

version := "1.0"

scalaVersion := "2.11.7"

scalaSource in Compile <<= (baseDirectory in Compile)(_ / "src")

scalaSource in Test <<= (baseDirectory in Test)(_ / "test")

libraryDependencies += "org.scala-lang.modules" %% "scala-java8-compat" % "0.5.0"

scalacOptions ++= List("-Ybackend:GenBCode", "-Ydelambdafy:method", "-target:jvm-1.8", "-Yopt:l:classpath")
