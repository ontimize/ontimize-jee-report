package com.ontimize.jee.report.common.services;
import java.util.List;
import java.util.Map;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;

public interface IPreferencesService {

 // PREFERENCES
 public EntityResult preferenceQuery(Map<String, Object> keyMap, List<String> attrList) throws OntimizeJEERuntimeException;
 public EntityResult preferenceInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException;
 public EntityResult preferenceUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap) throws OntimizeJEERuntimeException;
 public EntityResult preferenceDelete(Map<String, Object> keyMap) throws OntimizeJEERuntimeException;

}