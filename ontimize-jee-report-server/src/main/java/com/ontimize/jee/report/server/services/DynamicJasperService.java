package com.ontimize.jee.report.server.services;

import java.awt.Color;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import com.ontimize.jee.common.db.SQLStatementBuilder;
import com.ontimize.jee.report.common.dto.FunctionParamsDto;
import com.ontimize.jee.report.common.dto.OrderByDto;
import com.ontimize.jee.report.common.dto.ReportParamsDto;
import com.ontimize.jee.report.common.dto.ServiceRendererDto;
import com.ontimize.jee.report.common.exception.DynamicReportException;
import com.ontimize.jee.report.server.services.util.DynamicJasperHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.report.common.dto.ColumnStyleParamsDto;
import com.ontimize.jee.report.common.util.EntityResultDataSource;
import com.ontimize.jee.report.common.services.IDynamicJasperService;
import com.ontimize.jee.report.common.util.TypeMappingsUtils;
import com.ontimize.jee.common.tools.ReflectionTools;

import ar.com.fdvs.dj.domain.AutoText;
import ar.com.fdvs.dj.domain.CustomExpression;
import ar.com.fdvs.dj.domain.DJCalculation;
import ar.com.fdvs.dj.domain.DJValueFormatter;
import ar.com.fdvs.dj.domain.DynamicReport;
import ar.com.fdvs.dj.domain.Style;
import ar.com.fdvs.dj.domain.builders.ColumnBuilder;
import ar.com.fdvs.dj.domain.builders.DynamicReportBuilder;
import ar.com.fdvs.dj.domain.builders.GroupBuilder;
import ar.com.fdvs.dj.domain.constants.Border;
import ar.com.fdvs.dj.domain.constants.Font;
import ar.com.fdvs.dj.domain.constants.GroupLayout;
import ar.com.fdvs.dj.domain.constants.HorizontalAlign;
import ar.com.fdvs.dj.domain.constants.Page;
import ar.com.fdvs.dj.domain.constants.Transparency;
import ar.com.fdvs.dj.domain.constants.VerticalAlign;
import ar.com.fdvs.dj.domain.entities.DJGroup;
import ar.com.fdvs.dj.domain.entities.columns.AbstractColumn;
import ar.com.fdvs.dj.domain.entities.columns.PropertyColumn;
import net.sf.jasperreports.engine.JRDataSource;

@Service("DynamicJasperService")
@Lazy(value = true)
public class DynamicJasperService extends ReportBase implements IDynamicJasperService {
	/** The Constant MAX. */
	private static final String MAX = "MAX";
	/** The Constant MIN. */
	private static final String MIN = "MIN";
	/** The Constant SUM. */
	private static final String SUM = "SUM";
	/** The Constant AVERAGE. */
	private static final String AVERAGE = "AVERAGE";
	/** The Constant TOTAL. */
	private static final String TOTAL = "TOTAL";

	private ResourceBundle bundle;

	@Autowired
	private ApplicationContext applicationContext;
	private DynamicJasperHelper dynamicJasperHelper;
	
	@Override
	public InputStream createReport(final ReportParamsDto param) throws DynamicReportException {

		if (StringUtils.isBlank(param.getService())) {
			throw new DynamicReportException("Report cannot be created, 'service' parameter not found!");
		}
		if (StringUtils.isBlank(param.getEntity())) {
			throw new DynamicReportException("Report cannot be created, 'entity' parameter not found!");
		}
		if (param.getColumns() == null || param.getColumns().isEmpty()) {
			throw new DynamicReportException("Report cannot be created, 'columns' parameter not found!");
		}

		return this.generateReport(param.getColumns(), param.getTitle(), param.getGroups(), param.getEntity(),
				param.getService(), param.getOrientation(), param.getFunctions(), param.getStyleFunctions(),
				param.getSubtitle(), param.getColumnStyle(), param.getOrderBy(), param.getLanguage(), param.getServicRenderer());
	}

	@Override
	public List<String> getFunctionsName(final FunctionParamsDto params) throws DynamicReportException {
		try {

			String entity = params.getEntity();
			String service = params.getService();
			List<String> columns = params.getColumns();

			List<String> functions = new ArrayList<>();
			Map<String, Object> map = new HashMap<>();
			Object bean = this.applicationContext.getBean(service.concat("Service"));
			EntityResult e = (EntityResult) ReflectionTools.invoke(bean, entity.toLowerCase().concat("Query"), map,
					columns);
			for (String column : columns) {
				int type = e.getColumnSQLType(column);
				String className = TypeMappingsUtils.getClassName(type);
				if (TypeMappingsUtils.INTEGER_PATH.equals(className)
						|| TypeMappingsUtils.DOUBLE_PATH.equals(className)) {
					functions.add(column);
				}
			}
			return functions;
		} catch (Exception ex) {
			throw new DynamicReportException("Impossible to retrieve function names", ex);
		}
	}

