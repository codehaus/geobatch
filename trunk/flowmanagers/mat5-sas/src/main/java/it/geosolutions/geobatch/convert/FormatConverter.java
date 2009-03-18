/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.codehaus.org/
 *  Copyright (C) 2007-2008-2009 GeoSolutions S.A.S.
 *  http://www.geo-solutions.it
 *
 *  GPLv3 + Classpath exception
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.geosolutions.geobatch.convert;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.flow.event.action.Action;
import it.geosolutions.geobatch.flow.event.action.BaseAction;

import java.awt.RenderingHints;
import java.io.File;
import java.io.IOException;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;

import org.apache.commons.io.FilenameUtils;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.coverage.grid.io.UnknownFormat;
import org.geotools.coverage.grid.io.imageio.GeoToolsWriteParams;
import org.geotools.factory.Hints;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;

/**
 * Comments here ...
 * 
 * @author Simone Giannechini, GeoSolutions
 */
public class FormatConverter extends BaseAction<FileSystemMonitorEvent>
		implements Action<FileSystemMonitorEvent> {

	private FormatConverterConfiguration configuration;

	private final static Logger LOGGER = Logger.getLogger(FormatConverter.class
			.toString());

	// TODO: Temporarly set to public
	public FormatConverter(FormatConverterConfiguration configuration)
			throws IOException {
		this.configuration = configuration;
	}

	public Queue<FileSystemMonitorEvent> execute(
			Queue<FileSystemMonitorEvent> events) throws Exception {
		try {

			// TODO: TEMP solution
			JAI.getDefaultInstance().getTileCache().setMemoryCapacity(
					512 * 1024 * 1024);
			JAI.getDefaultInstance().getTileCache().setMemoryThreshold(1.0f);
			JAI.getDefaultInstance().getTileScheduler().setParallelism(8);
			JAI.getDefaultInstance().getTileScheduler().setPrefetchParallelism(
					8);
			JAI.getDefaultInstance().getTileScheduler().setPrefetchPriority(5);
			JAI.getDefaultInstance().getTileScheduler().setPriority(5);

			// TODO: Refactor this allowing empty queues
			// looking for file
			// if (events.size() != 1)
			// throw new IllegalArgumentException(
			// "Wrong number of elements for this action: "
			// + events.size());

			// //
			//
			// data flow configuration and dataStore name must not be null.
			//
			// //
			if (configuration == null) {
				LOGGER.log(Level.SEVERE, "DataFlowConfig is null.");
				throw new IllegalStateException("DataFlowConfig is null.");
			}

			// get the first event
			// final FileSystemMonitorEvent event = events.peek();
			// final File inputFile = event.getSource();
			final String inputFormats = configuration.getInputFormats();
			final String[] inputExtensions;
			if (inputFormats != null) {
				inputExtensions = inputFormats.split(":");
			} else {
				inputExtensions = null;
			}

			final String directory = configuration.getWorkingDirectory();
			final int tileW = configuration.getTileW();
			final int tileH = configuration.getTileH();

			File fileDir = new File(directory);
			if (fileDir != null && fileDir.isDirectory()) {
				final File files[] = fileDir.listFiles();
				if (files != null) {
					final int numFiles = files.length;
					for (int i = 0; i < numFiles; i++) {
						final File file = files[i];
						final String path = file.getAbsolutePath()
								.toLowerCase();
						if (inputExtensions != null) {
							boolean accepted = false;
							for (String ext : inputExtensions) {
								if (path.endsWith(ext)) {
									accepted = true;
									break;
								}
							}
							if (!accepted)
								continue;
						}

						// get a reader
						final String parent = file.getParent();
						final String name = FilenameUtils.getBaseName(path);
						final String fileOutputName = new StringBuilder(parent)
								.append("/").append(name).append(".tif")
								.toString();
						// Preparing an useful layout in case the image is
						// striped.
						final ImageLayout l = new ImageLayout();
						l.setTileGridXOffset(0).setTileGridYOffset(0)
								.setTileHeight(512).setTileWidth(512);

						Hints hints = new Hints();
						hints.add(new RenderingHints(JAI.KEY_IMAGE_LAYOUT, l));

						// //
						// Acquire proper format and reader
						// //
						GridFormatFinder.scanForPlugins();
						final AbstractGridFormat gridFormat = (AbstractGridFormat) GridFormatFinder
								.findFormat(file);
						if (gridFormat != null
								&& !(gridFormat instanceof UnknownFormat)) {
							final GridCoverageReader reader = gridFormat
									.getReader(file);

							
							GridCoverage2D gc = (GridCoverage2D) reader
									.read(null);

							final String outputFormatType = configuration
									.getOutputFormat();
							
							// //
							// Acquire required writer
							// //
							final AbstractGridFormat writerFormat = (AbstractGridFormat) acquireFormatByType(outputFormatType);

							if (writerFormat != null) {
								GridCoverageWriter writer = writerFormat
										.getWriter(new File(fileOutputName));
								
								GeoToolsWriteParams params = null;
								ParameterValueGroup wparams = null;
								try{
									wparams = writerFormat.getWriteParameters();
									params = writerFormat.getDefaultImageIOWriteParameters();
								}catch (UnsupportedOperationException uoe){
									params = null;
									wparams = null;
								}
								if (params!=null){
									params.setTilingMode(GeoToolsWriteParams.MODE_EXPLICIT);
									params.setTiling(tileW,tileH);
					    			wparams.parameter(
					    					AbstractGridFormat.GEOTOOLS_WRITE_PARAMS.getName()
					    							.toString()).setValue(params);
								}

								writer.write(gc, wparams!=null?(GeneralParameterValue[]) wparams.values().toArray(new GeneralParameterValue[1]):null);
								writer.dispose();
								gc.dispose(true);
								reader.dispose();
							}
						}
					}
				}
			}
			return events;
		} catch (Throwable t) {
			if (LOGGER.isLoggable(Level.SEVERE))
				LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
			return null;
		}

	}

	public ActionConfiguration getConfiguration() {
		return configuration;
	}

	public static Format acquireFormatByType(String type) {
		final Format[] formats = GridFormatFinder.getFormatArray();
		Format format = null;
		final int length = formats.length;

		for (int i = 0; i < length; i++) {
			format = formats[i];

			if (format.getName().equals(type)) {
				return format;
			}
		}

		return null;
	}
}
