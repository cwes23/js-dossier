/*
 Copyright 2013-2015 Jason Leyba
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
   http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.github.jsdossier;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;

import com.github.jsdossier.jscomp.JsDoc;
import com.github.jsdossier.jscomp.Module;
import com.github.jsdossier.jscomp.NominalType2;
import com.github.jsdossier.jscomp.Position;
import com.github.jsdossier.jscomp.TypeRegistry2;
import com.google.common.collect.ImmutableSet;
import com.google.common.jimfs.Jimfs;
import com.google.javascript.rhino.jstype.JSType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.nio.file.FileSystem;
import java.nio.file.Path;

/**
 * Tests for {@link DossierFileSystem}.
 */
@RunWith(JUnit4.class)
public class DossierFileSystemTest {
  
  private final FileSystem inputFs = Jimfs.newFileSystem();
  private final Path srcPrefix = inputFs.getPath("/input/src");
  private final Path modulePrefix = inputFs.getPath("/input/module");
  private final ImmutableSet<Path> allModules = ImmutableSet.of(
      modulePrefix.resolve("foo/index.js"),
      modulePrefix.resolve("foo/bar.js"),
      modulePrefix.resolve("foo/bar/index.js"),
      modulePrefix.resolve("foo/bar/baz.js"));

  private final TypeRegistry2 typeRegistry = new TypeRegistry2();

  private final FileSystem outputFs = Jimfs.newFileSystem();
  private final Path outputRoot = outputFs.getPath("/out");
  
  private final DossierFileSystem sut = new DossierFileSystem(
      outputRoot, srcPrefix, modulePrefix, ImmutableSet.<Path>of(), typeRegistry);
  
  @Test
  public void canGetThePathToARenderedSourceFile() {
    Path path = sut.getPath(srcPrefix.resolve("foo/bar/baz.js"));
    assertThat(path.toString()).isEqualTo(
        outputRoot.resolve("source/foo/bar/baz.js.src.html").toString());
  }
  
  @Test
  public void canGetThePathToANominalType() {
    NominalType2 type = createType("foo.bar.Baz");
    assertThat(sut.getPath(type).toString()).isEqualTo(
        outputRoot.resolve("foo.bar.Baz.html").toString());
  }
  
  @Test
  public void canGetThePathToAModule_index() {
    Path path = sut.getPath(commonJsModule("foo/bar/index.js"));
    assertThat(path.toString()).isEqualTo(
        outputRoot.resolve("module/foo_bar_index.html").toString());
  }
  
  @Test
  public void canGetThePathToAModule() {
    Path path = sut.getPath(commonJsModule("foo/bar/baz.js"));
    assertThat(path.toString()).isEqualTo(
        outputRoot.resolve("module/foo_bar_baz.html").toString());
  }
  
  @Test
  public void canGetThePathToAModuleExportedType() {
    NominalType2 type = createType("Clazz", commonJsModule("foo/bar.js"));
    Path path = sut.getPath(type);
    assertThat(path.toString()).isEqualTo(
        outputRoot.resolve("module/foo_bar_exports_Clazz.html").toString());
  }
  
  @Test
  public void canGetThePathToAModuleExportedType_exportedFromIndex() {
    NominalType2 type = createType("Clazz", commonJsModule("foo/bar/index.js"));
    Path path = sut.getPath(type);
    assertThat(path.toString()).isEqualTo(
        outputRoot.resolve("module/foo_bar_index_exports_Clazz.html").toString());
  }
  
  @Test
  public void getModuleDisplayName_index() {
    assertThat(sut.getDisplayName(commonJsModule("foo/index.js"))).isEqualTo("foo");
  }
  
  @Test
  public void getModuleDisplayName_notIndex() {
    assertThat(sut.getDisplayName(commonJsModule("foo/bar/baz.js"))).isEqualTo("foo/bar/baz");
  }
  
  @Test
  public void getModuleDisplayName_indexClashesWithSiblingInParentDir() {
    typeRegistry.addModule(commonJsModule("foo/bar.js"));
    typeRegistry.addModule(commonJsModule("foo/bar/index.js"));

    assertThat(sut.getDisplayName(commonJsModule("foo/bar.js"))).isEqualTo("foo/bar");
    assertThat(sut.getDisplayName(commonJsModule("foo/bar/index.js"))).isEqualTo("foo/bar/");
  }
  
