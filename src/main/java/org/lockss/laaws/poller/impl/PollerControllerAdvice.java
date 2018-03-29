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

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.VndErrors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import org.lockss.laaws.poller.api.NotFoundException;

@ControllerAdvice
@RequestMapping(produces = "application/vnd.error+json")
public class PollerControllerAdvice {

  private static Logger logger = LoggerFactory.getLogger(PollsApiServiceImpl.class);

  /**
   * Handles the general error case. Log track trace at error level
   *
   * @param e the exception not handled by other exception handler methods
   * @return the error response in JSON format with media type
   * application/vnd.error+json
   */
  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ResponseBody
  public ResponseEntity<VndErrors> onException(Exception e) {
    logger.error("Caught exception while handling a request", e);
    return error(e, HttpStatus.INTERNAL_SERVER_ERROR,getExceptionMessage(e));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<VndErrors> assertionException(final IllegalArgumentException e) {
    return error(e, HttpStatus.BAD_REQUEST, getExceptionMessage(e));
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<VndErrors> assertionException(final NotFoundException e) {
    return error(e, HttpStatus.NOT_FOUND, getExceptionMessage(e));
  }

  @ExceptionHandler(NullPointerException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseEntity<VndErrors> assertionException(final NullPointerException e) {
    return error(e, HttpStatus.INTERNAL_SERVER_ERROR, getExceptionMessage(e));
  }

  private ResponseEntity<VndErrors> error(
      final Exception exception, final HttpStatus httpStatus, final String logRef) {
    final String message =
        Optional.of(exception.getMessage()).orElse(exception.getClass().getSimpleName());
    return new ResponseEntity<>(new VndErrors(logRef, message), httpStatus);
  }

  private String getExceptionMessage(Exception e) {
    return StringUtils.hasText(e.getMessage()) ? e.getMessage() : e.getClass().getSimpleName();
  }

}
