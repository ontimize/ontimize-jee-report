package com.ontimize.jee.report.server.services.util;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.tools.ReflectionTools;
import com.ontimize.jee.report.common.dto.ServiceRendererDto;
import com.ontimize.jee.report.common.util.TypeMappingsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamicJasperHelper {

  private ApplicationContext applicationContext;

  public DynamicJasperHelper(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }
  
  public Map<String, String> getColumnClassnames(final String service, final String entity, final List<String> columns,
      final List<ServiceRendererDto> serviceRendererList) {
    Map<String,String> classNames = new HashMap<>();
    Map<String, Integer> defaultSqlColumnTypes = getSQLColumnTypes(service, entity, columns);
    for(String col : columns) {
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
  
  protected ServiceRendererDto retrieveServiceRenderForColumn(final String column, final List<ServiceRendererDto> serviceRendererList) {
    return serviceRendererList.parallelStream().filter(item -> {
      return item.getKeyColumn().equals(column);
    }).findFirst().orElse(null);
  }

}
