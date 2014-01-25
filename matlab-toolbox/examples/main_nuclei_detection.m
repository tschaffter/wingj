% Copyright (c) 2010-2013 Thomas Schaffter, Ricard Delgado-Gonzalo
%
% We release this software open source under a Creative Commons Attribution
% -NonCommercial 3.0 Unported License. Please cite the papers listed on 
% http://lis.epfl.ch/wingj when using WingJ in your publication.
%
% For commercial use, please contact Thomas Schaffter 
% (thomas.schaff...@gmail.com).
%
% A brief description of the license is available at 
% http://creativecommons.org/licenses/by-nc/3.0/ and the full license at 
% http://creativecommons.org/licenses/by-nc/3.0/legalcode.
%
% The above copyright notice and this permission notice shall be included 
% in all copies or substantial portions of the Software.
%
% THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS 
% OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
% MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
% IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY 
% CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
% OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR 
% THE USE OR OTHER DEALINGS IN THE SOFTWARE.
%
%
% This script gives a few examples to illustrate the methods implemented in
% the package 'nuclei_detector'.
%
% Author: Thomas Schaffter (thomas.schaff...@gmail.com)
% Version: September 10, 2012

close all; clear all; clc;

%% ------------------------------------------------------------------------
% SETTINGS

addpath('..')
addpath('../lib');
addpath('../nuclei_detector');

settings = WJSettings.getInstance;

%% ------------------------------------------------------------------------
% LOCATE EXPERIMENTS

% Defines a list including the root directory for each experiment. See
% documentation for an example of the files and folders organization.
% Typically, each of the folder name included below should contain one or
% more experiment folder. An experiment folder contains a single
% experiment (e.g. a single wing).
% For the nuclei detection, we use the nuclear staining TO-PRO
experimentRootDirectories = {'20091222_salLACZ_wg-ptcAB_TOPRO_73-74H/'
                             '20091222_salLACZ_wg-ptcAB_TOPRO_96-97H/'
                             '20091222_salLACZ_wg-ptcAB_TOPRO_114-115H/'
                             '20091222_wg-ptcAB_pentGFP_TOPRO_72-73H/'
                             '20091222_wg-ptcAB_pentGFP_TOPRO_95,5-96,5H/'
                             '20091222_wg-ptcAB_pentGFP_TOPRO_111-112H/'
                             '20100426_pent2-5-_ombLACZ_wg-ptcAB_TOPRO_72,5-73,5H'
                             '20100426_pent2-5-_ombLACZ_wg-ptcAB_TOPRO_79-80H'
                             '20100422_pent2-5-_ombLACZ_wg-ptcAB_TOPRO_91-92H'
                             '20100421_pent2-5-_ombLACZ_wg-ptcAB_TOPRO_100-101H'
                             '20100430_pent2-5-_ombLACZ_wg-ptcAB_TOPRO_112-113H'};
                              
% Adds a prefix to the above directory names to obtain complete paths.
experimentRootDirectories = strcat('/mnt/extra/wingj_benchmarks/',experimentRootDirectories);

%% ------------------------------------------------------------------------
% LOAD EXPERIMENTS

% Creates the Experiment objets by listing every experiment folder contained
% in the above 'root experiment folders'. Append '_IGNORE' to the name of
% an experiment folder to discard it.
repository = ExperimentList(experimentRootDirectories);

% Prepares/organizes batches of experiments to ease further analyses.
% First, experiments are separated into wild type and 'pent2-5' mutants
wt = repository.getWildTypeExperiments();
pent = repository.getExperimentsByMutantName('pent2-5');

% Create one repository per gene category
wt_TOPRO_salLACZ = wt.getExperimentsByGeneName('salLACZ'); wt_TOPRO_salLACZ.displayNumExperiments();
wt_TOPRO_pentGFP = wt.getExperimentsByGeneName('pentGFP'); wt_TOPRO_pentGFP.displayNumExperiments();
pent_TOPRO = pent.getExperimentsByGeneName('ombLACZ'); pent_TOPRO.displayNumExperiments();

% The repositories are then sorted by the age of the wings. The
% number of experiments for each new repository is displayed.
wt_TOPRO_salLACZ_73H = wt_TOPRO_salLACZ.getExperimentsByAge([73,74]); wt_TOPRO_salLACZ_73H.displayNumExperiments();
wt_TOPRO_salLACZ_96H = wt_TOPRO_salLACZ.getExperimentsByAge([96,97]); wt_TOPRO_salLACZ_96H.displayNumExperiments();
wt_TOPRO_salLACZ_114H = wt_TOPRO_salLACZ.getExperimentsByAge([114,115]); wt_TOPRO_salLACZ_114H.displayNumExperiments();

wt_TOPRO_pentGFP_72H = wt_TOPRO_pentGFP.getExperimentsByAge([72,73]); wt_TOPRO_pentGFP_72H.displayNumExperiments();
wt_TOPRO_pentGFP_95H = wt_TOPRO_pentGFP.getExperimentsByAge([95.5,96.5]); wt_TOPRO_pentGFP_95H.displayNumExperiments();
wt_TOPRO_pentGFP_111H = wt_TOPRO_pentGFP.getExperimentsByAge([111,112]); wt_TOPRO_pentGFP_111H.displayNumExperiments();