	public DynamicReport buildReport(List<String> columns, String title, List<String> groups, String entity,
			String service, String orientation, List<String> functions, List<String> styleFunctions, String subtitle,
			List<ColumnStyleParamsDto> columnStyle, String language, List<ServiceRendererDto> serviceRendererList) throws DynamicReportException {
		int numberGroups = 0;
		String name = "";
		String id = "";
		int width;
		ResourceBundle bundle = getBundle(language);

		DynamicReportBuilder drb = new DynamicReportBuilder();
		Style titleStyle = new Style();
		Style subtitleStyle = new Style();
		Font titleFont = new Font();
		titleFont.setBold(true);
		titleFont.setFontSize(20);
		Font subtitleFont = new Font();
		subtitleFont.setFontSize(14);
		subtitleFont.setBold(true);
		titleStyle.setBackgroundColor(new Color(255, 255, 255));
		titleStyle.setTextColor(Color.BLACK);
		titleStyle.setFont(titleFont);
		subtitleStyle.setFont(subtitleFont);
		drb.setTitle(title).setSubtitle(subtitle).setPrintBackgroundOnOddRows(false).setUseFullPageWidth(true)
				.setUseFullPageWidth(true).setTitleStyle(titleStyle).setSubtitleStyle(subtitleStyle);
		if (orientation.equals("horizontal")) {
			drb.setPageSizeAndOrientation(Page.Page_A4_Landscape());
		} else {
			drb.setPageSizeAndOrientation(Page.Page_A4_Portrait());
		}
		

		if (styleFunctions.contains("backgroundOnOddRows")) {
			drb.setPrintBackgroundOnOddRows(true);
		}

		if (styleFunctions.contains("rowNumber")) {
			AbstractColumn numbers = ColumnBuilder.getInstance().setCustomExpression(getExpression()).build();
			Style styleNumbers = new Style();
			styleNumbers = getStyleGrid(styleFunctions, styleNumbers);
			numbers.setStyle(styleNumbers);
			numbers.setWidth(6 * columns.size());
			numbers.setName("numbers");
			drb.addColumn(numbers);
		}
		Style footerStyle = new Style();
		footerStyle.setBackgroundColor(new Color(255, 255, 255));
		footerStyle.setTextColor(Color.BLACK);
		footerStyle.setHorizontalAlign(HorizontalAlign.JUSTIFY);
		footerStyle.setTransparency(Transparency.OPAQUE);
		footerStyle.setBorderTop(Border.NO_BORDER());

		this.dynamicJasperHelper = new DynamicJasperHelper(this.applicationContext);
		Map<String, String> columnClassnames = this.dynamicJasperHelper.getColumnClassnames(service, entity, columns, serviceRendererList);

		for (int i = 0; i < columnStyle.size(); i++) {
			AbstractColumn column;
			Style columnDataStyle = new Style();
			columnDataStyle = getStyleGrid(styleFunctions, columnDataStyle);
			columnDataStyle.setTransparent(false);
			columnDataStyle.setBackgroundColor(new Color(255, 255, 255));

			Style headerStyle = new Style();
			headerStyle.setBorderBottom(Border.THIN());
			Font headerFont = new Font();
			headerFont.setBold(true);
			headerStyle.setFont(headerFont);
			headerStyle.setPaddingBottom(-10);
			ColumnStyleParamsDto columnStyleParamsDto = columnStyle.get(i);
						
			id = columnStyleParamsDto.getId();
			name = columnStyleParamsDto.getName();
			width = columnStyleParamsDto.getWidth();
			String className = columnClassnames.get(id); 

			switch (columnStyleParamsDto.getAlignment()) {
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

			columnDataStyle.setVerticalAlign(VerticalAlign.MIDDLE);
			headerStyle.setVerticalAlign(VerticalAlign.MIDDLE);

			column = ColumnBuilder.getNew().setColumnProperty(columns.get(i), className).setTitle(name).setWidth(width)
					.setHeaderStyle(headerStyle).build();
			column.setName(id);
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

		}
		for (int z = 0; z < groups.size(); z++) {
			for (int i = 0; i < drb.getColumns().size(); i++) {
				if (groups.get(z).compareTo(drb.getColumn(i).getName()) == 0) {
					GroupBuilder gb1 = new GroupBuilder();
					DJGroup g1 = gb1.setCriteriaColumn((PropertyColumn) drb.getColumn(i)).build();
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
					Style groupStyle = new Style();
					groupStyle.setPaddingLeft(numberGroups * 20);
					groupStyle.setTransparent(false);
					if (numberGroups < 3) {
						groupStyle.setBackgroundColor(new Color(178 + (numberGroups * 26), 178 + (numberGroups * 26),
								178 + (numberGroups * 26)));
					} else if (numberGroups == 3) {
						groupStyle.setBackgroundColor(new Color(249, 249, 249));
					} else {
						groupStyle.setBackgroundColor(new Color(255, 255, 255));
					}
					drb.getColumn(i).setStyle(groupStyle);
					drb.addGroup(g1);
					numberGroups += 1;
				}
			}

		}
		drb.addAutoText(AutoText.AUTOTEXT_PAGE_X_OF_Y, AutoText.POSITION_FOOTER, AutoText.ALIGNMENT_CENTER);
		drb.setUseFullPageWidth(true);
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
	public JRDataSource getDataSource(List<String> columns, List<String> groups, List<OrderByDto> orderBy,
			String entity, String service, final List<ServiceRendererDto> serviceRendererList) throws SecurityException {

		Map<String, Object> map = new HashMap<>();
		Integer pageSize = Integer.MAX_VALUE;
		Integer offset = 0;
		boolean order = false;
		List<SQLStatementBuilder.SQLOrder> sqlOrders = new ArrayList<>();
		// If there are group columns, it is necessary to add to order by to allow
		// jasper engine perform grouping well...
		if (groups != null && !groups.isEmpty()) {
			for (String col : groups) {
				if (orderBy != null && !orderBy.isEmpty()) {

					OrderByDto orderByDto = orderBy.stream().filter(item -> item.getColumnId().equals(col))
							.findFirst().orElse(null);
					if (orderByDto != null) {
						SQLStatementBuilder.SQLOrder sqlO = new SQLStatementBuilder.SQLOrder(col,
								orderByDto.isAscendent());
						sqlOrders.add(sqlO);
						continue;
					}
				}
				SQLStatementBuilder.SQLOrder sqlO = new SQLStatementBuilder.SQLOrder(col);
				sqlOrders.add(sqlO);

			}
		}
		// Second, add the rest of the columns to orderBy
		if (orderBy != null && !orderBy.isEmpty()) {
			orderBy.stream().filter(ord -> !groups.contains(ord.getColumnId())).forEach(ord -> {
				SQLStatementBuilder.SQLOrder sqlOrder = new SQLStatementBuilder.SQLOrder(ord.getColumnId(),
						ord.isAscendent());
				sqlOrders.add(sqlOrder);
			});
		}

		Object bean = this.applicationContext.getBean(service.concat("Service"));
		EntityResult erReportData = (EntityResult) ReflectionTools.invoke(bean, entity.toLowerCase().concat("PaginationQuery"),
				map, columns, pageSize, offset, sqlOrders);

		EntityResultDataSource entityResultDataSource = new EntityResultDataSource(erReportData);
		if(serviceRendererList != null && !serviceRendererList.isEmpty()) {
			dynamicJasperHelper.evaluateServiceRenderer(entityResultDataSource, serviceRendererList);
		}
		
		return entityResultDataSource;

	}
	
	protected Style getStyleGrid(List<String> styleFunctions, Style style) {
		if (styleFunctions.contains("grid")) {

			style.setBorderBottom(Border.THIN());
			style.setBorderTop(Border.THIN());
			style.setBorderLeft(Border.THIN());
			style.setBorderRight(Border.THIN());
		} else {
			style.setBorderBottom(Border.NO_BORDER());
			style.setBorderTop(Border.NO_BORDER());
			style.setBorderLeft(Border.NO_BORDER());
			style.setBorderRight(Border.NO_BORDER());
		}
		return style;
	}

	protected ResourceBundle getBundle(final String language) {
		if (this.bundle == null) {
			Locale locale = null;
			String lang0 = "en";
			if (!StringUtils.isEmpty(language)) {
				lang0 = language;
			}
			switch (lang0) {
			case "es":
				locale = new Locale("es", "ES");
				break;
			case "gl":
				locale = new Locale("gl", "ES");
				break;
			default:
				locale = new Locale("en", "US");
				break;
			}
			bundle = ResourceBundle.getBundle("bundle/bundle", locale);
		}
		return this.bundle;
	}

}