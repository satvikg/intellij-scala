package org.jetbrains.sbt.project

import java.io.File

import com.intellij.openapi.externalSystem.model.ProjectSystemId
import com.intellij.openapi.externalSystem.settings.ExternalProjectSettings
import com.intellij.openapi.externalSystem.test.ExternalSystemImportingTestCase
import com.intellij.openapi.vfs.LocalFileSystem
import org.jetbrains.plugins.scala.codeInspection.internal.AnnotatorBasedErrorInspection
import org.jetbrains.plugins.scala.util.TestUtils
import org.jetbrains.sbt.project.settings.SbtProjectSettings
import org.jetbrains.sbt.settings.SbtSystemSettings

/**
 * @author Nikolay Obedin
 * @since 1/30/15.
 */

class SbtImportingTestCase extends ExternalSystemImportingTestCase {

  def testSimple(): Unit = doRunTest

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

  private def doRunTest(): Unit = {
    val projectDir = new File(getRootDir, getTestName(false))
    if (!projectDir.exists) {
      println("Project is not found. Test is skipped.")
      return
    }
    val expected = new TestProjectDataParser(projectDir).parse()
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

    assertModuleLibDeps(module.name, module.libraryDependencies.map(_.name):_*)
    module.libraryDependencies.foreach(doCheckLibrary(module.name, _))

    assertModuleModuleDeps(module.name, module.moduleDependencies.map(_.name):_*)
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

