package com.ontimize.jee.server.services.reportstore;

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.services.reportstore.EntityResultDataSource;
import com.ontimize.jee.common.services.reportstore.IDynamicJasperService;
import com.ontimize.jee.common.services.reportstore.ReportBase;
import com.ontimize.jee.common.services.reportstore.TypeMappingsUtils;
import com.ontimize.jee.common.tools.ReflectionTools;

import ar.com.fdvs.dj.domain.DynamicReport;
import ar.com.fdvs.dj.domain.ImageBanner;
import ar.com.fdvs.dj.domain.Style;
import ar.com.fdvs.dj.domain.builders.ColumnBuilder;
import ar.com.fdvs.dj.domain.builders.DynamicReportBuilder;
import ar.com.fdvs.dj.domain.builders.GroupBuilder;
import ar.com.fdvs.dj.domain.constants.GroupLayout;
import ar.com.fdvs.dj.domain.constants.Page;
import ar.com.fdvs.dj.domain.constants.VerticalAlign;
import ar.com.fdvs.dj.domain.entities.DJGroup;
import ar.com.fdvs.dj.domain.entities.columns.AbstractColumn;
import ar.com.fdvs.dj.domain.entities.columns.PropertyColumn;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;

@Service("DynamicJasperService")
@Lazy(value = true)
public class DynamicJasperService extends ReportBase implements IDynamicJasperService {

	@Autowired
	private ApplicationContext applicationContext;

	@Override
	public InputStream createReport(List<String> columns, String title, List<String> groups, String entity,
			String service) throws Exception {
		return this.generateReport(columns, title, groups, entity, service);
		// this.exportToJRXML();

	}

	public DynamicReport buildReport(List<String> columns, String title, List<String> groups, String entity,
			String service) throws Exception {
		// final String pathLogo = System.getProperty("user.dir") +
		// "/src/main/resources/logo.png";
		DynamicReportBuilder drb = new DynamicReportBuilder();
		drb.setTitle(title).setSubtitle("Ha sido generado " + new Date()).setPrintBackgroundOnOddRows(true)
				.setUseFullPageWidth(true).setUseFullPageWidth(true)
				.setPageSizeAndOrientation(Page.Page_A4_Landscape());
		// .addFirstPageImageBanner(pathLogo, 800, 50, ImageBanner.ALIGN_LEFT);

		Map<String, Object> map = new HashMap<>();
		Object bean = this.applicationContext.getBean(service.concat("Service"));
		EntityResult e = (EntityResult) ReflectionTools.invoke(bean, entity.toLowerCase().concat("Query"), map,
				columns);

		for (int i = 0; i < columns.size(); i++) {
			AbstractColumn column;

			int type = e.getColumnSQLType(columns.get(i));

			String className = TypeMappingsUtils.getClassName(type);

			column = ColumnBuilder.getNew().setColumnProperty(columns.get(i), className).setTitle(columns.get(i))
					.setWidth(new Integer(85)).build();

			drb.addColumn(column);
			if (groups.contains(columns.get(i))) {
				GroupBuilder gb1 = new GroupBuilder();
				DJGroup g1 = gb1.setCriteriaColumn((PropertyColumn) column).setGroupLayout(GroupLayout.VALUE_IN_HEADER)
						.build();

				drb.addGroup(g1);
			}
		}

		Style groupLabelStyle = new Style("groupLabel");
		groupLabelStyle.setVerticalAlign(VerticalAlign.BOTTOM);

		drb.setUseFullPageWidth(true);

		DynamicReport dr = drb.build();

		return dr;
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
