package com.ontimize.jee.report.server.services.util;

import ar.com.fdvs.dj.domain.AutoText;
import ar.com.fdvs.dj.domain.CustomExpression;
import ar.com.fdvs.dj.domain.DJCalculation;
import ar.com.fdvs.dj.domain.DJValueFormatter;
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
import ar.com.fdvs.dj.domain.entities.DJGroup;
import ar.com.fdvs.dj.domain.entities.columns.AbstractColumn;
import ar.com.fdvs.dj.domain.entities.columns.PropertyColumn;

import com.ontimize.jee.report.common.dto.StyleParamsDto;
import com.ontimize.jee.report.server.naming.DynamicJasperNaming;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class DynamicReportBuilderHelper {

	private static final Logger logger = LoggerFactory.getLogger(DynamicReportBuilderHelper.class);

	private DynamicJasperHelper dynamicJasperHelper;

	public DynamicReportBuilderHelper() {
	}

	public DynamicJasperHelper getDynamicJasperHelper() {
		return dynamicJasperHelper;
	}

	public void setDynamicJasperHelper(DynamicJasperHelper dynamicJasperHelper) {
		this.dynamicJasperHelper = dynamicJasperHelper;
	}

	public void configureTitle(final DynamicReportBuilder dynamicReportBuilder, final String title) {
		if (StringUtils.isBlank(title)) {
			logger.debug("Report title not configured because of 'subtitle' parameter is blank");
			return;
		}
		Font titleFont = new Font();
		titleFont.setBold(true);
		titleFont.setFontSize(20);

		Style titleStyle = new Style();
		titleStyle.setBackgroundColor(new Color(255, 255, 255));
		titleStyle.setTextColor(Color.BLACK);
		titleStyle.setFont(titleFont);

		dynamicReportBuilder.setTitle(title).setTitleStyle(titleStyle);
	}

	public void configureSubTitle(final DynamicReportBuilder dynamicReportBuilder, final String subtitle) {
		if (StringUtils.isBlank(subtitle)) {
			logger.debug("Report subtitle not configured because of 'subtitle' parameter is blank");
			return;
		}
		Font subtitleFont = new Font();
		subtitleFont.setFontSize(14);
		subtitleFont.setBold(true);

		Style subtitleStyle = new Style();
		subtitleStyle.setFont(subtitleFont);
		dynamicReportBuilder.setSubtitle(subtitle).setSubtitleStyle(subtitleStyle);
	}

	public void configureGenericStyles(final DynamicReportBuilder dynamicReportBuilder, final Boolean vertical,
			StyleParamsDto styleArgs, int numColumns) {

		if (Boolean.FALSE.equals(vertical)) {
			dynamicReportBuilder.setPageSizeAndOrientation(Page.Page_A4_Landscape());
		} else {
			dynamicReportBuilder.setPageSizeAndOrientation(Page.Page_A4_Portrait());
		}

		if (styleArgs != null) {
			dynamicReportBuilder.setPrintBackgroundOnOddRows(styleArgs.isBackgroundOnOddRows());

			if (styleArgs.isRowNumber()) {
				AbstractColumn numbers = ColumnBuilder.getInstance().setCustomExpression(getExpression()).build();
				Style styleNumbers = new Style();
				styleNumbers = this.getStyleGrid(styleArgs, styleNumbers);
				numbers.setStyle(styleNumbers);
				numbers.setWidth(6 * numColumns);
				numbers.setName("numbers");
				dynamicReportBuilder.addColumn(numbers);
			}
		}

		dynamicReportBuilder.setUseFullPageWidth(true).setUseFullPageWidth(true);
		dynamicReportBuilder.addAutoText(AutoText.AUTOTEXT_PAGE_X_OF_Y, AutoText.POSITION_FOOTER,
				AutoText.ALIGNMENT_CENTER);
	}

	public DJValueFormatter getFunctionValueFormatter(String type, ResourceBundle bundle) {
		return new DJValueFormatter() {

			public Object evaluate(Object value, Map fields, Map variables, Map parameters) {
				String valor = "";
				switch (type) {
				case DynamicJasperNaming.SUM:
					valor = bundle.getString("sum_text") + " : " + value;
					break;
				case DynamicJasperNaming.AVERAGE:
					valor = bundle.getString("average_text") + " : " + value;
					break;
				case DynamicJasperNaming.MAX:
					valor = bundle.getString("max_text") + " : " + value;
					break;
				case DynamicJasperNaming.MIN:
					valor = bundle.getString("min_text") + " : " + value;
					break;
				case DynamicJasperNaming.TOTAL:
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

	public DJGroup createReportGroup(final AbstractColumn column, final StyleParamsDto styleArgs,
			final int numberGroups) {
		GroupBuilder gb1 = new GroupBuilder();
		DJGroup g1 = gb1.setCriteriaColumn((PropertyColumn) column).build();
		if (numberGroups == 0 && styleArgs.isFirstGroupNewPage()) {
			g1.setStartInNewPage(true);
		}
		if (styleArgs.isHideGroupDetails()) {
			gb1.setGroupLayout(GroupLayout.EMPTY);

		} else {
			gb1.setGroupLayout(GroupLayout.VALUE_IN_HEADER);
		}

		if (styleArgs.isGroupNewPage()) {
			g1.setStartInNewPage(true);
		}
		Style groupStyle = new Style();
		groupStyle.setPaddingLeft(numberGroups * 20);
		groupStyle.setTransparent(false);
		if (numberGroups < 3) {
			groupStyle.setBackgroundColor(
					new Color(178 + (numberGroups * 26), 178 + (numberGroups * 26), 178 + (numberGroups * 26)));
		} else if (numberGroups == 3) {
			groupStyle.setBackgroundColor(new Color(249, 249, 249));
		} else {
			groupStyle.setBackgroundColor(new Color(255, 255, 255));
		}
		column.setStyle(groupStyle);
		return g1;
	}

	public Style getStyleGrid(StyleParamsDto styleArgs, Style style) {
		if (styleArgs.isGrid()) {
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

	public Style getFooterStyle() {
		Style footerStyle = new Style();
		footerStyle.setBackgroundColor(new Color(255, 255, 255));
		footerStyle.setTextColor(Color.BLACK);
		footerStyle.setHorizontalAlign(HorizontalAlign.JUSTIFY);
		footerStyle.setBorderTop(Border.THIN());
		footerStyle.setPaddingBottom(20);
		Font footerFont = new Font();
		footerFont.setBold(true);
		footerStyle.setFont(footerFont);
		return footerStyle;
	}

	public void configureReportFunction(final DynamicReportBuilder dynamicReportBuilder, final AbstractColumn column,
			final String function, final ResourceBundle bundle) {
		Style footerStyle = getFooterStyle();
		if (function.endsWith(bundle.getString("sum"))) {
			DJValueFormatter valueFormatter = getFunctionValueFormatter(DynamicJasperNaming.SUM, bundle);
			dynamicReportBuilder.addGlobalFooterVariable(column, DJCalculation.SUM, footerStyle, valueFormatter)
					.setGrandTotalLegend("");
		} else if (function.endsWith(bundle.getString("average"))) {
			DJValueFormatter valueFormatter = getFunctionValueFormatter(DynamicJasperNaming.AVERAGE, bundle);
			dynamicReportBuilder.addGlobalFooterVariable(column, DJCalculation.AVERAGE, footerStyle, valueFormatter)
					.setGrandTotalLegend("");
		} else if (function.endsWith(bundle.getString("max"))) {
			DJValueFormatter valueFormatter = getFunctionValueFormatter(DynamicJasperNaming.MAX, bundle);
			dynamicReportBuilder.addGlobalFooterVariable(column, DJCalculation.HIGHEST, footerStyle, valueFormatter)
					.setGrandTotalLegend("");
		} else if (function.endsWith(bundle.getString("min"))) {
			DJValueFormatter valueFormatter = getFunctionValueFormatter(DynamicJasperNaming.MIN, bundle);
			dynamicReportBuilder.addGlobalFooterVariable(column, DJCalculation.LOWEST, footerStyle, valueFormatter)
					.setGrandTotalLegend("");

		}
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
}
