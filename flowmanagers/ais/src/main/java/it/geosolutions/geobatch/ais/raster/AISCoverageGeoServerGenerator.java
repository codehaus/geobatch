package it.geosolutions.geobatch.ais.raster;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.configuration.event.action.geoserver.GeoServerActionConfiguration;
import it.geosolutions.geobatch.flow.event.action.geoserver.GeoServerConfiguratorAction;
import it.geosolutions.geobatch.flow.event.action.geoserver.GeoServerRESTHelper;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.io.utils.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;

import org.apache.commons.io.FilenameUtils;
import org.geotools.gce.arcgrid.ArcGridFormat;
import org.geotools.gce.arcgrid.ArcGridReader;

public class AISCoverageGeoServerGenerator extends
		GeoServerConfiguratorAction<FileSystemMonitorEvent> {

	protected AISCoverageGeoServerGenerator(
			GeoServerActionConfiguration configuration) throws IOException {
		super(configuration);
	}

	public void send(final File inputDataDir, final File data,
			final String geoserverBaseURL, final String timeStamp,
			final String coverageStoreId, final String storeFilePrefix,
			final List<String> dataStyles, final String configId,
			final String defaultStyle, final Map<String, String> queryParams,
			String dataTransferMethod) throws MalformedURLException,
			FileNotFoundException {
		URL geoserverREST_URL = null;
		boolean sent = false;

		if ("DIRECT".equals(getConfiguration().getDataTransferMethod())) {
			geoserverREST_URL = new URL(geoserverBaseURL + "/rest/workspaces/"
					+ queryParams.get("namespace") + "/coveragestores/"
					+ coverageStoreId + "/file.arcgrid?" + "style="
					+ queryParams.get("style") + "&" + "wmspath="
					+ queryParams.get("wmspath"));
			sent = GeoServerRESTHelper.putBinaryFileTo(geoserverREST_URL,
					new FileInputStream(data), getConfiguration()
							.getGeoserverUID(), getConfiguration()
							.getGeoserverPWD());
		} else if ("URL".equals(getConfiguration().getDataTransferMethod())) {
			geoserverREST_URL = new URL(geoserverBaseURL + "/rest/workspaces/"
					+ queryParams.get("namespace") + "/coveragestores/"
					+ coverageStoreId + "/url.arcgrid?" + "style="
					+ queryParams.get("style") + "&" + "wmspath="
					+ queryParams.get("wmspath"));
			sent = GeoServerRESTHelper.putContent(geoserverREST_URL, data
					.toURI().toURL().toExternalForm(), getConfiguration()
					.getGeoserverUID(), getConfiguration().getGeoserverPWD());
		} else if ("EXTERNAL"
				.equals(getConfiguration().getDataTransferMethod())) {
			geoserverREST_URL = new URL(geoserverBaseURL + "/rest/workspaces/"
					+ queryParams.get("namespace") + "/coveragestores/"
					+ coverageStoreId + "/external.arcgrid?" + "style="
					+ queryParams.get("style") + "&" + "wmspath="
					+ queryParams.get("wmspath"));
			System.out.println(geoserverREST_URL);
			sent = GeoServerRESTHelper.putContent(geoserverREST_URL, data
					.toURI().toURL().toExternalForm(), getConfiguration()
					.getGeoserverUID(), getConfiguration().getGeoserverPWD());
		}

		if (sent) {
			LOGGER
					.info("ArcGrid GeoServerConfiguratorAction: coverage SUCCESSFULLY sent to GeoServer!");

			// //
			// Storing SLDs
			// //
			boolean sldsCreatedOK = true;

			for (String styleName : dataStyles) {
				File sldFile = new File(inputDataDir, "/" + configId + "/"
						+ timeStamp + "/" + styleName + ".sld");
				geoserverREST_URL = new URL(geoserverBaseURL + "/rest/styles/"
						+ styleName);

				if (GeoServerRESTHelper.putTextFileTo(geoserverREST_URL,
						new FileInputStream(sldFile), getConfiguration()
								.getGeoserverUID(), getConfiguration()
								.getGeoserverPWD())) {
					geoserverREST_URL = new URL(geoserverBaseURL
							+ "/rest/sldservice/updateLayer/" + storeFilePrefix);
					GeoServerRESTHelper.putContent(geoserverREST_URL,
							"<LayerConfig><Style>" + styleName
									+ "</Style></LayerConfig>",
							getConfiguration().getGeoserverUID(),
							getConfiguration().getGeoserverPWD());

					LOGGER
							.info("ArcGrid GeoServerConfiguratorAction: SLD SUCCESSFULLY sent to GeoServer!");
				} else {
					LOGGER
							.info("ArcGrid GeoServerConfiguratorAction: SLD was NOT sent to GeoServer!");
					sldsCreatedOK = false;
				}
			}

			// //
			// if it's all OK, set the Default SLD
			// //
			if (sldsCreatedOK) {
				geoserverREST_URL = new URL(geoserverBaseURL
						+ "/rest/sldservice/updateLayer/" + storeFilePrefix);
				GeoServerRESTHelper.putContent(geoserverREST_URL,
						"<LayerConfig><DefaultStyle>" + defaultStyle
								+ "</DefaultStyle></LayerConfig>",
						getConfiguration().getGeoserverUID(),
						getConfiguration().getGeoserverPWD());
			}
		} else {
			LOGGER
					.info("ArcGrid GeoServerConfiguratorAction: coverage was NOT sent to GeoServer due to connection errors!");
		}
	}

	public Queue<FileSystemMonitorEvent> execute(
			Queue<FileSystemMonitorEvent> events) throws Exception {

		try {
			// ////////////////////////////////////////////////////////////////////
			//
			// Initializing input variables
			//
			// ////////////////////////////////////////////////////////////////////
			// looking for file
			if (events.size() != 1)
				throw new IllegalArgumentException(
						"Wrong number of elements for this action: "
								+ events.size());
			FileSystemMonitorEvent event = events.remove();
			final String configId = configuration.getName();

			// //
			// data flow configuration and dataStore name must not be null.
			// //
			if (configuration == null) {
				LOGGER.log(Level.SEVERE, "DataFlowConfig is null.");
				throw new IllegalStateException("DataFlowConfig is null.");
			}

			// ////////////////////////////////////////////////////////////////////
			//
			// Initializing input variables
			//
			// ////////////////////////////////////////////////////////////////////
			final File workingDir = IOUtils.findLocation(configuration
					.getWorkingDirectory(), new File(
					((FileBaseCatalog) CatalogHolder.getCatalog())
							.getBaseDirectory()));

			// ////////////////////////////////////////////////////////////////////
			//
			// Checking input files.
			//
			// ////////////////////////////////////////////////////////////////////
			if ((workingDir == null) || !workingDir.exists()
					|| !workingDir.isDirectory()) {
				LOGGER.log(Level.SEVERE,
						"GeoServerDataDirectory is null or does not exist.");
				throw new IllegalStateException(
						"GeoServerDataDirectory is null or does not exist.");
			}

			if ((getConfiguration().getGeoserverURL() == null)
					|| "".equals(getConfiguration().getGeoserverURL())) {
				LOGGER.log(Level.SEVERE, "GeoServerCatalogServiceURL is null.");
				throw new IllegalStateException(
						"GeoServerCatalogServiceURL is null.");
			}

			// ////////////////////////////////////////////////////////////////////
			//
			// Creating ArcGrid coverageStore.
			//
			// ////////////////////////////////////////////////////////////////////

			// //
			// looking for file
			// //
			String inputFileName = event.getSource().getAbsolutePath();
			final String filePrefix = FilenameUtils.getBaseName(inputFileName);
			final String fileSuffix = FilenameUtils.getExtension(inputFileName);
			String storeFilePrefix = getConfiguration().getStoreFilePrefix();

			if (storeFilePrefix != null) {
				if ((filePrefix.equals(storeFilePrefix) || filePrefix
						.matches(storeFilePrefix))
						&& ("tif".equalsIgnoreCase(fileSuffix) || "tiff"
								.equalsIgnoreCase(fileSuffix))) {
				}
			} else if ("tif".equalsIgnoreCase(fileSuffix)
					|| "tiff".equalsIgnoreCase(fileSuffix)) {
				storeFilePrefix = filePrefix;
			}

			inputFileName = FilenameUtils.getName(inputFileName);
			final String coverageStoreId = FilenameUtils
					.getBaseName(inputFileName);

			// //
			// creating coverageStore
			// //
			final ArcGridFormat format = new ArcGridFormat();
			ArcGridReader coverageReader;

			// //
			// Trying to read the ArcGrid
			// //
			/**
			 * GeoServer url: "file:data/" + coverageStoreId + "/" + ascFileName
			 */
			try {
				coverageReader = (ArcGridReader) format.getReader(event
						.getSource());

				if (coverageReader == null) {
					LOGGER.log(Level.SEVERE,
							"No valid ArcGrid File found for this Data Flow!");
					throw new IllegalStateException(
							"No valid ArcGrid File found for this Data Flow!");
				}
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE,
						"No valid ArcGrid File found for this Data Flow: "
								+ e.getLocalizedMessage());
				throw new IllegalStateException(
						"No valid ArcGrid File found for this Data Flow: "
								+ e.getLocalizedMessage());
			}

			// ////////////////////////////////////////////////////////////////////
			//
			// SENDING data to GeoServer via REST protocol.
			//
			// ////////////////////////////////////////////////////////////////////
			// http://localhost:8080/geoserver/rest/coveragestores/test_cv_store/test/file.tiff
			LOGGER.info("Sending ArcGrid to GeoServer ... "
					+ getConfiguration().getGeoserverURL());
			Map<String, String> queryParams = new HashMap<String, String>();
			queryParams.put("namespace", getConfiguration()
					.getDefaultNamespace());
			queryParams.put("wmspath", getConfiguration().getWmsPath());
			queryParams.put("style", getConfiguration().getDefaultStyle());

			send(workingDir, event.getSource(), getConfiguration()
					.getGeoserverURL(), new Long(event.getTimestamp())
					.toString(), coverageStoreId, storeFilePrefix,
					getConfiguration().getStyles(), configId,
					getConfiguration().getDefaultStyle(), queryParams,
					getConfiguration().getDataTransferMethod());

			LOGGER.info("Update Last" + getConfiguration().getId()
					+ " GeoServer layer  ... "
					+ getConfiguration().getGeoserverURL());

			send(workingDir, event.getSource(), getConfiguration()
					.getGeoserverURL(), new Long(event.getTimestamp())
					.toString(), "Last" + getConfiguration().getId(),
					storeFilePrefix, getConfiguration().getStyles(), configId,
					getConfiguration().getDefaultStyle(), queryParams,
					getConfiguration().getDataTransferMethod());

			return events;

		} catch (Throwable t) {
			if (LOGGER.isLoggable(Level.SEVERE))
				LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
			return null;
		}

	}

}
