package org.jetbrains.plugins.scala.finder;

import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.containers.HashMap;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.plugins.scala.components.ScalaComponents;
import org.jetbrains.plugins.scala.cache.module.ScalaModuleCachesManager;
import org.jetbrains.plugins.scala.cache.module.ScalaModuleCaches;
import org.jetbrains.plugins.scala.cache.info.ScalaFileInfo;
import org.jetbrains.plugins.scala.cache.VirtualFileScanner;

import java.util.Map;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Ilya.Sergey
 */
public class ScalaPsiElementFinder implements PsiElementFinder, ProjectComponent {

  private Project myProject;


  public ScalaPsiElementFinder(Project project) {
    myProject = project;
  }

  public PsiClass findClass(@NotNull String qualifiedName, GlobalSearchScope scope) {
    Module[] modules = ModuleManager.getInstance(myProject).getModules();
    for (Module module : modules) {
      ScalaModuleCachesManager manager =
              (ScalaModuleCachesManager) module.getComponent(ScalaComponents.SCALA_CACHE_MANAGER);
      ScalaModuleCaches caches = manager.getModuleFilesCache();
      PsiClass clazz;
      if ((clazz = caches.getClassByName(qualifiedName)) != null) {
        return clazz;
      }
    }
    return null;
  }

  @NotNull
  public PsiClass[] findClasses(String qualifiedName, GlobalSearchScope scope) {
    ArrayList<PsiClass> classesAcc = new ArrayList<PsiClass>();
    Module[] modules = ModuleManager.getInstance(myProject).getModules();
    for (Module module : modules) {
      ScalaModuleCachesManager manager =
              (ScalaModuleCachesManager) module.getComponent(ScalaComponents.SCALA_CACHE_MANAGER);
      ScalaModuleCaches caches = manager.getModuleFilesCache();
      PsiClass[] classes = caches.getClassesByName(qualifiedName);
      for (PsiClass clazz : classes) {
        classesAcc.add(clazz);
      }
    }
    return (PsiClass[]) classesAcc.toArray();
  }

  public PsiPackage findPackage(String qualifiedName) {
/*
    final PsiPackage aPackage = myFileManager.findPackage(qualifiedName);
    if (aPackage == null && myCurrentMigration != null) {
      final PsiPackage migrationPackage = myCurrentMigration.getMigrationPackage(qualifiedName);
      if (migrationPackage != null) return migrationPackage;
    }

    return aPackage;
*/
    return null;
  }

  @NotNull
  public PsiPackage[] getSubPackages(PsiPackage psiPackage, GlobalSearchScope scope) {
    final Map<String, PsiPackage> packagesMap = new HashMap<String, PsiPackage>();
    final String qualifiedName = psiPackage.getQualifiedName();
    final PsiDirectory[] dirs = psiPackage.getDirectories(scope);
    for (PsiDirectory dir : dirs) {
      PsiDirectory[] subdirs = dir.getSubdirectories();
      for (PsiDirectory subdir : subdirs) {
        final PsiPackage aPackage = subdir.getPackage();
        if (aPackage != null) {
          final String subQualifiedName = aPackage.getQualifiedName();
          if (subQualifiedName.startsWith(qualifiedName) && !packagesMap.containsKey(subQualifiedName)) {
            packagesMap.put(aPackage.getQualifiedName(), aPackage);
          }
        }
      }
    }
    return packagesMap.values().toArray(new PsiPackage[packagesMap.size()]);
  }

  @NotNull
  public PsiClass[] getClasses(PsiPackage psiPackage, GlobalSearchScope scope) {
    ArrayList<PsiClass> list = new ArrayList<PsiClass>();
    final PsiDirectory[] dirs = psiPackage.getDirectories(scope);
    for (PsiDirectory dir : dirs) {
      PsiClass[] classes = dir.getClasses();
      for (PsiClass aClass : classes) {
        list.add(aClass);
      }
    }
    return list.toArray(new PsiClass[list.size()]);
  }

  public void projectOpened() {

  }

  public void projectClosed() {
  }

  @NonNls
  @NotNull
  public String getComponentName() {
    return ScalaComponents.SCALA_PSI_ELEMENT_FINDER;
  }

  public void initComponent() {
  }

  public void disposeComponent() {

  }
}
