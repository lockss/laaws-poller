/*
 * Copyright (c) 2025 Board of Trustees of Leland Stanford Jr. University,
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

import org.lockss.util.StringUtil;
import org.lockss.util.UrlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * The continuation token used to paginate through a list of tally URLs.
 */
public class TallyContinuationToken {
  private static final Logger logger = LoggerFactory.getLogger(TallyContinuationToken.class);
  private static final String separator = ":";

  private String url = null;
  private Integer iteratorHashCode = null;

  /**
   * Constructor from a web request continuation token.
   *
   * @param webRequestContinuationToken A String with the web request continuation token.
   * @throws IllegalArgumentException if the web request continuation token is not syntactically valid.
   */
  public TallyContinuationToken(String webRequestContinuationToken)
      throws IllegalArgumentException {
    logger.debug("webRequestContinuationToken = {}", webRequestContinuationToken);

    String message = "Invalid web request continuation token '"
        + webRequestContinuationToken + "'";

    // Check whether a non-empty web request continuation token has been passed.
    if (webRequestContinuationToken != null
        && !webRequestContinuationToken.trim().isEmpty()) {
      // Yes: Parse it.
      List<String> tokenItems = null;

      try {
        tokenItems =
            StringUtil.breakAt(webRequestContinuationToken.trim(), separator);
        logger.trace("tokenItems = {}", tokenItems);

        url = UrlUtil.decodeUrl(tokenItems.get(0).trim());
        logger.trace("url = {}", url);

        iteratorHashCode = Integer.valueOf(tokenItems.get(1).trim());
        logger.trace("iteratorHashCode = {}", iteratorHashCode);
      } catch (Exception e) {
        logger.warn(message, e);
        throw new IllegalArgumentException(message, e);
      }

      // Validate the format of the web request continuation token.
      if (tokenItems.size() != 2) {
        logger.warn(message);
        throw new IllegalArgumentException(message);
      }

      validateMembers();
    }
  }

  /**
   * Constructor from members.
   *
   * @param url              A String with the URL of the last tally URL transferred.
   * @param iteratorHashCode An Integer with the hash code of the iterator used.
   */
  public TallyContinuationToken(String url, Integer iteratorHashCode) {
    this.url = url;
    this.iteratorHashCode = iteratorHashCode;

    validateMembers();
  }

  /**
   * Provides the URL of the last tally URL transferred.
   *
   * @return a String with the URL of the last tally URL transferred.
   */
  public String getUrl() {
    return url;
  }

  /**
   * Provides the hash code of the iterator used.
   *
   * @return an Integer with the hash code of the iterator used.
   */
  public Integer getIteratorHashCode() {
    return iteratorHashCode;
  }

  /**
   * Provides this object in the form of a web response continuation token.
   *
   * @return a String with this object in the form of a web response continuation token.
   */
  public String toWebResponseContinuationToken() {
    if (url != null && iteratorHashCode != null) {
      String encodedToken =
          UrlUtil.encodeUrl(url) + separator + iteratorHashCode;
      logger.trace("encodedToken = {}", encodedToken);
      return encodedToken;
    }

    String message = "Cannot get web request continuation token from " + this;
    logger.warn(message);
    throw new IllegalArgumentException(message);
  }

  @Override
  public String toString() {
    return "[TallyContinuationToken url=" + url
        + ", iteratorHashCode=" + iteratorHashCode + "]";
  }

  /**
   * Verifies the validity of the members of this class.
   */
  private void validateMembers() {
    // Validate that both members are both null or both non-null.
    if ((url == null && iteratorHashCode != null)
        || (url != null && iteratorHashCode == null)) {
      String message = "Invalid member combination: url = '" + url
          + "', iteratorHashCode = '" + iteratorHashCode + "'";
      logger.warn(message);
      throw new IllegalArgumentException(message);
    }

    // Validate that the url is not empty.
    if (url != null && url.isEmpty()) {
      String message = "Invalid member: url = '" + url + "'";
      logger.warn(message);
      throw new IllegalArgumentException(message);
    }

    // Validate that the iterator hash code is not negative.
    if (iteratorHashCode != null && iteratorHashCode.intValue() < 0) {
      String message =
          "Invalid member: iteratorHashCode = '" + iteratorHashCode + "'";
      logger.warn(message);
      throw new IllegalArgumentException(message);
    }
  }
}
