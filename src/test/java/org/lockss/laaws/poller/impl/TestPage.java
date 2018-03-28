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
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

class TestPage {

  private static final int BIG_TEST_SIZE = 100;
  private static final int SMALL_TEST_SIZE = 7;
  private static final int DEFAULT_PAGE = 1;
  private static final int DEFAULT_SIZE = 10;
  private static final String ENTRY_PREFIX = "StringEntry:";
  private static final String BASE_URI = "http://www.example.com";
  private Page<String> mPage;
  private List<String> mStrList = new ArrayList<>();

  @Test
  void testGetPageContent() {
    // get mPage content (returns all contents
    initList(mStrList, BIG_TEST_SIZE);
    mPage = new Page<>(mStrList, DEFAULT_PAGE, DEFAULT_SIZE, BASE_URI);
    Assertions.assertEquals(mStrList, mPage.getPageContent());
  }

  @Test
  void testHasContent() {
    initList(mStrList, BIG_TEST_SIZE);
    mPage = new Page<>(mStrList, DEFAULT_PAGE, DEFAULT_SIZE, BASE_URI);
    Assertions.assertTrue(mPage.hasContent());
    mPage = new Page<>(new ArrayList<String>(), 1, DEFAULT_SIZE, BASE_URI);
    Assertions.assertFalse(mPage.hasContent());

  }

  @Test
  void testGetNextLink() {
    // first mPage next link is 2nd mPage.
    initList(mStrList, BIG_TEST_SIZE);
    mPage = new Page<>(mStrList, DEFAULT_PAGE, DEFAULT_SIZE, BASE_URI);
    String expected = BASE_URI + "?page=2&size=10";
    Assertions.assertEquals(expected, mPage.getNextLink());

    // last mPage next link is null
    mPage = new Page<>(mStrList, 10, DEFAULT_SIZE, BASE_URI);
    Assertions.assertNull(mPage.getNextLink());

    // one mPage list has null next.
    mStrList.clear();
    initList(mStrList, SMALL_TEST_SIZE);
    mPage = new Page<>(mStrList, DEFAULT_PAGE, DEFAULT_SIZE, BASE_URI);
    Assertions.assertNull(mPage.getNextLink());
  }

  @Test
  void testGetPrevLink() {
    // first mPage prev link is null.
    initList(mStrList, BIG_TEST_SIZE);
    mPage = new Page<>(mStrList, DEFAULT_PAGE, DEFAULT_SIZE, BASE_URI);
    Assertions.assertNull(mPage.getPrevLink());

    // last mPage prev link is 9
    mPage = new Page<>(mStrList, 10, DEFAULT_SIZE, BASE_URI);
    String expected = BASE_URI + "?page=9&size=10";
    Assertions.assertEquals(expected, mPage.getPrevLink());

    // one mPage list has null prev.
    mStrList.clear();
    initList(mStrList, SMALL_TEST_SIZE);
    mPage = new Page<>(mStrList, DEFAULT_PAGE, DEFAULT_SIZE, BASE_URI);
    Assertions.assertNull(mPage.getPrevLink());
  }

  @Test
  void testGetFirstLink() {
    initList(mStrList, BIG_TEST_SIZE);
    mPage = new Page<>(mStrList, 3, DEFAULT_SIZE, BASE_URI);
    String expected = BASE_URI + "?page=1&size=10";
    Assertions.assertEquals(expected, mPage.getFirstLink());

    // one mPage list -  first mPage = first mPage
    mStrList.clear();
    initList(mStrList, SMALL_TEST_SIZE);
    mPage = new Page<>(mStrList, DEFAULT_PAGE, DEFAULT_SIZE, BASE_URI);
    expected = BASE_URI + "?page=1&size=" + SMALL_TEST_SIZE;
    Assertions.assertEquals(expected, mPage.getFirstLink());
  }

