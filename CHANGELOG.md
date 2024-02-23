<!-- ## [Unreleased] -->
<!-- ### Added ✔️-->
<!-- ### Changed 🛠️-->
<!-- ### Deprecated 🛑-->
<!-- ### Removed 🗑️-->
<!-- ### Fixed 🐛-->
<!-- ### Security 🛡️-->

## [Unreleased]
### Added ✔️
* **JaCoCo**: Add JaCoCo coverage on Sonar
### Changed 🛠️
* **Sonar**: Upgrade Java version on Sonar action
### Fixed 🐛
* **DatabaseReportStoreEngine**: Fix report store on databases with lowercase columns using name convention of the application.
* **Sonar**: Fix some sonar issues.
## [3.3.0] - 2023-07-31
### Added ✔️
* **Reports on-demand**: Add filters to query reports on-demand
### Changed 🛠️
* **Changelog**: The structure of the CHANGELOG.md file has been modified so that it follows the structure shown at [keepachangelog](https://keepachangelog.com/).
* **POM**: Add <repositories> tag to allow SNAPSHOT repositories when generating manual version
## [3.2.0] - 2023-01-03
### Added ✔️
* **Report on-demand**: On-demand reports can now be created with the help of Jasper Reports via HTTP requests. The parameters for creating these reports can be stored in preferences for later use.

[unreleased]: https://github.com/ontimize/ontimize-jee-report/compare/3.3.0...HEAD
[3.3.0]: https://github.com/ontimize/ontimize-jee-report/compare/3.2.0...3.3.0
[3.2.0]: https://github.com/ontimize/ontimize-jee-report/tree/3.2.0
