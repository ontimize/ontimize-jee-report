package util;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import javax.json.Json;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ontimize.jee.report.rest.dtos.PreferencesParamsDto;

public class JsonServicePreferencesDtoConversor {

	public static ObjectNode toObjectNode(PreferencesParamsDto preferences) {

		ObjectNode preferencesObject = JsonNodeFactory.instance.objectNode();
		preferencesObject.put("vertical", preferences.vertical).put("title", preferences.getTitle())
				.put("subtitle", preferences.getSubtitle()).put("columns", preferences.getColumns().toString())
				.put("groups", preferences.getGroups().toString())
				.put("functions", preferences.getFunctions().toString())
				.put("styleFunctions", preferences.getStyleFunctions().toString());

		return preferencesObject;
	}

	public static PreferencesParamsDto toPreferencesDto(Object preferences)
			throws JsonMappingException, JsonProcessingException, ParseException {
		JSONParser parser = new JSONParser();
		JSONObject json = (JSONObject) parser.parse(preferences.toString());
		PreferencesParamsDto params = new PreferencesParamsDto();
		if (json.get("title") != null) {
			params.setTitle(json.get("title").toString());
		}
		if (json.get("subtitle") != null) {
			params.setSubtitle(json.get("subtitle").toString());
		}

		params.setVertical(json.get("vertical").toString() == "true");
		params.setColumns(Arrays.asList(json.get("columns").toString().replace("[", "").replace("]", "").split(",")));
		params.setGroups(Arrays.asList(json.get("groups").toString().replace("[", "").replace("]", "").split(",")));
		params.setFunctions(
				Arrays.asList(json.get("functions").toString().replace("[", "").replace("]", "").split(",")));
		params.setStyleFunctions(
				Arrays.asList(json.get("styleFunctions").toString().replace("[", "").replace("]", "").split(",")));

		return params;
	}
}
