# WingJ

ImageJ plugin for unsupervised and systematic segmentation of *Drosophila* wing pouch and embryo.

## Developers

- [Thomas Schaffter, PhD](https://www.linkedin.com/in/tschaffter/)
- [Ricard Delgado, PhD](https://www.linkedin.com/in/ricarddelgadogonzalo/)

## Resources

Manuals:

- [User Manual (PDF)](manuals/wingj-user-manual.pdf)
- [Developer Manual (PDF)](manuals/wingj-developer-manual.pdf)

Videos:

- [Development of the Drosophila wing](https://youtu.be/ck0YbxXqUZU)
- [WingJ: unsupervised segmentation of the Drosophila wing pouch](https://youtu.be/IY1w_TS7Nac)
- [WingJ: unsupervised segmentation of the Drosophila embryo](https://youtu.be/othDMRkjhAg)
- [WingJ: 3D cell nuclei detection in the Drosophila wing pouch](https://youtu.be/0PD_rzDqoYw)

## Citation

Please include the following citation in any publication describing results achieved using WingJ.

> [Schaffter, Thomas. "From genes to organisms: Bioinformatics system models and software." *École polytechnique fédérale de Lausanne EPFL* (2014).](https://infoscience.epfl.ch/record/196586)

## Build

### Setup builder

The ant file *build.xml* describes the compilation process for the generation of *lib/wingj_.jar*, for example. The shortcut *Ctral+B* is used to compile WingJ. In order to enable this, one must:

1. Project > Properties > Builders
2. If "New_Builder" exists, there's nothing to do. Otherwise click on *New...*
3. Set the buildfile by clicking on *Browse Workspace...* and select *build.xml*
4. Tab *JRE*: ensure that the latest version of Java is used (must be the JRE used by the workspace)
5. Click *Ok* twice

*Ctrl+B* must now build *lib/wingj_.jar*.

### Export WingJ as a standalone app

The standalone binary *wingj_.jar* includes ImageJ and WingJ. It can be run using the command:

```bash
java -jar wingj_.jar
```

The latest release of this standalone app is the file [wingj_.jar](wingj_.jar)
located in the root folder of this repository.

Here we generate a version of *wingj_.jar* that includes all the dependencies:

1. Right click on WingJ project > *Export...*
2. Select *Runnable JAR file*

   - Launch configuration: ImageJ - imagej (anyone does the job, see below)
   - Export destination: wingj/wingj_.jar
   - Extract required libraries into generated JAR

:exclamation: The option *Runnable JAR file* is chosen because it's the only way I found to include easily and correctly the dependencies. However, the MANIFEST file, which defines among others which Java class implements the main() method, is not required here because WingJ is run through ImageJ. Since a class containing a main method has to be selected in *Launch configuration*, any existing one does the job.

## Install

This requires to have ImageJ installed on your system.

1. Download the [latest release of ImageJ](http://rsbweb.nih.gov/ij/download.html) for your operating system (64-bit preferably).
2. Install ImageJ

Then download and install the WingJ plugin.

:exclamation: The filename of the plugin ends with '_' (ImageJ standard). Thus, *wingj_.jar* refers to the ImageJ plugin while *wingj.jar* refers to the standalone release of WingJ. Note that the effective filenames include a string that indicates the version of WingJ, which doesn't need to be removed.

3. Download the [latest release of the WingJ plugin](wingj_.jar)
4. Move the WingJ plugin to

   - Windows 32-bit: *C:\Program Files (x86)\ImageJ\plugins*
   - Windows 64-bit: *C:\Program Files\ImageJ\plugins*

To run WingJ:

5. Run ImageJ > Menu *Plugins* > *WingJ*
