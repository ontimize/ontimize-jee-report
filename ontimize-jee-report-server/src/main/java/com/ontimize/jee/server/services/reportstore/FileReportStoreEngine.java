package com.ontimize.jee.server.services.reportstore;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.ontimize.db.EntityResult;
import com.ontimize.db.SQLStatementBuilder.SQLOrder;
import com.ontimize.jee.common.services.reportstore.AdvancedEntityResultDataSource;
import com.ontimize.jee.common.services.reportstore.BasicReportDefinition;
import com.ontimize.jee.common.services.reportstore.EntityResultDataSource;
import com.ontimize.jee.common.services.reportstore.IReportDefinition;
import com.ontimize.jee.common.services.reportstore.ReportOutputType;
import com.ontimize.jee.common.services.reportstore.ReportParameter;
import com.ontimize.jee.common.services.reportstore.ReportStoreException;
import com.ontimize.jee.common.spring.parser.AbstractPropertyResolver;
import com.ontimize.jee.common.tools.CheckingTools;
import com.ontimize.jee.common.tools.MapTools;
import com.ontimize.jee.common.tools.PathTools;
import com.ontimize.jee.common.tools.ReflectionTools;
import com.ontimize.jee.server.requestfilter.OntimizeServletFilter;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRGroup;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

/**
 * The Class FileReportStore.
 */
public class FileReportStoreEngine implements IReportStoreEngine, ApplicationContextAware, InitializingBean {

	/** The Constant logger. */
	private static final Logger					logger							= LoggerFactory.getLogger(FileReportStoreEngine.class);

	/** The Constant ERROR_LISTING_REPORTS. */
	private static final String					ERROR_LISTING_REPORTS			= "E_LISTING_REPORTS";

	/** The Constant ERROR_GETTING_REPORT_DEFINITION. */
	private static final String					ERROR_GETTING_REPORT_DEFINITION	= "E_GETTING_REPORT_DEFINITION";

	/** The Constant ERROR_REMOVING_REPORT. */
	private static final String					ERROR_REMOVING_REPORT			= "E_REMOVING_REPORT";

	/** The Constant ERROR_UPDATING_REPORT. */
	private static final String					ERROR_UPDATING_REPORT			= "E_UPDATING_REPORT";

	/** The Constant ERROR_ADDING_REPORT. */
	private static final String					ERROR_ADDING_REPORT				= "E_ADDING_REPORT";

	/** The Constant ERROR_REPORT_ID_ALREADY_EXISTS. */
	private static final String					ERROR_REPORT_ID_ALREADY_EXISTS	= "E_REPORT_ID_ALREADY_EXISTS";

	/** The Constant ERROR_NO_REPORT_DEFINITION. */
	private static final String					ERROR_NO_REPORT_DEFINITION		= "E_NO_REPORT_DEFINITION";

	/** The Constant ERROR_GETTING_REPORT_SOURCE. */
	private static final String					ERROR_GETTING_REPORT_SOURCE		= "E_GETTING_REPORT_SOURCE";

	/** The Constant ERROR_REPORT_ID_NOT_EXISTS. */
	private static final String					ERROR_REPORT_ID_NOT_EXISTS		= "E_REPORT_ID_NOT_EXISTS";

	/** The Constant ERROR_NO_REPORT_KEY. */
	private static final String					ERROR_NO_REPORT_KEY				= "E_NO_REPORT_KEY";

	/** The Constant PROPERTY_TYPE. */
	private static final String					PROPERTY_TYPE					= "type";

	/** The Constant PROPERTY_DESCRIPTION. */
	private static final String					PROPERTY_DESCRIPTION			= "description";

	/** The Constant PROPERTY_NAME. */
	private static final String					PROPERTY_NAME					= "name";

	/** The Constant PROPERTY_ID. */
	private static final String					PROPERTY_ID						= "id";

	/** The Constant PROPERTY_ID. */
	private static final String					PROPERTY_MAINREPORTFILENAME		= "mainreportfilename";
	
	/** The Constant PROPERTY_PARAMETERS. */
	private static final String					PROPERTY_PARAMETERS				= "parameters";

