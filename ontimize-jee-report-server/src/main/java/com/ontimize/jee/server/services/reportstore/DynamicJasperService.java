package com.ontimize.jee.server.services.reportstore;

import java.awt.Color;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.services.reportstore.ColumnStyleParamsDto;
import com.ontimize.jee.common.services.reportstore.EntityResultDataSource;
import com.ontimize.jee.common.services.reportstore.IDynamicJasperService;
import com.ontimize.jee.common.services.reportstore.ReportBase;
import com.ontimize.jee.common.services.reportstore.TypeMappingsUtils;
import com.ontimize.jee.common.tools.ReflectionTools;

import ar.com.fdvs.dj.domain.AutoText;
import ar.com.fdvs.dj.domain.CustomExpression;
import ar.com.fdvs.dj.domain.DJCalculation;
import ar.com.fdvs.dj.domain.DJValueFormatter;
import ar.com.fdvs.dj.domain.DynamicReport;
import ar.com.fdvs.dj.domain.ImageBanner;
import ar.com.fdvs.dj.domain.Style;
import ar.com.fdvs.dj.domain.builders.ColumnBuilder;
import ar.com.fdvs.dj.domain.builders.DynamicReportBuilder;
import ar.com.fdvs.dj.domain.builders.GroupBuilder;
import ar.com.fdvs.dj.domain.constants.Border;
import ar.com.fdvs.dj.domain.constants.GroupLayout;
import ar.com.fdvs.dj.domain.constants.HorizontalAlign;
import ar.com.fdvs.dj.domain.constants.Page;
import ar.com.fdvs.dj.domain.constants.Transparency;
import ar.com.fdvs.dj.domain.constants.VerticalAlign;
import ar.com.fdvs.dj.domain.entities.DJGroup;
import ar.com.fdvs.dj.domain.entities.columns.AbstractColumn;
import ar.com.fdvs.dj.domain.entities.columns.PropertyColumn;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;

@Service("DynamicJasperService")
@Lazy(value = true)
public class DynamicJasperService extends ReportBase implements IDynamicJasperService {
	/** The Constant MAX. */
	private static final String MAX = "MAXIMO";
	/** The Constant MIN. */
	private static final String MIN = "MINIMO";
	/** The Constant SUM. */
	private static final String SUM = "SUMA";
	/** The Constant AVERAGE. */
	private static final String AVERAGE = "MEDIA";
	/** The Constant TOTAL. */
	private static final String TOTAL = "TOTAL";

	Locale locale;
	ResourceBundle bundle = ResourceBundle.getBundle("bundle/bundle", locale);
	@Autowired
	private ApplicationContext applicationContext;

	@Override
	public InputStream createReport(List<String> columns, String title, List<String> groups, String entity,
			String service, String orientation, List<String> functions, List<String> styleFunctions, String subtitle,
			List<ColumnStyleParamsDto> columnStyle) throws Exception {
		return this.generateReport(columns, title, groups, entity, service, orientation, functions, styleFunctions,
				subtitle, columnStyle);

	}

	@Override
	public List<String> getFunctions(String entity, String service, List<String> columns, String language) {
		switch (language) {
		case "es":
			locale = new Locale("es", "ES");
		case "gl":
			locale = new Locale("gl", "ES");
		default:
			locale = new Locale("en", "US");
		}

		List<String> functions = new ArrayList<>();
		Map<String, Object> map = new HashMap<>();
		Object bean = this.applicationContext.getBean(service.concat("Service"));
		EntityResult e = (EntityResult) ReflectionTools.invoke(bean, entity.toLowerCase().concat("Query"), map,
				columns);
		for (int i = 0; i < columns.size(); i++) {

			int type = e.getColumnSQLType(columns.get(i));

			String className = TypeMappingsUtils.getClassName(type);
			if (className.equals("java.lang.Integer")) {
				functions.add(columns.get(i) + bundle.getString("sum"));
				functions.add(columns.get(i) + bundle.getString("average"));
				functions.add(columns.get(i) + bundle.getString("max"));
				functions.add(columns.get(i) + bundle.getString("min"));

			}
		}
		return functions;
	}

