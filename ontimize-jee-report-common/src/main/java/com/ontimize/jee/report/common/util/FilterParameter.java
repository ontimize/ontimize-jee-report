package com.ontimize.jee.report.common.util;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*This class belongs to the ontimize.jee.server.rest module 
and has been added to this module since it does not have a dependency on the source module*/
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", defaultImpl = AdvancedQueryParameter.class)
@JsonSubTypes({ @JsonSubTypes.Type(value = QueryParameter.class, name = "query"),
        @JsonSubTypes.Type(value = AdvancedQueryParameter.class, name = "advanced") })

public interface FilterParameter {

    Map<Object, Object> getFilter();

    void setKv(Map<Object, Object> filter);

    List<Object> getColumns();

    void setColumns(List<Object> columns);

    HashMap<Object, Object> getSqltypes();

    void setSqltypes(HashMap<Object, Object> sqltypes);

}
