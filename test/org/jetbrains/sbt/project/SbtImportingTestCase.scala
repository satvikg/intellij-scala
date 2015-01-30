package org.jetbrains.sbt.project

import java.io.File

import com.intellij.openapi.externalSystem.model.ProjectSystemId
import com.intellij.openapi.externalSystem.settings.ExternalProjectSettings
import com.intellij.openapi.externalSystem.test.ExternalSystemImportingTestCase
import com.intellij.openapi.projectRoots.JavaSdkType
import com.intellij.openapi.projectRoots.impl.JavaAwareProjectJdkTableImpl
import com.intellij.openapi.roots.DependencyScope
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.testFramework.IdeaTestUtil
import org.jetbrains.plugins.scala.codeInspection.internal.AnnotatorBasedErrorInspection
import org.jetbrains.plugins.scala.util.TestUtils
import org.jetbrains.sbt.project.settings.SbtProjectSettings
import org.jetbrains.sbt.settings.SbtSystemSettings

/**
 * @author Nikolay Obedin
 * @since 1/30/15.
 */

import Data._

class SbtImportingTestCase extends ExternalSystemImportingTestCase {

  def testSimple(): Unit = doRunTest(project.
    withModules(
      Data.module("simple").
        withContentRoots(ContentRoot(path = getProjectDir.getCanonicalPath,
                                     excludes = Seq("target"))).
        withSources("src/main/java", "src/main/scala", "target/scala-2.11/src_managed/main").
        withTestSources("src/test/java", "src/test/scala", "target/scala-2.11/src_managed/test").
        withResources("src/main/resources", "target/scala-2.11/resource_managed/main").
        withTestResources("src/test/resources", "target/scala-2.11/resource_managed/test").
        withExcludes("target"),
      Data.module("simple-build").
        withContentRoots(ContentRoot(path = new File(getProjectDir, "project").getCanonicalPath,
                                     excludes = Seq("project/target", "target"))).
        withSources("").
        withExcludes("project/target", "target")
    ).
    withLibraries("SBT: org.scala-lang:scala-library:2.11.2:jar")
  )

  protected def getExternalSystemId: ProjectSystemId = SbtProjectSystem.Id

  protected def getExternalSystemConfigFileName: String = "build.sbt"

  protected def getTestsTempDir: String = ""

  protected def getRootDir: String = TestUtils.getTestDataPath + "/sbt/testProjects"

  protected def getCurrentExternalProjectSettings: ExternalProjectSettings = {
    val settings = new SbtProjectSettings
    // TODO: find out why it's complaining about allowed roots violation
//    val internalSdk = JavaAwareProjectJdkTableImpl.getInstanceEx.getInternalJdk
//    val sdk = if (internalSdk == null) IdeaTestUtil.getMockJdk17 else internalSdk
//    val sdkType = sdk.getSdkType.asInstanceOf[JavaSdkType]
    settings.setJdk("/usr/lib/jvm/java-6-oracle") //sdkType.getVMExecutablePath(sdk))
    //settings.setCreateEmptyContentRootDirectories(true)
    settings
  }

  protected def getProjectDir: File = new File(getRootDir, getTestName(false))

  override protected def setUpInWriteAction(): Unit = {
    super.setUpInWriteAction()
    val projectDir = getProjectDir
    if (!projectDir.exists()) return
    myProjectRoot = LocalFileSystem.getInstance.refreshAndFindFileByIoFile(projectDir)
    SbtSystemSettings.getInstance(myProject).setCustomLauncherEnabled(true)
    SbtSystemSettings.getInstance(myProject).setCustomLauncherPath(new File("jars/sbt-launch.jar").getAbsolutePath)
    SbtSystemSettings.getInstance(myProject).setCustomSbtStructureDir(new File("jars").getAbsolutePath)
    myFixture.enableInspections(classOf[AnnotatorBasedErrorInspection])
  }

  private def doRunTest(expected: Project): Unit = {
    val projectDir: File = new File(getRootDir, getTestName(false))
    println(projectDir.getAbsolutePath)
    if (!projectDir.exists()) {
      println("Project is not found. Test is skipped.")
      return
    }
    importProject()
    doCheckProject(expected)
  }