	@Override
	public List<String> getFunctionsName(String entity, String service, List<String> columns) {
		List<String> functions = new ArrayList<>();
		Map<String, Object> map = new HashMap<>();
		Object bean = this.applicationContext.getBean(service.concat("Service"));
		EntityResult e = (EntityResult) ReflectionTools.invoke(bean, entity.toLowerCase().concat("Query"), map,
				columns);
		for (int i = 0; i < columns.size(); i++) {

			int type = e.getColumnSQLType(columns.get(i));

			String className = TypeMappingsUtils.getClassName(type);
			if (className.equals("java.lang.Integer")) {
				functions.add(columns.get(i));

			}
		}
		return functions;
	}

	public DynamicReport buildReport(List<String> columns, String title, List<String> groups, String entity,
			String service, String orientation, List<String> functions, List<String> styleFunctions, String subtitle,
			List<ColumnStyleParamsDto> columnStyle) throws Exception {
		int numberGroups = 0;
		String name = "";
		int width;
		URL url = getClass().getClassLoader().getResource("logo.png");
		URL urlTemplate = getClass().getClassLoader().getResource("template.jrxml");
		DynamicReportBuilder drb = new DynamicReportBuilder();
		drb.setTitle(title).setSubtitle(subtitle).setPrintBackgroundOnOddRows(true).setUseFullPageWidth(true)
				.setUseFullPageWidth(true).addFirstPageImageBanner(url.getPath(), 800, 50, ImageBanner.ALIGN_LEFT);
		if (orientation.equals("horizontal")) {
			drb.setPageSizeAndOrientation(Page.Page_A4_Landscape());
		} else
			drb.setPageSizeAndOrientation(Page.Page_A4_Portrait());
		Map<String, Object> map = new HashMap<>();
		Object bean = this.applicationContext.getBean(service.concat("Service"));
		EntityResult e = (EntityResult) ReflectionTools.invoke(bean, entity.toLowerCase().concat("Query"), map,
				columns);
		Style columnDataStyle = new Style();
		Style headerStyle = new Style();
		if (styleFunctions.contains("grid")) {

			columnDataStyle.setBorderBottom(Border.THIN());
			columnDataStyle.setBorderTop(Border.THIN());
			columnDataStyle.setBorderLeft(Border.THIN());
			columnDataStyle.setBorderRight(Border.THIN());
		} else {
			columnDataStyle.setBorderBottom(Border.NO_BORDER());
			columnDataStyle.setBorderTop(Border.NO_BORDER());
			columnDataStyle.setBorderLeft(Border.NO_BORDER());
			columnDataStyle.setBorderRight(Border.NO_BORDER());
		}

		if (styleFunctions.contains("rowNumber")) {
			AbstractColumn numbers = ColumnBuilder.getInstance().setCustomExpression(getExpression()).build();
			numbers.setStyle(columnDataStyle);
			drb.addColumn(numbers);
		}
		Style footerStyle = new Style();
		footerStyle.setBackgroundColor(new Color(255, 255, 255));
		footerStyle.setTextColor(Color.BLACK);
		footerStyle.setHorizontalAlign(HorizontalAlign.JUSTIFY);
		footerStyle.setTransparency(Transparency.OPAQUE);
		footerStyle.setBorderTop(Border.NO_BORDER());
		for (int i = 0; i < columns.size(); i++) {
			AbstractColumn column;

			int type = e.getColumnSQLType(columns.get(i));

			String className = TypeMappingsUtils.getClassName(type);
			name = columns.get(i);
			width = 85;
			if (columnStyle != null) {
				for (int j = 0; j < columnStyle.size(); j++) {
					columnDataStyle.setHorizontalAlign(HorizontalAlign.CENTER);
					headerStyle.setHorizontalAlign(HorizontalAlign.CENTER);
					if (columnStyle.get(j).getId().equals(columns.get(i))) {
						name = columnStyle.get(j).getName();
						width = columnStyle.get(j).getWidth();
						switch (columnStyle.get(j).getAlignment()) {
						case "center":

							columnDataStyle.setHorizontalAlign(HorizontalAlign.CENTER);
							headerStyle.setHorizontalAlign(HorizontalAlign.CENTER);
							break;

						case "left":

							columnDataStyle.setHorizontalAlign(HorizontalAlign.LEFT);
							headerStyle.setHorizontalAlign(HorizontalAlign.LEFT);
							break;
						case "right":
							columnDataStyle.setHorizontalAlign(HorizontalAlign.RIGHT);
							headerStyle.setHorizontalAlign(HorizontalAlign.RIGHT);
							break;

						}
					}
				}
			}
			columnDataStyle.setVerticalAlign(VerticalAlign.MIDDLE);
			headerStyle.setVerticalAlign(VerticalAlign.MIDDLE);

			column = ColumnBuilder.getNew().setColumnProperty(columns.get(i), className).setTitle(name)
					.setWidth(new Integer(width)).setHeaderStyle(headerStyle).build();
			if (styleFunctions.contains("columnName")) {
				drb.setPrintColumnNames(true);
			} else {
				drb.setPrintColumnNames(false);
			}
			column.setFixedWidth(false);

			column.setStyle(columnDataStyle);

			if (i == 0 && (functions.contains(bundle.getString("total_text"))
					|| functions.contains(bundle.getString("total")))) {
				drb.addGlobalFooterVariable(column, DJCalculation.COUNT, footerStyle, getValueFormatter(TOTAL))
						.setGrandTotalLegend("");

			}
			for (int z = 0; z < functions.size(); z++) {
				if (functions.get(z).startsWith(columns.get(i))) {
					if (functions.get(z).endsWith(bundle.getString("sum"))) {
						drb.addGlobalFooterVariable(column, DJCalculation.SUM, footerStyle, getValueFormatter(SUM))
								.setGrandTotalLegend("");
					} else if (functions.get(z).endsWith(bundle.getString("average"))) {
						drb.addGlobalFooterVariable(column, DJCalculation.AVERAGE, footerStyle,
								getValueFormatter(AVERAGE)).setGrandTotalLegend("");
					} else if (functions.get(z).endsWith(bundle.getString("max"))) {
						drb.addGlobalFooterVariable(column, DJCalculation.HIGHEST, footerStyle, getValueFormatter(MAX))
								.setGrandTotalLegend("");
					} else if (functions.get(z).endsWith(bundle.getString("min"))) {
						drb.addGlobalFooterVariable(column, DJCalculation.LOWEST, footerStyle, getValueFormatter(MIN))
								.setGrandTotalLegend("");

					}
				}
			}
			drb.addColumn(column);
			if (groups.contains(columns.get(i))) {
				GroupBuilder gb1 = new GroupBuilder();
				DJGroup g1 = gb1.setCriteriaColumn((PropertyColumn) column).build();
				if (numberGroups == 0 && styleFunctions.contains("firstGroupNewPage")) {
					g1.setStartInNewPage(true);
				}
				if (styleFunctions.contains("hideGroupDetails")) {
					gb1.setGroupLayout(GroupLayout.EMPTY);
				} else {
					gb1.setGroupLayout(GroupLayout.VALUE_IN_HEADER);
				}

				if (styleFunctions.contains("groupNewPage")) {
					g1.setStartInNewPage(true);
				}
				drb.addGroup(g1);
				numberGroups += 1;
			}

		}

		drb.addAutoText(AutoText.AUTOTEXT_PAGE_X_OF_Y, AutoText.POSITION_FOOTER, AutoText.ALIGNMENT_CENTER);
		drb.setUseFullPageWidth(true);
		// drb.setTemplateFile(urlTemplate.toString());
		DynamicReport dr = drb.build();
		return dr;
	}

