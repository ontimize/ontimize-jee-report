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

	@RequestMapping(value = "/save", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public void savePreferences(@RequestBody PreferencesParamsDto param) throws Exception {
		Map<String, Object> attrMap = new HashMap<>();
		attrMap.put("NAME", param.getName());
		attrMap.put("ORIENTATION", param.isVertical());
		attrMap.put("TITLE", param.getTitle());
		attrMap.put("SUBTITLE", param.getSubtitle());
		attrMap.put("COLUMNS", param.getColumns());
		attrMap.put("GROUPS", param.getGroups().toString());
		attrMap.put("FUNCTIONS", param.getFunctions().toString());
		attrMap.put("STYLEFUNCTIONS", param.getStyleFunctions().toString());
		preferencesService.preferenceInsert(attrMap);
	}

	@RequestMapping(value = "/preferences", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public EntityResult getPreferences() {
		Map<String, Object> map = new HashMap<>();
		List<String> attrList = new ArrayList<>();
		attrList.add("ID");
		attrList.add("NAME");
		attrList.add("ORIENTATION");
		attrList.add("TITLE");
		attrList.add("SUBTITLE");
		attrList.add("COLUMNS");
		attrList.add("GROUPS");
		attrList.add("FUNCTIONS");
		attrList.add("STYLEFUNCTIONS");
		return preferencesService.preferenceQuery(map, attrList);
	}

}
