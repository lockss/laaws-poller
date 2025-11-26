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
 * The continuation token used to paginate through a list of repair items.
 */
public class RepairContinuationToken {
  private static final Logger logger = LoggerFactory.getLogger(RepairContinuationToken.class);
  private static final String separator = ":";

  private String repairUrl = null;
  private Integer iteratorHashCode = null;

  public RepairContinuationToken(String webRequestContinuationToken)
      throws IllegalArgumentException {
    logger.debug("webRequestContinuationToken = {}", webRequestContinuationToken);

    String message = "Invalid web request continuation token '"
        + webRequestContinuationToken + "'";

    if (webRequestContinuationToken != null
        && !webRequestContinuationToken.trim().isEmpty()) {
      List<String> tokenItems = null;

      try {
        tokenItems =
            StringUtil.breakAt(webRequestContinuationToken.trim(), separator);
        logger.trace("tokenItems = {}", tokenItems);

        repairUrl = UrlUtil.decodeUrl(tokenItems.get(0).trim());
        logger.trace("repairUrl = {}", repairUrl);

        iteratorHashCode = Integer.valueOf(tokenItems.get(1).trim());
        logger.trace("iteratorHashCode = {}", iteratorHashCode);
      } catch (Exception e) {
        logger.warn(message, e);
        throw new IllegalArgumentException(message, e);
      }

      if (tokenItems.size() != 2) {
        logger.warn(message);
        throw new IllegalArgumentException(message);
      }

      validateMembers();
    }
  }

  public RepairContinuationToken(String repairUrl, Integer iteratorHashCode) {
    this.repairUrl = repairUrl;
    this.iteratorHashCode = iteratorHashCode;

    validateMembers();
  }

  public String getRepairUrl() {
    return repairUrl;
  }

  public Integer getIteratorHashCode() {
    return iteratorHashCode;
  }

  public String toWebResponseContinuationToken() {
    if (repairUrl != null && iteratorHashCode != null) {
      String encodedToken =
          UrlUtil.encodeUrl(repairUrl) + separator + iteratorHashCode;
      logger.trace("encodedToken = {}", encodedToken);
      return encodedToken;
    }

    String message = "Cannot get web request continuation token from " + this;
    logger.warn(message);
    throw new IllegalArgumentException(message);
  }

  @Override
  public String toString() {
    return "[RepairContinuationToken repairUrl=" + repairUrl
        + ", iteratorHashCode=" + iteratorHashCode + "]";
  }

  private void validateMembers() {
    if ((repairUrl == null && iteratorHashCode != null)
        || (repairUrl != null && iteratorHashCode == null)) {
      String message = "Invalid member combination: repairUrl = '" + repairUrl
          + "', iteratorHashCode = '" + iteratorHashCode + "'";
      logger.warn(message);
      throw new IllegalArgumentException(message);
    }

    if (repairUrl != null && repairUrl.isEmpty()) {
      String message = "Invalid member: repairUrl = '" + repairUrl + "'";
      logger.warn(message);
      throw new IllegalArgumentException(message);
    }

    if (iteratorHashCode != null && iteratorHashCode.intValue() < 0) {
      String message =
          "Invalid member: iteratorHashCode = '" + iteratorHashCode + "'";
      logger.warn(message);
      throw new IllegalArgumentException(message);
    }
  }
}
