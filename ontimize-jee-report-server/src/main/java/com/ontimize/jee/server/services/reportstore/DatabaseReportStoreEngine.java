package com.ontimize.jee.server.services.reportstore;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
//import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.ontimize.jee.common.db.SQLStatementBuilder.SQLOrder;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.common.services.reportstore.AdvancedEntityResultDataSource;
import com.ontimize.jee.common.services.reportstore.BasicReportDefinition;
import com.ontimize.jee.common.services.reportstore.EntityResultDataSource;
import com.ontimize.jee.common.services.reportstore.IReportDefinition;
import com.ontimize.jee.common.services.reportstore.ReportOutputType;
import com.ontimize.jee.common.services.reportstore.ReportParameter;
import com.ontimize.jee.common.services.reportstore.ReportStoreException;
import com.ontimize.jee.common.tools.CheckingTools;
import com.ontimize.jee.common.tools.PathTools;
import com.ontimize.jee.common.tools.ReflectionTools;
import com.ontimize.jee.common.util.remote.BytesBlock;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;
import com.ontimize.jee.server.requestfilter.OntimizeServletFilter;
import com.ontimize.jee.server.services.reportstore.dao.IReportDao;
import com.ontimize.jee.server.services.reportstore.dao.IReportParameterDao;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRGroup;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

public class DatabaseReportStoreEngine implements IReportStoreEngine, ApplicationContextAware, InitializingBean {
	
	/** The Constant logger. */
	private static final Logger					logger							= LoggerFactory.getLogger(DatabaseReportStoreEngine.class);

	/** The Constant ERROR_UPDATING_REPORT. */
	private static final String					ERROR_UPDATING_REPORT			= "E_UPDATING_REPORT";

	/** The Constant ERROR_ADDING_REPORT. */
	private static final String					ERROR_ADDING_REPORT				= "E_ADDING_REPORT";

	/** The Constant ERROR_NO_REPORT_DEFINITION. */
	private static final String					ERROR_NO_REPORT_DEFINITION		= "E_NO_REPORT_DEFINITION";

	/** The Constant ERROR_GETTING_REPORT_SOURCE. */
	private static final String					ERROR_GETTING_REPORT_SOURCE		= "E_GETTING_REPORT_SOURCE";

	/** The Constant ERROR_REPORT_ID_NOT_EXISTS. */
	private static final String					ERROR_REPORT_ID_NOT_EXISTS		= "E_REPORT_ID_NOT_EXISTS";

	/** The Constant ERROR_NO_REPORT_KEY. */
	private static final String					ERROR_NO_REPORT_KEY				= "E_NO_REPORT_KEY";

	/** The Constant ZIP_EXTENSION. */
	private static final String					ZIP_EXTENSION					= ".zip";

	/** The Constant PREFIX. */
	private static final String					PREFIX							= "rep";

	/** The Constant TMP_PREFIX. */
	private static final String					TMP_PREFIX						= "OJEE_RTMP";
	
	/** The Constant JASPER. */
	private static final String					JASPER							= ".jasper";
	
	/** The bundle. */
	private ResourceBundle						bundle;

	/** The report compiler. */
	private IReportCompiler						reportCompiler;

	/** The report filler. */
	private IReportFiller						reportFiller;

	/** The application context. */
	private ApplicationContext					applicationContext;
	
	/** The Ontimize DAO helper. */
	@Autowired
	private DefaultOntimizeDaoHelper daoHelper;
	
	/** The report entity DAO. */
	@Autowired
	private IReportDao reportDao;
	
	/** The report parameter entity DAO. */
	@Autowired
	private IReportParameterDao	reportParameterDao;

