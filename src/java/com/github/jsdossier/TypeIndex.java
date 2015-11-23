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

import static com.github.jsdossier.jscomp.Types.isTypedef;
import static com.google.common.base.Preconditions.checkArgument;

import com.github.jsdossier.annotations.DocumentationScoped;
import com.github.jsdossier.jscomp.NominalType2;
import com.github.jsdossier.jscomp.TypeRegistry2;
import com.github.jsdossier.proto.TypeLink;
import com.google.common.collect.FluentIterable;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.List;

import javax.inject.Inject;

/**
 * An index of all types and properties in generated documentation.
 */
@DocumentationScoped
final class TypeIndex {

  private final DossierFileSystem dfs;
  private final LinkFactory linkFactory;
  private final TypeRegistry2 typeRegistry;
  
  private final JsonObject json = new JsonObject();

  @Inject
  TypeIndex(
      DossierFileSystem dfs,
      LinkFactoryBuilder linkFactoryBuilder,
      TypeRegistry2 typeRegistry) {
    this.dfs = dfs;
    this.typeRegistry = typeRegistry;
    this.linkFactory = linkFactoryBuilder.create(null);
  }

  /**
   * Returns this index as a JSON string.
   */
  @Override
  public String toString() {
    return json.toString();
  }
  
  public IndexReference addModule(NominalType2 module) {
    checkArgument(module.isModuleExports(), "not a module exports object: %s", module.getName());
    String dest = dfs.getOutputRoot().relativize(dfs.getPath(module)).toString();

    JsonObject obj = new JsonObject();
    obj.addProperty("name", dfs.getDisplayName(module));
    obj.addProperty("href", dest);

    getJsonArray(json, "modules").add(obj);
    return new IndexReference(module, obj);
  }

  public IndexReference addType(NominalType2 type) {
    return addTypeInfo(getJsonArray(json, "types"), type);
  }

  private static JsonArray getJsonArray(JsonObject object, String name) {
    if (!object.has(name)) {
      object.add(name, new JsonArray());
    }
    return object.get(name).getAsJsonArray();
  }

  private IndexReference addTypeInfo(JsonArray array, NominalType2 type) {
    String dest = dfs.getOutputRoot().relativize(dfs.getPath(type)).toString();

    JsonObject details = new JsonObject();
    details.addProperty("name", dfs.getDisplayName(type));
    details.addProperty("href", dest);
    details.addProperty("namespace", type.isNamespace());
    details.addProperty("interface", type.getType().isInterface());
    array.add(details);

    List<NominalType2> allTypes = typeRegistry.getTypes(type.getType());
    if (allTypes.get(0) != type) {
      List<NominalType2> typedefs = FluentIterable.from(typeRegistry.getNestedTypes(type))
          .filter(isTypedef())
          .toSortedList(new QualifiedNameComparator2());
      for (NominalType2 typedef : typedefs) {
        TypeLink link = linkFactory.createLink(typedef);
        checkArgument(
            !link.getHref().isEmpty(), "Failed to build link for %s", typedef.getName());
        JsonObject typedefDetails = new JsonObject();
        typedefDetails.addProperty("name", link.getText());
        typedefDetails.addProperty("href", link.getHref());
        array.add(typedefDetails);
      }
    }
    return new IndexReference(type, details);
  }

  final class IndexReference {
    private final NominalType2 type;
    protected final JsonObject index;

    private IndexReference(NominalType2 type, JsonObject index) {
      this.type = type;
      this.index = index;
    }
    
    public NominalType2 getNominalType() {
      return type;
    }
    
    public IndexReference addNestedType(NominalType2 type) {
      checkArgument(getNominalType().isModuleExports(),
          "Nested types should only be recorded for modules: %s", getNominalType().getName());
      checkArgument(getNominalType().getModule().equals(type.getModule()),
          "Type does not belong to this module: (%s, %s)",
          getNominalType().getName(), type.getName());
      return addTypeInfo(getJsonArray(index, "types"), type);
    }
    
    public void addStaticProperty(String name) {
      getJsonArray(index, "statics").add(new JsonPrimitive(name));
    }
    
    public void addInstanceProperty(String name) {
      getJsonArray(index, "members").add(new JsonPrimitive(name));
    }
  }
}