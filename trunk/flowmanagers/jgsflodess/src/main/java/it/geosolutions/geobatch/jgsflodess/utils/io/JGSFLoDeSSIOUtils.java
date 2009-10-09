/**
 * 
 */
package it.geosolutions.geobatch.jgsflodess.utils.io;

import it.geosolutions.imageio.plugins.netcdf.NetCDFUtilities;
import it.geosolutions.utils.coamps.data.FlatFileGrid;

import java.awt.image.ColorModel;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.media.jai.PlanarImage;
import javax.media.jai.RasterFactory;
import javax.media.jai.TiledImage;
import javax.vecmath.GMatrix;

import org.apache.commons.io.FilenameUtils;
import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.io.AbstractGridCoverageWriter;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.imageio.GeoToolsWriteParams;
import org.geotools.factory.Hints;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffWriteParams;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;

import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;

import com.ice.tar.TarEntry;
import com.ice.tar.TarEntryEnumerator;
import com.ice.tar.TarInputStream;

/**
 * @author Alessio
 *
 */
public class JGSFLoDeSSIOUtils {

	protected final static Logger LOGGER = Logger.getLogger(JGSFLoDeSSIOUtils.class.toString());
	
	/**
	 * 
	 * @param outDir 
	 * @param fileName 
	 * @param varName 
	 * @param userRaster
	 * @param envelope 
	 * @param compressionType 
	 * @param compressionRatio 
	 * @param tileSize 
	 * @return 
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 */
	public static File storeCoverageAsGeoTIFF(
			final File outDir, 
			final String fileName, 
			final CharSequence varName, 
			WritableRaster userRaster, 
			Envelope envelope, 
			final String compressionType, final double compressionRatio, final int tileSize) 
	throws IllegalArgumentException, IOException {
		// /////////////////////////////////////////////////////////////////////
		//
		// PREPARING A WRITE
		//
		// /////////////////////////////////////////////////////////////////////
		if (LOGGER.isLoggable(Level.INFO))
			LOGGER.info("Writing down the file in the decoded directory...");
		final GeoTiffFormat wformat = new GeoTiffFormat();
		final GeoTiffWriteParams wp = new GeoTiffWriteParams();
		if (!Double.isNaN(compressionRatio)) {
			wp.setCompressionMode(GeoTiffWriteParams.MODE_EXPLICIT);
			wp.setCompressionType(compressionType);
			wp.setCompressionQuality((float) compressionRatio);
		}
		wp.setTilingMode(GeoToolsWriteParams.MODE_EXPLICIT);
		wp.setTiling(tileSize, tileSize);
		final ParameterValueGroup wparams = wformat.getWriteParameters();
		wparams.parameter(AbstractGridFormat.GEOTOOLS_WRITE_PARAMS.getName().toString()).setValue(wp);

		// keep original name
		final File outFile = new File(outDir, fileName.toString() + ".tiff");

		// /////////////////////////////////////////////////////////////////////
		//
		// ACQUIRING A WRITER AND PERFORMING A WRITE
		//
		// /////////////////////////////////////////////////////////////////////
		final Hints hints = new Hints(Hints.TILE_ENCODING, "raw");
        final GridCoverageFactory factory = CoverageFactoryFinder.getGridCoverageFactory(hints);
        
        final SampleModel iSampleModel = userRaster.getSampleModel();
		final ColorModel iColorModel = PlanarImage.createColorModel(iSampleModel);
		TiledImage image = new TiledImage(0, 0, userRaster.getWidth(), userRaster.getHeight(), 0, 0, 
        		iSampleModel, iColorModel);
		image.setData(userRaster);
		
        GridCoverage coverage = null;
        if (iColorModel != null)
        	coverage = factory.create(varName, image, envelope);
        else
        	coverage = factory.create(varName, userRaster, envelope);
        
		final AbstractGridCoverageWriter writer = (AbstractGridCoverageWriter) new GeoTiffWriter(outFile);
		writer.write(coverage, (GeneralParameterValue[]) wparams.values().toArray(new GeneralParameterValue[1]));

		// /////////////////////////////////////////////////////////////////////
		//
		// PERFORMING FINAL CLEAN UP AFTER THE WRITE PROCESS
		//
		// /////////////////////////////////////////////////////////////////////
		writer.dispose();
		
		return outFile;
	}
	
