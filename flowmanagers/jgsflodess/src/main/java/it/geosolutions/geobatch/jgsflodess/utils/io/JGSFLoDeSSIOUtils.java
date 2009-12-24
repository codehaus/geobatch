/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://code.google.com/p/geobatch/
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
package it.geosolutions.geobatch.jgsflodess.utils.io;

import it.geosolutions.imageio.plugins.netcdf.NetCDFUtilities;
import it.geosolutions.utils.coamps.data.FlatFileGrid;

import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.jai.RasterFactory;
import javax.vecmath.GMatrix;

import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;

/**
 * @author Alessio
 *
 */
public class JGSFLoDeSSIOUtils {
	
	private JGSFLoDeSSIOUtils(){
		
	}

	protected final static Logger LOGGER = Logger.getLogger(JGSFLoDeSSIOUtils.class.toString());
	
	/**
	 * NetCDF-CF Dimensions and Variables
	 */
    public final static String TIME_DIM = "time";

    public final static String DEPTH_DIM = "depth";

    public final static String HEIGHT_DIM = "height";

    public final static String LAT_DIM = "lat";

    public final static String LON_DIM = "lon";

    public final static String LAT_DIM_LONG = "latitude";

    public final static String LON_DIM_LONG = "longitude";



    public final static String LATITUDE = "Latitude";

    public final static String LONGITUDE = "Longitude";

    
    public static final String POSITIVE = "positive";
    
    public static final String UP = "up";
    
    public static final String DOWN = "down";

    public static final String DEG_NORTH = "degrees_north";
    
    public static final String DEG_EAST = "degrees_east";
    
    public static final String UNITS = "units";
    
    public static final String NAME = "name";
    
    public static final String LONG_NAME = "long_name";

    /**
     * Static WGS_84 CoordSys
     */
	public static final CoordinateReferenceSystem WGS_84;

	static {
		CoordinateReferenceSystem crs;
		try {
			crs = CRS.decode("EPSG:4326", true);

		} catch (NoSuchAuthorityCodeException e) {

			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			crs = DefaultGeographicCRS.WGS84;
		} catch (FactoryException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			crs = DefaultGeographicCRS.WGS84;
		}
		WGS_84 = crs;
	}

	public static final long startTime;

	static {
		GregorianCalendar calendar = new GregorianCalendar(1980, 00, 01, 00, 00, 00);
		calendar.setTimeZone(TimeZone.getTimeZone("GMT+0"));
		startTime = calendar.getTimeInMillis();
	}

