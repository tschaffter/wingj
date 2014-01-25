/*
Copyright (c) 2010-2013 Thomas Schaffter & Ricard Delgado-Gonzalo

We release this software open source under a Creative Commons Attribution
-NonCommercial 3.0 Unported License. Please cite the papers listed on 
http://lis.epfl.ch/wingj when using WingJ in your publication.

For commercial use, please contact Thomas Schaffter 
(thomas.schaff...@gmail.com).

A brief description of the license is available at 
http://creativecommons.org/licenses/by-nc/3.0/ and the full license at 
http://creativecommons.org/licenses/by-nc/3.0/legalcode.

The above copyright notice and this permission notice shall be included 
in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package ch.epfl.lis.wingj.expression;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import ch.epfl.lis.wingj.ImagePlusManager;
import ch.epfl.lis.wingj.WJSettings;
import ch.epfl.lis.wingj.WJStructureViewer;
import ch.epfl.lis.wingj.WingJ;
import ch.epfl.lis.wingj.structure.Structure;
import ch.epfl.lis.wingj.structure.geometry.FlatSphericalGridMaker;
import ch.epfl.lis.wingj.structure.geometry.Grid;
import ch.epfl.lis.wingj.utilities.ImageUtils;
import ch.epfl.lis.wingj.utilities.MathUtils;

/** 
 * Aggregates many expression maps together to get a unique model.
 * <p>
 * UPDATE: Now supports the use of different structures for generating the target
 * aggregated structure.
 * 
 * @version November 28, 2011
 *
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 */
public class ExpressionMapsAggregator {

//	/** Benchmarks directory (don't forget final /). */
//	protected String benchmarkDirectory_ = "file:///home/tschaffter/devel/java/WingJ/benchmarks/wingpouch_pmadAB_brkAB_wg-ptcAB_90H/";
//	private String benchmarkDirectory_ = "file:///Users/ricard/Desktop/benchmarks/wingpouch_pmadAB_brkAB_wg-ptcAB_90H/";
	
//	/** Name of the gene whose expression is to map. */
//	protected String geneName_ = "pmadAB"; // brkAB
	
//	/** XML structure filename. */
//	protected String structureFilename_ = "structure2.xml";
//	/** Projection filename (suffix only). */
//	protected String projectionFilenamePrefix_ = "_projection.tif";
	
	/** List of Structure objects to aggregate. */
	protected List<Structure> structures_ = null;
	/** List of Structure objects EXCLUSIVE to the generation of the target structure. */
	protected List<Structure> structuresForTargetStructure_ = null;
	/** List of expression maps (there should be one expression map per structure). */
	protected List<ExpressionMap> maps_ = null;
	
	/** Target structure (mean aggregated structure or single structure given). */
	protected Structure targetStructure_ = null;
	/** Mean+std aggregated structure. */
	protected Structure meanPlusStdAggregatedStructure_ = null;
	/** Mean-std aggregated structure. */
	protected Structure meanMinusStdAggregatedStructure_ = null;
	
	/** Target mean circular expression map. */
	protected CircularExpressionMap targetCircularExpressionMap_ = null;
	/** Target std circular expression map. */
	protected CircularExpressionMap targetStdCircularExpressionMap_ = null;
	
	/** Target mean expression map. */
	protected ExpressionMap targetExpressionMap_ = null;
	/** Target std expression map. */
	protected ExpressionMap targetStdExpressionMap_ = null;
	
	/** Should the equator of the grid be placed along the A/P or D/V boundary. */
	protected int equator_ = WJSettings.BOUNDARY_DV;
	
	// ============================================================================
	// PUBLIC METHODS
	
//	/** Returns a list of URIs describing oriented wing pouch structures. */
//	private List<URI> getBenchmarkStrctureURIs() throws Exception {
//		
//		// benchmark
//		List<URI> structureURIs = new ArrayList<URI>();
//		structureURIs.add(new URI(benchmarkDirectory_ + "20100716_pmadAB_brkAB_wg-ptcAB_90,5-91,5H_M_1/" + structureFilename_));
//		structureURIs.add(new URI(benchmarkDirectory_ + "20100716_pmadAB_brkAB_wg-ptcAB_90,5-91,5H_M_2/" + structureFilename_));
//		structureURIs.add(new URI(benchmarkDirectory_ + "20100716_pmadAB_brkAB_wg-ptcAB_90,5-91,5H_M_3/" + structureFilename_));
//		structureURIs.add(new URI(benchmarkDirectory_ + "20100716_pmadAB_brkAB_wg-ptcAB_90,5-91,5H_M_4/" + structureFilename_));
//		structureURIs.add(new URI(benchmarkDirectory_ + "20100716_pmadAB_brkAB_wg-ptcAB_90,5-91,5H_M_5/" + structureFilename_));
//		structureURIs.add(new URI(benchmarkDirectory_ + "20100716_pmadAB_brkAB_wg-ptcAB_90,5-91,5H_M_6/" + structureFilename_));
//		structureURIs.add(new URI(benchmarkDirectory_ + "20100716_pmadAB_brkAB_wg-ptcAB_90,5-91,5H_M_7/" + structureFilename_));
//		
//		return structureURIs;
//	}
	
