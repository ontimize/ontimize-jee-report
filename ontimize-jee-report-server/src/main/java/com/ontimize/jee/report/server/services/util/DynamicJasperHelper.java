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

  public DJValueFormatter getFunctionValueFormatter(String type, ResourceBundle bundle) {
    return new DJValueFormatter() {

      public Object evaluate(Object value, Map fields, Map variables, Map parameters) {
        String valor = "";
        switch (type) {
          case DynamicJasperNaming.SUM:
            valor = bundle.getString("sum_text") + " : " + value;
            break;
          case DynamicJasperNaming.AVERAGE:
            valor = bundle.getString("average_text") + " : " + value;
            break;
          case DynamicJasperNaming.MAX:
            valor = bundle.getString("max_text") + " : " + value;
            break;
          case DynamicJasperNaming.MIN:
            valor = bundle.getString("min_text") + " : " + value;
            break;
          case DynamicJasperNaming.TOTAL:
            valor = bundle.getString("total_text") + " : " + value;
            break;
        }
        return valor;
      }

      public String getClassName() {
        return String.class.getName();
      }
    };
  }
  
  public DJGroup createReportGroup(final AbstractColumn column, final List<String> styleArgs, final int numberGroups) {
    GroupBuilder gb1 = new GroupBuilder();
    DJGroup g1 = gb1.setCriteriaColumn((PropertyColumn) column).build();
    if (numberGroups == 0 && styleArgs.contains("firstGroupNewPage")) {
      g1.setStartInNewPage(true);
    }
    if (styleArgs.contains("hideGroupDetails")) {
      gb1.setGroupLayout(GroupLayout.EMPTY);

    } else {
      gb1.setGroupLayout(GroupLayout.VALUE_IN_HEADER);
    }

    if (styleArgs.contains("groupNewPage")) {
      g1.setStartInNewPage(true);
    }
    Style groupStyle = new Style();
    groupStyle.setPaddingLeft(numberGroups * 20);
    groupStyle.setTransparent(false);
    if (numberGroups < 3) {
      groupStyle.setBackgroundColor(new Color(178 + (numberGroups * 26), 178 + (numberGroups * 26),
              178 + (numberGroups * 26)));
    } else if (numberGroups == 3) {
      groupStyle.setBackgroundColor(new Color(249, 249, 249));
    } else {
      groupStyle.setBackgroundColor(new Color(255, 255, 255));
    }
    column.setStyle(groupStyle);
    return g1;
  }

  public Style getStyleGrid(List<String> styleArgs, Style style) {
    if (styleArgs.contains("grid")) {
      style.setBorderBottom(Border.THIN());
      style.setBorderTop(Border.THIN());
      style.setBorderLeft(Border.THIN());
      style.setBorderRight(Border.THIN());
    } else {
      style.setBorderBottom(Border.NO_BORDER());
      style.setBorderTop(Border.NO_BORDER());
      style.setBorderLeft(Border.NO_BORDER());
      style.setBorderRight(Border.NO_BORDER());
    }
    return style;
  }
  
  public Style getFooterStyle(){
    Style footerStyle = new Style();
    footerStyle.setBackgroundColor(new Color(255, 255, 255));
    footerStyle.setTextColor(Color.BLACK);
    footerStyle.setHorizontalAlign(HorizontalAlign.JUSTIFY);
    footerStyle.setTransparency(Transparency.OPAQUE);
    footerStyle.setBorderTop(Border.NO_BORDER());
    return footerStyle;
  }
  public void configureReportFunction(final DynamicReportBuilder drb, final AbstractColumn column, final String function,
                                      final ResourceBundle bundle) {
    Style footerStyle = getFooterStyle();
    if (function.endsWith(bundle.getString("sum"))) {
      DJValueFormatter valueFormatter = getFunctionValueFormatter(DynamicJasperNaming.SUM, bundle);
      drb.addGlobalFooterVariable(column, DJCalculation.SUM, footerStyle, valueFormatter)
              .setGrandTotalLegend("");
    } else if (function.endsWith(bundle.getString("average"))) {
      DJValueFormatter valueFormatter = getFunctionValueFormatter(DynamicJasperNaming.AVERAGE, bundle);
      drb.addGlobalFooterVariable(column, DJCalculation.AVERAGE, footerStyle,
              valueFormatter).setGrandTotalLegend("");
    } else if (function.endsWith(bundle.getString("max"))) {
      DJValueFormatter valueFormatter = getFunctionValueFormatter(DynamicJasperNaming.MAX, bundle);
      drb.addGlobalFooterVariable(column, DJCalculation.HIGHEST, footerStyle, valueFormatter)
              .setGrandTotalLegend("");
    } else if (function.endsWith(bundle.getString("min"))) {
      DJValueFormatter valueFormatter = getFunctionValueFormatter(DynamicJasperNaming.MIN, bundle);
      drb.addGlobalFooterVariable(column, DJCalculation.LOWEST, footerStyle, valueFormatter)
              .setGrandTotalLegend("");

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
