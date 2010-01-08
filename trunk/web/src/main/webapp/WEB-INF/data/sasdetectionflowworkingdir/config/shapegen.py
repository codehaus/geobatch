#------------------------------------------------
# Utility script which takes detection matfile
# as input and produces a shapefile for it.
#
# It also writes a PRJ with the WKT in case the
# proper definition is available in the specified
# dictionary (The PRJ dictionary folder is passed
# as third argument to the script). In case it 
# is missing, simply write out the "EPSG:XXXcode"
#
# CRS definitions are defined as file with name:
# crsXXX.prj where XXX is the EPSG code. As an 
# instance, crs4326.prj should contain the WKT
# of the EPSG:4326 CRS definition ( = WGS84)
#------------------------------------------------
import os.path
import shutil
import scipy.io
import sys
from osgeo import ogr
from osgeo import osr
from optparse import OptionParser


parser = OptionParser()
parser.add_option("-i", "--input", dest="filename",
                  help="Input detection filename", metavar="FILE")
parser.add_option("-o", "--outdir", dest="outdir",
                  help="Write shapefile to that output dir")
parser.add_option("-c", "--crsdir", dest="crsdir", default="NONE", 
                  help="Folder containing CRS definitions")

(options, args) = parser.parse_args()
if options.filename is None:
  print ("input filename is missing. use -h for the help")
  sys.exit(0)
elif options.outdir is None:
  print ("Output dir is missing. use -h for the help")
  sys.exit(0)


filein = options.filename
shp_path = options.outdir
#if (nArgs > 2):
crs_path = options.crsdir
#else:
#  crs_path = 'NONE'
  
#---------------------------------
# Getting the data name
#---------------------------------
basename = os.path.basename(filein)
basename = "target_" + basename[0:len(basename)-4]

#---------------------------------
# Preparing the output folder
#---------------------------------
shp_filepath = os.path.join(shp_path, basename)
dir = shp_filepath + os.sep
if (not os.path.isdir(dir)):
	os.mkdir(dir)
shp_filepath = (shp_filepath + os.sep + basename + ".shp")

#----------------------------------
# Getting the original raw tilename
#----------------------------------
index = 0
length = len(basename)
for i in range (0,11):
  index = basename.find('_',index+1,length)
name = basename[7:index]

#---------------------------------
# Getting matlab entries
#---------------------------------
detection = scipy.io.loadmat(filein, struct_as_record=True)

# Scores
d_score0 = detection['d_score0']
d_score = detection['d_score']
d_score_DW2 = detection['detection_score_DW2']

# Coordinates
northings_llc = detection['northings_llc'][0][0]
northings_urc = detection['northings_urc'][0][0]
eastings_llc = detection['eastings_llc'][0][0]
eastings_urc = detection['eastings_urc'][0][0]
northings_lrc = detection['northings_lrc'][0][0]
northings_ulc = detection['northings_ulc'][0][0]
eastings_lrc = detection['eastings_lrc'][0][0]
eastings_ulc = detection['eastings_ulc'][0][0]
lat_target = detection['lat_target']
lon_target = detection['lon_target']

# UTM Zone
uzone = detection['uzone']
layername = "it.geosolutions:"+name

#---------------------------------
# Preparing the BoundingBox
#---------------------------------
minX = min(eastings_llc,eastings_ulc,eastings_urc,eastings_lrc)
maxX = max(eastings_llc,eastings_ulc,eastings_urc,eastings_lrc)
minY = min(northings_llc,northings_ulc,northings_urc,northings_lrc)
maxY = max(northings_llc,northings_ulc,northings_urc,northings_lrc)
x0, y0, x1, y1 = str(minX), str(minY), str(maxX), str(maxY)

#---------------------------------
# Setting CRS
#---------------------------------
utmzone = uzone[0]
zone = utmzone[0:2]
vzone = utmzone[2:3]
if ord(vzone) < 77:
	tzone = '7'
else:
	tzone = '6'
code = '32' + tzone +zone
epsgcode = 'EPSG:' + code
t_srs = osr.SpatialReference()
t_srs.SetFromUserInput(str(epsgcode))
drv = ogr.GetDriverByName('ESRI Shapefile')
ds = drv.CreateDataSource(shp_filepath)

#---------------------------------
# Preparing fields
#---------------------------------
layer = ds.CreateLayer(ds.GetName(), geom_type = ogr.wkbPolygon, srs = t_srs)
layer.CreateField(ogr.FieldDefn('d_score', ogr.OFTReal))
layer.CreateField(ogr.FieldDefn('d_score0', ogr.OFTReal))
layer.CreateField(ogr.FieldDefn('d_scoreDW2', ogr.OFTReal))
layer.CreateField(ogr.FieldDefn('lon_target', ogr.OFTReal))
layer.CreateField(ogr.FieldDefn('lat_target', ogr.OFTReal))
layer.CreateField(ogr.FieldDefn('layername', ogr.OFTString))

geom = ogr.Geometry(type = layer.GetLayerDefn().GetGeomType())
geom.AssignSpatialReference(t_srs)
wkt = 'POLYGON(('+x0+' '+y0+','+x0+' '+y1+','+x1+' '+y1+','+x1+' '+y0+','+x0+' '+y0+'))'
geom2 = ogr.CreateGeometryFromWkt(wkt)
geom = geom2;

#---------------------------------
# Setting feature fields
#---------------------------------
feat = ogr.Feature(feature_def = layer.GetLayerDefn())
feat.SetGeometryDirectly(geom)
feat.SetField('d_score', d_score[0][0])
feat.SetField('d_score0', d_score0[0][0])
feat.SetField('d_scoreDW2', d_score_DW2[0][0])
feat.SetField('lat_target', lat_target[0][0])
feat.SetField('lon_target', lon_target[0][0])
feat.SetField('layername', layername)
layer.CreateFeature(feat)
feat.Destroy()

#--------------------------------------------------
# Updating PRJ with OGC WKT instead of ESRI
#   -------------------------------------
# In case a valid PRJ for this EPSG code exists,
# write the WKT. Otherwise, write the "EPSG:XXXcode"
#--------------------------------------------------
outprj = shp_filepath[0:len(shp_filepath)-4]+".prj"
crsfile = "crs" + code + ".prj"
found = False
if(crs_path != 'NONE'):
  crspath = os.path.join(crs_path, crsfile)
  if (os.path.isfile("filename")):
    found = true
if (found):
  shutil.copy(crspath, outprj)
else:
  f = open(outprj,'w')
  f.write(epsgcode)
  f.close()
