// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: dossier.proto

package com.github.jsdossier.proto;

public interface CommentOrBuilder extends
    // @@protoc_insertion_point(interface_extends:dossier.Comment)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>repeated .dossier.Comment.Token token = 1;</code>
   *
   * <pre>
   * The tokens that comprise this comment.
   * </pre>
   */
  java.util.List<com.github.jsdossier.proto.Comment.Token> 
      getTokenList();
  /**
   * <code>repeated .dossier.Comment.Token token = 1;</code>
   *
   * <pre>
   * The tokens that comprise this comment.
   * </pre>
   */
  com.github.jsdossier.proto.Comment.Token getToken(int index);
  /**
   * <code>repeated .dossier.Comment.Token token = 1;</code>
   *
   * <pre>
   * The tokens that comprise this comment.
   * </pre>
   */
  int getTokenCount();
  /**
   * <code>repeated .dossier.Comment.Token token = 1;</code>
   *
   * <pre>
   * The tokens that comprise this comment.
   * </pre>
   */
  java.util.List<? extends com.github.jsdossier.proto.Comment.TokenOrBuilder> 
      getTokenOrBuilderList();
  /**
   * <code>repeated .dossier.Comment.Token token = 1;</code>
   *
   * <pre>
   * The tokens that comprise this comment.
   * </pre>
   */
  com.github.jsdossier.proto.Comment.TokenOrBuilder getTokenOrBuilder(
      int index);
}