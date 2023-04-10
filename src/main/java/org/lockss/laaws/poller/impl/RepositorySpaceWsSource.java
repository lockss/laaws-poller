/*

 Copyright (c) 2014-2020 Board of Trustees of Leland Stanford Jr. University,
 all rights reserved.

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL
 STANFORD UNIVERSITY BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 Except as contained in this notice, the name of Stanford University shall not
 be used in advertising or otherwise to promote the sale, use or other dealings
 in this Software without prior written authorization from Stanford University.

 */

package org.lockss.laaws.poller.impl;

import java.io.IOException;
import java.util.Properties;
import org.lockss.app.LockssDaemon;
import org.lockss.config.ConfigManager;
import org.lockss.config.Configuration;
import org.lockss.db.DbException;
import org.lockss.util.rest.repo.LockssRepository;
import org.lockss.plugin.ArchivalUnit;
import org.lockss.plugin.AuUtil;
import org.lockss.plugin.Plugin;
import org.lockss.plugin.PluginManager;
import org.lockss.util.Logger;
import org.lockss.util.PropUtil;
import org.lockss.util.os.PlatformUtil;
import org.lockss.ws.entities.RepositorySpaceWsResult;

/**
 * Container for the information that is used as the source for a query related
 * to repository spaces.
 */
public class RepositorySpaceWsSource extends RepositorySpaceWsResult {
  private static Logger log = Logger.getLogger();

  private PlatformUtil.DF puDf;

  private boolean sizePopulated;
  private boolean usedPopulated;
  private boolean freePopulated;
  private boolean percentageFullPopulated;
  private boolean activeCountPopulated;
  private boolean inactiveCountPopulated;
  private boolean deletedCountPopulated;
  private boolean orphanedCountPopulated;

  private int allActiveCount = -1;
  private int allInactiveCount = -1;
  private int allDeletedCount = -1;
  private int allOrphanedCount = -1;

  /**
   * Constructor.
   * 
   * @param repositorySpaceId A String with the name of the store space.
   * @param puDf              A PlatformUtil.DF with disk space information.
   */
  public RepositorySpaceWsSource(String repositorySpaceId, PlatformUtil.DF puDf)
  {
    setRepositorySpaceId(repositorySpaceId);
    this.puDf = puDf;
  }

  @Override
  public String getRepositorySpaceId() {
    return super.getRepositorySpaceId();
  }

  @Override
  public Long getSize() {
    if (!sizePopulated) {
      setSize(Long.valueOf(puDf.getSize()));
      sizePopulated = true;
    }

    return super.getSize();
  }

  @Override
  public Long getUsed() {
    if (!usedPopulated) {
      setUsed(Long.valueOf(puDf.getUsed()));
      usedPopulated = true;
    }

    return super.getUsed();
  }

  @Override
  public Long getFree() {
    if (!freePopulated) {
      setFree(Long.valueOf(puDf.getAvail()));
      freePopulated = true;
    }

    return super.getFree();
  }

  @Override
  public Double getPercentageFull() {
    if (!percentageFullPopulated) {
      setPercentageFull(Double.valueOf(puDf.getPercent()));
      percentageFullPopulated = true;
    }

    return super.getPercentageFull();
  }

  @Override
  public Integer getActiveCount() {
    if (!activeCountPopulated) {
      if (allActiveCount < 0) {
	populateCounts();
      }

      setActiveCount(Integer.valueOf(allActiveCount));
      activeCountPopulated = true;
    }

    return super.getActiveCount();
  }

  @Override
  public Integer getInactiveCount() {
    if (!inactiveCountPopulated) {
      if (allInactiveCount < 0) {
	populateCounts();
      }

      setInactiveCount(Integer.valueOf(allInactiveCount));
      inactiveCountPopulated = true;
    }

    return super.getInactiveCount();
  }

  @Override
  public Integer getDeletedCount() {
    if (!deletedCountPopulated) {
      if (allDeletedCount < 0) {
	populateCounts();
      }

      setDeletedCount(Integer.valueOf(allDeletedCount));
      deletedCountPopulated = true;
    }

    return super.getDeletedCount();
  }

