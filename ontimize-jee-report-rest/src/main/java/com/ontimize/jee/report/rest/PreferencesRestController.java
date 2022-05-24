package com.ontimize.jee.report.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.common.services.reportstore.IPreferencesService;
import com.ontimize.jee.report.rest.dtos.PreferencesParamsDto;

import util.JsonServicePreferencesDtoConversor;

@RestController
@RequestMapping("${ontimize.report.preferences.url:/preferences}")
public class PreferencesRestController {
	@Qualifier("PreferencesService")
	@Autowired
	private IPreferencesService preferencesService;

	public IPreferencesService getService() {
		return this.preferencesService;
	}

	@Autowired
	private JsonServicePreferencesDtoConversor conversor;

	@RequestMapping(value = "/save", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public EntityResult savePreferences(@RequestBody PreferencesParamsDto param) throws Exception {

		Map<String, Object> attrMap = new HashMap<>();
		attrMap.put("NAME", param.getName());
		attrMap.put("DESCRIPTION", param.getDescription());
		attrMap.put("ENTITY", param.getEntity());
		attrMap.put("PREFERENCES", conversor.toObjectNode(param));

		return preferencesService.preferenceInsert(attrMap);

	}

	@RequestMapping(value = "/preferences", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public EntityResult getPreferences() {
		List<String> columns = new ArrayList<>();
		Map<String, Object> map = new HashMap<>();
		List<String> attrList = new ArrayList<>();
		PreferencesParamsDto preferences = new PreferencesParamsDto();
		attrList.add("ID");
		attrList.add("NAME");
		attrList.add("DESCRIPTION");
		attrList.add("ENTITY");
		attrList.add("PREFERENCES");
		EntityResult res = preferencesService.preferenceQuery(map, attrList);
		return res;
	}

	@RequestMapping(value = "/remove/{id}", method = RequestMethod.DELETE)
	public EntityResult removePreferences(@PathVariable("id") Long id) {
		EntityResult res = new EntityResultMapImpl();
		Map<String, Object> attrMap = new HashMap<>();
		try {
			attrMap.put("ID", id);
			this.preferencesService.preferenceDelete(attrMap);
			res.setCode(EntityResult.OPERATION_SUCCESSFUL);
		} catch (Exception e) {
			e.printStackTrace();
			res.setCode(EntityResult.OPERATION_WRONG);
			return res;
		}
		return res;
	}

	@RequestMapping(value = "/update/{id}", method = RequestMethod.PUT)
	public EntityResult updatePreferences(@PathVariable("id") Long id, @RequestBody PreferencesParamsDto param) {
		EntityResult res = new EntityResultMapImpl();
		Map<String, Object> attrMap = new HashMap<>();
		attrMap.put("NAME", param.getName());
		attrMap.put("DESCRIPTION", param.getDescription());
		attrMap.put("PREFERENCES", conversor.toObjectNode(param).toString());

		Map<String, Object> attrKey = new HashMap<>();
		try {
			attrKey.put("ID", id);
			this.preferencesService.preferenceUpdate(attrMap, attrKey);
			res.setCode(EntityResult.OPERATION_SUCCESSFUL);
		} catch (Exception e) {
			e.printStackTrace();
			res.setCode(EntityResult.OPERATION_WRONG);
			return res;
		}
		return res;
	}
}