	/** The Constant PROPERTY_EXTRA_PREFIX. */
	private static final String					PROPERTY_EXTRA_PREFIX			= "extra.";

	/** The Constant ZIP_EXTENSION. */
	private static final String					ZIP_EXTENSION					= ".zip";

	/** The Constant PREFIX. */
	private static final String					PREFIX							= "rep";

	/** The Constant PROP_EXTENSION. */
	private static final String					PROP_EXTENSION					= ".properties";

	/** The Constant TMP_PREFIX. */
	private static final String					TMP_PREFIX						= "OJEE_RTMP";

	/** The bundle. */
	private ResourceBundle						bundle;

	/** The base path. */
	private Path								basePath;
	protected AbstractPropertyResolver<String>	basePathResolver;

	/** The report compiler. */
	private IReportCompiler						reportCompiler;

	/** The report filler. */
	private IReportFiller						reportFiller;

	/** The application context. */
	private ApplicationContext					applicationContext;

	/**
	 * Instantiates a new file report store.
	 */
	public FileReportStoreEngine() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.ontimize.jee.server.services.reportstore.IReportStoreService#addReport(com.ontimize.jee.common.services.reportstore.IReportDefinition, java.io.InputStream)
	 */
	@Override
	public void addReport(IReportDefinition rDef, InputStream reportSource) throws ReportStoreException {
		Path reportFolder = null;
		try {
			CheckingTools.failIfNull(rDef, FileReportStoreEngine.ERROR_NO_REPORT_DEFINITION);
			CheckingTools.failIfNull(rDef.getId(), FileReportStoreEngine.ERROR_NO_REPORT_KEY);
			reportFolder = this.getReportFolder(rDef.getId());
			Path reportFile = this.getReportFile(reportFolder, rDef.getId());
			CheckingTools.failIf(Files.exists(reportFile), FileReportStoreEngine.ERROR_REPORT_ID_ALREADY_EXISTS);

			this.safeCreateDirectories(reportFolder);

			try (OutputStream os = Files.newOutputStream(reportFile)) {
				IOUtils.copy(reportSource, os);
			}

			this.saveReportProperties(reportFolder, rDef);
			this.compileReport(rDef.getId(), rDef);

		} catch (ReportStoreException error) {
			PathTools.deleteFolderSafe(reportFolder);
			throw error;
		} catch (Exception ex) {
			try {
				PathTools.deleteFolderSafe(reportFolder);
			} catch (Exception ex2) {
				logger.warn("", ex2);
			}
			throw new ReportStoreException(FileReportStoreEngine.ERROR_ADDING_REPORT, ex);
		}
	}

	private void safeCreateDirectories(Path dirToCreate) throws IOException {
		try {
			Files.createDirectories(dirToCreate);
		} catch (IOException ex) {
			// Some error in recursive directories creation, try to create one by one
			FileReportStoreEngine.logger.warn("E_BUILDING_BASE_DIRECTORIES__TRYING_AGAIN_ONE_BY_ONE", ex);
			List<Path> pathsInOrder = this.getPathsHierarchy(dirToCreate);
			for (Path path : pathsInOrder) {
				if (!Files.exists(path)) {
					try {
						Files.createDirectory(path);
					} catch (IOException ex2) {
						FileReportStoreEngine.logger.warn("E_BUILDING_BASE_DIRECTORY__" + path, ex2);
					}
				} else if (!Files.isDirectory(path)) {
					throw new IOException("E_INVALID_DIRECTORY__" + path);
				}
			}
		}
		if (!Files.exists(dirToCreate) || !Files.isDirectory(dirToCreate)) {
			throw new IOException("E_BUILDING_DIRECTORY__" + dirToCreate);
		}
	}

	private List<Path> getPathsHierarchy(Path basePath) {
		List<Path> paths = new ArrayList<>();
		Path current = basePath;
		while (current != null) {
			paths.add(0, current);
			current = current.getParent();
		}
		return paths;
	}