  @Override
  public Integer getOrphanedCount() {
    if (!orphanedCountPopulated) {
      if (allOrphanedCount < 0) {
	populateCounts();
      }

      setOrphanedCount(Integer.valueOf(allOrphanedCount));
      orphanedCountPopulated = true;
    }

    return super.getOrphanedCount();
  }

  private void populateCounts() {
    final String DEBUG_HEADER = "populateCounts(): ";
    allActiveCount = 0;
    allInactiveCount = 0;
    allDeletedCount = 0;
    allOrphanedCount = 0;

    // Get the repository.
    LockssRepository repo = LockssDaemon.getLockssDaemon()
	.getRepositoryManager().getV2Repository().getRepository();

    // Get the plugin manager.
    PluginManager pluginMgr = (PluginManager)LockssDaemon
	.getManagerByKeyStatic(LockssDaemon.PLUGIN_MANAGER);

    try {
      // Loop through all the namespaces in the repository.
      for (String namespace : repo.getNamespaces()) {
	if (log.isDebug3())
	  log.debug3(DEBUG_HEADER + "namespace = " + namespace);

	try {
	  // Loop through all the AU identifiers in the namespace.
	  for (String auid : repo.getAuIds(namespace)) {
	    if (log.isDebug3()) log.debug3(DEBUG_HEADER + "auid = " + auid);

	    // Get the archival unit.
	    ArchivalUnit au = pluginMgr.getAuFromId(auid);

	    // Check whether the Archival Unit exits.
	    if (au != null) {
	      // Yes: Count it as active.
	      allActiveCount++;
	    } else {
	      // No: Get the Archival Unit properties.
	      String auKey = PluginManager.auKeyFromAuId(auid);
	      Properties auidProps = null;

	      try {
		auidProps = PropUtil.canonicalEncodedStringToProps(auKey);
	      } catch (Exception e) {
		log.warning("Couldn't decode AUKey : " + auKey, e);
	      }

	      // Get the Archival Unit plugin.
	      String pluginKey = PluginManager
		  .pluginKeyFromId(PluginManager.pluginIdFromAuId(auid));
	      Plugin plugin = pluginMgr.getPlugin(pluginKey);
		  
	      boolean isOrphaned = true;

	      // Check whether both the Archival Unit plugin and properties
	      // exist.
	      if (plugin != null && auidProps != null) {
		// Yes: Get the Archival Unit configuration.
		Configuration defConfig =
		    ConfigManager.fromProperties(auidProps);

		// Determine whether the Archival Unit is orphaned because its
		// configration is incompatible with its plugin.
		isOrphaned =
		    !AuUtil.isConfigCompatibleWithPlugin(defConfig, plugin);
	      }

	      // Check whether the Archival Unit is orphaned.
	      if (isOrphaned) {
		// Yes: Count it as orphaned.
		allOrphanedCount++;
	      } else {
		// No: Get the Archival Unit configuration.
		Configuration config = null;

		try {
		  config =
		      pluginMgr.getStoredAuConfigurationAsConfiguration(auid);
		} catch (DbException dbe) {
		  log.warning("Exception caught getting stored "
		      + "configuration for auid '" + auid + "'", dbe);
		}

		// Check whether the Archival Unit configuration cannot be
		// found.
		if (config == null || config.isEmpty()) {
		  // Yes: Count it as deleted.
		  allDeletedCount++;
		} else {
		  // No: Determine whether the Archival Unit is inactive.
		  boolean isInactive =
		      config.getBoolean(PluginManager.AU_PARAM_DISABLED, false);
			
		  if (isInactive) {
		    allInactiveCount++;
		  } else {
		    allDeletedCount++;
		  }	  
		}
	      }
	    }
	  }
	} catch (IOException ioe) {
	  log.error("Exception caught for namespace '" + namespace
	      + "': Ignoring namespace", ioe);
	}
      }
    } catch (IOException ioe) {
      log.error("Exception caught getting namespaces", ioe);
    }
  }
}
