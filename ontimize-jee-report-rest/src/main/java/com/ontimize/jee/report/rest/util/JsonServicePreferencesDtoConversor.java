package com.ontimize.jee.report.rest.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontimize.jee.report.common.dto.PreferencesParamsDto;

public class JsonServicePreferencesDtoConversor {

	public static String toObjectNode(PreferencesParamsDto preferences) throws JsonProcessingException {

		ObjectMapper mapper = new ObjectMapper();
		String jsonString = mapper.writeValueAsString(preferences);

		return jsonString;
	}

}
