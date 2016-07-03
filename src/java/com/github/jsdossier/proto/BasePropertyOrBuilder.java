// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: dossier.proto

package com.github.jsdossier.proto;

public interface BasePropertyOrBuilder extends
    // @@protoc_insertion_point(interface_extends:dossier.BaseProperty)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>optional string name = 1;</code>
   *
   * <pre>
   * The property's name; this should not be the fully qualified name.
   * </pre>
   */
  java.lang.String getName();
  /**
   * <code>optional string name = 1;</code>
   *
   * <pre>
   * The property's name; this should not be the fully qualified name.
   * </pre>
   */
  com.google.protobuf.ByteString
      getNameBytes();

  /**
   * <code>optional .dossier.SourceLink source = 2;</code>
   *
   * <pre>
   * Link to the location in the source file where this property is defined.
   * </pre>
   */
  boolean hasSource();
  /**
   * <code>optional .dossier.SourceLink source = 2;</code>
   *
   * <pre>
   * Link to the location in the source file where this property is defined.
   * </pre>
   */
  com.github.jsdossier.proto.SourceLink getSource();
  /**
   * <code>optional .dossier.SourceLink source = 2;</code>
   *
   * <pre>
   * Link to the location in the source file where this property is defined.
   * </pre>
   */
  com.github.jsdossier.proto.SourceLinkOrBuilder getSourceOrBuilder();

  /**
   * <code>optional .dossier.Comment description = 3;</code>
   *
   * <pre>
   * The property's block comment.
   * </pre>
   */
  boolean hasDescription();
  /**
   * <code>optional .dossier.Comment description = 3;</code>
   *
   * <pre>
   * The property's block comment.
   * </pre>
   */
  com.github.jsdossier.proto.Comment getDescription();
  /**
   * <code>optional .dossier.Comment description = 3;</code>
   *
   * <pre>
   * The property's block comment.
   * </pre>
   */
  com.github.jsdossier.proto.CommentOrBuilder getDescriptionOrBuilder();

  /**
   * <code>optional .dossier.Comment deprecation = 4;</code>
   *
   * <pre>
   * Whether this property is deprecated.
   * </pre>
   */
  boolean hasDeprecation();
  /**
   * <code>optional .dossier.Comment deprecation = 4;</code>
   *
   * <pre>
   * Whether this property is deprecated.
   * </pre>
   */
  com.github.jsdossier.proto.Comment getDeprecation();
  /**
   * <code>optional .dossier.Comment deprecation = 4;</code>
   *
   * <pre>
   * Whether this property is deprecated.
   * </pre>
   */
  com.github.jsdossier.proto.CommentOrBuilder getDeprecationOrBuilder();

  /**
   * <code>optional .dossier.Visibility visibility = 5;</code>
   */
  int getVisibilityValue();
  /**
   * <code>optional .dossier.Visibility visibility = 5;</code>
   */
  com.github.jsdossier.proto.Visibility getVisibility();

  /**
   * <code>optional .dossier.Tags tags = 6;</code>
   */
  boolean hasTags();
  /**
   * <code>optional .dossier.Tags tags = 6;</code>
   */
  com.github.jsdossier.proto.Tags getTags();
  /**
   * <code>optional .dossier.Tags tags = 6;</code>
   */
  com.github.jsdossier.proto.TagsOrBuilder getTagsOrBuilder();

  /**
   * <code>optional .dossier.expression.NamedType defined_by = 7;</code>
   *
   * <pre>
   * The class or interface that defines this property.
   * </pre>
   */
  boolean hasDefinedBy();
  /**
   * <code>optional .dossier.expression.NamedType defined_by = 7;</code>
   *
   * <pre>
   * The class or interface that defines this property.
   * </pre>
   */
  com.github.jsdossier.proto.NamedType getDefinedBy();
  /**
   * <code>optional .dossier.expression.NamedType defined_by = 7;</code>
   *
   * <pre>
   * The class or interface that defines this property.
   * </pre>
   */
  com.github.jsdossier.proto.NamedTypeOrBuilder getDefinedByOrBuilder();

  /**
   * <code>optional .dossier.expression.NamedType overrides = 8;</code>
   *
   * <pre>
   * Parent class that defines this property.
   * </pre>
   */
  boolean hasOverrides();
  /**
   * <code>optional .dossier.expression.NamedType overrides = 8;</code>
   *
   * <pre>
   * Parent class that defines this property.
   * </pre>
   */
  com.github.jsdossier.proto.NamedType getOverrides();
  /**
   * <code>optional .dossier.expression.NamedType overrides = 8;</code>
   *
   * <pre>
   * Parent class that defines this property.
   * </pre>
   */
  com.github.jsdossier.proto.NamedTypeOrBuilder getOverridesOrBuilder();

  /**
   * <code>repeated .dossier.expression.NamedType specified_by = 9;</code>
   *
   * <pre>
   * Interfaces that define this property.
   * </pre>
   */
  java.util.List<com.github.jsdossier.proto.NamedType> 
      getSpecifiedByList();
  /**
   * <code>repeated .dossier.expression.NamedType specified_by = 9;</code>
   *
   * <pre>
   * Interfaces that define this property.
   * </pre>
   */
  com.github.jsdossier.proto.NamedType getSpecifiedBy(int index);
  /**
   * <code>repeated .dossier.expression.NamedType specified_by = 9;</code>
   *
   * <pre>
   * Interfaces that define this property.
   * </pre>
   */
  int getSpecifiedByCount();
  /**
   * <code>repeated .dossier.expression.NamedType specified_by = 9;</code>
   *
   * <pre>
   * Interfaces that define this property.
   * </pre>
   */
  java.util.List<? extends com.github.jsdossier.proto.NamedTypeOrBuilder> 
      getSpecifiedByOrBuilderList();
  /**
   * <code>repeated .dossier.expression.NamedType specified_by = 9;</code>
   *
   * <pre>
   * Interfaces that define this property.
   * </pre>
   */
  com.github.jsdossier.proto.NamedTypeOrBuilder getSpecifiedByOrBuilder(
      int index);

  /**
   * <code>repeated .dossier.Comment see_also = 10;</code>
   */
  java.util.List<com.github.jsdossier.proto.Comment> 
      getSeeAlsoList();
  /**
   * <code>repeated .dossier.Comment see_also = 10;</code>
   */
  com.github.jsdossier.proto.Comment getSeeAlso(int index);
  /**
   * <code>repeated .dossier.Comment see_also = 10;</code>
   */
  int getSeeAlsoCount();
  /**
   * <code>repeated .dossier.Comment see_also = 10;</code>
   */
  java.util.List<? extends com.github.jsdossier.proto.CommentOrBuilder> 
      getSeeAlsoOrBuilderList();
  /**
   * <code>repeated .dossier.Comment see_also = 10;</code>
   */
  com.github.jsdossier.proto.CommentOrBuilder getSeeAlsoOrBuilder(
      int index);
}
