WingJ Matlab Toolbox
====================

We release this toolbox for generating statistics and plots from structure and expression datasets exported from WingJ.

For demonstration purpose, we include four example scripts ready to be executed (see note below), each one aiming to illustrate a different tool. The four scripts are:

- benchmark_structure.m: loads structure datasets exported by WingJ, and generates plots and statistical tests.
- benchmark_expression_profiles.m: loads expression datasets exported by WingJ and generates mean expression profiles from individual profiles.
- benchmark_expression_maps.m: loads expression datasets exported by WingJ and generates mean expression maps from individual maps.
- benchmark_nuclei_detection.m: loads stacks of confocal fluorescence images (TO-PRO) and applies our 3D cell nuclei detection.

Before running the example scripts, please download the WingJ Matlab Toolbox image benchmark (lis.epfl.ch/wingj) and copy/paste all the forders it contains in the "benchmarks" folder of the toolbox.

You can refer to the video tutorials and the WingJ User Manual for more information on WingJ and its Matlab toolbox (lis.epfl.ch/wingj). Please feel free to contact me (thomas.schaffter@gmail.com) with bug reports, feature requests, and information on related projects.