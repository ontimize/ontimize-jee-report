<!-- ## [Unreleased] -->
<!-- ### Added ✔️-->
<!-- ### Changed 🛠️-->
<!-- ### Deprecated 🛑-->
<!-- ### Removed 🗑️-->
<!-- ### Fixed 🐛-->
<!-- ### Security 🛡️-->

## [Unreleased]
### Fixed 🐛
* **Report store**: Fixed problems when using parameters of Jasper Report. It was added new `ReportStoreParamsDto` object into `fillReport` API method for allowing customization of parameters as types, formattings, etc.
### Breaking changes
* The `/fillReport/{id}` method of `ReportStoreRestController` has changed from:
```
public EntityResult fillReport(@PathVariable("id") String id,
                                   @RequestBody(required = true) Map<String, Object> bodyParams)
```
to:
```
public EntityResult fillReport(@PathVariable("id") String id,
                                   @RequestBody(required = true) ReportStoreParamsDto bodyParams)
```

* The `buildReport` method of `IDynamicJasperService` interface has changed from:
```
public DynamicReport buildReport(List<ColumnDto> columns, String title, List<String> groups, String entity,
                                 String service, String path, Boolean vertical, List<FunctionTypeDto> functions, StyleParamsDto styles, String subtitle,
                                 String language)
        throws DynamicReportException;
```
to:
```
public DynamicReport buildReport(ReportParamsDto reportParamsDto) throws DynamicReportException;
```

* The `generateReport` method of `ReportBase` abstract class has changed from:
```
public InputStream generateReport(List<ColumnDto> columns, String title, List<String> groups, String entity,
            String service, String path, Boolean vertical, List<FunctionTypeDto> functions, StyleParamsDto style,
            String subtitle, List<OrderByDto> orderBy, String language, FilterParameter filters, Boolean advQuery)
            throws DynamicReportException
```
to:
```
public InputStream generateReport(final ReportParamsDto reportParamsDto)
        throws DynamicReportException
```

## [3.4.0] - 2024-02-23
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

[unreleased]: https://github.com/ontimize/ontimize-jee-report/compare/3.4.0...HEAD
[3.4.0]: https://github.com/ontimize/ontimize-jee-report/compare/3.3.0...3.4.0
[3.3.0]: https://github.com/ontimize/ontimize-jee-report/compare/3.2.0...3.3.0
[3.2.0]: https://github.com/ontimize/ontimize-jee-report/tree/3.2.0
