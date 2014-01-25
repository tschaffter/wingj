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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import ch.epfl.lis.wingj.WJSettings;
import ch.epfl.lis.wingj.WingJ;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.awt.DefaultFontMapper;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

/** 
 * Plots gene expression profile in a JDialog using JFree chart.
 * <p>
 * A method is implemented to export the vector plot in PDF format.
 * 
 * @version August 31, 2011
 * 
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 */
public class ExpressionPlot extends JDialog {

	/** Default serial version ID. */
	private static final long serialVersionUID = 1L;
	
	/** Instances of ExpressionPlot. */
	private static List<ExpressionPlot> instances_ = new ArrayList<ExpressionPlot>();
	
	/** JFree chart. */
	private JFreeChart chart_ = null;
	
	/** Expression profile to plot. */
	private ExpressionProfile expressionProfile_ = null;
	/** Title of the plot. */
	private String title_ = "Title";
	/** X label. */
	private String xlabel_ = "X-axis"; // X-axis [um]
	/** Y label. */
	private String ylabel_ = "Y-axis"; // Absolute fluorescence intensity
	/** Color. */
	private Color color_ = Color.BLUE; // like Matlab
	
	/** Plot width. */
	private int width_ = 1000;
	/** Plot height. */
	private int height_ = 400;
	
	// ============================================================================
	// PRIVATE METHODS
	
