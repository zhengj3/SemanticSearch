package org.apache.lucene.search;
/**
 * Copyright 2005 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import org.apache.lucene.index.IndexReader;

import java.io.IOException;
import java.util.BitSet;

/**
 * Wraps another SpanFilter's result and caches it.  The purpose is to allow
 * filters to simply filter, and then wrap with this class to add caching.
 */
public class CachingSpanFilter extends SpanFilter {
  protected SpanFilter filter;

  /**
   * A transient Filter cache.
   */
  private final CachingWrapperFilter.FilterCache cache;

  /**
   * New deletions always result in a cache miss, by default
   * ({@link CachingWrapperFilter.DeletesMode#RECACHE}.
   * @param filter Filter to cache results of
   */
  public CachingSpanFilter(SpanFilter filter) {
    this(filter, CachingWrapperFilter.DeletesMode.RECACHE);
  }

  /**
   * @param filter Filter to cache results of
   * @param deletesMode See {@link CachingWrapperFilter.DeletesMode}
   */
  public CachingSpanFilter(SpanFilter filter, CachingWrapperFilter.DeletesMode deletesMode) {
    this.filter = filter;
    if (deletesMode == CachingWrapperFilter.DeletesMode.DYNAMIC) {
      throw new IllegalArgumentException("DeletesMode.DYNAMIC is not supported");
    }
    this.cache = new CachingWrapperFilter.FilterCache(deletesMode) {
      protected Object mergeDeletes(final IndexReader r, final Object value) {
        throw new IllegalStateException("DeletesMode.DYNAMIC is not supported");
      }
    };
  }

  /**
   * @deprecated Use {@link #getDocIdSet(IndexReader)} instead.
   */
  public BitSet bits(IndexReader reader) throws IOException {
    SpanFilterResult result = getCachedResult(reader);
    return result != null ? result.getBits() : null;
  }
  
  public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
    SpanFilterResult result = getCachedResult(reader);
    return result != null ? result.getDocIdSet() : null;
  }
  
  // for testing
  int hitCount, missCount;

  private SpanFilterResult getCachedResult(IndexReader reader) throws IOException {

    final Object coreKey = reader.getFieldCacheKey();
    final Object delCoreKey = reader.hasDeletions() ? reader.getDeletesCacheKey() : coreKey;

    SpanFilterResult result = (SpanFilterResult) cache.get(reader, coreKey, delCoreKey);
    if (result != null) {
      hitCount++;
      return result;
    }

    missCount++;
    result = filter.bitSpans(reader);

    cache.put(coreKey, delCoreKey, result);
    return result;
  }


  public SpanFilterResult bitSpans(IndexReader reader) throws IOException {
    return getCachedResult(reader);
  }

  public String toString() {
    return "CachingSpanFilter("+filter+")";
  }

  public boolean equals(Object o) {
    if (!(o instanceof CachingSpanFilter)) return false;
    return this.filter.equals(((CachingSpanFilter)o).filter);
  }

  public int hashCode() {
    return filter.hashCode() ^ 0x1117BF25;
  }
}
