package com.ontimize.jee.report.server.services.util;

import ar.com.fdvs.dj.domain.DJCalculation;
import ar.com.fdvs.dj.domain.DJValueFormatter;
import ar.com.fdvs.dj.domain.Style;
import ar.com.fdvs.dj.domain.builders.DynamicReportBuilder;
import ar.com.fdvs.dj.domain.builders.GroupBuilder;
import ar.com.fdvs.dj.domain.constants.Border;
import ar.com.fdvs.dj.domain.constants.GroupLayout;
import ar.com.fdvs.dj.domain.constants.HorizontalAlign;
import ar.com.fdvs.dj.domain.constants.Transparency;
import ar.com.fdvs.dj.domain.entities.DJGroup;
import ar.com.fdvs.dj.domain.entities.columns.AbstractColumn;
import ar.com.fdvs.dj.domain.entities.columns.PropertyColumn;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.tools.ReflectionTools;
import com.ontimize.jee.report.common.dto.ColumnDto;
import com.ontimize.jee.report.common.dto.ServiceRendererDto;
import com.ontimize.jee.report.common.util.EntityResultDataSource;
import com.ontimize.jee.report.common.util.TypeMappingsUtils;
import com.ontimize.jee.report.server.naming.DynamicJasperNaming;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class DynamicJasperHelper {

  private ApplicationContext applicationContext;

  public DynamicJasperHelper(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }
  
  public Map<String, String> getColumnClassnames(final String service, final String entity, final List<ColumnDto> columns,
      final List<ServiceRendererDto> serviceRendererList) {
    Map<String,String> classNames = new HashMap<>();
    List<String> stringColumns = getColumnsFromDto(columns);
    Map<String, Integer> defaultSqlColumnTypes = getSQLColumnTypes(service, entity, stringColumns);
    for(String col : stringColumns) {
      int type = defaultSqlColumnTypes.get(col);
      ServiceRendererDto serviceRendererDto = retrieveServiceRenderForColumn(col, serviceRendererList);
      if(serviceRendererDto != null) {
        Map<String, Integer> rendererSqlColumnTypes =
            getSQLColumnTypes(serviceRendererDto.getService(), serviceRendererDto.getEntity(), serviceRendererDto.getColumns());
        type = rendererSqlColumnTypes.get(serviceRendererDto.getValueColumn());
      }
      String classname = TypeMappingsUtils.getClassName(type);
      classNames.put(col, classname);
    }
    return classNames;
  }
  
  
  public Map<String, Integer> getSQLColumnTypes(final String service, final String entity, final List<String> columns) {
    Map<String,Integer> sqlTypes = new HashMap<>();
    Object bean = this.applicationContext.getBean(service.concat("Service"));
    EntityResult entityResult = (EntityResult) ReflectionTools.invoke(bean, 
        entity.concat("Query"), 
        new HashMap<>(),
        columns);
    
    if(entityResult.getCode() == EntityResult.OPERATION_SUCCESSFUL) {
      return entityResult.getColumnSQLTypes();
    }
    
    return sqlTypes;
  }
  
  public List<String> getColumnsFromDto(List<ColumnDto> columnsDto) {
    List<String> columns = new ArrayList<>();
    columnsDto.forEach(column -> columns.add(column.getId()));
    return columns;
  }


  public void evaluateServiceRenderer(EntityResultDataSource entityResultDataSource, final List<ServiceRendererDto> serviceRendererList) {
    if(entityResultDataSource != null && serviceRendererList != null){
      for(ServiceRendererDto serviceRendererDto : serviceRendererList){

        if(StringUtils.isBlank(serviceRendererDto.getService())) {
          throw new IllegalArgumentException("'service' argument not found on ServiceRendererDto bean!");
        }

        if(StringUtils.isBlank(serviceRendererDto.getEntity())) {
          throw new IllegalArgumentException("'entity' argument not found on ServiceRendererDto bean!");
        }

        if(StringUtils.isBlank(serviceRendererDto.getKeyColumn())) {
          throw new IllegalArgumentException("'keyColumn' argument not found on ServiceRendererDto bean!");
        }

        if(StringUtils.isBlank(serviceRendererDto.getValueColumn())) {
          throw new IllegalArgumentException("'valueColumn' argument not found on ServiceRendererDto bean!");
        }

        Map<String, Object> map = new HashMap<>();
        List<String> columns = serviceRendererDto.getColumns();
        Object bean = this.applicationContext.getBean(serviceRendererDto.getService().concat("Service"));
        EntityResult eR_renderer = (EntityResult) ReflectionTools.invoke(bean,
                serviceRendererDto.getEntity().concat("Query"),
                map, columns);

        Map<String, EntityResult> renderData = new HashMap<>();
        renderData.put(serviceRendererDto.getKeyColumn(), eR_renderer);
        entityResultDataSource.setRendererData(renderData);

        Map<String, ServiceRendererDto> renderInfo = new HashMap<>();
        renderInfo.put(serviceRendererDto.getKeyColumn(), serviceRendererDto);
        entityResultDataSource.setRendererInfo(renderInfo);
      }
    }
  }

  
  protected ServiceRendererDto retrieveServiceRenderForColumn(final String column, final List<ServiceRendererDto> serviceRendererList) {
    if(!StringUtils.isBlank(column) && serviceRendererList != null) {
      return serviceRendererList.parallelStream().filter(item -> {
        return item.getKeyColumn().equals(column);
      }).findFirst().orElse(null);
    }
    return null;
  }

}