	/**
	 * 
	 * @param tempFile
	 * @return
	 * @throws IOException
	 */
	public static File decompress(final String prefix, final File inputFile, final File tempFile)
			throws IOException {
		final File tmpDestDir = createTodayPrefixedDirectory(prefix, new File(tempFile.getParent()));

		String ext = FilenameUtils.getExtension(inputFile.getName());

		if (ext.equalsIgnoreCase("tar")) {
			final TarInputStream stream = new TarInputStream(new FileInputStream(inputFile));
			final TarEntryEnumerator entryEnum = new TarEntryEnumerator(stream);

			if (stream == null) {
				throw new IOException("Not valid COAMPS archive file type.");
			}

			TarEntry entry;
			while (entryEnum.hasMoreElements()) {
				entry = (TarEntry) entryEnum.nextElement();
				final String entryName = entry.getName();

				if(entry.isDirectory()) {
					// Assume directories are stored parents first then children.
					(new File(tmpDestDir, entry.getName())).mkdir();
					continue;
				}
				
				byte[] buf = new byte[(int) entry.getSize()];
				stream.read(buf);

				File newFile = new File(tmpDestDir.getAbsolutePath(), entryName);
				FileOutputStream fos = new FileOutputStream(newFile);
				try {
					saveCompressedStream(buf, fos, buf.length);
				} catch (IOException e) {
					stream.close();
					throw new IOException("Not valid COAMPS archive file type.", e);
				} finally {
					fos.flush();
					fos.close();
				}
			}
			stream.close();

		} else if (ext.equalsIgnoreCase("zip")) {
			ZipFile zipFile = new ZipFile(inputFile);

			Enumeration<? extends ZipEntry> entries = zipFile.entries();

			while(entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry)entries.nextElement();
				InputStream stream = zipFile.getInputStream(entry);

				if(entry.isDirectory()) {
					// Assume directories are stored parents first then children.
					(new File(tmpDestDir, entry.getName())).mkdir();
					continue;
				}

				File newFile = new File(tmpDestDir, entry.getName());
				FileOutputStream fos = new FileOutputStream(newFile);
				try {
					byte[] buf = new byte[1024];
					int len;

				    while((len = stream.read(buf)) >= 0)
						saveCompressedStream(buf, fos, len);

				} catch (IOException e) {
					zipFile.close();
					throw new IOException("Not valid COAMPS archive file type.", e);
				} finally {
					fos.flush();
					fos.close();
					
					stream.close();
				}
			}
			zipFile.close();
		}

