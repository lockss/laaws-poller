package org.lockss.laaws.poller.api;

import org.springframework.stereotype.Controller;

@Controller
public class PollsApiController implements PollsApi {

    private final PollsApiDelegate delegate;

    @org.springframework.beans.factory.annotation.Autowired
    public PollsApiController(PollsApiDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public PollsApiDelegate getDelegate() {
        return delegate;
    }
}
