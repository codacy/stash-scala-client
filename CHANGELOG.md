# Changelog
All notable changes to this project are documented in this file.

## 5.1.1

### Changed
* Updated test documentation surrounding environment variables

## 5.1.0

### Added
* New `ProjectServices` to handle interactions with `/rest/api/1.0/projects` endpoints
* New `PageRequest` to some endpoints to allow control over the pagination process 
* New `RepositoryServices` end points to fetch additional information about repositories

### Changed
* `RepositoryServices getRepositories` takes an additional `Option[PageRequest]` method parameter. 


test
