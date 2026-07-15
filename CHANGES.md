# `laaws-poller` Release Notes

## 2.8.0 (LOCKSS 2.0.91-beta2)

### Features

* Refactor pagination logic with unified `ContinuationToken` and use UUID-based
  iterator keys to avoid hash code collisions
* Refactor enum types (`TallyTypeEnum`, `RepairTypeEnum`, `VoterUrlsEnum`,
  `ExportFileTypeEnum`, `ExportFilenameTranslationEnum`, `PollVariantEnum`) out
  of endpoint definitions and into standalone component schemas
* Update method signatures to use `TallyTypeEnum` and `RepairTypeEnum`
* Improve error logging with detailed context in `PollsApiServiceImpl`
* Rename `url` to `lastUrl` in continuation token classes for clarity and
  consistency across API
* Add pagination and continuation token models
* Create Python client
* Patch OpenAPI spec for swagger-codegen Spring and Python generators
* 2.0-beta2 port conventions


## Changes Since 2.3.0

* Remove  Travis CI
* Move to OpenAPI 3
* Move to Java 17
* Suppress extraneous messages in tests
* Spring 6.x and Spring Boot 3.x related changes
* Removed getRequest() override

## Changes Since 2.0.2.1

*   Switched to a 3-part version numbering scheme.

## 2.0.2.1

### Security

*   Out of an abundance of caution, re-released 2.0.2.0 with Jackson-Databind 2.9.10.8 (CVE-2021-20190).

## 2.0.2.0

### Features

*   ...

### Fixes

*   ...

## 2.0.1.0

### Features

*   REST services authenticate, clients provide credentials.
*   Improved startup coordination and ready waiting of all services and databases.
*   Improved coordination of initial plugin registry crawls.
