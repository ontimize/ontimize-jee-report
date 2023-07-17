package com.ontimize.jee.report.server.services;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.ontimize.jee.common.db.SQLStatementBuilder;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.tools.ReflectionTools;
import com.ontimize.jee.report.common.dto.ColumnDto;
import com.ontimize.jee.report.common.dto.ColumnStyleParamsDto;
import com.ontimize.jee.report.common.dto.FunctionParamsDto;
import com.ontimize.jee.report.common.dto.FunctionTypeDto;
import com.ontimize.jee.report.common.dto.FunctionTypeDto.Type;
import com.ontimize.jee.report.common.dto.OrderByDto;
import com.ontimize.jee.report.common.dto.ReportParamsDto;
import com.ontimize.jee.report.common.dto.StyleParamsDto;
import com.ontimize.jee.report.common.dto.renderer.RendererDto;
import com.ontimize.jee.report.common.exception.DynamicReportException;
import com.ontimize.jee.report.common.services.IDynamicJasperService;
import com.ontimize.jee.report.common.util.EntityResultDataSource;
import com.ontimize.jee.report.common.util.TypeMappingsUtils;
import com.ontimize.jee.report.server.ApplicationContextUtils;
import com.ontimize.jee.report.server.naming.DynamicJasperNaming;
import com.ontimize.jee.report.server.services.util.ColumnMetadata;
import com.ontimize.jee.report.server.services.util.DynamicJasperHelper;
import com.ontimize.jee.report.server.services.util.DynamicReportBuilderHelper;
import com.ontimize.jee.server.rest.FilterParameter;

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
public class DynamicJasperService extends ReportBase implements IDynamicJasperService, InitializingBean {

    private ResourceBundle bundle;

    @Autowired
    private ApplicationContext applicationContext;

