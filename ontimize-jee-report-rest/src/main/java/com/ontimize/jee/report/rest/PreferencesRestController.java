package com.ontimize.jee.report.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.services.reportstore.IPreferencesService;
import com.ontimize.jee.report.rest.dtos.PreferencesParamsDto;

import util.JsonServicePreferencesDtoConversor;

@RestController
@RequestMapping("/preferences")
@ComponentScan(basePackageClasses = { com.ontimize.jee.common.services.reportstore.IPreferencesService.class })
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
	public String savePreferences(@RequestBody PreferencesParamsDto param) throws Exception {

		Map<String, Object> attrMap = new HashMap<>();
		attrMap.put("NAME", param.getName());
		attrMap.put("DESCRIPTION", param.getDescription());
		attrMap.put("PREFERENCES", conversor.toObjectNode(param));

		preferencesService.preferenceInsert(attrMap);

		return param.toString();
	}

	@RequestMapping(value = "/preferences", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public EntityResult getPreferences() {
		Map<String, Object> map = new HashMap<>();
		List<String> attrList = new ArrayList<>();
		attrList.add("ID");
		attrList.add("NAME");
		attrList.add("DESCRIPTION");
		attrList.add("PREFERENCES");
		return preferencesService.preferenceQuery(map, attrList);
	}

}
