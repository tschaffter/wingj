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

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sun.org.apache.xerces.internal.dom.DocumentImpl;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

import big.ij.snake2D.Snake2DNode;

/** 
 * XML parser for importing and exporting WPouchStructure objects.
 * <p>
 * The organization of the XML file is:
 * <p>
 * To not break the compatibility with previously generated XML file, the old parsing
 * methods are used if an exception is generated during the execution of the new parsing
 * methods. If the old parsing methods are used, the orientation is not recovered and
 * the user must be asked to validate the orientation manually (the automatic inference
 * of the orientation must not be run).
 * 
 * @version February 11, 2013
 *
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 */
public class WPouchStructureParser {
	
//	 * <structure>
//	 * 		<name></name>
//	 * 		<snake>
//	 * 			<M0></M0>
//	 * 			<node index="0">
//	 * 				<x>...</x>
//	 * 				<y>...</y>
//	 * 			</node>
//	 * 			<node index="1">
//	 * 				...
//	 * 			</node>
//	 * 		</snake>
//	 * 		<orientation>
//	 * 			<com name="disc">
//	 * 				<x></x>
//	 * 				<y></y>
//	 * 			</com>
//	 *			<com name="pouch"> // it's not really a com but...
//	 * 				<x></x>
//	 * 				<y></y>
//	 * 			</com>
//	 * 			<com name="DA">
//	 * 				<x></x>
//	 * 				<y></y>
//	 * 			</com>
//	 * 			<com name="DP">
//	 * 				...
//	 * 			</com>
//	 * 		</orientation>
//	 * </structure>
//	 * 
//	 * Old organization:
//	 * 
//	 * <structure>
//	 * 		<M0></M0>
//	 * 		<node index="0">
//	 * 			<x>...</x>
//	 * 			<y>...</y>
//	 * 		</node>
//	 * 		<node index="1">
//	 * 			...
//	 * 		</node>
//	 * </structure>

	/** Structure. */
	private WPouchStructure structure_ = null;
	
	/** XML DOM document. */
	Document xmldoc_ = null;
	
	// ============================================================================
	// PRIVATE METHODS
	
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
	