	// ----------------------------------------------------------------------------
	
//	/** Returns a list of URIs wing pouch structures. */
//	private List<Structure> getBenchmarkStructures() throws Exception {
//		
//		List<URI> structureURIs = getBenchmarkStrctureURIs();
//		List<Structure> structures = new ArrayList<Structure>();
//		WPouchStructure structure = null;
//		for (URI uri : structureURIs) {
//			try {
//				structure = new WPouchStructure("structure");
//				structure.read(uri);
//				structures.add(structure);
//			} catch (Exception e) {
//				WJSettings.log("ERROR: Unable to read structure from file.");
//				e.printStackTrace();
//			}
//		}
//		
//		return structures;
//	}
	
	// ----------------------------------------------------------------------------
	
//	/** Returns a list of URIs describing expression data projection (geneName_projection.tif). */
//	private List<URI> getBenchmarkProjectionsURIs(String geneName) throws Exception {
//		
//		// benchmark
//		List<URI> projectionURIs = new ArrayList<URI>();
//		String projectionFilename_ = "structure.tif";//geneName_ + projectionFilenamePrefix_;
//		projectionURIs.add(new URI(benchmarkDirectory_ + "20100716_pmadAB_brkAB_wg-ptcAB_90,5-91,5H_M_1/" + projectionFilename_));
//		projectionURIs.add(new URI(benchmarkDirectory_ + "20100716_pmadAB_brkAB_wg-ptcAB_90,5-91,5H_M_2/" + projectionFilename_));
//		projectionURIs.add(new URI(benchmarkDirectory_ + "20100716_pmadAB_brkAB_wg-ptcAB_90,5-91,5H_M_3/" + projectionFilename_));
//		projectionURIs.add(new URI(benchmarkDirectory_ + "20100716_pmadAB_brkAB_wg-ptcAB_90,5-91,5H_M_4/" + projectionFilename_));
//		projectionURIs.add(new URI(benchmarkDirectory_ + "20100716_pmadAB_brkAB_wg-ptcAB_90,5-91,5H_M_5/" + projectionFilename_));
//		projectionURIs.add(new URI(benchmarkDirectory_ + "20100716_pmadAB_brkAB_wg-ptcAB_90,5-91,5H_M_6/" + projectionFilename_));
//		projectionURIs.add(new URI(benchmarkDirectory_ + "20100716_pmadAB_brkAB_wg-ptcAB_90,5-91,5H_M_7/" + projectionFilename_));
//		
//		return projectionURIs;
//	}
	