    private ApplicationContextUtils applicationContextUtils;

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
                param.getService(), param.getPath(), param.getVertical(), param.getFunctions(), param.getStyle(),
                param.getSubtitle(), param.getOrderBy(), param.getLanguage(), param.getFilters(), param.getAdvQuery());

    }

    @Override
    public List<FunctionTypeDto> getFunctionsName(final FunctionParamsDto params) throws DynamicReportException {
        try {

            String entity = params.getEntity();
            String service = params.getService();
            String path = params.getPath();
            List<String> columns = params.getColumns();

            List<FunctionTypeDto> functions = new ArrayList<>();
            Map<String, Object> map = new HashMap<>();
            Object bean = this.getApplicationContextUtils().getServiceBean(service, path);
            EntityResult e = (EntityResult) ReflectionTools.invoke(bean, entity.concat("Query"), map, columns);
            for (String column : columns) {
                int type = e.getColumnSQLType(column);
                String className = TypeMappingsUtils.getClassName(type);
                if (TypeMappingsUtils.INTEGER_PATH.equals(className)
                        || TypeMappingsUtils.DOUBLE_PATH.equals(className)) {
                    functions.add(new FunctionTypeDto(column, Type.SUM));
                }
            }
            functions.add(new FunctionTypeDto("TOTAL", Type.TOTAL));
            return functions;
        } catch (Exception ex) {
            throw new DynamicReportException("Impossible to retrieve function names", ex);
        }
    }

    public DynamicReport buildReport(List<ColumnDto> columns, String title, List<String> groups, String entity,
            String service, String path, Boolean vertical, List<FunctionTypeDto> functions, StyleParamsDto styles,
            String subtitle, String language) throws DynamicReportException {

        int numberGroups = 0;
        boolean functionColumn = false;
        ResourceBundle bundle = getBundle(language);
        DynamicReportBuilder drb = new DynamicReportBuilder();
        drb.setReportLocale(bundle.getLocale());
        DynamicReportBuilderHelper builderHelper = this.getDynamicReportBuilderHelper();

        // title
        builderHelper.configureTitle(drb, title);
        // subtitle
        builderHelper.configureSubTitle(drb, subtitle);
        // generic styles
        builderHelper.configureGenericStyles(drb, vertical, styles, columns.size(), bundle);

        Map<String, ColumnMetadata> columnMetadataMap = this.getDynamicJasperHelper().getColumnMetadata(service, path,
                entity, columns);

        boolean firstColumn = true;
        boolean totalFunction = false;
        for (ColumnDto columnDto : columns) {

            AbstractColumn column;
            Style columnDataStyle = new Style();
            columnDataStyle = builderHelper.getStyleGrid(styles, columnDataStyle);

            Style headerStyle = builderHelper.getHeaderStyle();
            ColumnStyleParamsDto columnStyleParamsDto = columnDto.getColumnStyle();

            String id = columnDto.getId();
            String name = columnDto.getName();
            String className = columnMetadataMap.get(id).getClassName();

            if (columnStyleParamsDto != null && columnStyleParamsDto.getAlignment() != null) {
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
            String columnPattern = this.getDynamicJasperHelper().getColumnPattern(columnMetadataMap.get(id),
                    columnStyleParamsDto, bundle.getLocale());
            if (columnPattern != null) {
                column.setPattern(columnPattern);
            }
            if (columnStyleParamsDto != null && columnStyleParamsDto.getWidth() != null
                    && columnStyleParamsDto.getWidth() > 0) {
                column.setWidth(columnStyleParamsDto.getWidth());
            }
            drb.setPrintColumnNames(styles != null && styles.isColumnName());
            column.setFixedWidth(false);

            column.setStyle(columnDataStyle);
            // The column of numbers is hidden if it is not wanted but it must always be
            // created to make the total if it is needed
            if (styles != null && !styles.isRowNumber()) {
                DJGroup g1 = new GroupBuilder().setCriteriaColumn((PropertyColumn) drb.getColumn(0))
                        .setGroupLayout(GroupLayout.EMPTY).build();

                drb.getColumn(0).setWidth(80);
                drb.addGroup(g1);
            }
            if (functions != null && !functions.isEmpty()) {
                Style footerStyle = builderHelper.getFooterStyle();

                // Configure aggregate functions...
                for (FunctionTypeDto function : functions) {
                    if (function.getColumnName().equals(id)) {
                        RendererDto renderer = null;
                        if (columnStyleParamsDto != null) {
                            renderer = columnStyleParamsDto.getRenderer();
                        }
                        builderHelper.configureReportFunction(drb, column, renderer, function, bundle);
                        functionColumn = true;
                    } else if (function.getType().name().equals(bundle.getString("total"))) {
                        totalFunction = true;
                    }
                }
                if (firstColumn) {
                    if (totalFunction) {
                        DJValueFormatter valueFormatter = builderHelper
                                .getFunctionValueFormatter(DynamicJasperNaming.TOTAL, bundle, null);
                        // The column of row numbers is used to perform a correct calculation of the
                        // total number of rows
                        Style totalStyle = dynamicReportBuilderHelper.getTotalStyle();

                        if (functionColumn && !styles.isRowNumber()) {
                            drb.addGlobalFooterVariable(drb.getColumn(0), DJCalculation.COUNT, totalStyle,
                                    valueFormatter).setGrandTotalLegend("");
                        } else {
                            drb.addGlobalFooterVariable(drb.getColumn(0), DJCalculation.COUNT, footerStyle,
                                    valueFormatter).setGrandTotalLegend("");
                        }
                    } else {
                        drb.addGlobalFooterVariable(
                                new DJGroupVariable(drb.getColumn(0), DJCalculation.SYSTEM, footerStyle))
                                .setGrandTotalLegend("");

                    }
                }
                if (!functionColumn) {
                    drb.addGlobalFooterVariable(new DJGroupVariable(column, DJCalculation.SYSTEM, footerStyle))
                            .setGrandTotalLegend("");

                }

            }
            drb.addColumn(column);
            firstColumn = false;
            functionColumn = false;
        }
        // Configure report grouping...
        if (groups != null) {
            for (String columnGroup : groups) {
                for (int i = 0; i < drb.getColumns().size(); i++) {
                    if (drb.getColumn(i).getName().equals(columnGroup)) {
                        DJGroup reportGroup = builderHelper.createReportGroup(drb.getColumn(i), styles, numberGroups);
                        drb.addGroup(reportGroup);
                        numberGroups += 1;
                    }
                }

            }
        }
        DynamicReport dr = drb.build();
        return dr;
    }

    @Override
    public JRDataSource getDataSource(List<ColumnDto> columns, List<String> groups, List<OrderByDto> orderBy,
            String entity, String service, String path, FilterParameter filters, Boolean advQuery)
            throws DynamicReportException {
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

        Object bean = this.getApplicationContextUtils().getServiceBean(service, path);
        EntityResult erReportData;
        if (advQuery) {
            erReportData = (EntityResult) ReflectionTools.invoke(bean, entity.concat("PaginationQuery"),
                    filters.getFilter(), columns1, pageSize, offset, sqlOrders);
        } else {
            erReportData = (EntityResult) ReflectionTools.invoke(bean, entity.concat("Query"),
                    filters.getFilter(), columns1);
        }
        EntityResultDataSource entityResultDataSource = new EntityResultDataSource(erReportData);
        dynamicJasperHelper.evaluateServiceRenderer(entityResultDataSource, columns);

        return entityResultDataSource;

    }

    protected ResourceBundle getBundle(final String language) {
        if (this.bundle == null || (!this.bundle.getLocale().getLanguage().equals(language))) {
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
            bundle = ResourceBundle.getBundle("bundle/bundle", locale);
        }
        return this.bundle;
    }

    protected ApplicationContextUtils getApplicationContextUtils() {
        return applicationContextUtils;
    }

    protected DynamicJasperHelper getDynamicJasperHelper() {
        return this.dynamicJasperHelper;
    }

    protected DynamicReportBuilderHelper getDynamicReportBuilderHelper() {
        return this.dynamicReportBuilderHelper;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.applicationContextUtils == null) {
            this.applicationContextUtils = new ApplicationContextUtils();
        }
        if (this.dynamicJasperHelper == null) {
            this.dynamicJasperHelper = new DynamicJasperHelper();
        }
        if (this.dynamicReportBuilderHelper == null) {
            this.dynamicReportBuilderHelper = new DynamicReportBuilderHelper();
        }

        ((ApplicationContextAware) this.applicationContextUtils).setApplicationContext(this.applicationContext);
        ((ApplicationContextAware) this.dynamicJasperHelper).setApplicationContext(this.applicationContext);
        this.dynamicJasperHelper.setApplicationContextUtils(this.applicationContextUtils);
    }
}
