import de.heikoseeberger.sbtheader.AutomateHeaderPlugin
import de.heikoseeberger.sbtheader.license.Apache2_0
import catext.Dependencies._

val dev  = Seq(Dev("47 Degrees (twitter: @47deg)", "47 Degrees"))
val gh   = GitHubSettings("com.fortysevendeg", "fetch", "47 Degrees", apache)
val vAll = Versions(versions, libraries, scalacPlugins)

addCommandAlias("makeDocs", ";docs/tut;docs/makeSite")

pgpPassphrase := Some(sys.env.getOrElse("PGP_PASSPHRASE", "").toCharArray)
pgpPublicRing := file(s"${sys.env.getOrElse("PGP_FOLDER", ".")}/pubring.gpg")
pgpSecretRing := file(s"${sys.env.getOrElse("PGP_FOLDER", ".")}/secring.gpg")

lazy val buildSettings = Seq(
  organization := gh.org,
  organizationName := gh.publishOrg,
  description := "Simple & Efficient data access for Scala and Scala.js",
  startYear := Option(2016),
  homepage := Option(url("http://47deg.github.io/fetch/")),
  organizationHomepage := Option(new URL("http://47deg.com")),
  scalaVersion := "2.11.8",
  headers := Map(
    "scala" -> Apache2_0("2016", "47 Degrees, LLC. <http://www.47deg.com>")
  )
)

lazy val commonSettings = Seq(
  resolvers += Resolver.sonatypeRepo("releases"),
  libraryDependencies ++= Seq(
    "org.typelevel" %%% "cats" % "0.7.2",
    "org.scalatest" %%% "scalatest" % "3.0.0" % "test",
    compilerPlugin(
      "org.spire-math" %% "kind-projector" % "0.7.1"
    )
  ),
  scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation",
    "-feature",
    "-Ywarn-dead-code",
    "-language:higherKinds",
    "-language:existentials",
    "-language:postfixOps"
  ),
  scalafmtConfig := Some(file(".scalafmt"))
) ++ reformatOnCompileSettings

lazy val allSettings = buildSettings ++
    commonSettings ++
    sharedCommonSettings ++
    miscSettings ++
    sharedReleaseProcess ++
    credentialSettings ++
    sharedPublishSettings(gh, dev)

lazy val fetch = crossProject.in(file("."))
  .settings(moduleName := "fetch")
  .settings(allSettings:_*)
  .jsSettings(sharedJsSettings:_*)
  .enablePlugins(AutomateHeaderPlugin)

lazy val fetchJVM = fetch.jvm
lazy val fetchJS = fetch.js

lazy val root = project.in(file("."))
  .aggregate(fetchJS, fetchJVM)
  .settings(noPublishSettings)

lazy val docsSettings = ghpages.settings ++ buildSettings ++ tutSettings ++ Seq(
  git.remoteRepo := "git@github.com:47deg/fetch.git",
  tutSourceDirectory := sourceDirectory.value / "tut",
  tutTargetDirectory := sourceDirectory.value / "jekyll",
  tutScalacOptions ~= (_.filterNot(Set("-Ywarn-unused-import", "-Ywarn-dead-code"))),
  aggregate in doc := true
)

lazy val docs = (project in file("docs"))
  .settings(
    moduleName := "fetch-docs"
   )
  .dependsOn(fetchJVM, fetchMonixJVM)
  .enablePlugins(JekyllPlugin)
  .settings(docsSettings: _*)
  .settings(noPublishSettings)


lazy val readmeSettings = buildSettings ++ tutSettings ++ Seq(
  tutSourceDirectory := baseDirectory.value,
  tutTargetDirectory := baseDirectory.value.getParentFile,
  tutScalacOptions ~= (_.filterNot(Set("-Ywarn-unused-import", "-Ywarn-dead-code"))),
  tutNameFilter := """README.md""".r
)

lazy val readme = (project in file("tut"))
  .settings(
    moduleName := "fetch-readme"
  )
  .dependsOn(fetchJVM)
  .settings(readmeSettings: _*)
  .settings(noPublishSettings)

lazy val monixSettings = (
  libraryDependencies ++= Seq(
    "io.monix" %%% "monix-eval" % "2.0.5"
  )
)

lazy val monix = crossProject.in(file("monix"))
  .dependsOn(fetch)
  .settings(moduleName := "fetch-monix")
  .settings(allSettings:_*)
  .jsSettings(sharedJsSettings:_*)
  .settings(monixSettings: _*)
  .enablePlugins(AutomateHeaderPlugin)


lazy val fetchMonixJVM = monix.jvm
lazy val fetchMonixJS = monix.js
