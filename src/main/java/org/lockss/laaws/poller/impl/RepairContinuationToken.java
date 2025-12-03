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
import org.lockss.util.rest.poller.model.RepairTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

/**
 * The continuation token used to paginate through a list of repair URLs.
 */
public class RepairContinuationToken {
  private static final Logger logger = LoggerFactory.getLogger(RepairContinuationToken.class);
  private static final String separator = ":";

  private String pollKey = null;
  private RepairTypeEnum repair = null;
  private String url = null;
  private String iteratorId = null;

  /**
   * Constructor from a web request continuation token.
   *
   * @param webRequestContinuationToken A String with the web request continuation token.
   * @throws IllegalArgumentException if the web request continuation token is not syntactically valid.
   */
  public RepairContinuationToken(String webRequestContinuationToken)
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

        pollKey = UrlUtil.decodeUrl(tokenItems.get(0).trim());
        logger.trace("pollKey = {}", pollKey);

        repair = RepairTypeEnum.fromValue(UrlUtil.decodeUrl(tokenItems.get(1).trim()));
        logger.trace("repair = {}", repair);

        url = UrlUtil.decodeUrl(tokenItems.get(2).trim());
        logger.trace("url = {}", url);

        iteratorId = tokenItems.get(3).trim();
        logger.trace("iteratorId = {}", iteratorId);
      } catch (Exception e) {
        logger.warn(message, e);
        throw new IllegalArgumentException(message, e);
      }

      // Validate the format of the web request continuation token.
      if (tokenItems.size() != 4) {
        logger.warn(message);
        throw new IllegalArgumentException(message);
      }

      validateMembers();
    }
  }

  /**
   * Constructor from members.
   *
   * @param pollKey    A String with the poll key.
   * @param repair     A RepairTypeEnum with the repair type.
   * @param url        A String with the URL of the last item transferred.
   * @param iteratorId A String with the UUID of the iterator used.
   */
  public RepairContinuationToken(String pollKey, RepairTypeEnum repair, String url, String iteratorId) {
    this.pollKey = pollKey;
    this.repair = repair;
    this.url = url;
    this.iteratorId = iteratorId;

    validateMembers();
  }

  /**
   * Provides the poll key.
   *
   * @return a String with the poll key.
   */
  public String getPollKey() {
    return pollKey;
  }

  /**
   * Provides the repair type.
   *
   * @return a RepairTypeEnum with the repair type.
   */
  public RepairTypeEnum getRepair() {
    return repair;
  }

  /**
   * Provides the URL of the last item transferred.
   *
   * @return a String with the URL of the last item transferred.
   */
  public String getUrl() {
    return url;
  }

  /**
   * Provides the UUID of the iterator used.
   *
   * @return a String with the UUID of the iterator used.
   */
  public String getIteratorId() {
    return iteratorId;
  }

  /**
   * Provides this object in the form of a web response continuation token.
   *
   * @return a String with this object in the form of a web response continuation token.
   */
  public String toWebResponseContinuationToken() {
    if (pollKey != null && repair != null && url != null && iteratorId != null) {
      String encodedToken =
          UrlUtil.encodeUrl(pollKey) + separator +
          UrlUtil.encodeUrl(repair.toString()) + separator +
          UrlUtil.encodeUrl(url) + separator +
          iteratorId;
      logger.trace("encodedToken = {}", encodedToken);
      return encodedToken;
    }

    String message = "Cannot get web request continuation token from " + this;
    logger.warn(message);
    throw new IllegalArgumentException(message);
  }

  @Override
  public String toString() {
    return "[RepairContinuationToken pollKey=" + pollKey
        + ", repair=" + repair
        + ", url=" + url
        + ", iteratorId=" + iteratorId + "]";
  }

  /**
   * Verifies the validity of the members of this class.
   */
  private void validateMembers() {
    // Validate that all members are all null or all non-null.
    if ((pollKey == null && repair == null && url == null && iteratorId != null)
        || (pollKey != null && repair != null && url != null && iteratorId == null)) {
      String message = "Invalid member combination: pollKey = '" + pollKey
          + "', repair = '" + repair
          + "', url = '" + url
          + "', iteratorId = '" + iteratorId + "'";
      logger.warn(message);
      throw new IllegalArgumentException(message);
    }

    // Validate that the pollKey is not empty.
    if (pollKey != null && pollKey.isEmpty()) {
      String message = "Invalid member: pollKey = '" + pollKey + "'";
      logger.warn(message);
      throw new IllegalArgumentException(message);
    }

    // Validate that the url is not empty.
    if (url != null && url.isEmpty()) {
      String message = "Invalid member: url = '" + url + "'";
      logger.warn(message);
      throw new IllegalArgumentException(message);
    }

    // Validate that the iterator ID is a valid UUID format.
    if (iteratorId != null) {
      try {
        UUID.fromString(iteratorId);
      } catch (IllegalArgumentException e) {
        String message = "Invalid member: iteratorId = '" + iteratorId
            + "' is not a valid UUID";
        logger.warn(message, e);
        throw new IllegalArgumentException(message, e);
      }
    }
  }
}
