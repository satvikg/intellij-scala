package org.jetbrains.sbt.project

import java.io.File

import com.intellij.openapi.roots.DependencyScope

import scala.util.matching.Regex
import scala.xml._

/**
 * @author Nikolay Obedin
 * @since 2/2/15.
 */

case class Project(modules: Seq[Module],
                   externalLibraryNames: Seq[String])

case class Module(name: String,
                  contentRoots: Seq[ContentRoot],
                  sources: Seq[String],
                  testSources: Seq[String],
                  generatedSources: Seq[String],
                  resources: Seq[String],
                  testResources: Seq[String],
                  excludes: Seq[String],
                  libraryDependencies: Seq[LibraryDep],
                  moduleDependencies: Seq[ModuleDep])

case class LibraryDep(name: String,
                      classes: Seq[String],
                      sources: Seq[String],
                      javadocs: Seq[String],
                      scopes: Seq[DependencyScope])

case class ModuleDep(name: String,
                     scopes: Seq[DependencyScope])

case class ContentRoot(path: String,
                       excludes: Seq[String])

class TestProjectDataParser(projectDir: File) {

  def parse(): Project =
    parseProject(XML.loadFile(new File(projectDir, "expected.xml")))

  private val rewritingRules = Map(
    "$PROJECT_DIR" -> projectDir.getAbsolutePath,
    "$HOME" -> System.getProperty("user.home")
  )

  private def parseProject(node: Node): Project = Project(
    modules = (node \ "module").map(parseModule),
    externalLibraryNames = (node \ "library").map(_.text)
  )

  private def parseModule(node: Node): Module = Module(
    name = node \@ "name",
    contentRoots = (node \ "contentRoot").map(parseContentRoot),
    sources = (node \ "source").map(rewritePath),
    testSources = (node \ "testSource").map(rewritePath),
    generatedSources = (node \ "genSource").map(rewritePath),
    resources = (node \ "resource").map(rewritePath),
    testResources = (node \ "testResource").map(rewritePath),
    excludes = (node \ "exclude").map(rewritePath),
    libraryDependencies = (node \ "library").map(parseLibDeps),
    moduleDependencies = (node \ "module").map(parseModuleDeps)
  )

  private def parseContentRoot(node: Node): ContentRoot = ContentRoot(
    path = rewritePath(node \@ "path"),
    excludes = (node \ "exclude").map(rewritePath)
  )

  private def parseLibDeps(node: Node): LibraryDep = LibraryDep(
    name = node \@ "name",
    classes = (node \ "jar").map(rewritePath),
    sources = (node \ "source").map(rewritePath),
    javadocs = (node \ "javadoc").map(rewritePath),
    scopes = parseScopes(node \@ "scopes")
  )

  private def parseModuleDeps(node: Node): ModuleDep = ModuleDep(
    name = node \@ "name",
    scopes = parseScopes(node \@ "scopes")
  )

  private def parseScopes(scopeStr: String): Seq[DependencyScope] =
    scopeStr.split(',').map(_.trim).collect {
      case "compile" => DependencyScope.COMPILE
      case "provided" => DependencyScope.PROVIDED
      case "runtime" => DependencyScope.RUNTIME
      case "test" => DependencyScope.TEST
    }

  private def rewritePath(path: String): String =
    rewritingRules.foldLeft(path) { (p, rule) =>
      p.replaceAll(Regex.quote(rule._1), rule._2)
    }

  private def rewritePath(node: Node): String =
    rewritePath(node.text)
}
