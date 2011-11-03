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
      description = "Maximum cache size to use for teacher available blocks",
      optional = true)
  int teacherAvailableCacheSize = 10000;

  @Flag(
      name = "prerequisiteCacheSize",
      description = "Maximum cache size to use for prerequisites",
      optional = true)
  int prerequisiteCacheSize = 1000;

  @Flag(
      name = "roomAvailableCacheSize",
      description = "Maximum cache size to use for room available blocks",
      optional = true)
  int roomAvailableCacheSize = 10000;

  @Flag(
      name = "courseCompatibleCacheSize",
      description = "Maximum cache size to use for course compatible blocks",
      optional = true)
  int courseCompatibleCacheSize = 10000;

  @Flag(
      name = "courseTeachersCacheSize",
      description = "Maximum cache size to use for teachers teaching courses",
      optional = true)
  int courseTeachersCacheSize = 10000;

  @Flag(
      name = "reqResCacheSize",
      description = "Maximum cache size to use for required resources",
      optional = true)
  int reqResCacheSize = 10000;

  @Flag(
      name = "roomResourceCacheSize",
      description = "Maximum cache size to use for resources of rooms",
      optional = true)
  int roomResCacheSize = 10000;

  @Flag(
      name = "programCacheConcurrencyLevel",
      description = "Number of concurrent updates allowed in caches for the Program",
      optional = true)
  int cacheConcurrencyLevel = 4;

  @Flag(name = "bindingResourceCacheSize")
  int bindingResourceCacheSize = 1000;
}
