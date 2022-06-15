package com.ontimize.jee.report.server.services;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.ontimize.jee.common.db.SQLStatementBuilder;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.tools.ReflectionTools;
import com.ontimize.jee.report.common.dto.ColumnDto;
import com.ontimize.jee.report.common.dto.ColumnStyleParamsDto;
import com.ontimize.jee.report.common.dto.FunctionParamsDto;
import com.ontimize.jee.report.common.dto.OrderByDto;
import com.ontimize.jee.report.common.dto.ReportParamsDto;
import com.ontimize.jee.report.common.dto.ServiceRendererDto;
import com.ontimize.jee.report.common.dto.StyleParamsDto;
import com.ontimize.jee.report.common.exception.DynamicReportException;
import com.ontimize.jee.report.common.services.IDynamicJasperService;
import com.ontimize.jee.report.common.util.EntityResultDataSource;
import com.ontimize.jee.report.common.util.TypeMappingsUtils;
import com.ontimize.jee.report.server.naming.DynamicJasperNaming;
import com.ontimize.jee.report.server.services.util.DynamicJasperHelper;
import com.ontimize.jee.report.server.services.util.DynamicReportBuilderHelper;

import ar.com.fdvs.dj.domain.DJCalculation;
import ar.com.fdvs.dj.domain.DJValueFormatter;
import ar.com.fdvs.dj.domain.DynamicReport;
import ar.com.fdvs.dj.domain.Style;
import ar.com.fdvs.dj.domain.builders.ColumnBuilder;
import ar.com.fdvs.dj.domain.builders.DynamicReportBuilder;
import ar.com.fdvs.dj.domain.builders.GroupBuilder;
import ar.com.fdvs.dj.domain.constants.GroupLayout;
import ar.com.fdvs.dj.domain.constants.HorizontalAlign;
import ar.com.fdvs.dj.domain.constants.VerticalAlign;
import ar.com.fdvs.dj.domain.entities.DJGroup;
import ar.com.fdvs.dj.domain.entities.DJGroupVariable;
import ar.com.fdvs.dj.domain.entities.columns.AbstractColumn;
import ar.com.fdvs.dj.domain.entities.columns.PropertyColumn;
import net.sf.jasperreports.engine.JRDataSource;

@Service("DynamicJasperService")
@Lazy(value = true)
public class DynamicJasperService extends ReportBase implements IDynamicJasperService {

    private ResourceBundle bundle;

    @Autowired
    private ApplicationContext applicationContext;
    private DynamicJasperHelper dynamicJasperHelper;

    private DynamicReportBuilderHelper dynamicReportBuilderHelper;

    @Override
    public InputStream createReport(final ReportParamsDto param) throws DynamicReportException {

        if (StringUtils.isBlank(param.getService())) {
            throw new DynamicReportException("Report cannot be created, 'service' parameter not found!");
        }
        if (StringUtils.isBlank(param.getEntity())) {
            throw new DynamicReportException("Report cannot be created, 'entity' parameter not found!");
        }
        if (param.getColumns() == null || param.getColumns().isEmpty()) {
            throw new DynamicReportException("Report cannot be created, 'columns' parameter not found!");
        }

        return this.generateReport(param.getColumns(), param.getTitle(), param.getGroups(), param.getEntity(),
                param.getService(), param.getVertical(), param.getFunctions(), param.getStyle(), param.getSubtitle(),
                param.getOrderBy(), param.getLanguage(), param.getServiceRenderer());

    }

    @Override
    public List<String> getFunctionsName(final FunctionParamsDto params) throws DynamicReportException {
        try {

            String entity = params.getEntity();
            String service = params.getService();
            List<String> columns = params.getColumns();

            List<String> functions = new ArrayList<>();
            Map<String, Object> map = new HashMap<>();
            Object bean = this.applicationContext.getBean(service.concat("Service"));
            EntityResult e = (EntityResult) ReflectionTools.invoke(bean, entity.toLowerCase().concat("Query"), map,
                    columns);
            for (String column : columns) {
                int type = e.getColumnSQLType(column);
                String className = TypeMappingsUtils.getClassName(type);
                if (TypeMappingsUtils.INTEGER_PATH.equals(className)
                        || TypeMappingsUtils.DOUBLE_PATH.equals(className)) {
                    functions.add(column);
                }
            }
            return functions;
        } catch (Exception ex) {
            throw new DynamicReportException("Impossible to retrieve function names", ex);
        }
    }

