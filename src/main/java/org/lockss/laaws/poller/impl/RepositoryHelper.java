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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.lockss.app.LockssDaemon;
import org.lockss.laaws.rs.core.LockssRepository;
import org.lockss.repository.RepoSpec;
import org.lockss.util.Logger;
import org.lockss.ws.entities.RepositoryWsResult;

/**
 * Helper of the DaemonStatus web service implementation of repository queries.
 */
public class RepositoryHelper {
  /**
   * The fully-qualified name of the class of the objects used as source in a
   * query.
   */
  static String SOURCE_FQCN = RepositoryWsSource.class.getCanonicalName();

  /**
   * The fully-qualified name of the class of the objects returned by the query.
   */
  static String RESULT_FQCN = RepositoryWsResult.class.getCanonicalName();

  //
  // Property names used in repository queries.
  //
  static String REPOSITORY_SPACE_ID = "repositorySpaceId";
  static String DIRECTORY_NAME = "directoryName";
  static String AU_NAME = "auName";
  static String INTERNAL = "internal";
  static String STATUS = "status";
  static String DISK_USAGE = "diskUsage";
  static String PLUGIN_NAME = "pluginName";
  static String PARAMS = "params";

  /**
   * All the property names used in repository queries.
   */
  @SuppressWarnings("serial")
  static final Set<String> PROPERTY_NAMES = new HashSet<String>() {
    {
      add(REPOSITORY_SPACE_ID);
      add(DIRECTORY_NAME);
      add(AU_NAME);
      add(INTERNAL);
      add(STATUS);
      add(DISK_USAGE);
      add(PLUGIN_NAME);
      add(PARAMS);
    }
  };

  private static Logger log = Logger.getLogger();

  /**
   * Provides the universe of repository-related objects used as the source for
   * a query.
   * 
   * @return a List<RepositoryWsProxy> with the universe.
   */
  List<RepositoryWsSource> createUniverse() {
    final String DEBUG_HEADER = "createUniverse(): ";

    // Initialize the universe.
    List<RepositoryWsSource> universe = new ArrayList<RepositoryWsSource>();

    // Get the repository specification.
    RepoSpec repoSpec =
	LockssDaemon.getLockssDaemon().getRepositoryManager().getV2Repository();

    String repositorySpaceId = repoSpec.getSpec();
    if (log.isDebug3())
      log.debug3(DEBUG_HEADER + "repositorySpaceId = " + repositorySpaceId);

    // Get the repository.
    LockssRepository repo = repoSpec.getRepository();

    try {
      // Loop through all the collections in the repository.
      for (String collectionId : repo.getCollectionIds()) {
	if (log.isDebug3())
	  log.debug3(DEBUG_HEADER + "collectionId = " + collectionId);

	try {
	  // Loop through all the AU identifiers in the collection.
	  for (String auId : repo.getAuIds(collectionId)) {
	    if (log.isDebug3()) log.debug3(DEBUG_HEADER + "auId = " + auId);
	    // Add this AU to the universe.
	    universe.add(new RepositoryWsSource(repositorySpaceId, collectionId,
		auId));
	  }
	} catch (IOException ioe) {
	  log.error("Exception caught for collectionId '" + collectionId
	      + "': Ignoring collection", ioe);
	}
      }
    } catch (IOException ioe) {
      log.error("Exception caught getting collections", ioe);
    }

    if (log.isDebug2())
      log.debug2(DEBUG_HEADER + "universe.size() = " + universe.size());
    return universe;
  }

  /**
   * Provides a printable copy of a collection of repository-related query
   * results.
   * 
   * @param results
   *          A {@code Collection<RepositoryWsResult>} with the query results.
   * @return a String with the requested printable copy.
   */
  String nonDefaultToString(Collection<RepositoryWsResult> results) {
    StringBuilder builder = new StringBuilder("[");
    boolean isFirst = true;

    // Loop through through all the results in the collection.
    for (RepositoryWsResult result : results) {
      // Handle the first result differently.
      if (!isFirst) {
	builder.append(", ");
      } else {
	isFirst = false;
      }

      builder.append(nonDefaultToString(result));
    }

    // Add this result to the printable copy.
    return builder.append("]").toString();
  }

  /**
   * Provides a printable copy of a repository-related query result.
   * 
   * @param result
   *          A RepositoryWsResult with the query result.
   * @return a String with the requested printable copy.
   */
  private String nonDefaultToString(RepositoryWsResult result) {
    StringBuilder builder = new StringBuilder("RepositoryWsResult [");
    boolean isFirst = true;

    if (result.getRepositorySpaceId() != null) {
      builder.append("repositorySpaceId=")
      .append(result.getRepositorySpaceId());
      isFirst = false;
    }

    if (result.getDirectoryName() != null) {
      if (!isFirst) {
	builder.append(", ");
      } else {
	isFirst = false;
      }

      builder.append("directoryName=").append(result.getDirectoryName());
    }

    if (result.getAuName() != null) {
      if (!isFirst) {
	builder.append(", ");
      } else {
	isFirst = false;
      }

      builder.append("auName=").append(result.getAuName());
    }

    if (result.getInternal() != null) {
      if (!isFirst) {
	builder.append(", ");
      } else {
	isFirst = false;
      }

      builder.append("internal=").append(result.getInternal());
    }

    if (result.getStatus() != null) {
      if (!isFirst) {
	builder.append(", ");
      } else {
	isFirst = false;
      }

      builder.append("status=").append(result.getStatus());
    }

    if (result.getDiskUsage() != null) {
      if (!isFirst) {
	builder.append(", ");
      } else {
	isFirst = false;
      }

      builder.append("diskUsage=").append(result.getDiskUsage());
    }

    if (result.getPluginName() != null) {
      if (!isFirst) {
	builder.append(", ");
      } else {
	isFirst = false;
      }

      builder.append("pluginName=").append(result.getPluginName());
    }

    if (result.getParams() != null) {
      if (!isFirst) {
	builder.append(", ");
      } else {
	isFirst = false;
      }

      builder.append("params=").append(result.getParams());
    }

    return builder.append("]").toString();
  }
}
