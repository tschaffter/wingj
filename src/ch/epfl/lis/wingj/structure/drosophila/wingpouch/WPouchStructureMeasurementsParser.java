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

package ch.epfl.lis.wingj.structure.drosophila.wingpouch;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sun.org.apache.xerces.internal.dom.DocumentImpl;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

import ch.epfl.lis.wingj.structure.Boundary;
import ch.epfl.lis.wingj.structure.Compartment;

/** 
 * Implements methods to write the measurements taken from the structure model of the wing pouch to files.
 * <p>
 * Read method is implemented for loading benchmarks to test WingJ.
 * 
 * @version November 9, 2011
 *
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 */
public class WPouchStructureMeasurementsParser {
	
	/** Wing pouch structure whose measurements must be saved to file. */
	WPouchStructure structure_ = null;
	
	/** XML DOM document. */
	Document xmldoc_ = null;
	
	// ============================================================================
	// PRIVATE METHODS
	
	/** Adds a boundary to XML DOM structure. */
	@SuppressWarnings("unused")
	private void addBoundary(Element root, String name, Boundary boundary) throws Exception {
		
		addBoundary(root, name, boundary.length());
	}
	
	// ----------------------------------------------------------------------------
	
	/** Adds a boundary to XML DOM structure. */
	private void addBoundary(Element root, String name, double length) throws Exception {
		
		Element e1 = xmldoc_.createElementNS(null, "boundary");
		e1.setAttribute("name", name);
		Element e2 = xmldoc_.createElementNS(null, "length");
		e2.setAttribute("unit", "um");
		e2.appendChild(xmldoc_.createTextNode(new Double(length).toString()));
		e1.appendChild(e2);
		root.appendChild(e1);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Adds a compartment to XML DOM structure. */
	@SuppressWarnings("unused")
	private void addCompartment(Element root, String name, Compartment compartment) throws Exception {
		
		addCompartment(root, name, compartment.perimeter(), compartment.area());
	}
	
	// ----------------------------------------------------------------------------
	
	/** Adds a compartment to XML DOM structure. */
	private void addCompartment(Element root, String name, double perimeter, double area) throws Exception {
		
		Element e1 = xmldoc_.createElementNS(null, "compartment");
		e1.setAttribute("name", name);
		Element e2 = xmldoc_.createElementNS(null, "perimeter");
		e2.setAttribute("unit", "um");
		e2.appendChild(xmldoc_.createTextNode(new Double(perimeter).toString()));
		e1.appendChild(e2);
		e2 = xmldoc_.createElementNS(null, "area");
		e2.setAttribute("unit", "um2");
		e2.appendChild(xmldoc_.createTextNode(new Double(area).toString()));
		e1.appendChild(e2);
		root.appendChild(e1);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Helper function to get all the level-1 children of an Element. */
	private static ArrayList<Element> getChildrenByTagName(Element parent, String name) {
		ArrayList<Element> nodeList = new ArrayList<Element>();
		for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getNodeType() == Node.ELEMENT_NODE && name.equals(child.getNodeName()))
				nodeList.add((Element) child);
		}
		return nodeList;
	}
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Default constructor. */
	public WPouchStructureMeasurementsParser() {}
	
	// ----------------------------------------------------------------------------
	