    public DynamicReport buildReport(List<ColumnDto> columns, String title, List<String> groups, String entity,
                                     String service, Boolean vertical, List<String> functions, StyleParamsDto styles, String subtitle,
                                     String language, List<ServiceRendererDto> serviceRendererList) throws DynamicReportException {

        int numberGroups = 0;
        boolean functionColumn = false;
        DynamicReportBuilder drb = new DynamicReportBuilder();
        DynamicReportBuilderHelper builderHelper = this.getDynamicReportBuilderHelper();
        ResourceBundle bundle = getBundle(language, drb);
        // title
        builderHelper.configureTitle(drb, title);
        // subtitle
        builderHelper.configureSubTitle(drb, subtitle);
        // generic styles
        builderHelper.configureGenericStyles(drb, vertical, styles, columns.size(), bundle);

        Map<String, String> columnClassnames = this.getDynamicJasperHelper().getColumnClassnames(service, entity,
                columns, serviceRendererList);

        boolean firstColumn = true;
        for (ColumnDto columnDto : columns) {

            AbstractColumn column;
            Style columnDataStyle = new Style();
            columnDataStyle = builderHelper.getStyleGrid(styles, columnDataStyle);

            Style headerStyle = builderHelper.getHeaderStyle();
            ColumnStyleParamsDto columnStyleParamsDto = columnDto.getColumnStyle();

            String id = columnDto.getId();
            String name = columnDto.getName();
            String className = columnClassnames.get(id);

            if (columnStyleParamsDto != null) {
                switch (columnStyleParamsDto.getAlignment()) {
                    case "center":
                        columnDataStyle.setHorizontalAlign(HorizontalAlign.CENTER);
                        headerStyle.setHorizontalAlign(HorizontalAlign.CENTER);
                        break;
                    case "left":
                        columnDataStyle.setHorizontalAlign(HorizontalAlign.LEFT);
                        headerStyle.setHorizontalAlign(HorizontalAlign.LEFT);
                        break;
                    case "right":
                        columnDataStyle.setHorizontalAlign(HorizontalAlign.RIGHT);
                        headerStyle.setHorizontalAlign(HorizontalAlign.RIGHT);
                        break;
                }
            }

            columnDataStyle.setVerticalAlign(VerticalAlign.MIDDLE);
            headerStyle.setVerticalAlign(VerticalAlign.MIDDLE);

            column = ColumnBuilder.getNew().setColumnProperty(id, className).setTitle(name).setHeaderStyle(headerStyle)
                    .build();

            column.setName(id);
            if (columnStyleParamsDto != null && columnStyleParamsDto.getWidth() >= 0) {
                column.setWidth(columnStyleParamsDto.getWidth());
            }
            drb.setPrintColumnNames(styles.isColumnName());
            column.setFixedWidth(false);

            column.setStyle(columnDataStyle);
            // The column of numbers is hidden if it is not wanted but it must always be
            // created to make the total if it is needed
            if (!styles.isRowNumber()) {
                DJGroup g1 = new GroupBuilder().setCriteriaColumn((PropertyColumn) drb.getColumn(0))
                        .setGroupLayout(GroupLayout.EMPTY).build();

                drb.getColumn(0).setWidth(50);
                drb.addGroup(g1);
            }
            if (functions != null && !functions.isEmpty()) {
                Style footerStyle = builderHelper.getFooterStyle();
                if (firstColumn && (functions.contains(bundle.getString("total_text"))
                        || functions.contains(bundle.getString("total")))) {
                    DJValueFormatter valueFormatter = builderHelper.getFunctionValueFormatter(DynamicJasperNaming.TOTAL,
                            bundle);
                    // The column of row numbers is used to perform a correct calculation of the
                    // total number of rows
                    drb.addGlobalFooterVariable(drb.getColumn(0), DJCalculation.COUNT, footerStyle, valueFormatter)
                            .setGrandTotalLegend("");

                } else if (firstColumn) {
                    drb.addGlobalFooterVariable(
                            new DJGroupVariable(drb.getColumn(0), DJCalculation.SYSTEM, footerStyle));
                }

                // Configure aggregate functions...
                for (String function : functions) {
                    if (function.startsWith(id)) {
                        builderHelper.configureReportFunction(drb, column, function, bundle);
                        functionColumn = true;
                    }
                }
                if (!functionColumn) {
                    drb.addGlobalFooterVariable(new DJGroupVariable(column, DJCalculation.SYSTEM, footerStyle));

                }

            }
            drb.addColumn(column);

            // Configure report grouping...
            if (groups != null && groups.contains(column.getName())) {
                DJGroup reportGroup = builderHelper.createReportGroup(column, styles, numberGroups);
                drb.addGroup(reportGroup);
                numberGroups += 1;
            }

            firstColumn = false;
        }

        DynamicReport dr = drb.build();
        return dr;
    }