	/**
	 * 
	 * @param userRaster
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
			Variable var, final Array originalVarData, 
	        final boolean findNewRange, final boolean updateFillValue, 
	        final int[] loopLengths,
	        final boolean flipY) throws IOException, InvalidRangeException {
		
		int tPos 		 = -1;
		int zPos 		 = -1;
		int latPositions = -1;
		int lonPositions = -1;
		
		if (loopLengths.length == 2) {
			latPositions = loopLengths[0];
			lonPositions = loopLengths[1];
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
			final Dimension Y_Index, final Dimension X_Index) {
		double[] extrema = new double[4];
		extrema[0] = Double.POSITIVE_INFINITY;
		extrema[1] = Double.POSITIVE_INFINITY;
		extrema[2] = Double.NEGATIVE_INFINITY;
		extrema[3] = Double.NEGATIVE_INFINITY;
		
		if (latOriginalData.getRank() == 1 && lonOriginalData.getRank() == 1) {
			for (int Y = 0; Y < Y_Index.getLength(); Y++) {
				double lat = latOriginalData.getDouble(latOriginalData.getIndex().set(Y));

				extrema[1] = lat < extrema[1] ? lat : extrema[1];
				extrema[3] = lat > extrema[3] ? lat : extrema[3];
			}

			for (int X = 0; X < X_Index.getLength(); X++) {
				double lon = lonOriginalData.getDouble(lonOriginalData.getIndex().set(X));

				extrema[0] = lon < extrema[0] ? lon : extrema[0];
				extrema[2] = lon > extrema[2] ? lon : extrema[2];
			}
		} else if (latOriginalData.getRank() == 2 && lonOriginalData.getRank() == 2) {
			for (int X = 0; X < X_Index.getLength(); X++)
				for (int Y = 0; Y < Y_Index.getLength(); Y++) {
					double lon = lonOriginalData.getDouble(lonOriginalData.getIndex().set(Y, X));
					double lat = latOriginalData.getDouble(latOriginalData.getIndex().set(Y, X));
					
					extrema[0] = lon < extrema[0] ? lon : extrema[0];
					extrema[1] = lat < extrema[1] ? lat : extrema[1];
					extrema[2] = lon > extrema[2] ? lon : extrema[2];
					extrema[3] = lat > extrema[3] ? lat : extrema[3];
				}
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

	/**
	 * For the NetCDF_CF Geodetic file we assume that it contains georectified
	 * geodetic grids and therefore has a maximum set of dimensions as follows:
	 * 
	 * lat {
	 *  lat:long_name = "Latitude"
	 *  lat:units = "degrees_north"
	 * }
	 * 
	 * lon {
	 *  lon:long_name = "Longitude"
	 *  lon:units = "degrees_east"
	 * }
	 * 
	 * time {
	 *  time:long_name = "time"
	 *  time:units = "seconds since 1980-1-1 0:0:0"
	 * }
	 * 
	 * depth {
	 *  depth:long_name = "depth";
	 *  depth:units = "m";
	 *  depth:positive = "down";
	 * }
	 * 
	 * height {
	 *  height:long_name = "height";
	 *  height:units = "m";
	 *  height:positive = "up";
	 * }
	 * 
	 * @param ncFileOut
	 * @param hasTimeDim
	 * @param tDimLength
	 * @param hasZetaDim
	 * @param zDimLength
	 * @param hasLatDim
	 * @param latDimLength
	 * @param hasLonDimaram length3
	 * @return 
	 */
	public static List<Dimension> createNetCDFCFGeodeticDimensions(
			NetcdfFileWriteable ncFileOut, 
			final boolean hasTimeDim, final int tDimLength,
			final boolean hasZetaDim, final int zDimLength, final String zOrder, 
			final boolean hasLatDim,  final int latDimLength, 
			final boolean hasLonDim,  final int lonDimLength) {
		final List<Dimension> dimensions = new ArrayList<Dimension>();
		
		if (hasTimeDim) {
			Dimension timeDim = ncFileOut.addDimension(TIME_DIM, tDimLength);

			ncFileOut.addVariable(TIME_DIM, DataType.FLOAT, new Dimension[] { timeDim });
	        ncFileOut.addVariableAttribute(TIME_DIM, LONG_NAME, TIME_DIM);
	        ncFileOut.addVariableAttribute(TIME_DIM, UNITS, "seconds since 1980-1-1 0:0:0");
	        
	        dimensions.add(timeDim);
		}
		
		if (hasZetaDim) {
			Dimension zDim = ncFileOut.addDimension(zOrder.equals(DOWN) ? DEPTH_DIM : HEIGHT_DIM, zDimLength);

	        ncFileOut.addVariable(zOrder.equals(DOWN) ? DEPTH_DIM : HEIGHT_DIM, DataType.FLOAT, new Dimension[] { zDim });
	        ncFileOut.addVariableAttribute(zOrder.equals(DOWN) ? DEPTH_DIM : HEIGHT_DIM, LONG_NAME, NetCDFUtilities.DEPTH);
	        ncFileOut.addVariableAttribute(zOrder.equals(DOWN) ? DEPTH_DIM : HEIGHT_DIM, UNITS, "m");
	        ncFileOut.addVariableAttribute(zOrder.equals(DOWN) ? DEPTH_DIM : HEIGHT_DIM, POSITIVE, zOrder);

	        dimensions.add(zDim);
		}
		
		if (hasLatDim) {
			Dimension latDim = ncFileOut.addDimension(LAT_DIM, latDimLength);

	        ncFileOut.addVariable(LAT_DIM, DataType.FLOAT, new Dimension[] { latDim });
	        ncFileOut.addVariableAttribute(LAT_DIM, LONG_NAME, NetCDFUtilities.LATITUDE);
	        ncFileOut.addVariableAttribute(LAT_DIM, UNITS, DEG_NORTH);
	        
	        dimensions.add(latDim);
		}
		
		if (hasLonDim) {
			Dimension lonDim = ncFileOut.addDimension(LON_DIM, lonDimLength);

	        ncFileOut.addVariable(LON_DIM, DataType.FLOAT, new Dimension[] { lonDim });
	        ncFileOut.addVariableAttribute(LON_DIM, LONG_NAME, NetCDFUtilities.LONGITUDE);
	        ncFileOut.addVariableAttribute(LON_DIM, UNITS, DEG_EAST);
	        
	        dimensions.add(lonDim);
		}

		return dimensions;
	}
	
	/**
	 * @param registryURL
	 * @param providerURL
	 * @param coverageName
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws ProtocolException
	 */
	public static boolean sendHarvestRequest(final String registryURL,
			final String providerURL, final String coverageName)
			throws MalformedURLException, IOException, ProtocolException {
		boolean res = false;
		
		final String content = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" >" +
				"<soapenv:Header/>" +
				"<soapenv:Body>" +
				"<csw:Harvest xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\" service=\"CSW\" version=\"2.0.2\">" +
				"<csw:Source>"+providerURL+"/" + coverageName + ".xml" + "</csw:Source>" +
				"</csw:Harvest>" +
				"</soapenv:Body>" +
				"</soapenv:Envelope>";
		
		URL registryWS_URL = new URL(registryURL);
		HttpURLConnection con = (HttpURLConnection) registryWS_URL.openConnection();
        con.setDoOutput(true);
        con.setDoInput(true);
        con.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
        con.setRequestProperty("SOAPAction", "\"http://www.opengis.net/cat/csw/2.0.2/requests#Harvest\"");
        con.setRequestMethod("POST");

        OutputStreamWriter outReq = new OutputStreamWriter(con.getOutputStream());
		outReq.write(content);
        outReq.flush();
        outReq.close();

        final int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            LOGGER.info("Registry - HTTP OK: " + responseCode);
            res = true;
        }
        
        return res;
	}
}
