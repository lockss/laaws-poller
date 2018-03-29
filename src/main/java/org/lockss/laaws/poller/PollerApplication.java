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

package org.lockss.laaws.poller;

import static org.lockss.app.LockssApp.PARAM_START_PLUGINS;
import static org.lockss.app.ManagerDescs.ACCOUNT_MANAGER_DESC;
import static org.lockss.app.ManagerDescs.HASH_SERVICE_DESC;
import static org.lockss.app.ManagerDescs.IDENTITY_MANAGER_DESC;
import static org.lockss.app.ManagerDescs.PLUGIN_MANAGER_DESC;
import static org.lockss.app.ManagerDescs.POLL_MANAGER_DESC;
import static org.lockss.app.ManagerDescs.REPOSITORY_MANAGER_DESC;
import static org.lockss.app.ManagerDescs.ROUTER_MANAGER_DESC;
import static org.lockss.app.ManagerDescs.SCHED_SERVICE_DESC;
import static org.lockss.app.ManagerDescs.STREAM_COMM_MANAGER_DESC;
import static org.lockss.app.ManagerDescs.SYSTEM_METRICS_DESC;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.lockss.app.LockssApp;
import org.lockss.app.LockssApp.AppSpec;
import org.lockss.app.LockssApp.ManagerDesc;
import org.lockss.app.LockssDaemon;
import org.lockss.plugin.PluginManager;
import org.lockss.rs.base.BaseSpringBootApplication;

@SpringBootApplication
public class PollerApplication extends BaseSpringBootApplication implements CommandLineRunner {

  private static final Logger logger =
      LoggerFactory.getLogger(PollerApplication.class);

  // Manager descriptors.  The order of this table determines the order in
  // which managers are initialized and started.
  private static final ManagerDesc[] myManagerDescs = {
      ACCOUNT_MANAGER_DESC,
      PLUGIN_MANAGER_DESC,
      IDENTITY_MANAGER_DESC,
      SCHED_SERVICE_DESC,
      SYSTEM_METRICS_DESC,
      HASH_SERVICE_DESC,
      ROUTER_MANAGER_DESC,
      STREAM_COMM_MANAGER_DESC,
      REPOSITORY_MANAGER_DESC,
      POLL_MANAGER_DESC
  };

  public static void main(String[] args) {
    logger.info("Starting the  Poller REST service...");
    configure();
    SpringApplication.run(PollerApplication.class, args);
  }

  @Override
  public void run(String... args) {
    if (args != null && args.length > 0) {
      logger.info("Starting the LOCKSS daemon");
      AppSpec spec = new AppSpec()
          .setName("Poller Service")
          .setArgs(args)
          .setAppManagers(myManagerDescs)
          .addAppConfig(PARAM_START_PLUGINS, "true")
          .addAppConfig(PluginManager.PARAM_START_ALL_AUS, "true");
      LockssApp.startStatic(LockssDaemon.class, spec);
    }
  }
}
