package com.ontimize.jee.report.server.services.util;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.tools.ReflectionTools;
import com.ontimize.jee.report.common.dto.ColumnDto;
import com.ontimize.jee.report.common.dto.ColumnStyleParamsDto;
import com.ontimize.jee.report.common.dto.renderer.RendererDto;
import com.ontimize.jee.report.common.dto.renderer.ServiceRendererDto;
import com.ontimize.jee.report.common.util.EntityResultDataSource;
import com.ontimize.jee.report.common.util.TypeMappingsUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import javax.xml.rpc.Service;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DynamicJasperHelper {

  private ApplicationContext applicationContext;

  public DynamicJasperHelper(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }
  
  public String getColumnPattern(final ColumnMetadata columnMetadata, final ColumnStyleParamsDto columnStyleParamsDto,
                                 final Locale locale){
    String pattern = null;
    if(columnMetadata != null){
      Class<?> aClass = TypeMappingsUtils.getClass(columnMetadata.getType());
      RendererDto rendererDto = columnStyleParamsDto != null ? columnStyleParamsDto.getRenderer() : null;
      pattern = ColumnPatternHelper.getPatternForClass(aClass, rendererDto, locale);
    }
    return pattern;
  }
  
  public Map<String, ColumnMetadata> getColumnMetadata(final String service, final String entity, final List<ColumnDto> columns) {
    Map<String,ColumnMetadata> columnMetadataMap = new HashMap<>();
    List<String> stringColumns = getColumnsFromDto(columns);
    Map<String, Integer> defaultSqlColumnTypes = getSQLColumnTypes(service, entity, stringColumns);
    for(ColumnDto col : columns) {
      String id = col.getId();
      int type = defaultSqlColumnTypes.get(id);
      ServiceRendererDto serviceRendererDto = retrieveServiceRenderForColumn(col);
      if(serviceRendererDto != null) {
        Map<String, Integer> rendererSqlColumnTypes =
            getSQLColumnTypes(serviceRendererDto.getService(), serviceRendererDto.getEntity(), serviceRendererDto.getColumns());
//        type = rendererSqlColumnTypes.get(serviceRendererDto.getValueColumn());
        //FIXME Only for testing
        type = Types.LONGVARCHAR;
      }
      String classname = TypeMappingsUtils.getClassName(type);
      ColumnMetadata columnMetadata = new ColumnMetadata(id, type, classname);
      columnMetadataMap.put(id, columnMetadata);
    }
    return columnMetadataMap;
  }
  
  
  public Map<String, Integer> getSQLColumnTypes(final String service, final String entity, final List<String> columns) {
    Map<String,Integer> sqlTypes = new HashMap<>();
//    Object bean = this.applicationContext.getBean(service.concat("Service"));
//    EntityResult entityResult = (EntityResult) ReflectionTools.invoke(bean, 
//        entity.concat("Query"), 
//        new HashMap<>(),
//        columns);
//    
//    if(entityResult.getCode() == EntityResult.OPERATION_SUCCESSFUL) {
//      return entityResult.getColumnSQLTypes();
//    }
    
    //FIXME Only for testing
    sqlTypes.put("integer", Types.INTEGER);
    sqlTypes.put("real", Types.REAL);
    sqlTypes.put("currency",Types.DOUBLE);
    sqlTypes.put("percentage", Types.DOUBLE);
    sqlTypes.put("date", Types.DATE);
    sqlTypes.put("CUSTOMERTYPEID", Types.INTEGER);
    return sqlTypes;
  }
  
  public List<String> getColumnsFromDto(List<ColumnDto> columnsDto) {
    List<String> columns = new ArrayList<>();
    columnsDto.forEach(column -> columns.add(column.getId()));
    return columns;
  }


  public void evaluateServiceRenderer(EntityResultDataSource entityResultDataSource, List<ColumnDto> columns) {
    if(entityResultDataSource != null && columns != null){
      for(ColumnDto columnDto : columns) {
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
          Object bean = this.applicationContext.getBean(serviceRendererDto.getService().concat("Service"));
          EntityResult eR_renderer = (EntityResult) ReflectionTools.invoke(bean,
                  serviceRendererDto.getEntity().concat("Query"),
                  map, cols);

          Map<String, EntityResult> renderData = new HashMap<>();
          renderData.put(serviceRendererDto.getKeyColumn(), eR_renderer);
          entityResultDataSource.setRendererData(renderData);

          Map<String, ServiceRendererDto> renderInfo = new HashMap<>();
          renderInfo.put(serviceRendererDto.getKeyColumn(), serviceRendererDto);
          entityResultDataSource.setRendererInfo(renderInfo);
        }
      }
    }
  }

  protected ServiceRendererDto retrieveServiceRenderForColumn(final ColumnDto column) {
    if(column != null && column.getColumnStyle() != null) {
      if(column.getColumnStyle().getRenderer() instanceof ServiceRendererDto) {
        return (ServiceRendererDto) column.getColumnStyle().getRenderer();
      }
    }
    return null;
  }

}
