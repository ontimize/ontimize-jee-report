package com.ontimize.jee.report.server.services.util;

import com.ontimize.jee.common.db.SQLStatementBuilder;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.tools.ReflectionTools;
import com.ontimize.jee.report.common.dto.ColumnDto;
import com.ontimize.jee.report.common.dto.ColumnStyleParamsDto;
import com.ontimize.jee.report.common.dto.renderer.BooleanRendererDto;
import com.ontimize.jee.report.common.dto.renderer.Renderer;
import com.ontimize.jee.report.common.dto.renderer.RendererDto;
import com.ontimize.jee.report.common.dto.renderer.ServiceRendererDto;
import com.ontimize.jee.report.common.exception.DynamicReportException;
import com.ontimize.jee.report.common.util.EntityResultDataSource;
import com.ontimize.jee.report.common.util.TypeMappingsUtils;
import com.ontimize.jee.report.server.ApplicationContextUtils;
import com.ontimize.jee.server.rest.FilterParameter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DynamicJasperHelper implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    private ApplicationContextUtils applicationContextUtils;

    public DynamicJasperHelper() {
        // no-op
    }

    public String getColumnPattern(final ColumnMetadata columnMetadata, final ColumnStyleParamsDto columnStyleParamsDto,
                                   final Locale locale) {
        String pattern = null;
        if (columnMetadata != null) {
            Class<?> aClass = TypeMappingsUtils.getClass(columnMetadata.getType());
            RendererDto rendererDto = columnStyleParamsDto != null ? columnStyleParamsDto.getRenderer() : null;
            pattern = ColumnPatternHelper.getPatternForClass(aClass, rendererDto, locale);
        }
        return pattern;
    }

    public Map<String, ColumnMetadata> getColumnMetadata(final String service, final String path, final String entity, final List<ColumnDto> columns,
                                                         FilterParameter filters, Boolean advQuery) throws DynamicReportException {
        Map<String, ColumnMetadata> columnMetadataMap = new HashMap<>();
        List<String> stringColumns = getColumnsFromDto(columns);
        Map<String, Integer> defaultSqlColumnTypes = getSQLColumnTypes(service, path, entity, stringColumns, filters, advQuery);
        for (ColumnDto col : columns) {
            String id = col.getId();
            int type = defaultSqlColumnTypes.containsKey(id) ? defaultSqlColumnTypes.get(id) : Types.OTHER;
            if(hasRenderForColumn(col)){
                int auxType = retrieveTypeFromRenderer(col);
                if(auxType != -1) {
                    type = auxType;
                }
            }

            String classname = TypeMappingsUtils.getClassName(type);
            ColumnMetadata columnMetadata = new ColumnMetadata(id, type, classname);
            columnMetadataMap.put(id, columnMetadata);
        }
        return columnMetadataMap;
    }


    public Map<String, Integer> getSQLColumnTypes(final String service, final String path, final String entity,
                                                  final List<String> columns, FilterParameter filters, Boolean advQuery) throws DynamicReportException {
        Map<String, Integer> sqlTypes = new HashMap<>();
        Object bean = this.getApplicationContextUtils().getServiceBean(service, path);

        Map<Object, Object> kv = (filters != null && filters.getFilter()!=null) ? filters.getFilter() : new HashMap<>();

        EntityResult entityResult;
        if (Boolean.TRUE.equals(advQuery)) {
            List<SQLStatementBuilder.SQLOrder> sqlOrders = new ArrayList<>();
            entityResult = (EntityResult) ReflectionTools.invoke(bean, entity.concat("PaginationQuery"),
                    kv, columns, Integer.MAX_VALUE, 0, sqlOrders);
        } else {
            entityResult = (EntityResult) ReflectionTools.invoke(bean,
                    entity.concat("Query"), kv, columns);
        }

        if (entityResult.getCode() == EntityResult.OPERATION_SUCCESSFUL) {
            return entityResult.getColumnSQLTypes();
        }
        return sqlTypes;
    }

    public List<String> getColumnsFromDto(List<ColumnDto> columnsDto) {
        List<String> columns = new ArrayList<>();
        columnsDto.forEach(column -> columns.add(column.getId()));
        return columns;
    }


    public void evaluateServiceRenderer(EntityResultDataSource entityResultDataSource, List<ColumnDto> columns) throws DynamicReportException {
        if (entityResultDataSource != null && columns != null) {
            for (ColumnDto columnDto : columns) {
                RendererDto renderer = columnDto.getColumnStyle() != null ? columnDto.getColumnStyle().getRenderer() : null;
                if (renderer instanceof ServiceRendererDto) {
                    ServiceRendererDto serviceRendererDto = (ServiceRendererDto) renderer;
                    if (StringUtils.isBlank(serviceRendererDto.getService())) {
                        throw new IllegalArgumentException("'service' argument not found on ServiceRendererDto bean!");
                    }

                    if (StringUtils.isBlank(serviceRendererDto.getEntity())) {
                        throw new IllegalArgumentException("'entity' argument not found on ServiceRendererDto bean!");
                    }

                    if (StringUtils.isBlank(serviceRendererDto.getKeyColumn())) {
                        throw new IllegalArgumentException("'keyColumn' argument not found on ServiceRendererDto bean!");
                    }

                    if (StringUtils.isBlank(serviceRendererDto.getValueColumn())) {
                        throw new IllegalArgumentException("'valueColumn' argument not found on ServiceRendererDto bean!");
                    }

                    Map<String, Object> map = new HashMap<>();
                    List<String> cols = serviceRendererDto.getColumns();
                    Object bean = this.getApplicationContextUtils().getServiceBean(serviceRendererDto.getService(), serviceRendererDto.getPath());
                    EntityResult eR_renderer = (EntityResult) ReflectionTools.invoke(bean,
                            serviceRendererDto.getEntity().concat("Query"),
                            map, cols);

                    Map<String, EntityResult> renderData = new HashMap<>();
                    renderData.put(serviceRendererDto.getKeyColumn(), eR_renderer);
                    entityResultDataSource.addRendererData(renderData);

                    Map<String, Renderer> renderInfo = new HashMap<>();
                    renderInfo.put(serviceRendererDto.getKeyColumn(), serviceRendererDto);
                    entityResultDataSource.addRendererInfo(renderInfo);
                } else if(renderer instanceof BooleanRendererDto) {
                    BooleanRendererDto booleanRendererDto = (BooleanRendererDto) renderer;

                    Map<String, Renderer> renderInfo = new HashMap<>();
                    renderInfo.put(columnDto.getId(), booleanRendererDto);
                    entityResultDataSource.addRendererInfo(renderInfo);

                }
            }
        }
    }

    protected boolean hasRenderForColumn(final ColumnDto column) {
        if (column != null && column.getColumnStyle() != null && column.getColumnStyle().getRenderer() instanceof Renderer) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    protected int retrieveTypeFromRenderer(final ColumnDto column) throws DynamicReportException {
        int type = -1;
        if (column != null && column.getColumnStyle() != null) {
            if (column.getColumnStyle().getRenderer() instanceof ServiceRendererDto) {
                ServiceRendererDto serviceRendererDto = (ServiceRendererDto) column.getColumnStyle().getRenderer();
                Map<String, Integer> rendererSqlColumnTypes =
                        getSQLColumnTypes(serviceRendererDto.getService(), serviceRendererDto.getPath(),
                                serviceRendererDto.getEntity(), serviceRendererDto.getColumns(), null, false);
                type = rendererSqlColumnTypes.get(serviceRendererDto.getValueColumn());
            } else if(column.getColumnStyle().getRenderer() instanceof BooleanRendererDto) {
                BooleanRendererDto booleanRendererDto = (BooleanRendererDto) column.getColumnStyle().getRenderer();
                if(BooleanRendererDto.STRING_TYPE.equals(booleanRendererDto.getRenderType())) {
                    type = Types.VARCHAR;
                } else if(BooleanRendererDto.NUMBER_TYPE.equals(booleanRendererDto.getRenderType())) {
                    type = Types.INTEGER;
                }
            }
        }
        return type;
    }

    protected ServiceRendererDto retrieveServiceRenderForColumn(final ColumnDto column) {
        if (column != null && column.getColumnStyle() != null) {
            if (column.getColumnStyle().getRenderer() instanceof ServiceRendererDto) {
                return (ServiceRendererDto) column.getColumnStyle().getRenderer();
            }
        }
        return null;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public ApplicationContextUtils getApplicationContextUtils() {
        return applicationContextUtils;
    }

    public void setApplicationContextUtils(ApplicationContextUtils applicationContextUtils) {
        this.applicationContextUtils = applicationContextUtils;
    }
}
