// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: state.proto

package com.github.jsdossier.proto;

public interface PageSnapshotOrBuilder extends
    // @@protoc_insertion_point(interface_extends:dossier.state.PageSnapshot)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>optional string id = 1;</code>
   *
   * <pre>
   * The snapshot ID.
   * </pre>
   */
  java.lang.String getId();
  /**
   * <code>optional string id = 1;</code>
   *
   * <pre>
   * The snapshot ID.
   * </pre>
   */
  com.google.protobuf.ByteString
      getIdBytes();

  /**
   * <code>optional string data_uri = 2;</code>
   *
   * <pre>
   * The URI for the JSON data file used to generate the page.
   * </pre>
   */
  java.lang.String getDataUri();
  /**
   * <code>optional string data_uri = 2;</code>
   *
   * <pre>
   * The URI for the JSON data file used to generate the page.
   * </pre>
   */
  com.google.protobuf.ByteString
      getDataUriBytes();

  /**
   * <code>optional string title = 3;</code>
   *
   * <pre>
   * The page title.
   * </pre>
   */
  java.lang.String getTitle();
  /**
   * <code>optional string title = 3;</code>
   *
   * <pre>
   * The page title.
   * </pre>
   */
  com.google.protobuf.ByteString
      getTitleBytes();

  /**
   * <code>optional int32 scroll = 4;</code>
   *
   * <pre>
   * The page's scrolling offset.
   * </pre>
   */
  int getScroll();

  /**
   * <code>repeated string open_card = 5;</code>
   *
   * <pre>
   * The IDs of open cards on the page.
   * </pre>
   */
  com.google.protobuf.ProtocolStringList
      getOpenCardList();
  /**
   * <code>repeated string open_card = 5;</code>
   *
   * <pre>
   * The IDs of open cards on the page.
   * </pre>
   */
  int getOpenCardCount();
  /**
   * <code>repeated string open_card = 5;</code>
   *
   * <pre>
   * The IDs of open cards on the page.
   * </pre>
   */
  java.lang.String getOpenCard(int index);
  /**
   * <code>repeated string open_card = 5;</code>
   *
   * <pre>
   * The IDs of open cards on the page.
   * </pre>
   */
  com.google.protobuf.ByteString
      getOpenCardBytes(int index);
}
