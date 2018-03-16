/*
 * Copyright (c) 2018 Board of Trustees of Leland Stanford Jr. University,
 * all rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL
 * STANFORD UNIVERSITY BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of Stanford University shall not
 * be used in advertising or otherwise to promote the sale, use or other dealings
 * in this Software without prior written authorization from Stanford University.
 */

package org.lockss.laaws.poller.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.springframework.http.HttpHeaders;

public class Page<T> {

  public static final int DEFAULT_OFFSET = 1;
  public static final int MIN_OFFSET = 1;
  public static final int DEFAULT_LIMIT = 20;
  public static final int MAX_LIMIT = 100;

  protected int pageNum = 1;
  protected int pageSize;
  protected int lastPage = 1;

  protected int offset = 0;
  protected int total = 0;

  private final List<T> content = new ArrayList<>();

  public Page(Collection<T> content, int page, int size) {
    if (null == content) {
      throw new IllegalArgumentException("Content must not be null!");
    }
    this.content.addAll(content);

    pageNum = page;
    pageSize = size;
    total = content.size();
    if (total == 0 || size <= 0 || size >= total) {
      // we return everything (or nothing)
      pageNum = 1;
      lastPage = 1;
      pageSize = total;
      offset = 0;
    } else {
      // we need to calculate pageNum and LastPage
      lastPage = total / size + (total % size > 0 ? 1 : 0) + 1;
      pageNum = page > 0 ? page : 1;
      pageNum = pageNum > lastPage ? lastPage : pageNum;
      offset = (pageNum - 1) * size;
    }
  }

  public int getPageNum() {
    return pageNum;
  }

  public int getPageSize() {
    return pageSize;
  }

  public int getLastPage() {
    return lastPage;
  }

  public int getOffset() {
    return offset;
  }

  public int getTotal() {
    return total;
  }

  public List<T> getPageContent() {
    return Collections.unmodifiableList(this.content);
  }

  public boolean hasContent() {
    return !content.isEmpty();
  }


  public String getNextLink(String baseUri) {
    String nextPage = null;
    if (pageNum < lastPage) {
      nextPage = baseUri + "?page=" + (pageNum + 1) + "&size=" + pageSize;
    }
    return nextPage;
  }

  public String getPrevLink(String baseUri) {
    String prevPage = null;
    if (pageNum > 1) {
      prevPage = baseUri + "?page=" + (pageNum - 1) + "&size=" + pageSize;
    }
    return prevPage;
  }

  public String getFirstLink(String baseUri) {
    return baseUri + "?page=" + 1 + "&size=" + pageSize;
  }

  public String getLastLink(String baseUri) {
    return baseUri + "?page=" + lastPage + "&size=" + pageSize;
  }

  public HttpHeaders getPageHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Page-Count", "" + lastPage);
    headers.add("X-Page-Number", "" + pageNum);
    headers.add("X-Page-Size", "" + pageSize);
    headers.add("X-Total-Count", "" + total);
    return headers;
  }
}