	/** Constructor. */
	public WPouchStructureParser(WPouchStructure structure) {
		
		structure_ = structure;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Write method to save a WPouchStructure to XML. */
	public void write(URI uri) throws Exception {
		
		if (structure_ == null)
			throw new Exception("ERROR: Structure is null.");
		
		// XML DOM document
		xmldoc_ = new DocumentImpl();
		
//		String pName = "pName";
//		DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
//		DOMImplementation implementation = registry.getDOMImplementation("XML 1.0");
//		DocumentType type = implementation.createDocumentType(pName, null, null);
//		xmldoc_ = implementation.createDocument(null, pName, type);
		
		// root element
		Element root = xmldoc_.createElement("structure");
		
		// add comment
		Comment comment = xmldoc_.createComment("Generated by WingJ on " + new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss").format(new Date()) + " (lis.epfl.ch/wingj)");
		root.appendChild(comment);
		
		Element e = null;
		
		// add structure name (actually it's the experiment name)
		e = xmldoc_.createElementNS(null, "name");
		e.appendChild(xmldoc_.createTextNode(structure_.getName()));
		root.appendChild(e);
		
		// add age
//		e = xmldoc_.createElementNS(null, "age");
//		e.appendChild(xmldoc_.createTextNode(new Integer(structure_.getAge()).toString()));
//		root.appendChild(e);
		
		// add the snake section
		e = xmldoc_.createElementNS(null, "snake");
		
		// add MO
		Element e2 = xmldoc_.createElementNS(null, "M0");
		// TODO: get M0
		e2.appendChild(xmldoc_.createTextNode(new Integer(structure_.getStructureSnake().getNumControlPointsPerSegment()).toString()));
		e.appendChild(e2);
		
		// add snake nodes
		Element xe = null;
		Element ye = null;
		Snake2DNode[] nodes = structure_.getStructureSnake().getNodes();
		for (int i = 0; i < nodes.length; i++) {
			e2 = xmldoc_.createElementNS(null, "node");
			e2.setAttribute("index", new Integer(i).toString());
			xe = xmldoc_.createElementNS(null, "x");
			xe.appendChild(xmldoc_.createTextNode(new Double(nodes[i].getX()).toString()));
			ye = xmldoc_.createElementNS(null, "y");
			ye.appendChild(xmldoc_.createTextNode(new Double(nodes[i].getY()).toString()));
			e2.appendChild(xe);
			e2.appendChild(ye);
			e.appendChild(e2);
		}
		root.appendChild(e);
		
		// add the orientation section
		e = xmldoc_.createElementNS(null, "orientation");
		
		// center of the wing disc
		e2 = xmldoc_.createElementNS(null, "com");
		e2.setAttribute("name", "disc");
		xe = xmldoc_.createElementNS(null, "x");
		xe.appendChild(xmldoc_.createTextNode(new Double(structure_.discCenter_.x).toString()));
		ye = xmldoc_.createElementNS(null, "y");
		ye.appendChild(xmldoc_.createTextNode(new Double(structure_.discCenter_.y).toString()));
		e2.appendChild(xe);
		e2.appendChild(ye);
		e.appendChild(e2);
		
		// center of the wing pouch (it's not really com but...)
		e2 = xmldoc_.createElementNS(null, "com");
		e2.setAttribute("name", "pouch");
		xe = xmldoc_.createElementNS(null, "x");
		xe.appendChild(xmldoc_.createTextNode(new Double(structure_.center_.x).toString()));
		ye = xmldoc_.createElementNS(null, "y");
		ye.appendChild(xmldoc_.createTextNode(new Double(structure_.center_.y).toString()));
		e2.appendChild(xe);
		e2.appendChild(ye);
		e.appendChild(e2);
		
		// DA compartment center of mass
		e2 = xmldoc_.createElementNS(null, "com");
		e2.setAttribute("name", "DA");
		xe = xmldoc_.createElementNS(null, "x");
		xe.appendChild(xmldoc_.createTextNode(new Double(structure_.getDACompartment().centroid().x).toString()));
		ye = xmldoc_.createElementNS(null, "y");
		ye.appendChild(xmldoc_.createTextNode(new Double(structure_.getDACompartment().centroid().y).toString()));
		e2.appendChild(xe);
		e2.appendChild(ye);
		e.appendChild(e2);
		
		// DP compartment center of mass
		e2 = xmldoc_.createElementNS(null, "com");
		e2.setAttribute("name", "DP");
		xe = xmldoc_.createElementNS(null, "x");
		xe.appendChild(xmldoc_.createTextNode(new Double(structure_.getDPCompartment().centroid().x).toString()));
		ye = xmldoc_.createElementNS(null, "y");
		ye.appendChild(xmldoc_.createTextNode(new Double(structure_.getDPCompartment().centroid().y).toString()));
		e2.appendChild(xe);
		e2.appendChild(ye);
		e.appendChild(e2);
		
		// VA compartment center of mass
		e2 = xmldoc_.createElementNS(null, "com");
		e2.setAttribute("name", "VA");
		xe = xmldoc_.createElementNS(null, "x");
		xe.appendChild(xmldoc_.createTextNode(new Double(structure_.getVACompartment().centroid().x).toString()));
		ye = xmldoc_.createElementNS(null, "y");
		ye.appendChild(xmldoc_.createTextNode(new Double(structure_.getVACompartment().centroid().y).toString()));
		e2.appendChild(xe);
		e2.appendChild(ye);
		e.appendChild(e2);
		
		// VP compartment center of mass
		e2 = xmldoc_.createElementNS(null, "com");
		e2.setAttribute("name", "VP");
		xe = xmldoc_.createElementNS(null, "x");
		xe.appendChild(xmldoc_.createTextNode(new Double(structure_.getVPCompartment().centroid().x).toString()));
		ye = xmldoc_.createElementNS(null, "y");
		ye.appendChild(xmldoc_.createTextNode(new Double(structure_.getVPCompartment().centroid().y).toString()));
		e2.appendChild(xe);
		e2.appendChild(ye);
		e.appendChild(e2);
		
		root.appendChild(e);
		
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
	
	/** Read method to construct a WPouchStructure from an XML file. */
	public void read(URI uri) throws Exception {
		
		if (structure_ == null)
			throw new Exception("ERROR: Structure is null.");
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		xmldoc_ = db.parse(uri.toURL().openStream());
		xmldoc_.getDocumentElement().normalize();
		Element root = xmldoc_.getDocumentElement();
		ArrayList<Element> list = null;
		
		// read structure name
		list = getChildrenByTagName(root, "name");
		if (list == null || list.size() < 1)
			throw new Exception("ERROR: Structure name is missing.");
		String name = list.get(0).getTextContent();
		
//		list = getChildrenByTagName(root, "age");
//		if (list == null || list.size() < 1)
//			throw new Exception("ERROR: Structure age is missing.");
//		String age = list.get(0).getTextContent();
		
//		// read scale
//		list = getChildrenByTagName(root, "scale");
//		if (list == null || list.size() < 1)
//			throw new Exception("ERROR: Scale is missing.");
//		Double scale = new Double(list.get(0).getTextContent()).doubleValue();
		
		// read snake section
		list = getChildrenByTagName(root, "snake");
		if (list == null || list.size() < 1)
			throw new Exception("ERROR: Snake section is missing.");
		
		Element s = list.get(0);
		
		// read M0
		list = getChildrenByTagName(s, "M0");
		if (list == null || list.size() < 1)
			throw new Exception("ERROR: M0 is missing.");
		Integer M0 = new Double(list.get(0).getTextContent()).intValue();
		
		// read nodes
		list = getChildrenByTagName(s, "node");
		if (list == null || list.size() < 3)
			throw new Exception("ERROR: At least three snake nodes are required.");
		Snake2DNode[] nodes = new Snake2DNode[list.size()];
		for (int i = 0; i < nodes.length; i++) {
			nodes[i] = new Snake2DNode(new Double(getChildrenByTagName(list.get(i), "x").get(0).getTextContent()),
									   new Double(getChildrenByTagName(list.get(i), "y").get(0).getTextContent()));
		}
		
		// read orientation section
		list = getChildrenByTagName(root, "orientation");
		if (list == null || list.size() < 1)
			throw new Exception("ERROR: Orientation section is missing.");
		s = list.get(0);
		
		// read center of mass of wing disc
		list = getChildrenByTagName(s, "com");
		if (list == null || list.size() < 1)
			throw new Exception("ERROR: Wing disc com missing.");
		double x = new Double(getChildrenByTagName(list.get(0), "x").get(0).getTextContent());
		double y = new Double(getChildrenByTagName(list.get(0), "y").get(0).getTextContent());
		Point2D.Double discCom = new Point2D.Double(x, y);
		
		// read wing pouch center
		if (list == null || list.size() < 2)
			throw new Exception("ERROR: Wing pouch com missing.");
		x = new Double(getChildrenByTagName(list.get(1), "x").get(0).getTextContent());
		y = new Double(getChildrenByTagName(list.get(1), "y").get(0).getTextContent());
		@SuppressWarnings("unused")
		Point2D.Double pouchCenter = new Point2D.Double(x, y);
		
		// read DA compartment com
		list = getChildrenByTagName(s, "com");
		if (list == null || list.size() < 3)
			throw new Exception("ERROR: DA compartment com missing.");
		x = new Double(getChildrenByTagName(list.get(2), "x").get(0).getTextContent());
		y = new Double(getChildrenByTagName(list.get(2), "y").get(0).getTextContent());
		Point2D.Double daCom = new Point2D.Double(x, y);
		
		// read DP compartment com
		list = getChildrenByTagName(s, "com");
		if (list == null || list.size() < 4)
			throw new Exception("ERROR: DP compartment com missing.");
		x = new Double(getChildrenByTagName(list.get(3), "x").get(0).getTextContent());
		y = new Double(getChildrenByTagName(list.get(3), "y").get(0).getTextContent());
		Point2D.Double dpCom = new Point2D.Double(x, y);
		
		// read VA compartment com
		list = getChildrenByTagName(s, "com");
		if (list == null || list.size() < 5)
			throw new Exception("ERROR: VA compartment com missing.");
		x = new Double(getChildrenByTagName(list.get(4), "x").get(0).getTextContent());
		y = new Double(getChildrenByTagName(list.get(4), "y").get(0).getTextContent());
		Point2D.Double vaCom = new Point2D.Double(x, y);
		
		// read VP compartment com
		list = getChildrenByTagName(s, "com");
		if (list == null || list.size() < 6)
			throw new Exception("ERROR: VP compartment com missing.");
		x = new Double(getChildrenByTagName(list.get(5), "x").get(0).getTextContent());
		y = new Double(getChildrenByTagName(list.get(5), "y").get(0).getTextContent());
		Point2D.Double vpCom = new Point2D.Double(x, y);
		
		// set structure direct members
		structure_.setName(name);
		
		// build a consistent snake (must already have been created by the detector)
		
		//FIXME My awesome hack (revise)
		WPouchStructureSnake snake = new WPouchStructureSnake();
//		if (structure_ instanceof ch.epfl.lis.wingj.structure.drosophila.embryo.EmbryoStructure){
//			 snake = new EmbryoStructureSnake(); // (WPouchStructureSnake)structure_.getStructureSnake();
//		}else if(structure_ instanceof ch.epfl.lis.wingj.structure.drosophila.wingpouch.WPouchStructure){
//			snake = new WPouchStructureSnake();
//		}
		
		snake.setNumControlPointsPerSegment(M0);
		snake.initialize(nodes);
		structure_.discCenter_ = discCom;
		structure_.center_ = snake.getWPouchCenter();//pouchCenter;
		Point2D.Double[] coms = new Point2D.Double[4];
		coms[0] = daCom;
		coms[1] = dpCom;
		coms[2] = vaCom;
		coms[3] = vpCom;
		structure_.setStructureSnake(snake);
		
//		WJSettings.log("da com: " + daCom);
//		WJSettings.log("dp com: " + dpCom);
//		WJSettings.log("va com: " + vaCom);
//		WJSettings.log("vp com: " + vpCom);
		
		// set the orientation
		WPouchOrientationDetection orientationModule = new WPouchOrientationDetection();
		orientationModule.setOrientationFromCompartmentCenterOfMass(structure_, coms);
		structure_.isOrientationKnown(true);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Old read method used in the past to read a WingSnake object from XML. */
	public void readVersion1(URI uri) throws Exception {
		
		if (structure_ == null)
			throw new Exception("ERROR: Structure snake is null.");
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		xmldoc_ = db.parse(uri.toURL().openStream());
		xmldoc_.getDocumentElement().normalize();
		Element root = xmldoc_.getDocumentElement();
		
		ArrayList<Element> list = null;
//		// read wing disc center
//		ArrayList<Element> list = getChildrenByTagName(root, "wdisc_center");
//		if (list == null || list.size() < 1)
//			throw new Exception("ERROR: Parameter wdisc_center missing");
//		snake_.setWDiscCenter(new Double(getChildrenByTagName(list.get(0), "x").get(0).getTextContent()),
//							  new Double(getChildrenByTagName(list.get(0), "y").get(0).getTextContent()));
//		
//		// read wing pouch center
//		list = getChildrenByTagName(root, "wpouch_center");
//		if (list == null || list.size() < 1)
//			throw new Exception("ERROR: Parameter wpouch_center missing");
//		snake_.setWDiscCenter(new Double(getChildrenByTagName(list.get(0), "x").get(0).getTextContent()),
//							  new Double(getChildrenByTagName(list.get(0), "y").get(0).getTextContent()));
		
		// read M0
		list = getChildrenByTagName(root, "M0");
		if (list == null || list.size() < 1)
			throw new Exception("ERROR: Parameter M0 missing.");
		int M0 = new Integer(list.get(0).getTextContent()).intValue();
		
		// read nodes
		list = getChildrenByTagName(root, "node");
		if (list == null || list.size() < 3)
			throw new Exception("ERROR: At least three parameters node are required.");
		Snake2DNode[] nodes = new Snake2DNode[list.size()];
		for (int i = 0; i < nodes.length; i++) {
			nodes[i] = new Snake2DNode(new Double(getChildrenByTagName(list.get(i), "x").get(0).getTextContent()),
									   new Double(getChildrenByTagName(list.get(i), "y").get(0).getTextContent()));
		}
		
		// build a consistent snake
		WPouchStructureSnake snake = new WPouchStructureSnake(); //(WPouchStructureSnake)structure_.getStructureSnake();
		snake.setNumControlPointsPerSegment(M0);
		snake.initialize(nodes);
		structure_.discCenter_ = snake.getWDiscCenter();
		structure_.center_ = snake.getWPouchCenter();
		structure_.setStructureSnake(snake);

		structure_.isOrientationKnown(false);
	}

	// ============================================================================
	// SETTERS AND GETTERS
	
}