	private DJValueFormatter getValueFormatter(String type) {
		return new DJValueFormatter() {

			public Object evaluate(Object value, Map fields, Map variables, Map parameters) {
				String valor = "";
				switch (type) {
				case SUM:
					valor = bundle.getString("sum_text") + " : " + value;
					break;
				case AVERAGE:
					valor = bundle.getString("average_text") + " : " + value;
					break;
				case MAX:
					valor = bundle.getString("max_text") + " : " + value;
					break;
				case MIN:
					valor = bundle.getString("min_text") + " : " + value;
					break;
				case TOTAL:
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

	private CustomExpression getExpression() {
		return new CustomExpression() {

			public Object evaluate(Map fields, Map variables, Map parameters) {
				return variables.get("REPORT_COUNT");
			}

			public String getClassName() {
				return Integer.class.getName();
			}
		};
	}

	@Override
	public JRDataSource getDataSource(List<String> columns, String entity, String service)
			throws JRException, NoSuchFieldException, SecurityException, ClassNotFoundException {

		Map<String, Object> map = new HashMap<>();
		Object bean = this.applicationContext.getBean(service.concat("Service"));
		EntityResult e = (EntityResult) ReflectionTools.invoke(bean, entity.toLowerCase().concat("Query"), map,
				columns);
		EntityResultDataSource er = new EntityResultDataSource(e);
		return er;

	}

}
