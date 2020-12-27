# WingJ

ImageJ plugin for unsupervised and systematic segmentation of *Drosophila* wing pouch and embryo.

## Developers

- [Thomas Schaffter, PhD](https://www.linkedin.com/in/tschaffter/)
- [Ricard Delgado, PhD](https://www.linkedin.com/in/ricarddelgadogonzalo/)

## Resources

### Manuals

- [User Manual (PDF)](manuals/wingj-user-manual.pdf)
- [Developer Manual (PDF)](manuals/wingj-developer-manual.pdf)

### Videos

- [Development of the Drosophila wing](https://youtu.be/ck0YbxXqUZU)
- [WingJ: unsupervised segmentation of the Drosophila wing pouch](https://youtu.be/IY1w_TS7Nac)
- [WingJ: unsupervised segmentation of the Drosophila embryo](https://youtu.be/othDMRkjhAg)
- [WingJ: 3D cell nuclei detection in the Drosophila wing pouch](https://youtu.be/0PD_rzDqoYw)

### Models

- [Robust Quantitative Model of the Drosophila Wing Pouch](https://tschaffter.github.io/wingj/wingviewer/)

## Citation

Please include the following citation in any publication describing results achieved using WingJ.

> [Schaffter, Thomas. "From genes to organisms: Bioinformatics system models and software." *École polytechnique fédérale de Lausanne EPFL* (2014).](https://infoscience.epfl.ch/record/196586)

## Quick Start

The easiest way to start WingJ is to download the standalone version located in
the root folder of this repository ([wingj.jar](wingj.jar)), then run

```bash
java -jar wingj.jar
```

WingJ is also available as a plugin that can be added to an existing installation
of ImageJ. See below for more information.

## Build

### Export WingJ as a plugin for ImageJ

The ant file *build.xml* describes the compilation process for the generation of *lib/wingj_.jar*. The shortcut *Ctral+B* is used to compile WingJ. In order to enable this, one must:

1. Project > Properties > Builders
2. If "New_Builder" exists, there's nothing to do. Otherwise click on *New...*
3. Set the buildfile by clicking on *Browse Workspace...* and select *build.xml*
4. Tab *JRE*: ensure that the latest version of Java is used (must be the JRE used by the workspace)
5. Click *Ok* twice

*Ctrl+B* must now build *lib/wingj_.jar*.

### Export WingJ as a standalone app

Here we generate a version of *wingj.jar* that includes all the dependencies:

1. Right click on WingJ project > *Export...*
2. Select *Runnable JAR file*

   - Launch configuration: ImageJ - imagej
   - Export destination: wingj.jar
   - Extract required libraries into generated JAR

The option *Runnable JAR file* is chosen because it's the only way I found to include easily and correctly the dependencies. However, the MANIFEST file, which defines among others which Java class implements the main() method, is not required here because WingJ is run through ImageJ. Since a class containing a main method has to be selected in *Launch configuration*, any existing one does the job.

## Add the WingJ plugin to ImageJ

In case you want to add the WingJ plugin to an existing installation of ImageJ,
you need first to build the plugin (see above) or download the version available
([lib/wingj_.jar](lib/wingj_.jar)). Then,

- Add *wingj_.jar* to the *plugins* folder of ImageJ
- In ImageJ: ImageJ > Menu *Plugins* > *WingJ* to start WingJ