  @Test
  void testGetLastLink() {
    initList(mStrList, BIG_TEST_SIZE);
    mPage = new Page<>(mStrList, 3, DEFAULT_SIZE, BASE_URI);
    String result = mPage.getLastLink();
    String expected = BASE_URI + "?page=10&size=10";
    Assertions.assertEquals(expected, result);

    // one mPage list  first mPage = last mPage
    mStrList.clear();
    initList(mStrList, SMALL_TEST_SIZE);
    mPage = new Page<>(mStrList, DEFAULT_PAGE, DEFAULT_SIZE, BASE_URI);
    expected = BASE_URI + "?page=1&size=" + SMALL_TEST_SIZE;
    Assertions.assertEquals(expected, mPage.getLastLink());
  }

  @Test
  void testGetPageHeaders() {
    initList(mStrList, BIG_TEST_SIZE);
    mPage = new Page<>(mStrList, 3, DEFAULT_SIZE, BASE_URI);
    HttpHeaders headers = mPage.getPageHeaders();
    Assertions.assertEquals("10", headers.get("X-Page-Count").get(0));
    Assertions.assertEquals("3", headers.get("X-Page-Number").get(0));
    Assertions.assertEquals("10", headers.get("X-Page-Size").get(0));
    Assertions.assertEquals("100", headers.get("X-Total-Count").get(0));

    mStrList.clear();
    initList(mStrList, SMALL_TEST_SIZE);
    mPage = new Page<>(mStrList, DEFAULT_PAGE, DEFAULT_SIZE, BASE_URI);
    headers = mPage.getPageHeaders();
    Assertions.assertEquals("1", headers.get("X-Page-Count").get(0));
    Assertions.assertEquals("1", headers.get("X-Page-Number").get(0));
    Assertions.assertEquals("7", headers.get("X-Page-Size").get(0));
    Assertions.assertEquals("7", headers.get("X-Total-Count").get(0));
  }

  @Test
  void testUndefinedListSize() {
    initList(mStrList, BIG_TEST_SIZE);
    mPage = new Page<>(mStrList, 3, -1, BASE_URI);
    Assertions.assertEquals(1, mPage.getPageNum());
    Assertions.assertEquals(BIG_TEST_SIZE, mPage.getPageSize());
    Assertions.assertNull(mPage.getNextLink());
    Assertions.assertNull(mPage.getPrevLink());
  }

  @Test
  void testUndefinedPageNum() {
    initList(mStrList, BIG_TEST_SIZE);
    mPage = new Page<>(mStrList, -1, DEFAULT_SIZE, BASE_URI);
    Assertions.assertEquals(1, mPage.getPageNum());
    Assertions.assertEquals(10, mPage.getPageSize());
    Assertions.assertEquals(BASE_URI + "?page=2&size=10", mPage.getNextLink());
    Assertions.assertNull(mPage.getPrevLink());

  }

  @Test
  void testNullorEmpty() {
    // Null
    mPage = new Page<>(null, 0, 0, BASE_URI);
    Assertions.assertEquals(1, mPage.getPageNum());
    Assertions.assertEquals(0, mPage.getPageSize());
    Assertions.assertEquals(0, mPage.getTotal());
    Assertions.assertNull(mPage.getNextLink());
    Assertions.assertNull(mPage.getPrevLink());
    Assertions.assertNotNull(mPage.getPageContent());
    Assertions.assertFalse(mPage.hasContent());

    // Empty List
    mPage = new Page<>(mStrList, 0, 0, BASE_URI);
    Assertions.assertEquals(1, mPage.getPageNum());
    Assertions.assertEquals(0, mPage.getPageSize());
    Assertions.assertEquals(0, mPage.getTotal());
    Assertions.assertNull(mPage.getNextLink());
    Assertions.assertNull(mPage.getPrevLink());
    Assertions.assertNotNull(mPage.getPageContent());
    Assertions.assertFalse(mPage.hasContent());
  }

  private void initList(List<String> szList, int size) {
    for (int i = 0; i < size; i++) {
      szList.add(i, ENTRY_PREFIX + i);
    }
  }

}