	// ----------------------------------------------------------------------------
	
//	/** Returns a list of ImagePlus describing expression data projection (//projection.tif) */
//	private List<ExpressionMap> getBenchmarkProjections(String geneName) throws Exception {
//		
//		// benchmark
//		List<URI> projectionURIs = getBenchmarkProjectionsURIs(geneName);
//		List<ExpressionMap> projections = new ArrayList<ExpressionMap>();
//		ImagePlus ip = null;
//		URI uri = null;
////		for (URI uri : projectionURIs) {
//		for (int i = 0; i < projectionURIs.size(); i++) {
//			try {
//				uri = projectionURIs.get(i);
//				ip = IJ.openImage(uri.getPath());
//				projections.add(new ExpressionMap("expression_projection_" + i, ip.getProcessor()));
//				WJSettings.log("[x] Reading projection (tif)");
//			} catch (Exception e) {
//				WJSettings.log("[ ] Reading projection (tif)");
//				e.printStackTrace();
//			}
//		}
//		
//		return projections;
//	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns a list of circular expression maps computed for each pair of structure and projections. */
	public static List<CircularExpressionMap> getCircularExpressionMaps(List<Structure> structures, List<ExpressionMap> maps, int projectionMode) throws Exception {
		
		if (structures == null)
			throw new Exception("ERROR: List of structures is null.");
		if (maps == null)
			throw new Exception("ERROR: List of projections is null.");
		if (structures.size() != maps.size())
			throw new Exception("ERROR: List of structures and projections must have the same size.");
		
		List<CircularExpressionMap> circularExpressionMaps = new ArrayList<CircularExpressionMap>();
		for (int i = 0; i < structures.size(); i++) {
			circularExpressionMaps.add(computeCircularExpressionMap(structures.get(i), maps.get(i), projectionMode));
			circularExpressionMaps.get(i).setTitle(circularExpressionMaps.get(i).getTitle() + "_" + i);
		}
		return circularExpressionMaps;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns the circular expression map computed for the given structure and projection. */
	public static CircularExpressionMap computeCircularExpressionMap(Structure structure, ExpressionMap projection, int projectionMode) throws Exception {
		
		if (structure == null)
			throw new Exception("ERROR: Structures is null.");
		if (projection == null)
			throw new Exception("ERROR: Projections is null.");
		
		WJSettings settings = WJSettings.getInstance();

		FlatSphericalGridMaker wPouchMorpher = new FlatSphericalGridMaker(structure);

		int nPoints = settings.getExpression2DNumPoints();
		if (nPoints % 2 == 0) { // nPoints must be even
			nPoints = (nPoints/2)+1;
		} else {
			nPoints = ((nPoints-1)/2)+1;
		}

		FloatProcessor expression = null;
		Grid grid = wPouchMorpher.generateSphereLikeGrid(projectionMode, nPoints);
		switch(projectionMode){
			case WJSettings.BOUNDARY_DV:
				expression = CircularExpressionMap.computeExpressionMap(projection, structure, grid, WJSettings.BOUNDARY_DV);
				break;
			case WJSettings.BOUNDARY_AP:
				expression = CircularExpressionMap.computeExpressionMap(projection, structure, grid, WJSettings.BOUNDARY_AP);
				expression.flipVertical();
				break;
			default:
		}		

		return new CircularExpressionMap("circular_expression_map", expression);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns the mean and std circular expression map computed from the given list of map. */
	public static CircularExpressionMap[] computeMeanCircularExpressionMap(List<CircularExpressionMap> maps) throws Exception {
		
		if (maps == null)
			throw new Exception("ERROR: Maps is null.");
		if (maps.size() < 1)
			throw new Exception("ERROR: At least one structure is needed.");
		
		int width = maps.get(0).getWidth();
		int height = maps.get(0).getHeight();
		
		Double[][] data = new Double[width*height][maps.size()];
		double[] meanMap = new double[width*height];
		double[] stdMap = new double[width*height];
		ImagePlus map = null;
		for (int i = 0; i < maps.size(); i++) {
			map = maps.get(i);
			FloatProcessor ip = (FloatProcessor) map.getProcessor();
			if(!(ip instanceof FloatProcessor))
				ip = (FloatProcessor) ip.convertToFloat();

			if(width != ip.getWidth() || height != ip.getHeight())
				throw new Exception("ERROR: Dimensions missmatch.");
			
			float[] pixels = (float[]) ip.getPixels();
			for(int j = 0; j < pixels.length; j++)
				data[j][i] = (double)pixels[j];
		}
		Double[] meanAndStd = null;
		for (int i = 0; i < meanMap.length; i++) {
			meanAndStd = MathUtils.computeMeanAndStd(data[i]);
			meanMap[i] = meanAndStd[0];
			stdMap[i] = meanAndStd[1];
		}
		
		CircularExpressionMap[] meanAndStdMaps = new CircularExpressionMap[2];
		meanAndStdMaps[0] = new CircularExpressionMap("mean_circular_expression_map", new FloatProcessor(width, height, meanMap));
		meanAndStdMaps[1] = new CircularExpressionMap("std_circular_expression_map", new FloatProcessor(width, height, stdMap));
		
		return meanAndStdMaps;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Wraps the mean circular expression map on the structure synthesis. */
	public static ExpressionMap wrapCircularExpressionMapOnStructure(CircularExpressionMap circularMap, Structure structure, int projectionMode, int width, int height) throws Exception {

		if (circularMap == null)
			throw new Exception("ERROR: Circular map is null.");
		if (structure == null)
			throw new Exception("ERROR: Structure is null.");
		
		ImagePlusManager manager = ImagePlusManager.getInstance();
		
		FloatProcessor expressionProcessor = (FloatProcessor) circularMap.getProcessor().convertToFloat();
		expressionProcessor.setInterpolationMethod(ImageProcessor.BICUBIC);
		int circularMapWidth = circularMap.getWidth();
		int circularMapHeight = circularMap.getHeight();
		int iCenter = (int) Math.round((circularMapWidth-1) / 2.0);
		int jCenter = (int) Math.round((circularMapHeight-1) / 2.0);
		int h = (int) Math.round(((circularMapWidth+circularMapHeight)/2.0-1) / 2.0);
		
		double[] wPouchSquareExpressionMap = new double[circularMapWidth * circularMapHeight];
		switch(projectionMode){
		case WJSettings.BOUNDARY_DV:
			for (int i = 0; i < circularMapWidth; i++) {
				double x = i - iCenter;
				for (int j = 0; j < circularMapHeight; j++) {
					double y = j - jCenter;
					double scalling = Math.sqrt(1 - (y/h)*(y/h));
					double xp = scalling*x + iCenter;
					wPouchSquareExpressionMap[i + j*circularMapWidth] = expressionProcessor.getInterpolatedPixel(xp, j);
				}
			}
			break;
		case WJSettings.BOUNDARY_AP:
			for (int i = 0; i < circularMapWidth; i++) {
				double x = i - iCenter;
				for (int j = 0; j < circularMapHeight; j++) {
					double y = j - jCenter;
					double scalling = Math.sqrt(1 - (x/h)*(x/h));
					double yp = scalling*y + jCenter;
					wPouchSquareExpressionMap[i + j*circularMapWidth] = expressionProcessor.getInterpolatedPixel(i, yp);
				}
			}
			break;
		default:
		}
		
		FloatProcessor wPouchSquareExpressionMapProcessor = new FloatProcessor(circularMapWidth,circularMapHeight,wPouchSquareExpressionMap);

		if(projectionMode == WJSettings.BOUNDARY_AP)
			wPouchSquareExpressionMapProcessor = (FloatProcessor) wPouchSquareExpressionMapProcessor.rotateLeft();
		
		wPouchSquareExpressionMapProcessor.flipVertical();
		wPouchSquareExpressionMapProcessor.flipHorizontal();
		
		if (WJSettings.DEBUG) {
			ImagePlus wPouchSquareExpressionMapPlus = new ImagePlus("square_expression_map", wPouchSquareExpressionMapProcessor);
			manager.add(wPouchSquareExpressionMapPlus.getTitle(), wPouchSquareExpressionMapPlus, true);
		}
		
		Structure sc = structure.copy(); 
//		sc.moveToTopLeftCorner();
		
		//Morpher following the averaged structure
		FlatSphericalGridMaker wPouchMorpher = new FlatSphericalGridMaker(sc);
		Grid grid = wPouchMorpher.generateSphereLikeGrid(projectionMode, (circularMapWidth-1)/2+1);
		double[] expressionPixels = new double[width*height];
		
//		if (WJSettings.DEBUG) {
		// can be exported so don't place it in debug
		ImagePlus gridPreview = grid.draw(IJ.createImage(structure.getName(), "black", width, height, 1), Color.RED, 20);
				gridPreview.setTitle("expression_sampling_grid_preview");
		manager.add(gridPreview.getTitle(), gridPreview, WJSettings.DEBUG);
//		}
		
		for(int i=0; i<grid.getGridLength(); i++){
			for(int j=0; j<grid.getGridLength(); j++){
				int x = (int)(grid.getCoordinate(i, j).x);
				int y = (int)(grid.getCoordinate(i, j).y);
				if(sc.contains(x,y)){
					if(x>=0 && x<width && y>=0 && y<height){
						expressionPixels[x+width*y] = wPouchSquareExpressionMapProcessor.getPixelValue(i, j);
					}
				}
			}
		}
		
		return new ExpressionMap("reversed_expression_map", new FloatProcessor(width, height, expressionPixels));
	}
	
	// ----------------------------------------------------------------------------

	/** Default constructor. */
	public ExpressionMapsAggregator() {
		
		initialize(null, null, null);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Constructor. */
	public ExpressionMapsAggregator(List<Structure> structures, List<ExpressionMap> maps) {
		
		initialize(structures, null, maps);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Constructor. */
	public ExpressionMapsAggregator(List<Structure> structures, List<Structure> structuresForTargetStructure, List<ExpressionMap> maps) {
		
		initialize(structures, structuresForTargetStructure, maps);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Initialization. */
	protected void initialize(List<Structure> structures, List<Structure> structuresForTargetStructure, List<ExpressionMap> maps) {
		
		// gets the equator (A/P or D/V boundary)
		equator_ = WJSettings.getInstance().getExpression2DAggEquator();
		
		structures_ = new ArrayList<Structure>();
		if (structures != null) {
			for(Structure strucure : structures)
				structures_.add(strucure.copy());
		}
		
		if (structuresForTargetStructure != null)
			setStructuresForTargetStructure(structuresForTargetStructure);
		
		maps_ = new ArrayList<ExpressionMap>();
		if (maps != null) {
			for (ExpressionMap map : maps)
				maps_.add(new ExpressionMap(map.getTitle(), new Duplicator().run(map).getProcessor()));
		}
	}
	
	// ----------------------------------------------------------------------------
	
//	/** Main method. */
//	public static void main(String[] args) {
//
//		try {
//			new ImageJ(); // not required to display images but tools can be handy
//			WingJ.getInstance(); // instantiate WingJ
//			WJSettings.getInstance(); // initialize settings
//			
//			ExpressionMapsAggregator synthesisTest = new ExpressionMapsAggregator();
//			synthesisTest.test();
//		} catch (Exception e) {
//			WJSettings.log("ERROR: Expression maps aggregation failed.");
//			e.printStackTrace();
//		}
//	}
	
	// ----------------------------------------------------------------------------
	
	/** Run method. */
	public void run() throws Exception {
		
		WJSettings settings = WJSettings.getInstance();
		ImagePlusManager manager = ImagePlusManager.getInstance();
		
		WJSettings.log("Running model aggregator.");
		
		// if the target structure has not been set manually
		if (targetStructure_ == null) {
			WJSettings.log("Aggregating structure models.");
			
			if(WJSettings.DEBUG){
				for (int i = 0; i < structures_.size(); i++) {
					ImagePlus duplicatedImageMap = new Duplicator().run(maps_.get(i));
					duplicatedImageMap.setTitle("expression_projection_" + i + "_viewer");
					WJStructureViewer v = new WJStructureViewer(structures_.get(i), duplicatedImageMap);
					ImagePlus ip2 = v.toImagePlus();
					ip2.setTitle("expression_projection_" + i);
					manager.add(ip2.getTitle(), ip2, true);
					v.setVisible(false);
				}
			}
			
			// selects the structures to use for generating the
			// aggregated structure
			List<Structure> inputStructures = null;
			if (structuresForTargetStructure_ != null)
				inputStructures = structuresForTargetStructure_;
			else
				inputStructures = structures_;
			
			targetStructure_ = WingJ.getInstance().getSystem().newStructure();
			targetStructure_.setName("mean-aggregated-structure");
			targetStructure_.aggregate(inputStructures, Structure.AGGREGATION_MEAN);
			meanPlusStdAggregatedStructure_ = WingJ.getInstance().getSystem().newStructure();
			meanPlusStdAggregatedStructure_.aggregate(inputStructures, Structure.AGGREGATION_MEAN_PLUS_STD);
			meanMinusStdAggregatedStructure_ = WingJ.getInstance().getSystem().newStructure();;
			meanMinusStdAggregatedStructure_.aggregate(inputStructures, Structure.AGGREGATION_MEAN_MINUS_STD);
			
			
			double[] transforms = meanPlusStdAggregatedStructure_.setCanonicalOrientation();;
			targetStructure_.rotate(transforms[0]);
			meanPlusStdAggregatedStructure_.rotate(2*transforms[0]);
			meanMinusStdAggregatedStructure_.rotate(transforms[0]);
			
			// modify effectively the structure
			double[] dxdy = meanPlusStdAggregatedStructure_.moveToTopLeftCorner();
			targetStructure_.translate(dxdy[0], dxdy[1]);
			meanMinusStdAggregatedStructure_.translate(dxdy[0], dxdy[1]);
		} else {
			WJSettings.log("Using the given structure model.");
			targetStructure_.setCanonicalOrientation();
			targetStructure_.moveToTopLeftCorner();
		}
		
		// if the target circular expression map has not been set manually
		if (targetCircularExpressionMap_ == null) {
			WJSettings.log("Aggregating expression maps.");
			List<CircularExpressionMap> circularExpressionMaps = getCircularExpressionMaps(structures_, maps_, equator_);
			if (WJSettings.DEBUG) {
				int index = 0;
				for(CircularExpressionMap map : circularExpressionMaps){
					manager.add(map.getTitle() + "_" + index, map, true);
					index++;
				}
			}
			// compute mean circular expression map
			CircularExpressionMap[] meanAndStdMaps = computeMeanCircularExpressionMap(circularExpressionMaps);
			targetCircularExpressionMap_ = meanAndStdMaps[0];
			if (settings.getExpression2DAggStd())
				targetStdCircularExpressionMap_ = meanAndStdMaps[1];
		} else {
			WJSettings.log("Using the given circular expression map.");
			// normalize the given circular expression map if required
			FloatProcessor fp = (FloatProcessor)targetCircularExpressionMap_.getProcessor().convertToFloat();
			fp.flipVertical();// flip because of magic
			if (settings.normalizeExpression() && ImageUtils.getMaxPixelValue(targetCircularExpressionMap_) > 1.) { // normalize only if image is not yet in [0,1]
				WJSettings.log("Normalizing expression data.");
				float[] data = (float[])fp.getPixels();
				for (int i = 0; i < data.length; i++)
					data[i] /= 255.;
				fp.setPixels(data);
			}
			targetCircularExpressionMap_ = new CircularExpressionMap(targetCircularExpressionMap_.getTitle(), fp);
		}
		
		if (WJSettings.DEBUG) {
			manager.add(targetCircularExpressionMap_.getTitle(), targetCircularExpressionMap_, true);
			if (targetStdCircularExpressionMap_ != null)
				manager.add(targetStdCircularExpressionMap_.getTitle(), targetStdCircularExpressionMap_, true);
		}
		
		// Wrap the mean circular expression map on the structure synthesis
		WJSettings.log("Reversing expression map (wrapping expression map on target structure).");
		if (meanPlusStdAggregatedStructure_ != null) {
			targetExpressionMap_ = wrapCircularExpressionMapOnStructure(targetCircularExpressionMap_, targetStructure_, equator_, 
					(int)meanPlusStdAggregatedStructure_.getHorizontalWidth(), (int)meanPlusStdAggregatedStructure_.getVerticalHeight());
			if (settings.getExpression2DAggStd()) {
				targetStdExpressionMap_ = wrapCircularExpressionMapOnStructure(targetStdCircularExpressionMap_, targetStructure_, equator_, 
						(int)meanPlusStdAggregatedStructure_.getHorizontalWidth(), (int)meanPlusStdAggregatedStructure_.getVerticalHeight());
			}
		} else {
			targetExpressionMap_ = wrapCircularExpressionMapOnStructure(targetCircularExpressionMap_, targetStructure_, equator_, 
					(int)targetStructure_.getHorizontalWidth(), (int)targetStructure_.getVerticalHeight());
		}
	}
	
	// ============================================================================
	// SETTERS AND GETTERS
	
	public void setEquator(int equator) { equator_ = equator; }
	public int getEquator() { return equator_; }
	
	public void setTargetStructure(Structure targetStructure) { targetStructure_ = targetStructure.copy(); }
	public Structure getTargetStructure() { return targetStructure_; }
	
	public Structure getMeanPlusStdAggregatedStructure() { return meanPlusStdAggregatedStructure_; }
	public Structure getMeanMinusStdAggregatedStructure() { return meanMinusStdAggregatedStructure_; }
	
	public void setTargetCircularExpressionMap(CircularExpressionMap circularMap) { targetCircularExpressionMap_ = new CircularExpressionMap(circularMap.getTitle(), new Duplicator().run(circularMap).getProcessor()); }
	public CircularExpressionMap getTargetCircularExpressionMap() { return targetCircularExpressionMap_; }
	public CircularExpressionMap getTargetStdCircularExpressionMap() { return targetStdCircularExpressionMap_; }
	
	public ExpressionMap getTargetExpressionMap() { return targetExpressionMap_; }
	public ExpressionMap getTargetStdExpressionMap() { return targetStdExpressionMap_; }
	
	public void setStructuresForTargetStructure(List<Structure> structures) {
		structuresForTargetStructure_ = new ArrayList<Structure>();
		for (Structure s : structures)
			structuresForTargetStructure_.add(s.copy());
	}
}
