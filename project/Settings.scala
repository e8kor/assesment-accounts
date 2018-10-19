import sbt.{Def, _}
import sbt.Keys._
import sbtassembly.AssemblyPlugin.autoImport._
import sbtbuildinfo.BuildInfoPlugin.autoImport._
import sbtdocker.DockerPlugin.autoImport._

object Settings {

  val withCommonSettings: Seq[Setting[_]] = {
    inThisBuild(Seq(
      organization := "accounts",
      scalaVersion := "2.12.6",
      javaOptions ++= Seq(
        "-XX:+CMSClassUnloadingEnabled"
      )
    ))
  }

  val withBuildInfo: Seq[Setting[_]] = Seq(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := organization.value + "." + name.value.filter(_.isLetter)
  )

  val withTesting = Seq(
    testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oD")
  )

  val withAssembly: Seq[Setting[_]] = baseAssemblySettings ++ Seq(
    assemblyJarName in assembly := "../../app.jar",
    test in assembly := {}
  )

  val withDockerSetting = Seq(
    dockerfile in docker := {
      val artifact: File = assembly.value
      val artifactTargetPath = s"/app/${artifact.name}"

      new Dockerfile {
        from("openjdk:8-jre")
        add(artifact, artifactTargetPath)
        entryPoint("java", "-jar", artifactTargetPath, "-c", "/config/application.conf")
      }
    },
    imageNames in docker := Seq(
      ImageName(s"${organization.value}/${name.value}:latest"),

      ImageName(
        namespace = Some(organization.value),
        repository = name.value,
        tag = Some("v" + version.value)
      )
    )
  )

  val http = Seq(
    "org.http4s" %% "http4s-core" % "0.19.0-M2",
    "org.http4s" %% "http4s-dsl" % "0.19.0-M2",
    "org.http4s" %% "http4s-circe" % "0.19.0-M2",
    "org.http4s" %% "http4s-blaze-server" % "0.19.0-M2",
    "io.circe" %% "circe-generic" % "0.10.0"
  )

  val configs = Seq(
    "com.github.pureconfig" %% "pureconfig" % "0.9.2",
    "com.github.scopt" %% "scopt" % "3.7.0"
  )

  val testing = Seq(
    "org.scalactic" %% "scalactic" % "3.0.4" % "it,test",
    "org.scalatest" %% "scalatest" % "3.0.4" % "it,test",
    "org.http4s" %% "http4s-blaze-client" % "0.19.0-M2" % "it,test",
    "org.tpolecat" %% "doobie-scalatest" % "0.5.3" % "test"
  )
  val database = Seq(
    "org.tpolecat" %% "doobie-core" % "0.5.3",
    "org.tpolecat" %% "doobie-hikari" % "0.5.3",
    "com.h2database" % "h2" % "1.4.197",
    "org.flywaydb" % "flyway-core" % "4.2.0"
  )

  val logging = Seq(
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
    "ch.qos.logback" % "logback-classic" % "1.2.3"
  )

}
