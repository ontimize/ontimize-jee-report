package com.ontimize.jee.server.services.reportstore;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.common.services.reportstore.IPreferencesService;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;
//import com.ontimize.jee.server.services.reportstore.dao.IPreferencesDao;

@Service("PreferencesService")
@Lazy(value = true)
public class PreferencesService implements IPreferencesService {

	/*@Autowired
	private IPreferencesDao preferencesDao;*/
	
	@Autowired
	private DefaultOntimizeDaoHelper daoHelper;

	@Override
	public EntityResult preferenceQuery(Map<String, Object> keyMap, List<String> attrList)
			throws OntimizeJEERuntimeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EntityResult preferenceInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EntityResult preferenceUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap)
			throws OntimizeJEERuntimeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EntityResult preferenceDelete(Map<String, Object> keyMap) throws OntimizeJEERuntimeException {
		// TODO Auto-generated method stub
		return null;
	}

	/*@Override
	public EntityResult preferenceQuery(Map<String, Object> keyMap, List<String> attrList)
			throws OntimizeJEERuntimeException {
		return this.daoHelper.query(this.preferencesDao, keyMap, attrList);
	}

	@Override
	public EntityResult preferenceInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException {
		return this.daoHelper.insert(this.preferencesDao, attrMap);
	}

	@Override
	public EntityResult preferenceUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap)
			throws OntimizeJEERuntimeException {
		return this.daoHelper.update(this.preferencesDao, attrMap, keyMap);
	}

	@Override
	public EntityResult preferenceDelete(Map<String, Object> keyMap) throws OntimizeJEERuntimeException {
		return this.daoHelper.delete(this.preferencesDao, keyMap);
	}*/

}