  private def doCheckProject(project: Project): Unit = {
    assertModules(project.modules.map(_.name):_*)
    project.modules.foreach(doCheckModule)

    assertProjectLibraries(project.externalLibraryNames:_*)
  }

  private def doCheckModule(module: Module): Unit = {
    assertContentRoots(module.name, module.contentRoots.map(_.path):_*)
    module.contentRoots.foreach { root =>
      assertContentRootExcludes(module.name, root.path, root.excludes:_*)
    }
    assertSources(module.name, module.sources:_*)
    assertTestSources(module.name, module.testSources:_*)
    assertGeneratedSources(module.name, module.generatedSources:_*)
    assertResources(module.name, module.resources:_*)
    assertTestResources(module.name, module.testResources:_*)
    assertExcludes(module.name, module.excludes:_*)
    module.libraryDependencies.foreach(doCheckLibrary(module.name, _))
    module.moduleDependencies.foreach { dep =>
      assertModuleModuleDepScope(module.name, dep.name, dep.scopes:_*)
    }
  }

  private def doCheckLibrary(moduleName: String, library: LibraryDep): Unit = {
    import scala.collection.JavaConverters._
    assertModuleLibDep(moduleName, library.name, library.classes.asJava, library.sources.asJava, library.javadocs.asJava)
    assertModuleLibDepScope(moduleName, library.name, library.scopes:_*)
  }
}

private object Data {
  case class Project(modules: Seq[Module], externalLibraryNames: Seq[String]) {
    def withModules(m: Module*) = this.copy(modules = modules ++ m)
    def withLibraries(l: String*)    = this.copy(externalLibraryNames = externalLibraryNames ++ l)
  }

  case class Module(name: String,
                    contentRoots: Seq[ContentRoot],
                    sources: Seq[String],
                    testSources: Seq[String],
                    generatedSources: Seq[String],
                    resources: Seq[String],
                    testResources: Seq[String],
                    excludes: Seq[String],
                    libraryDependencies: Seq[LibraryDep],
                    moduleDependencies: Seq[ModuleDep]) {
    def withContentRoots(cr: ContentRoot*)  = this.copy(contentRoots = contentRoots ++ cr)
    def withSources(s: String*)             = this.copy(sources = sources ++ s)
    def withTestSources(s: String*)         = this.copy(testSources = testSources ++ s)
    def withGeneratedSources(s: String*)    = this.copy(generatedSources = generatedSources ++ s)
    def withResources(r: String*)           = this.copy(resources = resources ++ r)
    def withTestResources(r: String*)       = this.copy(testResources = testResources ++ r)
    def withExcludes(e: String*)            = this.copy(excludes = excludes ++ e)
    def withLibraries(l: LibraryDep*)  = this.copy(libraryDependencies = libraryDependencies ++ l)
    def withModules(m: ModuleDep*)     = this.copy(moduleDependencies = moduleDependencies ++ m)
  }

  case class LibraryDep(name: String,
                        classes: Seq[String],
                        sources: Seq[String],
                        javadocs: Seq[String],
                        scopes: Seq[DependencyScope]) {
    def withClasses(c: String*) = this.copy(classes = classes ++ c)
    def withSources(s: String*) = this.copy(sources = sources ++ s)
    def withJavadocs(d: String*) = this.copy(javadocs = javadocs ++ d)
    def withScopes(s: DependencyScope*) = this.copy(scopes = scopes ++ s)
  }

  case class ModuleDep(name: String, scopes: Seq[DependencyScope])

  case class ContentRoot(path: String, excludes: Seq[String])

  def project = Project(Seq.empty, Seq.empty)
  def module(name: String) = Module(name, Seq.empty, Seq.empty, Seq.empty, Seq.empty, Seq.empty, Seq.empty, Seq.empty, Seq.empty, Seq.empty)
  def library(name: String) = LibraryDep(name, Seq.empty, Seq.empty, Seq.empty, Seq.empty)
}

