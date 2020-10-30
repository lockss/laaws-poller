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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.lockss.app.LockssDaemon;
import org.lockss.config.ConfigManager;
import org.lockss.config.Configuration;
import org.lockss.plugin.ArchivalUnit;
import org.lockss.plugin.AuUtil;
import org.lockss.plugin.Plugin;
import org.lockss.plugin.PluginManager;
import org.lockss.util.Logger;
import org.lockss.util.PropUtil;
import org.lockss.ws.entities.RepositoryWsResult;

/**
 * Container for the information that is used as the source for a query related
 * to repositories.
 */
public class RepositoryWsSource extends RepositoryWsResult {
  private static final String NOT_APPLICABLE = "NOT APPLICABLE";
  private static Logger log = Logger.getLogger();
  private boolean directoryNamePopulated;
  private boolean auNamePopulated;
  private boolean internalPopulated;
  private boolean statusPopulated;
  private boolean diskUsagePopulated;
  private boolean pluginNamePopulated;
  private boolean paramsPopulated;

  private String collectionId;
  private String auId;
  private ArchivalUnit au;
  private boolean auPopulated;
  private Properties auIdProps;
  private boolean auIdPropsPopulated;

  private LockssDaemon theDaemon = null;
  private PluginManager pluginMgr = null;

  /**
   * Constructor.
   * 
   * @param repositorySpaceId A String with the name of the store space.
   * @param collectionId      A String with the identifier of a collection.
   * @param auId              A String with the identifier of an Archival Unit.
   */
  public RepositoryWsSource(String repositorySpaceId, String collectionId,
      String auId) {
    setRepositorySpaceId(repositorySpaceId);
    this.collectionId = collectionId;
    this.auId = auId;
  }

  @Override
  public String getRepositorySpaceId() {
    return super.getRepositorySpaceId();
  }

  @Override
  public String getDirectoryName() {
    if (!directoryNamePopulated) {
      setDirectoryName(NOT_APPLICABLE);
      directoryNamePopulated = true;
    }

    return super.getDirectoryName();
  }

  @Override
  public String getAuName() {
    if (!auNamePopulated) {
      if (auId != null) {
	String name = null;

	if (getArchivalUnit() != null) {
	  name = au.getName();
	} else {
	  name = "";

	  if (!isOrphaned()) {
	    try {
	      Configuration config = getPluginManager()
		  .getStoredAuConfigurationAsConfiguration(auId);

	      if (config != null && !config.isEmpty()) {
		name = config.get(PluginManager.AU_PARAM_DISPLAY_NAME);
	      }
	    } catch (Exception e) {
	      throw new RuntimeException(e);
	    }
	  }
	}

	if (name != null) {
	  setAuName(name);
	}
      }

      auNamePopulated = true;
    }

    return super.getAuName();
  }

  @Override
  public Boolean getInternal() {
    if (!internalPopulated) {
      setInternal(Boolean.valueOf(getPluginManager()
	  .isInternalAu(getPluginManager().getAuFromIdIfExists(auId))));

      internalPopulated = true;
    }

    return super.getInternal();
  }

  @Override
  public String getStatus() {
    if (!statusPopulated) {
      if (auId == null) {
	setStatus("No AUID");
      } else {
	if (getArchivalUnit() != null) {
	  setStatus("Active");
	} else {
	  if (isOrphaned()) {
	    setStatus("Orphaned");
	  } else {
	    try {
	      Configuration config = getPluginManager()
		  .getStoredAuConfigurationAsConfiguration(auId);

	      if (config == null || config.isEmpty()) {
		setStatus("Deleted");
	      } else {
		if (config.getBoolean(PluginManager.AU_PARAM_DISABLED, false)) {
		  setStatus("Inactive");
		} else {
		  setStatus("Deleted");
		}	  
	      }
	    } catch (Exception e) {
	      throw new RuntimeException(e);
	    }
	  }
	}
      }

      statusPopulated = true;
    }

    return super.getStatus();
  }

  @Override
  public Long getDiskUsage() {
    if (!diskUsagePopulated) {
      // TODO: Implement it once the repository provides the information.
      diskUsagePopulated = true;
    }

    return super.getDiskUsage();
  }

  @Override
  public String getPluginName() {
    if (!pluginNamePopulated) {
      if (auId != null) {
	setPluginName(PluginManager.pluginNameFromAuId(auId));
      }

      pluginNamePopulated = true;
    }

    return super.getPluginName();
  }

  @Override
  public Map<String, String> getParams() {
    if (!paramsPopulated) {
      if (auId != null) {
	if (getArchivalUnit() != null) {
	  Configuration config = au.getConfiguration();

	  if (config != null && !config.isEmpty()) {
	    setParams(makeParams(config));
	  }
	} else {
	  if (getAuIdProps() != null && !auIdProps.isEmpty()) {
	    setParams(makeParams(auIdProps));
	  }

	  if (!isOrphaned()) {
	    try {
	      Configuration config = getPluginManager()
		  .getStoredAuConfigurationAsConfiguration(auId);

	      if (config != null && !config.isEmpty()) {
		setParams(makeParams(config));
	      }
	    } catch (Exception e) {
	      throw new RuntimeException(e);
	    }
	  }
	}
      }

      paramsPopulated = true;
    }

    return super.getParams();
  }

  private Properties getAuIdProps() {
    if (!auIdPropsPopulated) {
      String auKey = null;

      try {
	auKey = PluginManager.auKeyFromAuId(auId);
	auIdProps = PropUtil.canonicalEncodedStringToProps(auKey);
      } catch (Exception e) {
	log.warning("Couldn't decode AUKey: " + auKey, e);
      }

      auIdPropsPopulated = true;
    }

    return auIdProps;
  }

  /**
   * Provides the daemon, initializing it if necessary.
   * 
   * @return a LockssDaemon with the daemon.
   */
  private LockssDaemon getTheDaemon() {
    if (theDaemon == null) {
      theDaemon = LockssDaemon.getLockssDaemon();
    }

    return theDaemon;
  }

  /**
   * Provides the plugin manager, initializing it if necessary.
   * 
   * @return a PluginManager with the plugin manager.
   */
  private PluginManager getPluginManager() {
    if (pluginMgr == null) {
      pluginMgr = getTheDaemon().getPluginManager();
    }

    return pluginMgr;
  }

  /**
   * Provides the Archival Unit, initializing it if necessary.
   * 
   * @return an ArchivalUnit with the Archival Unit.
   */
  private ArchivalUnit getArchivalUnit() {
    if (!auPopulated) {
      au = getPluginManager().getAuFromId(auId);

      auPopulated = true;
    }

    return au;
  }

  private boolean isOrphaned() {
    if (getAuIdProps() == null) {
	return true;
    }

    String pluginKey =
	PluginManager.pluginKeyFromId(PluginManager.pluginIdFromAuId(auId));
    Plugin plugin = getPluginManager().getPlugin(pluginKey);

    if (plugin == null) {
      return true;
    }

    Configuration defConfig = ConfigManager.fromProperties(auIdProps);
    return !AuUtil.isConfigCompatibleWithPlugin(defConfig, plugin);
  }

  private Map<String, String> makeParams(Configuration config) {
    Map<String, String> result = new HashMap<String, String>();

    for (String key : config.keySet()) {
      result.put(key, config.get(key));
    }

    return result;
  }

  private Map<String, String> makeParams(Properties props) {
    Map<String, String> result = new HashMap<String, String>();

    for (String key : props.stringPropertyNames()) {
      result.put(key, props.getProperty(key));
    }

    return result;
  }
}