	/**
	 * Instantiates a new file report store.
	 */
	public DatabaseReportStoreEngine() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.ontimize.jee.server.services.reportstore.IReportStoreService#addReport(com.ontimize.jee.common.services.reportstore.IReportDefinition, java.io.InputStream)
	 */
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	@Override
	public void addReport(IReportDefinition rDef, InputStream reportSource) throws ReportStoreException {
		Path reportFolder = null;
		try {
			CheckingTools.failIfNull(rDef, DatabaseReportStoreEngine.ERROR_NO_REPORT_DEFINITION);
			CheckingTools.failIfNull(rDef.getId(), DatabaseReportStoreEngine.ERROR_NO_REPORT_KEY);

			Map<String, Object> attrMap = this.parseReportAttributes(rDef);
			byte[] zip = IOUtils.toByteArray(reportSource);
			attrMap.put("ZIP", zip);
			this.daoHelper.insert(this.reportDao, attrMap);
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
			throw new ReportStoreException(DatabaseReportStoreEngine.ERROR_ADDING_REPORT, ex);
		}
	}

	private void safeCreateDirectories(Path dirToCreate) throws IOException {
		try {
			Files.createDirectories(dirToCreate);
		} catch (IOException ex) {
			// Some error in recursive directories creation, try to create one by one
			DatabaseReportStoreEngine.logger.warn("E_BUILDING_BASE_DIRECTORIES__TRYING_AGAIN_ONE_BY_ONE", ex);
			List<Path> pathsInOrder = this.getPathsHierarchy(dirToCreate);
			for (Path path : pathsInOrder) {
				if (!Files.exists(path)) {
					try {
						Files.createDirectory(path);
					} catch (IOException ex2) {
						DatabaseReportStoreEngine.logger.warn("E_BUILDING_BASE_DIRECTORY__" + path, ex2);
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
	@Transactional
	@Override
	public void updateReportDefinition(IReportDefinition rDef) throws ReportStoreException {
		CheckingTools.failIfNull(rDef, DatabaseReportStoreEngine.ERROR_NO_REPORT_DEFINITION);
		CheckingTools.failIfNull(rDef.getId(), DatabaseReportStoreEngine.ERROR_NO_REPORT_KEY);
		
		Map<String, Object> keyMap = new HashMap<String, Object>();
		List<String> attrList = new ArrayList<String> ();
		
		keyMap.put("UUID", rDef.getId());
		attrList.add("ID");
		EntityResult res = this.daoHelper.query(this.reportDao, keyMap, attrList);
		Integer id = (Integer) ((ArrayList<?>) res.get("ID")).get(0);
		
		keyMap.clear();
		keyMap.put("ID", id);
		Map<String, Object> attrValues = new HashMap<String, Object>();
		attrValues = this.parseReportAttributes(rDef);
		this.daoHelper.update(this.reportDao, attrValues, keyMap);
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
			CheckingTools.failIfNull(reportId, DatabaseReportStoreEngine.ERROR_NO_REPORT_KEY);

			// Delete old files
			Path reportFolder = this.getReportFolder(reportId);
			reportFile = this.getReportFile(reportFolder, reportId);
			CheckingTools.failIf(!Files.exists(reportFolder), DatabaseReportStoreEngine.ERROR_REPORT_ID_NOT_EXISTS);
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
				DatabaseReportStoreEngine.logger.error(null, e);
			}
			throw new ReportStoreException(DatabaseReportStoreEngine.ERROR_UPDATING_REPORT, ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.ontimize.jee.server.services.reportstore.IReportStoreService#removeReport(java.lang.Object)
	 */
	@Override
	public void removeReport(Object reportId) throws ReportStoreException {
		CheckingTools.failIfNull(reportId, DatabaseReportStoreEngine.ERROR_NO_REPORT_KEY);
		
		Map<String, Object> keyMap = new HashMap<String, Object>();
		List<String> attrList = new ArrayList<String> ();
		
		keyMap.put("UUID", reportId);
		attrList.add("ID");
		EntityResult res = this.daoHelper.query(this.reportDao, keyMap, attrList);
		Integer id = (Integer) ((ArrayList<?>) res.get("ID")).get(0);
		
		// Check if there's parameters for this reportId (FK restriction)
		keyMap.clear();
		keyMap.put("REPORT_ID", id);
		res = this.daoHelper.query(this.reportParameterDao, keyMap, attrList);
		if (!res.entrySet().isEmpty()) {
			Integer size = ((ArrayList<?>) res.get("ID")).size();
			for (int i=0; i<size; i++) {
				Integer paramId = (Integer) ((ArrayList<?>) res.get("ID")).get(i);
				keyMap.clear();
				keyMap.put("ID", paramId);
				this.daoHelper.delete(this.reportParameterDao, keyMap);
			}
		}
		
		// Remove the report
		keyMap.clear();
		keyMap.put("ID", id);
		this.daoHelper.delete(this.reportDao, keyMap);
		PathTools.deleteFolderSafe(this.getReportFolder(reportId));
	}

	/*
	 * (non-Javadoc)
	 * @see com.ontimize.jee.server.services.reportstore.IReportStoreService#getReportDefinition(java.lang.Object)
	 */
	@Override
	public IReportDefinition getReportDefinition(Object reportId) throws ReportStoreException {
		CheckingTools.failIfNull(reportId, DatabaseReportStoreEngine.ERROR_NO_REPORT_KEY);
		
		Map<String, Object> keyMap = new HashMap<String, Object>();
		List<String> attrList = new ArrayList<String> ();
		
		// Retrieve report data
		attrList.add("ID");
		attrList.add("UUID");
		attrList.add("NAME");
		attrList.add("DESCRIPTION");
		attrList.add("REPORT_TYPE");
		attrList.add("MAIN_REPORT_FILENAME");
		keyMap.put("UUID", reportId);
		EntityResult res = this.daoHelper.query(this.reportDao, keyMap, attrList);
		IReportDefinition rDef = this.parseReportEntityResult(res).iterator().next();
		
		// Retrieve report parameters data
		Integer id = (Integer) ((ArrayList<?>) res.get("ID")).get(0);
		attrList.clear();
		keyMap.clear();
		attrList.add("NAME");
		attrList.add("DESCRIPTION");
		attrList.add("NESTED_TYPE");
		attrList.add("VALUE_CLASS");
		keyMap.put("REPORT_ID", id);
		res = this.daoHelper.query(this.reportParameterDao, keyMap, attrList);
		if (!res.entrySet().isEmpty()) {
			List<ReportParameter> params = this.parseReportParameterEntityResult(res);
			for (int i=0; i<params.size(); i++) {
				if (params.get(i).getName().equalsIgnoreCase("service") 
						|| params.get(i).getName().equalsIgnoreCase("entity")
						|| params.get(i).getName().equalsIgnoreCase("pagesize")) {
					params.remove(i);
					i--;
				}
			}
			rDef.setParameters(params);
		}
			
		return rDef;
	}

	/*
	 * (non-Javadoc)
	 * @see com.ontimize.jee.server.services.reportstore.IReportStoreService#getReportSource(java.lang.Object)
	 */
	@Override
	public InputStream getReportSource(Object reportId) throws ReportStoreException {
		try {
			CheckingTools.failIfNull(reportId, DatabaseReportStoreEngine.ERROR_NO_REPORT_KEY);
			Path reportFolder = this.getReportFolder(reportId);
			Path reportFile = this.getReportFile(reportFolder, reportId);
			CheckingTools.failIf(!Files.exists(reportFile), DatabaseReportStoreEngine.ERROR_REPORT_ID_NOT_EXISTS);
			return Files.newInputStream(reportFile);
		} catch (IOException ex) {
			throw new ReportStoreException(DatabaseReportStoreEngine.ERROR_GETTING_REPORT_SOURCE, ex);
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

			final Path temporaryFile = Files.createTempFile(DatabaseReportStoreEngine.TMP_PREFIX, DatabaseReportStoreEngine.ZIP_EXTENSION);
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
				DatabaseReportStoreEngine.logger.error(null, ex);
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
		List<String> attrList = new ArrayList<String> ();
		attrList.add("UUID");
		attrList.add("NAME");
		attrList.add("DESCRIPTION");
		attrList.add("REPORT_TYPE");
		attrList.add("MAIN_REPORT_FILENAME");
		Map<String, Object> keyMap = new HashMap<String, Object>();
		
		EntityResult res = this.daoHelper.query(this.reportDao, keyMap, attrList);
		
		return this.parseReportEntityResult(res);
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
		
		try {
			// Retrieve the compiled report from the database (REPORT.COMPILED)
			Map<String, Object> keyMap = new HashMap<String, Object>();
			keyMap.put("UUID", reportId);
			List<String> attrList = new ArrayList<String> ();
			attrList.add("COMPILED");
			EntityResult res = this.daoHelper.query(this.reportDao, keyMap, attrList);
			
			// Parse the byte array to JasperReport object
			BytesBlock bytesBlock = (BytesBlock) ((ArrayList<?>) res.get("COMPILED")).get(0);
			byte[] byteArray = bytesBlock.getBytes();
			InputStream compiledReport = new ByteArrayInputStream(byteArray);	
			JasperReport jasperReport = (JasperReport)JRLoader.loadObject(compiledReport);

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
				return this.reportFiller.fillReport(this.getReportDefinition(reportId), jasperReport, reportParameters, outputType, otherType, this.getBundle(),
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
			
		} catch (ReportStoreException error) {
			throw error;
		} catch (Exception ex) {
			throw new ReportStoreException(ex);
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
			// Create temporary folder for the report
			Path reportFolder = this.getReportFolder(reportId);
			CheckingTools.failIf((reportFolder == null) || !Files.exists(reportFolder), DatabaseReportStoreEngine.ERROR_REPORT_ID_NOT_EXISTS);
			this.safeCreateDirectories(reportFolder);
			
			Path reportFile = this.getReportFile(reportFolder, reportId);
			CheckingTools.failIf((reportFile == null), DatabaseReportStoreEngine.ERROR_REPORT_ID_NOT_EXISTS);
			
			// Retrieve the report zip folder from the database (REPORT.ZIP)
			Map<String, Object> keyMap = new HashMap<String, Object>();
			keyMap.put("UUID", rDef.getId());
			List<String> attrList = new ArrayList<String> ();
			attrList.add("ID");
			attrList.add("ZIP");
			EntityResult res = new EntityResultMapImpl();
			res = this.daoHelper.query(this.reportDao, keyMap, attrList);
			keyMap.clear();
			
			// Store the report zip folder in temporary folder
//			BytesBlock zipBlock = (BytesBlock) ((Vector<?>) res.get("ZIP")).get(0);
			BytesBlock zipBlock = (BytesBlock) ((ArrayList<?>) res.get("ZIP")).get(0);
			byte[] zip = zipBlock.getBytes();
			InputStream is = new ByteArrayInputStream(zip);
			try (OutputStream os = Files.newOutputStream(reportFile)) {
				IOUtils.copy(is, os);
			}
			CheckingTools.failIf(!Files.exists(reportFile), DatabaseReportStoreEngine.ERROR_GETTING_REPORT_SOURCE);

			// Create temporary folder for the compiled report
			compileFolder = this.getReportCompiledFolder(reportFolder, reportId);
			if (!Files.exists(compileFolder)) {
				this.safeCreateDirectories(compileFolder);
			} else if (!Files.isDirectory(compileFolder)) {
				Files.delete(compileFolder);
			} else {
				PathTools.deleteFolderContent(compileFolder);
			}

			// Compile the report
			rDef = this.reportCompiler.compile(reportFile, compileFolder, rDef);
			
			// Store the compiled report in the database (REPORT.COMPILED)
			Map<String, Object> attrMap = new HashMap<String, Object>();
			Integer id = (Integer) ((ArrayList<?>) res.get("ID")).get(0);
			keyMap.put("ID", id);
			byte[] compiledReport = Files.readAllBytes(compileFolder.resolve(rDef.getMainReportFileName() + DatabaseReportStoreEngine.JASPER));
			attrMap.put("COMPILED", compiledReport);
			this.daoHelper.update(this.reportDao, attrMap, keyMap);
			attrMap.clear();
			
			// Parse the report parameters and store them in the database (REPORT_PARAMETER)
			List<ReportParameter> params = rDef.getParameters();
			for (int i=0; i<params.size(); i++) {
				attrMap = this.parseReportParameterAttributes(params.get(i));
				attrMap.put("REPORT_ID", id);
				this.daoHelper.insert(this.reportParameterDao, attrMap);
			}
		} catch (Exception ex) {
			try {
				PathTools.deleteFolderSafe(compileFolder);
			} catch (Exception ex2) {
				logger.warn("", ex2);
			}
			throw new ReportStoreException("E_COMPILING_REPORT", ex);
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
		try {
			String tDir = System.getProperty("java.io.tmpdir").concat(DatabaseReportStoreEngine.PREFIX + reportId.toString());
			Path path = Paths.get(tDir);
			if (!Files.exists(path))
				return Files.createDirectory(path);
			else return path;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
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
		return reportFolder.resolve(DatabaseReportStoreEngine.PREFIX + reportId.toString() + DatabaseReportStoreEngine.ZIP_EXTENSION);
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
	
	private Path getReportCompiledFolder(Path reportFolder, Object reportId) {
		return reportFolder.resolve("compiled");
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
	
	/**
	 * Parses the report attributes.
	 *
	 * @param rDef
	 *            the report definition object
	 * @return the report attributes map (key, value)
	 */
	private Map<String, Object> parseReportAttributes(IReportDefinition rDef) {
		Map<String, Object> attrMap = new HashMap<String, Object>();
		attrMap.put("UUID", rDef.getId());
		attrMap.put("NAME", rDef.getName());
		attrMap.put("DESCRIPTION", rDef.getDescription());
		attrMap.put("REPORT_TYPE", rDef.getType());
		attrMap.put("MAIN_REPORT_FILENAME", rDef.getMainReportFileName());
		return attrMap;
	}
	
	/**
	 * Parses the report parameter attributes.
	 *
	 * @param param
	 *            the report parameter definition object
	 * @return the report parameter attributes map (key, value)
	 */
	private Map<String, Object> parseReportParameterAttributes(ReportParameter param) {
		Map<String, Object> attrMap = new HashMap<String, Object>();
		attrMap.put("NAME", param.getName());
		attrMap.put("DESCRIPTION", param.getDescription());
		attrMap.put("NESTED_TYPE", param.getType());
		attrMap.put("VALUE_CLASS", param.getValueClass());
		return attrMap;
	}
	
	/**
	 * Parses the result to a collection of report definition objects.
	 *
	 * @param res
	 *            the report EntityResult
	 * @return the report definition object collection
	 */
	private Collection<IReportDefinition> parseReportEntityResult(EntityResult res) {
		final Collection<IReportDefinition> reportList = new ArrayList<>();
		IReportDefinition rDef;
		String uuid, name, description, type, mainReportFilename;
		Integer size = ((ArrayList<?>) res.get("UUID")).size();
		for (int i=0; i<size; i++) {
			uuid = (String) ((ArrayList<?>) res.get("UUID")).get(i);
			name = (String) ((ArrayList<?>) res.get("NAME")).get(i);
			description = (String) ((ArrayList<?>) res.get("DESCRIPTION")).get(i);
			type = (String) ((ArrayList<?>) res.get("REPORT_TYPE")).get(i);
			mainReportFilename = (String) ((ArrayList<?>) res.get("MAIN_REPORT_FILENAME")).get(i);
			rDef = new BasicReportDefinition(uuid, name, description, type, mainReportFilename);
			reportList.add(rDef);
		}
		return reportList;
	}
	
	/**
	 * Parses the result to a list of report parameter definition objects.
	 *
	 * @param res
	 *            the report parameter EntityResult
	 * @return the report parameter definition object list
	 */
	private List<ReportParameter> parseReportParameterEntityResult(EntityResult res) {
		final List<ReportParameter> paramList = new ArrayList<>();
		ReportParameter param;
		String name, description, nestedType, valueClass;
		Integer size = ((ArrayList<?>) res.get("NAME")).size();
		for (int i=0; i<size; i++) {
			name = (String) ((ArrayList<?>) res.get("NAME")).get(i);
			description = (String) ((ArrayList<?>) res.get("DESCRIPTION")).get(i);
			valueClass = (String) ((ArrayList<?>) res.get("VALUE_CLASS")).get(i);
			nestedType = (String) ((ArrayList<?>) res.get("NESTED_TYPE")).get(i);
			
			param = new ReportParameter(name, description, valueClass, nestedType);
			paramList.add(param);
		}
		return paramList;
	}

	@Override
	public void updateSettings() {
		// TODO Auto-generated method stub
		
	}

}