    @Override
    public JRDataSource getDataSource(List<ColumnDto> columns, List<String> groups, List<OrderByDto> orderBy,
                                      String entity, String service, final List<ServiceRendererDto> serviceRendererList)
            throws SecurityException {

        Map<String, Object> map = new HashMap<>();
        List<String> columns1 = this.getDynamicJasperHelper().getColumnsFromDto(columns);
        Integer pageSize = Integer.MAX_VALUE;
        Integer offset = 0;
        boolean order = false;
        List<SQLStatementBuilder.SQLOrder> sqlOrders = new ArrayList<>();
        // If there are group columns, it is necessary to add to order by to allow
        // jasper engine perform grouping well...
        if (groups != null && !groups.isEmpty()) {
            for (String col : groups) {
                if (orderBy != null && !orderBy.isEmpty()) {

                    OrderByDto orderByDto = orderBy.stream().filter(item -> item.getColumnId().equals(col)).findFirst()
                            .orElse(null);
                    if (orderByDto != null) {
                        SQLStatementBuilder.SQLOrder sqlO = new SQLStatementBuilder.SQLOrder(col,
                                orderByDto.isAscendent());
                        sqlOrders.add(sqlO);
                        continue;
                    }
                }
                SQLStatementBuilder.SQLOrder sqlO = new SQLStatementBuilder.SQLOrder(col);
                sqlOrders.add(sqlO);

            }
        }
        // Second, add the rest of the columns to orderBy
        if (orderBy != null && !orderBy.isEmpty()) {
            orderBy.stream().filter(ord -> !groups.contains(ord.getColumnId())).forEach(ord -> {
                SQLStatementBuilder.SQLOrder sqlOrder = new SQLStatementBuilder.SQLOrder(ord.getColumnId(),
                        ord.isAscendent());
                sqlOrders.add(sqlOrder);
            });
        }

        Object bean = this.applicationContext.getBean(service.concat("Service"));
        EntityResult erReportData = (EntityResult) ReflectionTools.invoke(bean,
                entity.toLowerCase().concat("PaginationQuery"), map, columns1, pageSize, offset, sqlOrders);

        EntityResultDataSource entityResultDataSource = new EntityResultDataSource(erReportData);
        if (serviceRendererList != null && !serviceRendererList.isEmpty()) {
            dynamicJasperHelper.evaluateServiceRenderer(entityResultDataSource, serviceRendererList);
        }

        return entityResultDataSource;

    }

    protected ResourceBundle getBundle(final String language, final DynamicReportBuilder drb) {
        if (this.bundle == null) {
            Locale locale = null;
            String lang0 = "en";
            if (!StringUtils.isEmpty(language)) {
                lang0 = language;
            }
            switch (lang0) {
                case "es":
                    locale = new Locale("es", "ES");
                    break;
                case "gl":
                    locale = new Locale("gl", "ES");
                    break;
                default:
                    locale = new Locale("en", "US");
                    break;
            }
            drb.setReportLocale(locale);
            bundle = ResourceBundle.getBundle("bundle/bundle", locale);
        }
        return this.bundle;
    }

    protected DynamicJasperHelper getDynamicJasperHelper() {
        if (this.dynamicJasperHelper == null) {
            this.dynamicJasperHelper = new DynamicJasperHelper(this.applicationContext);
        }
        return this.dynamicJasperHelper;
    }

    protected DynamicReportBuilderHelper getDynamicReportBuilderHelper() {
        if (this.dynamicReportBuilderHelper == null) {
            this.dynamicReportBuilderHelper = new DynamicReportBuilderHelper();
            this.dynamicReportBuilderHelper.setDynamicJasperHelper(getDynamicJasperHelper());
        }
        return this.dynamicReportBuilderHelper;
    }
}