	/*
	 * (non-Javadoc)
	 * @see com.ontimize.jee.server.services.reportstore.IReportStoreService#updateReportDefinition(com.ontimize.jee.common.services.reportstore. IReportDefinition)
	 */
	@Override
	public void updateReportDefinition(IReportDefinition rDef) throws ReportStoreException {
		try {
			CheckingTools.failIfNull(rDef, FileReportStoreEngine.ERROR_NO_REPORT_DEFINITION);
			CheckingTools.failIfNull(rDef.getId(), FileReportStoreEngine.ERROR_NO_REPORT_KEY);
			Path reportFolder = this.getReportFolder(rDef.getId());
			Path reportPropertiesFile = this.getReportPropertiesFile(reportFolder, rDef.getId());
			CheckingTools.failIf(!Files.exists(reportPropertiesFile), FileReportStoreEngine.ERROR_REPORT_ID_NOT_EXISTS);
			this.saveReportProperties(reportFolder, rDef);
		} catch (IOException ex) {
			throw new ReportStoreException(FileReportStoreEngine.ERROR_UPDATING_REPORT, ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.ontimize.jee.server.services.reportstore.IReportStoreService#updateReportSource(java.lang.Object, java.io.InputStream)
	 */
	@Override
	public void updateReportSource(Object reportId, InputStream reportSource) throws ReportStoreException {
		// To ensure to save previous version until be sure this oepration finish successfully
		Path reportFile = null;
		Path tempOldReportFile = null;
		try {
			CheckingTools.failIfNull(reportId, FileReportStoreEngine.ERROR_NO_REPORT_KEY);

			// Delete old files
			Path reportFolder = this.getReportFolder(reportId);
			reportFile = this.getReportFile(reportFolder, reportId);
			CheckingTools.failIf(!Files.exists(reportFolder), FileReportStoreEngine.ERROR_REPORT_ID_NOT_EXISTS);
			tempOldReportFile = reportFile.resolveSibling(reportFile.getFileName() + "_old");
			Files.move(reportFile, tempOldReportFile);

			// Put new file
			try (OutputStream os = Files.newOutputStream(reportFile)) {
				IOUtils.copy(reportSource, os);
			}

			PathTools.deleteFileSafe(tempOldReportFile);

			// Recompile new file
			this.compileReport(reportId, null);
		} catch (IOException ex) {
			try {
				// Restore previous version
				if ((reportFile != null) && (tempOldReportFile != null)) {
					PathTools.deleteFileSafe(reportFile);
					Files.move(tempOldReportFile, reportFile);
				}
			} catch (IOException e) {
				FileReportStoreEngine.logger.error(null, e);
			}
			throw new ReportStoreException(FileReportStoreEngine.ERROR_UPDATING_REPORT, ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.ontimize.jee.server.services.reportstore.IReportStoreService#removeReport(java.lang.Object)
	 */
	@Override
	public void removeReport(Object reportId) throws ReportStoreException {
		try {
			CheckingTools.failIfNull(reportId, FileReportStoreEngine.ERROR_NO_REPORT_KEY);
			Path reportFolder = this.getReportFolder(reportId);
			CheckingTools.failIf(!Files.exists(reportFolder), FileReportStoreEngine.ERROR_REPORT_ID_NOT_EXISTS);
			PathTools.deleteFolder(reportFolder);
		} catch (IOException ex) {
			throw new ReportStoreException(FileReportStoreEngine.ERROR_REMOVING_REPORT, ex);
		}

	}

	/*
	 * (non-Javadoc)
	 * @see com.ontimize.jee.server.services.reportstore.IReportStoreService#getReportDefinition(java.lang.Object)
	 */
	@Override
	public IReportDefinition getReportDefinition(Object reportId) throws ReportStoreException {
		try {
			CheckingTools.failIfNull(reportId, FileReportStoreEngine.ERROR_NO_REPORT_KEY);
			Path reportFolder = this.getReportFolder(reportId);
			CheckingTools.failIf(!Files.exists(reportFolder), FileReportStoreEngine.ERROR_REPORT_ID_NOT_EXISTS);
			IReportDefinition rDef = this.loadReportProperties(reportFolder, reportId);
			List<ReportParameter> params = rDef.getParameters();
			for (int i=0; i<params.size(); i++) {
				if (params.get(i).getName().equalsIgnoreCase("service") 
						|| params.get(i).getName().equalsIgnoreCase("entity")
						|| params.get(i).getName().equalsIgnoreCase("pagesize")) {
					params.remove(i);
					i--;
				}
			}
			rDef.setParameters(params);
			return rDef;
		} catch (IOException ex) {
			throw new ReportStoreException(FileReportStoreEngine.ERROR_GETTING_REPORT_DEFINITION, ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.ontimize.jee.server.services.reportstore.IReportStoreService#getReportSource(java.lang.Object)
	 */
	@Override
	public InputStream getReportSource(Object reportId) throws ReportStoreException {
		try {
			CheckingTools.failIfNull(reportId, FileReportStoreEngine.ERROR_NO_REPORT_KEY);
			Path reportFolder = this.getReportFolder(reportId);
			Path reportFile = this.getReportFile(reportFolder, reportId);
			CheckingTools.failIf(!Files.exists(reportFile), FileReportStoreEngine.ERROR_REPORT_ID_NOT_EXISTS);
			return Files.newInputStream(reportFile);
		} catch (IOException ex) {
			throw new ReportStoreException(FileReportStoreEngine.ERROR_GETTING_REPORT_SOURCE, ex);
		}
	}

	/**
	 * Gets the report compiled.
	 *
	 * @param reportId
	 *            the report id
	 * @return the report compiled in zip format
	 * @throws ReportStoreException
	 *             the report store exception
	 */
	@Override
	public InputStream getReportCompiled(Object reportId) throws ReportStoreException {
		try {
			// ensure everything is ok
			this.getReportDefinition(reportId);

			Path reportCompiledFolder = this.getReportCompiledFolder(reportId);

			final Path temporaryFile = Files.createTempFile(FileReportStoreEngine.TMP_PREFIX, FileReportStoreEngine.ZIP_EXTENSION);
			ZipFile zipFile = new ZipFile(temporaryFile.toFile());
			ZipParameters parameters = new ZipParameters();
			// set compression method to store compression
			parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
			// Set the compression level
			parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
			final ArrayList<File> sourceFileList = new ArrayList<>();
			try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(reportCompiledFolder)) {
				for (Path path : directoryStream) {
					sourceFileList.add(path.toFile());
				}
			} catch (IOException ex) {
				FileReportStoreEngine.logger.error(null, ex);
			}

			zipFile.addFiles(sourceFileList, parameters);
			return Files.newInputStream(temporaryFile, StandardOpenOption.DELETE_ON_CLOSE);
		} catch (ReportStoreException error) {
			throw error;
		} catch (Exception ex) {
			throw new ReportStoreException(ex);
		}

	}

	/*
	 * (non-Javadoc)
	 * @see com.ontimize.jee.server.services.reportstore.IReportStoreService#listAllReports()
	 */
	@Override
	public Collection<IReportDefinition> listAllReports() throws ReportStoreException {
		try {
			final Collection<IReportDefinition> res = new ArrayList<>();
			Path reportFolder = this.getBasePath();
			if (!Files.exists(reportFolder)) {
				return res;
			}
			Files.walkFileTree(reportFolder, new SimpleFileVisitor<Path>() {

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if (file.getFileName().toString().startsWith(FileReportStoreEngine.PREFIX) && file.getFileName().toString().endsWith(FileReportStoreEngine.PROP_EXTENSION)) {
						IReportDefinition rDef = FileReportStoreEngine.this.loadReportProperties(file);
						res.add(rDef);
					}
					return FileVisitResult.CONTINUE;
				}
			});
			return res;
		} catch (IOException ex) {
			throw new ReportStoreException(FileReportStoreEngine.ERROR_LISTING_REPORTS, ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.ontimize.jee.server.services.reportstore.IReportStoreService#listReportsOfType(java.lang.String)
	 */
	@Override
	public Collection<IReportDefinition> listReportsOfType(String type) throws ReportStoreException {
		List<IReportDefinition> reports = new ArrayList<>(this.listAllReports());
		ListIterator<IReportDefinition> listIterator = reports.listIterator();
		while (listIterator.hasNext()) {
			IReportDefinition current = listIterator.next();
			if (!type.equals(current.getType())) {
				listIterator.remove();
			}
		}
		return reports;
	}

	/*
	 * (non-Javadoc)
	 * @see com.ontimize.jee.server.services.reportstore.IReportStoreService#fillReport(java.lang.Object, java.util.Map,
	 * com.ontimize.jee.server.services.reportstore.ReportOutputType, java.lang.String)
	 */
	@Override
	public InputStream fillReport(Object reportId, Map<String, Object> reportParameters, String dataSourceName, ReportOutputType outputType, String otherType)
			throws ReportStoreException {
		String service = null;
		String entity = null;
		Integer index;
		Integer pagesize = null;
		IReportDefinition rDef = this.getReportDefinition(reportId);
		Path compiled = this.getReportCompiledFolder(reportId);
		try {
			Path compiledReport = compiled.resolve(rDef.getMainReportFileName().concat(".jasper"));
			InputStream is = Files.newInputStream(compiledReport);
			JasperReport jasperReport = (JasperReport)JRLoader.loadObject(is);
			
			// Check special parameters (service, entity, pagesize)
			JRParameter[] params = jasperReport.getParameters();
			for (int i=0; i<params.length; i++) {
	        	if (params[i].isForPrompting() && !params[i].isSystemDefined()) {
	        		if (params[i].getName().equalsIgnoreCase("service")) {
	        			index = params[i].getDefaultValueExpression().getText().length() - 1;
	    				service = params[i].getDefaultValueExpression().getText().substring(1, index);
	        		} else if (params[i].getName().equalsIgnoreCase("entity")) {
	        			index = params[i].getDefaultValueExpression().getText().length() - 1;
	    				entity = params[i].getDefaultValueExpression().getText().substring(1, index);
	        		}
	        		if (params[i].getName().equalsIgnoreCase("pagesize")) {
	        			index = params[i].getDefaultValueExpression().getText().length() - 1;
	        			pagesize = Integer.valueOf(params[i].getDefaultValueExpression().getText().substring(1, index));
	        		}
	        	}
	        }
			
			// Fill the report
			if (service == null) {
				return this.reportFiller.fillReport(this.getReportDefinition(reportId), this.getReportCompiledFolder(reportId), reportParameters, outputType, otherType, this.getBundle(),
						this.getLocale(), dataSourceName);
			} else if (entity != null) {
				StringBuffer buffer = new StringBuffer();
				List<Object> attributes = new ArrayList<>();
				for (JRField field : jasperReport.getFields()) {
					attributes.add(field.getName());
				}
				Object bean = this.applicationContext.getBean(service);
				if (pagesize == null) {
					buffer.append(entity).append("Query");
					EntityResult entityResult = (EntityResult) ReflectionTools.invoke(bean, buffer.toString(), reportParameters, attributes);
					EntityResultDataSource ods = new EntityResultDataSource(entityResult);
					return this.reportFiller.fillReport(this.getReportDefinition(reportId), jasperReport, reportParameters, outputType, otherType, this.getBundle(),
							this.getLocale(), ods);
				} else {
					buffer.append(entity).append("AdvancedQuery");
					List<SQLOrder> order = new ArrayList<SQLOrder>();
					JRGroup[] group = jasperReport.getGroups();
					if (group != null) {
						SQLOrder o;
						for (int i=0; i<group.length; i++) {
							index = group[i].getExpression().getText().length() - 1;
							o = new SQLOrder(group[i].getExpression().getText().substring(3, index));
							order.add(o);
						}
					}
					AdvancedEntityResultDataSource ods = new AdvancedEntityResultDataSource(bean, buffer.toString(), reportParameters,
							attributes, pagesize, 0, order);
					return this.reportFiller.fillReport(this.getReportDefinition(reportId), jasperReport, reportParameters, outputType, otherType, this.getBundle(),
							this.getLocale(), ods);
				}
			}
		} catch (JRException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.ontimize.jee.server.services.reportstore.IReportStoreService#fillReport(java.lang.Object, java.lang.String, java.util.Map,
	 * com.ontimize.jee.server.services.reportstore.ReportOutputType, java.lang.String)
	 */
	@Override
	public InputStream fillReport(Object reportId, String serviceName, Map<String, Object> reportParameters, String dataSourceName, ReportOutputType outputType, String otherType)
			throws ReportStoreException {
		IReportAdapter adapter = this.applicationContext.getBean(IReportAdapter.class, serviceName);
		return this.reportFiller.fillReport(this.getReportDefinition(reportId), this.getReportCompiledFolder(reportId), adapter, reportParameters, outputType, otherType,
				this.getBundle(), this.getLocale(), dataSourceName);
	}

	@Override
	public void compileReport(Object reportId, IReportDefinition rDef) throws ReportStoreException {
		Path compileFolder = null;
		try {
			Path reportFolder = this.getReportFolder(reportId);
			CheckingTools.failIf((reportFolder == null) || !Files.exists(reportFolder), FileReportStoreEngine.ERROR_REPORT_ID_NOT_EXISTS);
			Path reportFile = this.getReportFile(reportFolder, reportId);
			CheckingTools.failIf((reportFile == null) || !Files.exists(reportFile), FileReportStoreEngine.ERROR_REPORT_ID_NOT_EXISTS);

			compileFolder = this.getReportCompiledFolder(reportId);
			if (!Files.exists(compileFolder)) {
				this.safeCreateDirectories(compileFolder);
			} else if (!Files.isDirectory(compileFolder)) {
				Files.delete(compileFolder);
			} else {
				PathTools.deleteFolderContent(compileFolder);
			}

			rDef = this.reportCompiler.compile(reportFile, compileFolder, rDef);
			this.updateReportDefinition(rDef);
		} catch (Exception ex) {
			try {
				PathTools.deleteFolderSafe(compileFolder);
			} catch (Exception ex2) {
				logger.warn("", ex2);
			}
			throw new ReportStoreException(FileReportStoreEngine.ERROR_ADDING_REPORT, ex);
		}
	}

	/**
	 * Gets the report folder.
	 *
	 * @param reportId
	 *            the report id
	 * @return the report folder
	 */
	private Path getReportFolder(Object reportId) {
		return this.getBasePath().resolve(FileReportStoreEngine.PREFIX + reportId.toString());
	}

	/**
	 * Gets the report properties file.
	 *
	 * @param reportFolder
	 *            the report folder
	 * @param reportId
	 *            the report id
	 * @return the report properties file
	 */
	private Path getReportPropertiesFile(Path reportFolder, Object reportId) {
		return reportFolder.resolve(FileReportStoreEngine.PREFIX + reportId.toString() + FileReportStoreEngine.PROP_EXTENSION);
	}

	/**
	 * Gets the report file.
	 *
	 * @param reportFolder
	 *            the report folder
	 * @param reportId
	 *            the report id
	 * @return the report file
	 */
	private Path getReportFile(Path reportFolder, Object reportId) {
		return reportFolder.resolve(FileReportStoreEngine.PREFIX + reportId.toString() + FileReportStoreEngine.ZIP_EXTENSION);
	}

	/**
	 * Gets the report compiled folder.
	 *
	 * @param reportId
	 *            the report id
	 * @return the report compiled folder
	 */
	private Path getReportCompiledFolder(Object reportId) {
		return this.getReportFolder(reportId).resolve("compiled");
	}

	/**
	 * Gets the bundle.
	 *
	 * @return the bundle
	 */
	public ResourceBundle getBundle() {
		return this.bundle;
	}

	/**
	 * Sets the bundle.
	 *
	 * @param bundle
	 *            the new bundle
	 */
	public void setBundle(ResourceBundle bundle) {
		this.bundle = bundle;
	}

	/**
	 * Sets the resource bundle.
	 *
	 * @param bundle
	 *            the new resource bundle
	 */
	public void setResourceBundle(ResourceBundle bundle) {
		this.bundle = bundle;
	}

	/**
	 * Gets the locale.
	 *
	 * @return the locale
	 */
	private Locale getLocale() {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		Locale locale = (Locale) request.getAttribute(OntimizeServletFilter.LOCALE);
		if (locale == null) {
			return Locale.getDefault();
		}
		return locale;
	}

	/**
	 * Save report properties.
	 *
	 * @param reportFolder
	 *            the report folder
	 * @param rDef
	 *            the r def
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void saveReportProperties(Path reportFolder, IReportDefinition rDef) throws IOException {
		Path reportPropertiesFile = this.getReportPropertiesFile(reportFolder, rDef.getId());
		Properties prop = new Properties();
		MapTools.safePut(prop, FileReportStoreEngine.PROPERTY_ID, rDef.getId().toString());
		MapTools.safePut(prop, FileReportStoreEngine.PROPERTY_NAME, rDef.getName());
		MapTools.safePut(prop, FileReportStoreEngine.PROPERTY_DESCRIPTION, rDef.getDescription());
		MapTools.safePut(prop, FileReportStoreEngine.PROPERTY_TYPE, rDef.getType());
		MapTools.safePut(prop, FileReportStoreEngine.PROPERTY_MAINREPORTFILENAME, rDef.getMainReportFileName());
		MapTools.safePut(prop, FileReportStoreEngine.PROPERTY_PARAMETERS, rDef.getParameters().toString());
		if (rDef.getOtherInfo() != null) {
			for (Entry<String, String> entry : rDef.getOtherInfo().entrySet()) {
				MapTools.safePut(prop, FileReportStoreEngine.PROPERTY_EXTRA_PREFIX + entry.getKey(), entry.getValue());
			}
		}
		try (OutputStream os = Files.newOutputStream(reportPropertiesFile)) {
			prop.store(os, " Report Properties ");
		}
	}

	/**
	 * Load report properties.
	 *
	 * @param reportFolder
	 *            the report folder
	 * @param reportId
	 *            the report id
	 * @return the i report definition
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private IReportDefinition loadReportProperties(Path reportFolder, Object reportId) throws IOException {
		Path reportPropertiesFile = this.getReportPropertiesFile(reportFolder, reportId);
		return this.loadReportProperties(reportPropertiesFile);
	}

	/**
	 * Load report properties.
	 *
	 * @param reportPropertiesFile
	 *            the report properties file
	 * @return the i report definition
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private IReportDefinition loadReportProperties(Path reportPropertiesFile) throws IOException {
		Properties prop = new Properties();
		try (InputStream is = Files.newInputStream(reportPropertiesFile)) {
			prop.load(is);
		}
		
		String params = prop.getProperty(FileReportStoreEngine.PROPERTY_PARAMETERS);
		List<ReportParameter> reportParams = this.parseParameters(params);
		
		BasicReportDefinition reportDefinition = new BasicReportDefinition(
				prop.getProperty(FileReportStoreEngine.PROPERTY_ID),
				prop.getProperty(FileReportStoreEngine.PROPERTY_NAME),
				prop.getProperty(FileReportStoreEngine.PROPERTY_DESCRIPTION),
				prop.getProperty(FileReportStoreEngine.PROPERTY_TYPE),
				prop.getProperty(FileReportStoreEngine.PROPERTY_MAINREPORTFILENAME),
				reportParams);
		reportDefinition.setMainReportFileName(prop.getProperty(FileReportStoreEngine.PROPERTY_MAINREPORTFILENAME));
		
		for (Entry<Object, Object> entry : prop.entrySet()) {
			String key = (String) entry.getKey();
			if (key.startsWith(FileReportStoreEngine.PROPERTY_EXTRA_PREFIX)) {
				String value = (String) entry.getValue();
				reportDefinition.addOtherInfo(key.substring(FileReportStoreEngine.PROPERTY_EXTRA_PREFIX.length()), value);
			}
		}
		return reportDefinition;
	}

	@Override
	public void updateSettings() {
		FileReportStoreEngine.logger.debug("Updating settings...");
		if (this.basePathResolver != null) {
			Path resolvedValue = this.getResolverValue(this.basePathResolver);
			CheckingTools.failIfNull(resolvedValue, "Report store base path is not configured.");
			FileReportStoreEngine.logger.info("Base path changed from \"{}\" to \"{}\"", this.basePath, resolvedValue);
			this.basePath = resolvedValue;
		} else {
			FileReportStoreEngine.logger.warn("Report store base path resolver is not configured.");
		}
	}

	/**
	 * Gets the documents base path.
	 *
	 * @return the documents base path
	 */
	public Path getBasePath() {
		CheckingTools.failIfNull(this.basePath, "Report store base path is not configured.");
		return this.basePath;
	}

	/**
	 * Gets the resolver value.
	 *
	 * @param resolver
	 *            the resolver
	 * @return the resolver value
	 */
	protected Path getResolverValue(AbstractPropertyResolver<String> resolver) {
		if (resolver != null) {
			try {
				return Paths.get(resolver.getValue());
			} catch (Exception ex) {
				FileReportStoreEngine.logger.error(null, ex);
			}
		}
		return null;
	}

	/**
	 * Sets the report store base path.
	 *
	 * @param basePath
	 *            the report store base path
	 */
	public void setBasePath(final String basePath) {
		FileReportStoreEngine.logger.info("Base path changed from \"{}\" to \"{}\"", this.basePath, basePath);
		this.basePath = Paths.get(basePath);
		this.basePathResolver = new AbstractPropertyResolver<String>() {

			@Override
			public String getValue() {
				return basePath;
			}
		};
	}

	/**
	 *
	 * @return
	 */
	public AbstractPropertyResolver<String> getBasePathResolver() {
		return this.basePathResolver;
	}

	/**
	 * Sets the documents base path resolver.
	 *
	 * @param basePathResolver
	 *            the base path resolver
	 */
	public void setBasePathResolver(AbstractPropertyResolver<String> basePathResolver) {
		this.basePathResolver = basePathResolver;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// Load defaults if not configured
		if (this.reportCompiler == null) {
			this.reportCompiler = new JasperReportsCompilerFiller();
		}
		if (this.reportFiller == null) {
			if (this.reportCompiler instanceof IReportFiller) {
				this.reportFiller = (IReportFiller) this.reportCompiler;
			} else {
				this.reportFiller = new JasperReportsCompilerFiller();
			}
		}

		// Propague context (is required?!?)
		if (this.reportCompiler instanceof ApplicationContextAware) {
			((ApplicationContextAware) this.reportCompiler).setApplicationContext(this.applicationContext);
		}
		if (this.reportFiller instanceof ApplicationContextAware) {
			((ApplicationContextAware) this.reportFiller).setApplicationContext(this.applicationContext);
		}

		this.updateSettings();
	}
	
	private List<ReportParameter> parseParameters(String params) {
		String name, description, valueClass, type, param; 
		String [] paramArray;
		int start, end;
		List<ReportParameter> paramList = new ArrayList<ReportParameter>();
		ReportParameter rp;
		//params = "[ReportParameter [..], ReportParameter [..], ...]"
		
		params = params.substring(1, params.length() - 1);
		//params = "ReportParameter [..], ReportParameter [..], ..."
		
		while (params.contains("ReportParameter")) {
			start = params.indexOf("[");
			end = params.indexOf("]", start);
			if (start<0 || end<0)
				break;
			param = params.substring(start + 1, end);
			//param = "name=.., description=.., valueClass=.., type=.."
			paramArray = param.split("\\,");
			for (int i=0; i<paramArray.length; i++) {
				paramArray[i] = paramArray[i].trim();
			}
			name = paramArray[0].split("\\=")[1];
			description = paramArray[1].split("\\=")[1];
			valueClass = paramArray[2].split("\\=")[1];
			type = paramArray[3].split("\\=")[1];
			
			rp = new ReportParameter(name, description, valueClass, type);
			paramList.add(rp);
			
			params = params.substring(end);
		}
		
		return paramList;
	}

}