		return tmpDestDir;
	}

	/**
	 * Create a subDirectory having the actual date as name, within a specified
	 * destination directory.
	 * 
	 * @param destDir
	 *            the destination directory where to build the "today"
	 *            directory.
	 * @return the created directory.
	 */
	public final static File createTodayDirectory(File destDir) {
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd");
		final String newPath = (new StringBuffer(destDir.getAbsolutePath()
				.trim()).append(File.separatorChar).append(sdf
				.format(new Date()))).toString();
		File dir = new File(newPath);
		if (!dir.exists())
			dir.mkdir();
		return dir;
	}
	
	/**
	 * Create a subDirectory having the actual date as name, within a specified
	 * destination directory.
	 *
	 * @param prefix
	 * @param parent
	 *            the destination directory where to build the "today"
	 *            directory.
	 * @return the created directory.
	 */
	public static File createTodayPrefixedDirectory(final String prefix, final File parent) {
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_hhmmsss");
		final String newPath = (new StringBuffer(parent.getAbsolutePath().trim())
				.append(File.separatorChar)
				.append(prefix)
				.append(File.separatorChar)
				.append(sdf.format(new Date())))
				.toString();
		File dir = new File(newPath);
		if (!dir.exists())
			dir.mkdirs();
		return dir;
	}

	/**
	 * @param len 
	 * @param stream
	 * @param fos
	 * @return 
	 * @throws IOException
	 */
	public static void saveCompressedStream(final byte[] buffer, final OutputStream out, final int len) throws IOException {
		try {
			out.write(buffer, 0, len);
			
		} catch (Exception e) {
			out.flush();
			out.close();
			throw new IOException("Not valid COAMPS archive file type.", e);
		}
	}
	
	/**
	 * 
	 * @param userRaster
	 * @param varName
	 * @param var
	 * @param originalVarData
	 * @param findNewRange
	 * @param updateFillValue
	 * @param loopLengths
	 * @param flipY
	 * @throws IOException
	 * @throws InvalidRangeException
	 */
	public static void write2DData(
			WritableRaster userRaster,
			final String varName, Variable var, final Array originalVarData, 
	        final boolean findNewRange, final boolean updateFillValue, 
	        final int[] loopLengths,
	        final boolean flipY) throws IOException, InvalidRangeException {
		
		int tPos 		 = -1;
		int zPos 		 = -1;
		int latPositions = -1;
		int lonPositions = -1;
		
		if (loopLengths.length == 2) {
			latPositions = loopLengths[1];
			lonPositions = loopLengths[2];
		} else if (loopLengths.length == 3) {
			zPos         = loopLengths[0];
			latPositions = loopLengths[1];
			lonPositions = loopLengths[2];
		} else if (loopLengths.length == 4) {
			tPos         = loopLengths[0];
			zPos         = loopLengths[1];
			latPositions = loopLengths[2];
			lonPositions = loopLengths[3];
		}

		final DataType varDataType = var.getDataType();
		Attribute fv = null;
		if (updateFillValue)
			fv = var.findAttribute(NetCDFUtilities.DatasetAttribs.MISSING_VALUE);
		else
			fv = var.findAttribute(NetCDFUtilities.DatasetAttribs.FILL_VALUE);
		Index varIndex = originalVarData.getIndex();

		// //
		//
		// FLOAT
		//
		// //
		if (varDataType == DataType.FLOAT) {
			float min = Float.MAX_VALUE;
			float max = Float.MIN_VALUE;
			float fillValue = Float.MAX_VALUE;
			if (fv != null) {
				fillValue = (fv.getNumericValue()).floatValue();
			}

			for (int yPos = 0; yPos < latPositions; yPos++) {
				for (int xPos = 0; xPos < lonPositions; xPos++) {
					float sVal = originalVarData.getFloat(
							tPos >= 0 ? 
									(zPos >= 0 ? varIndex.set(tPos, zPos, yPos, xPos) : varIndex.set(tPos, yPos, xPos))
									:
									(zPos >= 0 ? varIndex.set(zPos, yPos, xPos) : varIndex.set(yPos, xPos))
					);
					if (findNewRange) {
						if (sVal >= max && sVal != fillValue)
							max = sVal;
						if (sVal <= min && sVal != fillValue)
							min = sVal;
					}
					// Flipping y
					int newYpos = yPos;
					// Flipping y
					if (flipY) {
						newYpos = latPositions - yPos - 1;
					}
					userRaster.setSample(xPos , newYpos, 0, (double)sVal); // setSample( x, y, band, value )
				}
			}
		} 
		
		// //
		//
		// DOUBLE
		//
		// //
		else if (varDataType == DataType.DOUBLE) {
			double min = Double.MAX_VALUE;
			double max = Double.MIN_VALUE;
			double fillValue = Double.MAX_VALUE;
			if (fv != null) {
				fillValue = (fv.getNumericValue()).doubleValue();
			}

			for (int yPos = 0; yPos < latPositions; yPos++) {
				for (int xPos = 0; xPos < lonPositions; xPos++) {
					double sVal = originalVarData.getDouble(
							tPos >= 0 ? 
									(zPos >= 0 ? varIndex.set(tPos, zPos, yPos, xPos) : varIndex.set(tPos, yPos, xPos))
									:
									(zPos >= 0 ? varIndex.set(zPos, yPos, xPos) : varIndex.set(yPos, xPos))
					);
					if (findNewRange) {
						if (sVal >= max && sVal != fillValue)
							max = sVal;
						if (sVal <= min && sVal != fillValue)
							min = sVal;
					}
					// Flipping y
					int newYpos = yPos;
					// Flipping y
					if (flipY) {
						newYpos = latPositions - yPos - 1;
					}
					userRaster.setSample(xPos , newYpos, 0, sVal); // setSample( x, y, band, value )
				}
			}
		}

		
		// //
		//
		// BYTE
		//
		// //
		else if (varDataType == DataType.BYTE) {
			byte min = Byte.MAX_VALUE;
			byte max = Byte.MIN_VALUE;
			byte fillValue = Byte.MAX_VALUE;
			if (fv != null) {
				fillValue = (fv.getNumericValue()).byteValue();
			}

			for (int yPos = 0; yPos < latPositions; yPos++) {
				for (int xPos = 0; xPos < lonPositions; xPos++) {
					byte sVal = originalVarData.getByte(
							tPos >= 0 ? 
									(zPos >= 0 ? varIndex.set(tPos, zPos, yPos, xPos) : varIndex.set(tPos, yPos, xPos))
									:
									(zPos >= 0 ? varIndex.set(zPos, yPos, xPos) : varIndex.set(yPos, xPos))
					);
					if (findNewRange) {
						if (sVal >= max && sVal != fillValue)
							max = sVal;
						if (sVal <= min && sVal != fillValue)
							min = sVal;
					}
					// Flipping y
					int newYpos = yPos;
					// Flipping y
					if (flipY) {
						newYpos = latPositions - yPos - 1;
					}
					userRaster.setSample(xPos , newYpos, 0, sVal); // setSample( x, y, band, value )
				}
			}
		}

		// //
		//
		// SHORT
		//
		// //
		else if (varDataType == DataType.SHORT) {
			short min = Short.MAX_VALUE;
			short max = Short.MIN_VALUE;
			short fillValue = Short.MAX_VALUE;
			if (fv != null) {
				fillValue = (fv.getNumericValue()).shortValue();
			}

			for (int yPos = 0; yPos < latPositions; yPos++) {
				for (int xPos = 0; xPos < lonPositions; xPos++) {
					short sVal = originalVarData.getShort(
							tPos >= 0 ? 
									(zPos >= 0 ? varIndex.set(tPos, zPos, yPos, xPos) : varIndex.set(tPos, yPos, xPos))
									:
									(zPos >= 0 ? varIndex.set(zPos, yPos, xPos) : varIndex.set(yPos, xPos))
					);
					if (findNewRange) {
						if (sVal >= max && sVal != fillValue)
							max = sVal;
						if (sVal <= min && sVal != fillValue)
							min = sVal;
					}
					// Flipping y
					int newYpos = yPos;
					// Flipping y
					if (flipY) {
						newYpos = latPositions - yPos - 1;
					}
					userRaster.setSample(xPos , newYpos, 0, sVal); // setSample( x, y, band, value )
				}
			}
		}

		// //
		//
		// INTEGER
		//
		// //
		else if (varDataType == DataType.INT) {
			int min = Integer.MAX_VALUE;
			int max = Integer.MIN_VALUE;
			int fillValue = Integer.MAX_VALUE;
			if (fv != null) {
				fillValue = (fv.getNumericValue()).intValue();
			}

			for (int yPos = 0; yPos < latPositions; yPos++) {
				for (int xPos = 0; xPos < lonPositions; xPos++) {
					int sVal = originalVarData.getInt(
							tPos >= 0 ? 
									(zPos >= 0 ? varIndex.set(tPos, zPos, yPos, xPos) : varIndex.set(tPos, yPos, xPos))
									:
									(zPos >= 0 ? varIndex.set(zPos, yPos, xPos) : varIndex.set(yPos, xPos))
					);
					if (findNewRange) {
						if (sVal >= max && sVal != fillValue)
							max = sVal;
						if (sVal <= min && sVal != fillValue)
							min = sVal;
					}
					// Flipping y
					int newYpos = yPos;
					// Flipping y
					if (flipY) {
						newYpos = latPositions - yPos - 1;
					}
					userRaster.setSample(xPos , newYpos, 0, sVal); // setSample( x, y, band, value )
				}
			}
		}

		else
			throw new IllegalArgumentException("Unsupported DataType");
	}

	/**
	 * 
	 * @param userRaster
	 * @param fileGrid
	 * @param findNewRange
	 * @param flipY
	 */
	public static void write2DData(WritableRaster userRaster, final FlatFileGrid fileGrid, final boolean findNewRange, final boolean flipY) {
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		double fillValue = Double.MAX_VALUE;

		final float[] data = fileGrid.getData();
		
		for (int yPos = 0; yPos < fileGrid.getHeight(); yPos++) {
			for (int xPos = 0; xPos < fileGrid.getWidth(); xPos++) {
				double sVal = data[yPos*fileGrid.getHeight() + xPos];
				if (findNewRange) {
					if (sVal >= max && sVal != fillValue)
						max = sVal;
					if (sVal <= min && sVal != fillValue)
						min = sVal;
				}
				// Flipping y
				int newYpos = yPos;
				// Flipping y
				if (flipY) {
					newYpos = fileGrid.getHeight() - yPos - 1;
				}
				userRaster.setSample(xPos, newYpos, 0, sVal); // setSample( x, y, band, value )
			}
		}
	}
	
	/**
	 * 
	 * @param latOriginalData
	 * @param lonOriginalData
	 * @param index2 
	 * @param index 
	 * @return
	 */
	public static double[] computeExtrema(
			final Array latOriginalData, final Array lonOriginalData, 
			final Dimension X_Index, final Dimension Y_Index) {
		double[] extrema = new double[4];
		extrema[0] = Double.POSITIVE_INFINITY;
		extrema[1] = Double.POSITIVE_INFINITY;
		extrema[2] = Double.NEGATIVE_INFINITY;
		extrema[3] = Double.NEGATIVE_INFINITY;
		
		for (int X = 0; X < X_Index.getLength(); X++)
			for (int Y = 0; Y < Y_Index.getLength(); Y++) {
				double lon = lonOriginalData.getDouble(lonOriginalData.getIndex().set(Y, X));
				double lat = latOriginalData.getDouble(latOriginalData.getIndex().set(Y, X));
				
				extrema[0] = lon < extrema[0] ? lon : extrema[0];
				extrema[1] = lat < extrema[1] ? lat : extrema[1];
				extrema[2] = lon > extrema[2] ? lon : extrema[2];
				extrema[3] = lat > extrema[3] ? lat : extrema[3];
			}
		
		return extrema;
	}
	
	/**
	 * 
	 * @param userRaster
	 * @param fileGrid
	 * @param latData 
	 * @param lonData 
	 * @param bbox
	 * @param flipY 
	 */
	public static WritableRaster warping(
			final double[] bbox, final Array lonData, final Array latData,
			final int imageWidth, final int imageHeight, final int polyDegree,
			final WritableRaster data, final float fillValue, final boolean flipY) {

		final int numCoeffs = (polyDegree + 1) * (polyDegree + 2) / 2;

		final int XOFFSET = 0;
		final int YOFFSET = 1;

		final int stepX = 2;
		final int stepY = 2;

		int numNeededPoints = 0;
		for (int xi = 0; xi < imageWidth; xi += stepX) {
			for (int yi = 0; yi < imageHeight; yi += stepY) {
				numNeededPoints++;
			}
		}

		float[] destCoords = new float[2 * numNeededPoints];
		float[] srcCoords = new float[2 * numNeededPoints];
		double periodX = (bbox[2] - bbox[0]) / (imageWidth-1);
		double periodY = (bbox[3] - bbox[1]) / (imageHeight-1);
		
		/*
		 * Copy source and destination coordinates into float arrays. The
		 * destination coordinates are scaled in order to gets values similar to
		 * source coordinates (values will be identical if all "real world"
		 * coordinates are grid indices multiplied by a constant).
		 */
		int offset = 0;
		for (int yi = 0; yi < imageHeight; yi += stepY) {
			for (int xi = 0; xi < imageWidth; xi += stepX) {
				srcCoords[offset] = xi;
				srcCoords[offset + 1] = yi;

				destCoords[offset] = (float) ((lonData.getFloat(lonData.getIndex().set(yi, xi)) - bbox[0]) / periodX);
				// Flipping y
				if (flipY) {
					destCoords[offset + 1] = (float) ((bbox[3] - latData.getFloat(latData.getIndex().set(yi, xi))) / periodY);
				} else {
					destCoords[offset + 1] = (float) ((latData.getFloat(latData.getIndex().set(yi, xi)) - bbox[1]) / periodY);	
				}
				
				offset += 2;
			}
		}

		GMatrix A = new GMatrix(numNeededPoints, numCoeffs);

		for (int coord = 0; coord < numNeededPoints; coord++) {
			int var = 0;
			for (int i = 0; i <= polyDegree; i++) {
				for (int j = 0; j <= i; j++) {
					double value = Math.pow(destCoords[2 * coord + XOFFSET], (double) (i - j)) * Math.pow(destCoords[2 * coord + YOFFSET], (double) j);
					A.setElement(coord, var++, value);
				}
			}
		}

		GMatrix AtAi = new GMatrix(numCoeffs, numCoeffs);
		GMatrix Ap = new GMatrix(numCoeffs, numNeededPoints);

		AtAi.mulTransposeLeft(A, A);
		AtAi.invert();
		Ap.mulTransposeRight(AtAi, A);

		GMatrix xVector = new GMatrix(numNeededPoints, 1);
		GMatrix yVector = new GMatrix(numNeededPoints, 1);

		for (int idx = 0; idx < numNeededPoints; idx++) {
			xVector.setElement(idx, 0, srcCoords[2 * idx + XOFFSET]);
			yVector.setElement(idx, 0, srcCoords[2 * idx + YOFFSET]);
		}

		GMatrix xCoeffsG = new GMatrix(numCoeffs, 1);
		GMatrix yCoeffsG = new GMatrix(numCoeffs, 1);

		xCoeffsG.mul(Ap, xVector);
		yCoeffsG.mul(Ap, yVector);

		float[] xCoeffs = new float[numCoeffs];
		float[] yCoeffs = new float[numCoeffs];

		for (int ii = 0; ii < numCoeffs; ii++) {
			xCoeffs[ii] = new Double(xCoeffsG.getElement(ii, 0)).floatValue();
			yCoeffs[ii] = new Double(yCoeffsG.getElement(ii, 0)).floatValue();
		}

		WritableRaster target = RasterFactory.createWritableRaster(data.getSampleModel(), null);

		for (int bi = 0; bi < data.getNumBands(); bi++) {
			for (int yi = 0; yi < imageHeight; yi++) {
				for (int xi = 0; xi < imageWidth; xi++) {
					GMatrix regressionVec = new GMatrix(numCoeffs, 1);
					int var = 0;
					for (int i = 0; i <= polyDegree; i++) {
						for (int j = 0; j <= i; j++) {
							double value = Math.pow(xi, (double) (i - j))
							* Math.pow(yi, (double) j);
							regressionVec.setElement(var++, 0, value);
						}
					}

					GMatrix xG = new GMatrix(1, 1);
					GMatrix yG = new GMatrix(1, 1);

					xG.mulTransposeLeft(regressionVec, xCoeffsG);
					yG.mulTransposeLeft(regressionVec, yCoeffsG);

					int X = (int) Math.round(xG.getElement(0, 0));
					int Y = (int) Math.round(yG.getElement(0, 0));

					if (X >= 0 && Y >= 0 && X < imageWidth && Y < imageHeight) {
						target.setSample(xi, yi, bi, data.getSampleFloat(X, Y, bi));
					} else {
						target.setSample(xi, yi, bi, fillValue);
					}
				}
			}
		}

		return target;
	}
	
	/**
	 * 
	 * @param userRaster
	 * @param fileGrid
	 * @param latData 
	 * @param lonData 
	 * @param bbox
	 * @param flipY 
	 */
	public static WritableRaster warping(
			final FlatFileGrid fileGrid, 
			final double[] bbox, final float[] lonData, final float[] latData,
			final int imageWidth, final int imageHeight, final int polyDegree,
			final WritableRaster data, final float fillValue, final boolean flipY) {

		final int numCoeffs = (polyDegree + 1) * (polyDegree + 2) / 2;

		final int XOFFSET = 0;
		final int YOFFSET = 1;

		final int stepX = 2;
		final int stepY = 2;

		int numNeededPoints = 0;
		for (int xi = 0; xi < imageWidth; xi += stepX) {
			for (int yi = 0; yi < imageHeight; yi += stepY) {
				numNeededPoints++;
			}
		}

		float[] destCoords = new float[2 * numNeededPoints];
		float[] srcCoords = new float[2 * numNeededPoints];
		double periodX = (bbox[2] - bbox[0]) / (imageWidth-1);
		double periodY = (bbox[3] - bbox[1]) / (imageHeight-1);
		
		/*
		 * Copy source and destination coordinates into float arrays. The
		 * destination coordinates are scaled in order to gets values similar to
		 * source coordinates (values will be identical if all "real world"
		 * coordinates are grid indices multiplied by a constant).
		 */
		int offset = 0;
		for (int yi = 0; yi < imageHeight; yi += stepY) {
			for (int xi = 0; xi < imageWidth; xi += stepX) {
				srcCoords[offset] = xi;
				srcCoords[offset + 1] = yi;

				destCoords[offset] = (float) ((lonData[yi*imageHeight + xi] - bbox[0]) / periodX);
				// Flipping y
				if (flipY) {
					destCoords[offset + 1] = (float) ((bbox[3] - latData[yi*imageHeight + xi]) / periodY);
				} else {
					destCoords[offset + 1] = (float) ((latData[yi*imageHeight + xi] - bbox[1]) / periodY);	
				}
				
				offset += 2;
			}
		}

		GMatrix A = new GMatrix(numNeededPoints, numCoeffs);

		for (int coord = 0; coord < numNeededPoints; coord++) {
			int var = 0;
			for (int i = 0; i <= polyDegree; i++) {
				for (int j = 0; j <= i; j++) {
					double value = Math.pow(destCoords[2 * coord + XOFFSET], (double) (i - j)) * Math.pow(destCoords[2 * coord + YOFFSET], (double) j);
					A.setElement(coord, var++, value);
				}
			}
		}

		GMatrix AtAi = new GMatrix(numCoeffs, numCoeffs);
		GMatrix Ap = new GMatrix(numCoeffs, numNeededPoints);

		AtAi.mulTransposeLeft(A, A);
		AtAi.invert();
		Ap.mulTransposeRight(AtAi, A);

		GMatrix xVector = new GMatrix(numNeededPoints, 1);
		GMatrix yVector = new GMatrix(numNeededPoints, 1);

		for (int idx = 0; idx < numNeededPoints; idx++) {
			xVector.setElement(idx, 0, srcCoords[2 * idx + XOFFSET]);
			yVector.setElement(idx, 0, srcCoords[2 * idx + YOFFSET]);
		}

		GMatrix xCoeffsG = new GMatrix(numCoeffs, 1);
		GMatrix yCoeffsG = new GMatrix(numCoeffs, 1);

		xCoeffsG.mul(Ap, xVector);
		yCoeffsG.mul(Ap, yVector);

		float[] xCoeffs = new float[numCoeffs];
		float[] yCoeffs = new float[numCoeffs];

		for (int ii = 0; ii < numCoeffs; ii++) {
			xCoeffs[ii] = new Double(xCoeffsG.getElement(ii, 0)).floatValue();
			yCoeffs[ii] = new Double(yCoeffsG.getElement(ii, 0)).floatValue();
		}

		WritableRaster target = RasterFactory.createWritableRaster(data.getSampleModel(), null);

		for (int bi = 0; bi < data.getNumBands(); bi++) {
			for (int yi = 0; yi < imageHeight; yi++) {
				for (int xi = 0; xi < imageWidth; xi++) {
					GMatrix regressionVec = new GMatrix(numCoeffs, 1);
					int var = 0;
					for (int i = 0; i <= polyDegree; i++) {
						for (int j = 0; j <= i; j++) {
							double value = Math.pow(xi, (double) (i - j))
							* Math.pow(yi, (double) j);
							regressionVec.setElement(var++, 0, value);
						}
					}

					GMatrix xG = new GMatrix(1, 1);
					GMatrix yG = new GMatrix(1, 1);

					xG.mulTransposeLeft(regressionVec, xCoeffsG);
					yG.mulTransposeLeft(regressionVec, yCoeffsG);

					int X = (int) Math.round(xG.getElement(0, 0));
					int Y = (int) Math.round(yG.getElement(0, 0));

					if (X >= 0 && Y >= 0 && X < imageWidth && Y < imageHeight) {
						target.setSample(xi, yi, bi, data.getSampleFloat(X, Y, bi));
					} else {
						target.setSample(xi, yi, bi, fillValue);
					}
				}
			}
		}

		return target;
	}
	
}
