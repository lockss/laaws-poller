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
import java.util.List;
import org.springframework.http.HttpHeaders;

public class Page<T> {

  private Integer mPageNum = 1;
  private Integer mLastPage = 1;
  private Integer mPageSize = 0;

  private int mFirstItem = 0;
  private int mLastItem = 0;
  private int mTotal = 0;
  private String mLinkBase;

  private static final String LINK_TEMPLATE = "%s?page=%d&size=%d";
  private List<T> mContent = new ArrayList<>();

  public Page(Collection<T> content, Integer page, Integer size, String linkBase) {
    if (null !=content) {
      mContent.addAll(content);
    }
    mLinkBase = linkBase;
    mPageSize = size == null ? 0 : size;
    mPageNum = page == null ? 0 : page;
    mTotal = mContent.size();
    if (mTotal == 0 || mPageSize <= 0 || mPageSize >= mTotal) {
      // we return everything (or nothing)
      mPageNum = 1;
      mLastPage = 1;
      mFirstItem = 0;
      mLastItem = mTotal;
      mPageSize = mTotal;
    } else {
      // we need to calculate
      mPageNum = mPageNum > 0 ? mPageNum : 1;
      mLastPage = mTotal / mPageSize + (mTotal % mPageSize > 0 ? 1 : 0);
      mPageNum = mPageNum > mLastPage ? mLastPage : mPageNum;
      mFirstItem = (mPageNum - 1) * mPageSize;
      mLastItem = mFirstItem + mPageSize;
      if (mLastItem > mTotal) {
        mLastItem = mTotal;
      }
    }
  }

  public int getPageNum() {
    return mPageNum;
  }

  public int getPageSize() {
    return mPageSize;
  }

  public int getLastPage() {
    return mLastPage;
  }

  public int getFirstItem() {
    return mFirstItem;
  }

  public int getLastItem() {
    return mLastItem;
  }

  public int getTotal() {
    return mTotal;
  }

  public List<T> getPageContent() {
    if (mFirstItem != 0 && mLastItem != mTotal) {
      return mContent.subList(mFirstItem, mLastItem);
    }
    return mContent;
  }

  public boolean hasContent() {
    return !mContent.isEmpty();
  }


  public String getNextLink() {
    String nextPage = null;
    if (mPageNum < mLastPage) {
      nextPage = String.format(LINK_TEMPLATE, mLinkBase, mPageNum + 1, mPageSize);
    }
    return nextPage;
  }

  public String getPrevLink() {
    String prevPage = null;
    if (mPageNum > 1) {
      prevPage = String.format(LINK_TEMPLATE, mLinkBase, mPageNum - 1, mPageSize);
    }
    return prevPage;
  }

  public String getFirstLink() {
    return String.format(LINK_TEMPLATE, mLinkBase, 1, mPageSize);
  }

  public String getLastLink() {
    return String.format(LINK_TEMPLATE, mLinkBase, mLastPage, mPageSize);
  }

  public HttpHeaders getPageHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Page-Count", "" + mLastPage);
    headers.add("X-Page-Number", "" + mPageNum);
    headers.add("X-Page-Size", "" + mPageSize);
    headers.add("X-Total-Count", "" + mTotal);
    return headers;
  }
}
