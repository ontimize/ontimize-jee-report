package util;

import java.util.List;

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
}