	/** Constructor. */
	public WPouchStructureMeasurementsParser(WPouchStructure structure) {
		
		structure_ = structure;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Saves measurements of the given wing pouch to XML file. */
	public void write(URI uri) throws Exception {
		
		WPouchStructureMeasurements data = new WPouchStructureMeasurements();
		data.initialize(structure_);
		write(uri, data);
	}
	
	// ----------------------------------------------------------------------------
	
	
	/** Saves measurements of the given wing pouch to XML file. */
	public void write(URI uri, WPouchStructure structure) throws Exception {
		
		WPouchStructureMeasurements data = new WPouchStructureMeasurements();
		data.initialize(structure);
		write(uri, data);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Saves measurements of the given wing pouch to XML file. */
	public void write(URI uri, WPouchStructureMeasurements data) throws Exception {
	
		// XML DOM document
		xmldoc_ = new DocumentImpl();
		
//		String pName = "pName";
//		DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
//		DOMImplementation implementation = registry.getDOMImplementation("XML 1.0");
//		DocumentType type = implementation.createDocumentType(pName, null, null);
//		xmldoc_ = implementation.createDocument(null, pName, type);
		
		// root element
		Element root = xmldoc_.createElement("structure_measurements");
		root.setAttribute("name", data.getName());
		root.setAttribute("age", data.getAge());
		
		// add comment
		Comment comment = xmldoc_.createComment("Generated by WingJ (lis.epfl.ch/wingj)");
		root.appendChild(comment);
		
		// add boundaries
		addBoundary(root, "D/V", data.getBoundaryLength(WPouchStructureMeasurements.BOUNDARY_DV));
		addBoundary(root, "A/P", data.getBoundaryLength(WPouchStructureMeasurements.BOUNDARY_AP));
		addBoundary(root, "CD_axis", data.getAxisLength(WPouchStructureMeasurements.AXIS_CD));
		addBoundary(root, "CV_axis", data.getAxisLength(WPouchStructureMeasurements.AXIS_CV));
		addBoundary(root, "CA_axis", data.getAxisLength(WPouchStructureMeasurements.AXIS_CA));
		addBoundary(root, "CP_axis", data.getAxisLength(WPouchStructureMeasurements.AXIS_CP));
		
		// add compartments
		// structure was pouch
		addCompartment(root, "structure", data.getCompartmentPerimeter(WPouchStructureMeasurements.COMPARTMENT_POUCH), data.getCompartmentArea(WPouchStructureMeasurements.COMPARTMENT_POUCH));
		addCompartment(root, "DA", data.getCompartmentPerimeter(WPouchStructureMeasurements.COMPARTMENT_DA), data.getCompartmentArea(WPouchStructureMeasurements.COMPARTMENT_DA));
		addCompartment(root, "DP", data.getCompartmentPerimeter(WPouchStructureMeasurements.COMPARTMENT_DP), data.getCompartmentArea(WPouchStructureMeasurements.COMPARTMENT_DP));
		addCompartment(root, "VA", data.getCompartmentPerimeter(WPouchStructureMeasurements.COMPARTMENT_VA), data.getCompartmentArea(WPouchStructureMeasurements.COMPARTMENT_VA));
		addCompartment(root, "VP", data.getCompartmentPerimeter(WPouchStructureMeasurements.COMPARTMENT_VP), data.getCompartmentArea(WPouchStructureMeasurements.COMPARTMENT_VP));
		
		xmldoc_.appendChild(root);
		
		// write to XML file
		FileOutputStream fos = new FileOutputStream(new File(uri));
		OutputFormat of = new OutputFormat("XML","ISO-8859-1",true);
		of.setIndent(1);
		of.setIndenting(true);
		XMLSerializer serializer = new XMLSerializer(fos, of);
		serializer.asDOMSerializer();
		serializer.serialize(xmldoc_.getDocumentElement());
		fos.close();
		
//		TransformerFactory transformerFactory = TransformerFactory.newInstance();
//		Transformer transformer = transformerFactory.newTransformer();
//		DOMSource source = new DOMSource(xmldoc_);
//		StreamResult result = new StreamResult(new File(uri));
//		// Output to console for testing
//		// StreamResult result = new StreamResult(System.out);
//		transformer.transform(source, result);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Reads measurements from XML file and return a WPouchData object containing them (useful for testing). */
	public WPouchStructureMeasurements read(URI uri) throws Exception {
		
		WPouchStructureMeasurements data = new WPouchStructureMeasurements();
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		xmldoc_ = db.parse(new File(uri));
		xmldoc_.getDocumentElement().normalize();
		Element root = xmldoc_.getDocumentElement();
		
		data.setName(root.getAttribute("name"));
		data.setAge(root.getAttribute("age"));

		ArrayList<Element> list = getChildrenByTagName(root, "boundary");
		if (list.size() != WPouchStructureMeasurements.NUM_BOUNDARIES + WPouchStructureMeasurements.NUM_AXES)
			throw new Exception("ERROR: Incorrect number of boundaries and axes in the XML file.");
		
		Element e = null;
		for (int i = 0; i < list.size(); i++) {
			e = list.get(i);
			if (e.getAttribute("name").compareTo("D/V") == 0)
				data.setBoundaryLength(WPouchStructureMeasurements.BOUNDARY_DV, Double.parseDouble(e.getTextContent()));
			else if (e.getAttribute("name").compareTo("A/P") == 0)
				data.setBoundaryLength(WPouchStructureMeasurements.BOUNDARY_AP, Double.parseDouble(e.getTextContent()));
			else if (e.getAttribute("name").compareTo("CD_axis") == 0)
				data.setAxisLength(WPouchStructureMeasurements.AXIS_CD, Double.parseDouble(e.getTextContent()));
			else if (e.getAttribute("name").compareTo("CV_axis") == 0)
				data.setAxisLength(WPouchStructureMeasurements.AXIS_CV, Double.parseDouble(e.getTextContent()));
			else if (e.getAttribute("name").compareTo("CA_axis") == 0)
				data.setAxisLength(WPouchStructureMeasurements.AXIS_CA, Double.parseDouble(e.getTextContent()));
			else if (e.getAttribute("name").compareTo("CP_axis") == 0)
				data.setAxisLength(WPouchStructureMeasurements.AXIS_CP, Double.parseDouble(e.getTextContent()));
		}
		
		list = getChildrenByTagName(root, "compartment");
		if (list.size() != WPouchStructureMeasurements.NUM_COMPARTMENTS)
			throw new Exception("ERROR: Incorrect number of compartments in the XML file.");
		
		Element e2 = null;
		for (int i = 0; i < list.size(); i++) {
			e = list.get(i);
			if (e.getAttribute("name").compareTo("DA") == 0) {
				e2 = getChildrenByTagName(e, "perimeter").get(0); // the list must contain only one element
				data.setCompartmentPerimeter(WPouchStructureMeasurements.COMPARTMENT_DA, Double.parseDouble(e2.getTextContent()));
				e2 = getChildrenByTagName(e, "area").get(0); // the list must contain only one element
				data.setCompartmentArea(WPouchStructureMeasurements.COMPARTMENT_DA, Double.parseDouble(e2.getTextContent()));
			}
			else if (e.getAttribute("name").compareTo("DP") == 0) {
				e2 = getChildrenByTagName(e, "perimeter").get(0); // the list must contain only one element
				data.setCompartmentPerimeter(WPouchStructureMeasurements.COMPARTMENT_DP, Double.parseDouble(e2.getTextContent()));
				e2 = getChildrenByTagName(e, "area").get(0); // the list must contain only one element
				data.setCompartmentArea(WPouchStructureMeasurements.COMPARTMENT_DP, Double.parseDouble(e2.getTextContent()));
			}
			else if (e.getAttribute("name").compareTo("VA") == 0) {
				e2 = getChildrenByTagName(e, "perimeter").get(0); // the list must contain only one element
				data.setCompartmentPerimeter(WPouchStructureMeasurements.COMPARTMENT_VA, Double.parseDouble(e2.getTextContent()));
				e2 = getChildrenByTagName(e, "area").get(0); // the list must contain only one element
				data.setCompartmentArea(WPouchStructureMeasurements.COMPARTMENT_VA, Double.parseDouble(e2.getTextContent()));
			}
			else if (e.getAttribute("name").compareTo("VP") == 0) {
				e2 = getChildrenByTagName(e, "perimeter").get(0); // the list must contain only one element
				data.setCompartmentPerimeter(WPouchStructureMeasurements.COMPARTMENT_VP, Double.parseDouble(e2.getTextContent()));
				e2 = getChildrenByTagName(e, "area").get(0); // the list must contain only one element
				data.setCompartmentArea(WPouchStructureMeasurements.COMPARTMENT_VP, Double.parseDouble(e2.getTextContent()));
			}
			else if (e.getAttribute("name").compareTo("structure") == 0) { // was pouch
				e2 = getChildrenByTagName(e, "perimeter").get(0); // the list must contain only one element
				data.setCompartmentPerimeter(WPouchStructureMeasurements.COMPARTMENT_POUCH, Double.parseDouble(e2.getTextContent()));
				e2 = getChildrenByTagName(e, "area").get(0); // the list must contain only one element
				data.setCompartmentArea(WPouchStructureMeasurements.COMPARTMENT_POUCH, Double.parseDouble(e2.getTextContent()));
			}
		}
		
		return data;
	}
}
