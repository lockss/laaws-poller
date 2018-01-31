package org.lockss.laaws.poller.impl;

import org.lockss.laaws.poller.api.PollsApiDelegate;

public class PollsApiServiceFactory {
  private final static PollsApiDelegate delegate = new PollsApiServiceImpl();

  public static PollsApiDelegate getPollsApi()
  {
    return delegate;
  }
}
