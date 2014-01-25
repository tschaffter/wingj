# WingJ

WingJ is an ImageJ plugin for unsupervised and systematic segmentation of *Drosophila* wing pouch and embryo.

## Setup builder (for development only)

The ant file "build.xml" describes the compilation process for the generation of *lib/wingj_.jar*, for example. The shortcut *Ctral+B* is used to compile WingJ. In order to enable this, one must:

1. Project > Properties > Builders
2. If "New_Builder" exists, there's nothing to do. Otherwise click on *New...*
3. Set the buildfile by clicking on *Browse Workspace...* and select "build.xml"
4. Tab *JRE*: ensure that the latest version of Java is used (must be the JRE used by the workspace)
5. Click *Ok* twice

*Ctrl+B* must now build WingJ JAR file.

## Export WingJ standalone

The standalone binary *wingj.jar* includes ImageJ and WingJ. It can be run using the command:

```
$ java -jar wingj.jar
```

Please refer to the README of the [ImageJ for WingJ](http://84.75.29.70/wingj/imagej/master).

## Export WingJ as an independent plugin for ImageJ

Here we generate a version of *wingj_.jar* that includes all the dependencies:

1. Right click on WingJ project > *Export...*
2. Select *Runnable JAR file*
	* Launch configuration: ImageJ - imagej (anyone does the job, see below)
	* Export destination: wingj/wingj_.jar
	* Extract required libraries into generated JAR

:exclamation: The option *Runnable JAR file* is chosen because it's the only way to include correctly the dependencies. However, the MANIFEST file, which defines which Java class includes the main() method, is not required here because WingJ is run through ImageJ. Since a class containing a main method has to be selected in *Launch configuration*, any existing one does the job.

## Install WingJ plugin

This requires to have ImageJ installed on your system.

1. Download the [latest release of ImageJ](http://rsbweb.nih.gov/ij/download.html) for your operating system (64-bit preferably).
2. Install ImageJ

Then download and install WingJ plugin.

:exclamation: The filename of the plugin ends with '_' (ImageJ standard). Thus, *wingj_.jar* refers to the ImageJ plugin, while *wingj.jar* refers to the standalone release of WingJ. Note that the effective filenames include a string that indicates the version of WingJ.

3. Download the [latest release of WingJ plugin](http://tschaffter.ch/projects/wingj)
4. Move WingJ plugin to
	* Windows 32-bit: *C:\Program Files (x86)\ImageJ\plugins*
	* Windows 64-bit: *C:\Program Files\ImageJ\plugins*

To run WingJ:

5. Run ImageJ > Menu *Plugins* > *WingJ*

## WingJ Analytics

WingJ Analytics enables to get information on how WingJ is used, which version of Java is run, etc.

Analytics code blocks are defined as

```java
// DO NOT REMOVE THIS LINE
// ANALYTICS CODE: START
...
// END
```

## List of Analytics code blocks

#### ch.epfl.lis.wingj.WingJ

At the end of the private constructor WingJ():

```java
	// DO NOT REMOVE THIS LINE
	// ANALYTICS CODE: START
	Analytics.getInstance().start();
	// END
```

In computeExpressionDataset(boolean save) for datasetType == WJSettings.EXPRESSION_DATASET_1D:

```java
		// DO NOT REMOVE THIS LINE
		// ANALYTICS CODE: START
		Analytics.getInstance().addExpressionDataset(ExpressionStats.EXPRESSION_PROFILE, 1);
		// END
```

Idem for:

* datasetType == WJSettings.EXPRESSION_DATASET_2D (ExpressionStats.EXPRESSION_MAP)
* datasetType == WJSettings.EXPRESSION_DATASET_2D_REVERSE (ExpressionStats.EXPRESSION_MAP_REVERSED)
* datasetType == WJSettings.EXPRESSION_DATASET_2D_AGGREGATED (ExpressionStats.MEAN_MODEL)
* datasetType == WJSettings.EXPRESSION_DATASET_COMPOSITE (ExpressionStats.COMPOSITE)

In actionPerformed(ActionEvent e):

```java
	} catch (OutOfMemoryError oome) {
		WJMessage.showMessage(WJSettings.OUT_OF_MEMORY_ERROR_MESSAGE, "ERROR");
		// DO NOT REMOVE THIS LINE
		// ANALYTICS CODE: START
		Analytics.getInstance().incrementNumOutOfMemoryErrors();
		// END
	} catch (Exception e1) {
		WJMessage.showMessage(e1);
	}
```

#### ch.epfl.lis.wingj.expression.ExpressionDataset1D.done()

```java
	if (eStr.contains("OutOfMemoryError")) {
		WJMessage.showMessage(WJSettings.OUT_OF_MEMORY_ERROR_MESSAGE, "ERROR");
		// DO NOT REMOVE THIS LINE
		// ANALYTICS CODE: START
		Analytics.getInstance().incrementNumOutOfMemoryErrors();
		// END
	} else {
		WJMessage.showMessage(e1);
	}
```

Idem in:

* ch.epfl.lis.wingj.expression.ExpressionDataset2D
* ch.epfl.lis.wingj.expression.ExpressionDataset2DAggregated
* ch.epfl.lis.wingj.expression.ExpressionDataset2DReversed

#### ch.epfl.lis.wingj.structure.StructureDetector.step(int)

```java
	while (!detection.test()) {
		if (stop_) // in case detection is stopped during tests
			return moduleIndex_;
		detection.update();
		WJSettings.log(detection.toString() + suffix);
		detection.removeImages();
		detection.run();
	}
	// do level++ here
	moduleIndex_++;
		
	// DO NOT REMOVE THIS LINE
	// ANALYTICS CODE: START
	if (interactive_) // supervised detection
		Analytics.getInstance().addStructureDetection(StructureDetectionStats.SUPERVISED_STRUCTURE_DETECTION, 1.0/detections_.size());
	else // automatic detection
		Analytics.getInstance().addStructureDetection(StructureDetectionStats.AUTO_STRUCTURE_DETECTION, 1.0/detections_.size());
	// END
```

#### ch.epfl.lis.wingj.structure.drosophila.embryo.EmbryoDetector

In openStructure(URI):

```java
public void openStructure(URI uri) throws Exception {

	if (structureProjection_ == null || structureProjection_.getProcessor() == null)
		throw new Exception("INFO: Single image or image stack required.");
		
	if (structure_ == null)
		throw new Exception("ERROR: Structure is null.");
		
	try {
		// DO NOT REMOVE THIS LINE
		// ANALYTICS CODE: START
		Analytics.getInstance().saveStructureDetectionStats(false);
		// END
			
		int w = structureProjection_.getWidth();
		int h = structureProjection_.getHeight();
		EmbryoStructureSnake snake = new EmbryoStructureSnake(w,h);
		structure_.setStructureSnake(snake);
		structure_.read(uri);
			
		if (!structure_.isOrientationKnown()) {
			moduleIndex_ = 3;
			resume();
		} else {
			moduleIndex_ = 4; // required so that isComplete() returns true
			done();
		}
			
		// DO NOT REMOVE THIS LINE
		// ANALYTICS CODE: START
		Analytics.getInstance().saveStructureDetectionStats(true);
		Analytics.getInstance().addStructureDetection(StructureDetectionStats.OPENED_STRUCTURE, 1);
		// END
	} catch (Exception e) {
		// DO NOT REMOVE THIS LINE
		// ANALYTICS CODE: START
		Analytics.getInstance().saveStructureDetectionStats(true);
		// END
		throw e;
	}
}
```

In runManualDetection():

```java
public void runManualDetection() throws Exception {

	try {
		// DO NOT REMOVE THIS LINE
		// ANALYTICS CODE: START
		Analytics.getInstance().saveStructureDetectionStats(false);
		// END
		
		if (tmpSnake_ == null)
			throw new Exception("INFO: Tmp structure snake is null.");
	
		moduleIndex_ = 2;
		resume();
			
		// DO NOT REMOVE THIS LINE
		// ANALYTICS CODE: START
		Analytics.getInstance().saveStructureDetectionStats(true);
		Analytics.getInstance().addStructureDetection(StructureDetectionStats.MANUAL_STRUCTURE_DETECTION, 1);
		// END
	} catch (Exception e) {
		// DO NOT REMOVE THIS LINE
		// ANALYTICS CODE: START
		Analytics.getInstance().saveStructureDetectionStats(true);
		// END
		throw e;
	}
}
```

#### ch.epfl.lis.wingj.structure.drosophila.wingpouch.WPouchDetector

In openStructure(URI):

```java
public void openStructure(URI uri) throws Exception {

	if (structureProjection_ == null || structureProjection_.getProcessor() == null)
		throw new Exception("INFO: Single image or image stack required.");
		
	if (structure_ == null)
		throw new Exception("ERROR: Structure is null.");
		
	try {
		// DO NOT REMOVE THIS LINE
		// ANALYTICS CODE: START
		Analytics.getInstance().saveStructureDetectionStats(false);
		// END
		
		int w = structureProjection_.getWidth();
		int h = structureProjection_.getHeight();
		WPouchStructureSnake snake = new WPouchStructureSnake(w,h);
		structure_.setStructureSnake(snake);
		structure_.read(uri);
			
		if (!structure_.isOrientationKnown()) {
			moduleIndex_ = 7;
			resume();
		} else {
			moduleIndex_ = 8; // required so that isComplete() returns true
			done();
		}
			
		// DO NOT REMOVE THIS LINE
		// ANALYTICS CODE: START
		Analytics.getInstance().saveStructureDetectionStats(true);
		Analytics.getInstance().addStructureDetection(StructureDetectionStats.OPENED_STRUCTURE, 1);
		// END
	} catch (Exception e) {
		// DO NOT REMOVE THIS LINE
		// ANALYTICS CODE: START
		Analytics.getInstance().saveStructureDetectionStats(true);
		// END
		throw e;
	}
}
```

In runManualDetection():

```java
public void runManualDetection() throws Exception {
		
	try {
		// DO NOT REMOVE THIS LINE
		// ANALYTICS CODE: START
		Analytics.getInstance().saveStructureDetectionStats(false);
		// END
	
//		WPouchStructure structure = (WPouchStructure)structure_;
//		WPouchStructureSnake snake = (WPouchStructureSnake)structure.getStructureSnake();
//		
//		if (snake == null)
//			throw new Exception("INFO: Snake structure is null.");
		if (tmpSnake_ == null)
			throw new Exception("INFO: Tmp structure snake is null.");
	
		moduleIndex_ = 6;
		resume();
	
		// DO NOT REMOVE THIS LINE
		// ANALYTICS CODE: START
		Analytics.getInstance().saveStructureDetectionStats(true);
		Analytics.getInstance().addStructureDetection(StructureDetectionStats.MANUAL_STRUCTURE_DETECTION, 1);
		// END
	} catch (Exception e) {
		// DO NOT REMOVE THIS LINE
		// ANALYTICS CODE: START
		Analytics.getInstance().saveStructureDetectionStats(true);
		// END
		throw e;
	}
}
```

Analytics works when using the standalone release on Linux (Java 1.7.0_51).

However this is not the case on Windows 7 (Java 1.7.0_51). The first time 'java -jar wingj.jar' is run, the user is prompt to set a firewall rule. Disabling Windows 7 firewall doesn't change anything. Same issue when using WingJ plugin in ImageJ. Idem when decresing the security level of Java.
