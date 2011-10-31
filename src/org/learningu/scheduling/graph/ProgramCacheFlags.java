package org.learningu.scheduling.graph;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.learningu.scheduling.annotations.Flag;

/**
 * Flags for configuring the caches in a {@code Program}.
 * 
 * @author lowasser
 */
public final class ProgramCacheFlags {

  public static final ProgramCacheFlags DEFAULTS = new ProgramCacheFlags(1000, 10000, 10000,
      10000, 10000, 10000, 10000, 4);

  @Flag(
      value = "prerequisiteCacheSize",
      description = "Maximum cache size to use for prerequisites",
      defaultValue = "1000")
  final int prerequisiteCacheSize;

  @Flag(
      value = "teacherAvailableCacheSize",
      description = "Maximum cache size to use for teacher available blocks",
      defaultValue = "10000")
  final int teacherAvailableCacheSize;

  @Flag(
      value = "roomAvailableCacheSize",
      description = "Maximum cache size to use for room available blocks",
      defaultValue = "10000")
  final int roomAvailableCacheSize;

  @Flag(
      value = "courseCompatibleCacheSize",
      description = "Maximum cache size to use for course compatible blocks",
      defaultValue = "10000")
  final int courseCompatibleCacheSize;

  @Flag(
      value = "courseTeachersCacheSize",
      description = "Maximum cache size to use for teachers teaching courses",
      defaultValue = "10000")
  final int courseTeachersCacheSize;

  @Flag(
      value = "reqPropsCacheSize",
      description = "Maximum cache size to use for required properties of rooms",
      defaultValue = "10000")
  final int reqPropsCacheSize;

  @Flag(
      value = "roomPropsCacheSize",
      description = "Maximum cache size to use for properties of rooms",
      defaultValue = "10000")
  final int roomPropsCacheSize;

  @Flag(
      value = "programCacheConcurrencyLevel",
      description = "Number of concurrent updates allowed in caches for the Program",
      defaultValue = "4")
  final int cacheConcurrencyLevel;

  @Inject
  ProgramCacheFlags(
      @Named("prerequisiteCacheSize") int prerequisiteCacheSize,
      @Named("teacherAvailableCacheSize") int teacherAvailableCacheSize,
      @Named("roomAvailableCacheSize") int roomAvailableCacheSize,
      @Named("courseCompatibleCacheSize") int courseCompatibleCacheSize,
      @Named("courseTeachersCacheSize") int courseTeachersCacheSize,
      @Named("roomPropsCacheSize") int roomPropsCacheSize,
      @Named("reqPropsCacheSize") int reqPropsCacheSize,
      @Named("programCacheConcurrencyLevel") int cacheConcurrencyLevel) {
    this.prerequisiteCacheSize = prerequisiteCacheSize;
    this.teacherAvailableCacheSize = teacherAvailableCacheSize;
    this.roomAvailableCacheSize = roomAvailableCacheSize;
    this.courseCompatibleCacheSize = courseCompatibleCacheSize;
    this.courseTeachersCacheSize = courseTeachersCacheSize;
    this.roomPropsCacheSize = roomPropsCacheSize;
    this.reqPropsCacheSize = reqPropsCacheSize;
    this.cacheConcurrencyLevel = cacheConcurrencyLevel;
  }
}