  @Test
  public void getGoogModuleDisplayName() {
    assertThat(sut.getDisplayName(googModule("foo.bar.baz"))).isEqualTo("foo.bar.baz");
  }
  
  @Test
  public void getModuleExportedTypeDisplayName() {
    NominalType2 type = createType("Foo", commonJsModule("foo/bar.js"));
    assertThat(sut.getDisplayName(type)).isEqualTo("Foo");
  }
  
  @Test
  public void getGoogModuleExportedTypeDisplayName() {
    NominalType2 type = createType("Baz", googModule("foo.bar"));
    assertThat(sut.getDisplayName(type)).isEqualTo("Baz");
  }
  
  @Test
  public void getRelativePath_fromGlobalType() {
    NominalType2 type = createType("Baz");
    Path path = sut.getPath(srcPrefix.resolve("foo/bar/baz.js"));
    assertThat(sut.getRelativePath(type, path).toString()).isEqualTo(
        "source/foo/bar/baz.js.src.html");
  }
  
  @Test
  public void getRelativePath_fromModuleType() {
    NominalType2 type = createType("Baz", commonJsModule("foo/bar/baz.js"));
    Path path = sut.getPath(srcPrefix.resolve("foo/bar/baz.js"));
    assertThat(sut.getRelativePath(type, path).toString()).isEqualTo(
        "../source/foo/bar/baz.js.src.html");
  }
  
  @Test
  public void getRelativePath_betweenTypesExportedByTheSameModule() {
    NominalType2 a = createType("One", commonJsModule("foo/bar/baz.js"));
    NominalType2 b = createType("Two", commonJsModule("foo/bar/baz.js"));
    assertThat(sut.getRelativePath(a, b).toString()).isEqualTo("foo_bar_baz_exports_Two.html");
    assertThat(sut.getRelativePath(b, a).toString()).isEqualTo("foo_bar_baz_exports_One.html");
  }
  
  @Test
  public void getRelativePath_betweenTypesExportedModulesInTheSameDirectory() {
    NominalType2 a = createType("One", commonJsModule("foo/bar/one.js"));
    NominalType2 b = createType("Two", commonJsModule("foo/bar/two.js"));
    assertThat(sut.getRelativePath(a, b).toString()).isEqualTo("foo_bar_two_exports_Two.html");
    assertThat(sut.getRelativePath(b, a).toString()).isEqualTo("foo_bar_one_exports_One.html");
  }
  
  @Test
  public void getRelativePath_betweenTypesExportedByDifferentModules() {
    NominalType2 a = createType("One", commonJsModule("foo/one.js"));
    NominalType2 b = createType("Two", commonJsModule("foo/bar/two.js"));
    assertThat(sut.getRelativePath(a, b).toString()).isEqualTo("foo_bar_two_exports_Two.html");
    assertThat(sut.getRelativePath(b, a).toString()).isEqualTo("foo_one_exports_One.html");
  }
  
  private Module commonJsModule(String path) {
    return Module.builder()
        .setId(path.substring(0, path.length() - 3).replace('/', '.'))
        .setJsDoc(JsDoc.from(null))
        .setType(Module.Type.NODE)
        .setPath(modulePrefix.resolve(path))
        .build();
  }
  
  private Module googModule(String name) {
    return Module.builder()
        .setId(name)
        .setJsDoc(JsDoc.from(null))
        .setType(Module.Type.CLOSURE)
        .setPath(modulePrefix.resolve("unused"))
        .build();
  }

  private NominalType2 createType(String name) {
    return createType(name, mock(JSType.class), null);
  }
  
  private NominalType2 createType(String name, Module module) {
    return createType(name, mock(JSType.class), module);
  }

  private NominalType2 createType(String name, JSType type, Module module) {
    return NominalType2.builder()
        .setName(module == null ? name : (module.getId() + "." + name))
        .setSourcePosition(Position.of(0, 0))
        .setSourceFile(srcPrefix.resolve(name))
        .setJsDoc(JsDoc.from(null))
        .setType(type)
        .setModule(module)
        .build();
  }
}
