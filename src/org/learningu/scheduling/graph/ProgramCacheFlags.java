package org.learningu.scheduling.graph;

import org.learningu.scheduling.flags.Flag;

/**
 * Flags for configuring the caches in a {@code Program}.
 * 
 * @author lowasser
 */
public final class ProgramCacheFlags {
  @Flag(
      name = "teacherAvailableCacheSize",
      description = "Maximum cache size to use for teacher available blocks")
  int teacherAvailableCacheSize = 10000;

  @Flag(
      name = "roomAvailableCacheSize",
      description = "Maximum cache size to use for room available blocks")
  int roomAvailableCacheSize = 10000;

  @Flag(
      name = "courseCompatibleCacheSize",
      description = "Maximum cache size to use for course compatible blocks")
  int courseCompatibleCacheSize = 10000;

  @Flag(
      name = "courseTeachersCacheSize",
      description = "Maximum cache size to use for teachers teaching courses")
  int courseTeachersCacheSize = 10000;

  @Flag(
      name = "reqPropsCacheSize",
      description = "Maximum cache size to use for required properties of rooms")
  int reqPropsCacheSize = 10000;

  @Flag(
      name = "roomPropsCacheSize",
      description = "Maximum cache size to use for properties of rooms")
  int roomPropsCacheSize = 10000;

  @Flag(
      name = "programCacheConcurrencyLevel",
      description = "Number of concurrent updates allowed in caches for the Program")
  int cacheConcurrencyLevel = 4;
}