pent_TOPRO_72H = pent_TOPRO.getExperimentsByAge([72.5,73.5]); pent_TOPRO_72H.displayNumExperiments();
pent_TOPRO_79H = pent_TOPRO.getExperimentsByAge([79,80]); pent_TOPRO_79H.displayNumExperiments();
pent_TOPRO_91H = pent_TOPRO.getExperimentsByAge([91,92]); pent_TOPRO_91H.displayNumExperiments();
pent_TOPRO_100H = pent_TOPRO.getExperimentsByAge([100,101]); pent_TOPRO_100H.displayNumExperiments();
pent_TOPRO_112H = pent_TOPRO.getExperimentsByAge([112,113]); pent_TOPRO_112H.displayNumExperiments();

%% ------------------------------------------------------------------------
% NUCLEI DETECTION

% Here the examples can not be run directly but are there to provide a
% reference document to show how to use specific functions/methods.
% Test datasets can be downloaded on http://lis.epfl.ch/wingj

% For more information on how to select specific experiments in a
% repository, please refer the the first example given in main_structure.m

% IMPORTANT: There is a memory leak depending on the version of Matlab
% used. We recommend to first run nuclei detection on a few experiments
% rather than on a repository containg dozen of experiments.

% -------------------------------------------------------------------------
% EXAMPLE 1: run nuclear detection per repository. The output data are
% saved for each experiment in their folder WingJ/Matlab/NucleiDetector
% (defined in Settings). First, run the nuclei detection on a single
% experiment. The parameter value 'TOPRO' indicates which image channel use
% for the detection.
wt_TOPRO_salLACZ_73H.get(1).detectNuclei('TOPRO');

% Running the nuclei detection for every experiments included in a
% repository.
wt_TOPRO_salLACZ_73H.get(:).detectNuclei('TOPRO');

% By default, images where the detected nuclei are shown on top of the
% input image (here TO-PRO staining) are NOT saved. Idem for the movie made
% out of these images AVI format. To save both images and the AVI movie
% (FPS can be set in Settings), use:
wt_TOPRO_salLACZ_73H.get(1).detectNuclei('TOPRO',true);

% -------------------------------------------------------------------------
% EXAMPLE 2: Boxplot the number of nuclei for wt_TOPRO_salLACZ_ and
% wt_TOPRO_pentGFP experiments.
wt_TOPRO_salLACZ_73H.get(:).detectNuclei('TOPRO');
wt_TOPRO_salLACZ_96H.get(:).detectNuclei('TOPRO');
wt_TOPRO_salLACZ_114H.get(:).detectNuclei('TOPRO');
wt_TOPRO_pentGFP_72H.get(:).detectNuclei('TOPRO');
wt_TOPRO_pentGFP_95H.get(:).detectNuclei('TOPRO');
wt_TOPRO_pentGFP_111H.get(:).detectNuclei('TOPRO');

% OR
% IMPORTANT: See comments at the beginning of the main section about
% possible memory leak.
wt_TOPRO_salLACZ.get(:).detectNuclei('TOPRO');
wt_TOPRO_pentGFP.get(:).detectNuclei('TOPRO');

% Get the number of nuclei for each experiment. Note that the method
% Experiment.nucleiDetection() save the number of nuclei detected to file.
% The methods below load this information form these files and thus do not
% require to systematically run again Experiment.nucleiDetection(). Running
% Experiment.nucleiDetection() once is enough.

wt_TOPRO_salLACZ_73H_num_nuclei = wt_TOPRO_salLACZ_73H.get(:).getNumNuclei();
wt_TOPRO_salLACZ_96H_num_nuclei = wt_TOPRO_salLACZ_96H.get(:).getNumNuclei();
wt_TOPRO_salLACZ_114H_num_nuclei = wt_TOPRO_salLACZ_114H.get(:).getNumNuclei();
wt_TOPRO_pentGFP_72H_num_nuclei = wt_TOPRO_pentGFP_72H.get(:).getNumNuclei();
wt_TOPRO_pentGFP_95H_num_nuclei = wt_TOPRO_pentGFP_95H.get(:).getNumNuclei();
wt_TOPRO_pentGFP_111H_num_nuclei = wt_TOPRO_pentGFP_111H.get(:).getNumNuclei();

% Here the selected repositories would be useful to check that there are no
% differences in term of number of nuclei between the experiments obtained
% for salLACZ and pentGFP.

data = [wt_TOPRO_salLACZ_73H_num_nuclei;wt_TOPRO_salLACZ_96H_num_nuclei;wt_TOPRO_salLACZ_114H_num_nuclei;...
    wt_TOPRO_pentGFP_72H_num_nuclei;wt_TOPRO_pentGFP_95H_num_nuclei;wt_TOPRO_pentGFP_111H_num_nuclei];
G = boxplot_groups({wt_TOPRO_salLACZ_73H_num_nuclei,wt_TOPRO_salLACZ_96H_num_nuclei,wt_TOPRO_salLACZ_114H_num_nuclei,...
    wt_TOPRO_pentGFP_72H_num_nuclei,wt_TOPRO_pentGFP_95H_num_nuclei,wt_TOPRO_pentGFP_111H_num_nuclei});
labels = {'wt_salLACZ_73H','wt_salLACZ_96H','wt_salLACZ_114H',...
    'wt_pentGFP_73H','wt_pentGFP_96H','wt_pentGFP_114H'};

boxplot(data,G,'labels',labels);
set(gcf, 'OuterPosition',[0 0 600 500]);
xlabel('Experiment');
ylabel('Number of nuclei detected (wing pouch)')

% The same approach can be used to compare the number of nuclei in pent2-5
% mutant wings (repositories pent_TOPRO_72H, pent_TOPRO_79H, etc.).