	/** Formats the expression data to XYDataset. */
	private XYDataset getXYDataset() throws Exception {
		
		XYSeries series = new XYSeries("");
		
		double[] x = expressionProfile_.getX();
		double[] y = expressionProfile_.getY();
		
		if (x.length != y .length)
			throw new Exception("ERROR: Dimensions X and Y of the expression dataset mismatch.");
		
		for (int i = 0; i < x.length; i++)
			series.add(x[i], y[i]);
		
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);
        return dataset;
	}
	
	// ----------------------------------------------------------------------------

	/** Generates a PDF document containing the chart using iText. */
	private ByteArrayOutputStream generatePdf() throws Exception {
		
		Document doc = new Document();
		ByteArrayOutputStream baosPDF = new ByteArrayOutputStream();
        PdfWriter docWriter = null;
        
        try {
			docWriter = PdfWriter.getInstance(doc, baosPDF);
			 
			doc.addProducer();
			doc.addCreator(this.getClass().getName());
			doc.addTitle("jfreechart pdf");
			Rectangle documentArea = PageSize.LETTER.rotate();
			doc.setPageSize(documentArea); // or PageSize.A4.rotate()
			doc.open();
			 
//			// add some text to the document
//			doc.add(new Phrase("WingJ (wingj.sf.net)"));
			
			int margin = 40;
			int width = (int) Math.floor(documentArea.getWidth()) - 2*margin;
			int height = (int) Math.floor(documentArea.getHeight()) - 2*margin;
			 
			// SOLUTION 1 from http://www.wirelust.com/2008/03/17/creating-an-itext-pdf-with-embedded-jfreechart/
			// NOTE: createGraphics() is deprecated
			// get the direct pdf content
			PdfContentByte dc = docWriter.getDirectContent();
			 
			// get a pdf template from the direct content
			PdfTemplate tp = dc.createTemplate(width, height);
			 
			// create an AWT renderer from the pdf template
			@SuppressWarnings("deprecation")
			Graphics2D g2 = tp.createGraphics(width, height, new DefaultFontMapper());
			Rectangle2D r2D = new Rectangle2D.Double(0,0, width,height);
			chart_.draw(g2,r2D,null);
			g2.dispose();
			 
			// add the rendered pdf template to the direct content
			// you will have to play around with this because the chart is absolutely positioned.
			// 38 is just a typical left margin
			// docWriter.getVerticalPosition(true) will approximate the position that the content above the chart ended
			dc.addTemplate(tp, margin, docWriter.getVerticalPosition(true)-height);
			
//			// SOLUTION 2 from http://vangjee.wordpress.com/2010/11/03/how-to-use-and-not-use-itext-and-jfreechart/
//			// NOTE: convert the chart to an image and thus no interest in saving to PDF
//			BufferedImage bufferedImage = chart_.createBufferedImage(width, height);
//			Image image = Image.getInstance(docWriter, bufferedImage, 1.0f);
//			doc.add(image);

        } catch (DocumentException dex) {
            baosPDF.reset();
            dex.printStackTrace();
            throw new DocumentException(dex);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (doc != null && doc.isOpen()) {
                doc.close();
            }
            if (docWriter != null) {
                docWriter.close();
            }
        }
		
        if (baosPDF.size() < 1) {
            throw new DocumentException("document has "     + baosPDF.size() + " bytes");
        }
        return baosPDF;		
	}
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Default constructor. */
	public ExpressionPlot() {
		
		super();
		instances_.add(this);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Constructor. */
	public ExpressionPlot(String title, String xlabel, String ylabel, ExpressionProfile expressionProfile) {
		
		super();
		initialize(title, xlabel, ylabel, expressionProfile);
		instances_.add(this);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Initialization. */
	public void initialize(String title, String xlabel, String ylabel, ExpressionProfile expressionProfile) {
		
//		// Override the default behavior of the closing button
//		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
//		addWindowListener(new WindowAdapter() {
//			public void windowClosing(WindowEvent event) {
//				// TBD
//			}
//		});
		
		title_ = title;
		xlabel_ = xlabel;
		ylabel_ = ylabel;
		expressionProfile_ = expressionProfile;
		
		// set meaningful window title
		setTitle(expressionProfile_.getName());
	}
	
	// ----------------------------------------------------------------------------
	
	/** Generates the plot. */
	public void generatePlot() throws Exception {
		
		WJSettings settings = WJSettings.getInstance();
		
		// create formated dataset for JFreeChart
		XYDataset dataset = getXYDataset();
		// create chart
		chart_ = ChartFactory.createXYLineChart(title_, xlabel_, ylabel_, dataset, PlotOrientation.VERTICAL, false, false, false);
		Font titleFont = chart_.getTitle().getFont();
		titleFont = new Font(titleFont.getFontName(), titleFont.getStyle(), 14);
		chart_.getTitle().setFont(titleFont);
        XYPlot plot = (XYPlot) chart_.getPlot();
        plot.setBackgroundAlpha(0);
        
        double xmin = expressionProfile_.getX()[0];
        double xmax = expressionProfile_.getX()[expressionProfile_.getX().length-1];
        plot.getDomainAxis().setRange(xmin, xmax);
        
        double max = 255.0;
        if (settings.normalizeExpression()) max = 1.0;
        plot.getRangeAxis(0).setRange(0.0, max); // set y-axis range [0, 255]
        
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, true); // show the data line
        renderer.setSeriesShapesVisible(0, false);
        renderer.setSeriesPaint(0, color_);
        plot.setRenderer(renderer);
        
        // place the chart in the dialog
        final ChartPanel chartPanel = new ChartPanel(chart_);
        
        getContentPane().add(chartPanel);
		setPreferredSize(new Dimension(width_, height_));
		WingJ.setAppIcon(this);
		pack();
	}

	// ----------------------------------------------------------------------------
	
	/** Saves the plot in PNG format. */
	public void savePNG(String filename) throws Exception {
		
		if (chart_ == null)
			throw new Exception("ERROR: Expression plot doesn't exist and so can not be saved in PNG format.");
		
		ChartUtilities.saveChartAsPNG(new File(filename), chart_, width_, height_);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Saves the plot in PDF format. */
	public void savePDF(URI uri) throws Exception {
		
        ByteArrayOutputStream baosPDF = generatePdf();
        OutputStream outputStream = new FileOutputStream(new File(uri));
        try {
            baosPDF.writeTo(outputStream);
        } catch (NullPointerException npe) {
        	outputStream.flush();
        	outputStream.close();
        	throw npe;
        }
        outputStream.flush();
        outputStream.close();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Closes all instances of ExpressionPlot in instances_. */
	public static void disposeAll() throws Exception {
		
		for (int i = 0; i < instances_.size(); i++) {
			try {
				instances_.get(i).dispose(); // instances_.get(i) could be null
			} catch (Exception e) {}
		}
	}
	
	// ============================================================================
	// SETTERS AND GETTERS

	public void setColor(Color c) { color_ = c; }
}