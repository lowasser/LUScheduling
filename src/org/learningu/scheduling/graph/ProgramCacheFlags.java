package org.learningu.scheduling.graph;

import com.google.inject.Inject;

import org.learningu.scheduling.flags.Flag;

/**
 * Flags for configuring the caches in a {@code Program}.
 * 
 * @author lowasser
 */
public final class ProgramCacheFlags {
  @Inject(optional = true)
  @Flag(
      name = "teacherAvailableCacheSize",
      description = "Maximum cache size to use for teacher available blocks",
      optional = true)
  int teacherAvailableCacheSize = 10000;

  @Inject(optional = true)
  @Flag(
      name = "prerequisiteCacheSize",
      description = "Maximum cache size to use for prerequisites",
      optional = true)
  int prerequisiteCacheSize = 1000;

  @Inject(optional = true)
  @Flag(
      name = "roomAvailableCacheSize",
      description = "Maximum cache size to use for room available blocks",
      optional = true)
  int roomAvailableCacheSize = 10000;

  @Inject(optional = true)
  @Flag(
      name = "courseCompatibleCacheSize",
      description = "Maximum cache size to use for course compatible blocks",
      optional = true)
  int courseCompatibleCacheSize = 10000;

  @Inject(optional = true)
  @Flag(
      name = "courseTeachersCacheSize",
      description = "Maximum cache size to use for teachers teaching courses",
      optional = true)
  int courseTeachersCacheSize = 10000;

  @Inject(optional = true)
  @Flag(
      name = "reqResCacheSize",
      description = "Maximum cache size to use for required resources",
      optional = true)
  int reqResCacheSize = 10000;

  @Inject(optional = true)
  @Flag(
      name = "roomResourceCacheSize",
      description = "Maximum cache size to use for resources of rooms",
      optional = true)
  int roomResCacheSize = 10000;

  @Inject(optional = true)
  @Flag(
      name = "programCacheConcurrencyLevel",
      description = "Number of concurrent updates allowed in caches for the Program",
      optional = true)
  int cacheConcurrencyLevel = 4;

  @Inject(optional = true)
  @Flag(
      name = "bindingResourceCacheSize",
      optional = true,
      description = "Maximum cache size to use for binding resources.")
  int bindingResourceCacheSize = 1000;

  @Inject(optional = true)
  @Flag(
      name = "teacherGroupCacheSize",
      optional = true,
      description = "Maximum cache size to use for teacher groups.")
  int teacherGroupCacheSize = 1000;
}
