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
% the package 'expression' for analyzing 1D expression profiles.
%
% Author: Thomas Schaffter (thomas.schaff...@gmail.com)
% Version: September 10, 2012

close all; clear all; clc;

%% ------------------------------------------------------------------------
% SETTINGS

addpath('lib');
addpath('expression');

settings = WJSettings.getInstance;

GOOGLEBLUE=[64,93,170]/255;
GOOGLERED=[222,30,50]/255;
GOOGLEYELLOW=[255,199,56]/255;
GOOGLEGREEN=[5,165,74]/255;

% Number of interpolation points required when combining expression
% profiles.
numInterpSamples = settings.expressionProfileNumInterpSamples;

% Number of samples per mean expression profile.
numPlotSamples = 50;

%% ------------------------------------------------------------------------
% LOCATE EXPERIMENTS

% Defines a list including the experiments root directories. See
% User Manual for an example of the files and folders organization.
% Typically, each of the folder name included below should contain one or
% more experiment folder. An experiment folder contains a single
% experiment (e.g. a single wing).
experimentRootDirectories = {'20100716_pmadAB_brkAB_wg-ptcAB_78-79H'
                             '20100716_pmadAB_brkAB_wg-ptcAB_90,5-91,5H'
                             '20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_78-79H'
                             '20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_90-91H'};

% Adds a prefix to the above directory names to obtain complete paths.
experimentRootDirectories = strcat(['benchmarks' filesep],experimentRootDirectories);

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

% % Get only the experiments that contain the marker 'pmadAB'.
% wt_pmadAB = wt.getExperimentsByGeneName('pmadAB'); wt_pmadAB.displayNumExperiments();
% pent_pmadAB = pent.getExperimentsByGeneName('pmadAB'); pent_pmadAB.displayNumExperiments();

% The wt and pent repository are then sorted by the age of the wings. The
% number of experiments for each new repository is displayed.
wt80H = wt.getExperimentsByExperimentName('pmadAB_brkAB_wg-ptcAB_78-79H'); wt80H.displayNumExperiments();
wt90H = wt.getExperimentsByExperimentName('pmadAB_brkAB_wg-ptcAB_90,5-91,5H'); wt90H.displayNumExperiments();
% wt100H = wt.getExperimentsByExperimentName('pmadAB_brkAB_wg-ptcAB_99-100H'); wt100H.displayNumExperiments();
% wt110H = wt.getExperimentsByExperimentName('pmadAB_brkAB_wg-ptcAB_110,5-111,5H'); wt110H.displayNumExperiments();
pent80H = pent.getExperimentsByExperimentName('pmadAB_brkAB_wg-ptcAB_78-79H'); pent80H.displayNumExperiments();
pent90H = pent.getExperimentsByExperimentName('pmadAB_brkAB_wg-ptcAB_90-91H'); pent90H.displayNumExperiments();
% pent100H = pent.getExperimentsByExperimentName('pmadAB_brkAB_wg-ptcAB_99-100H'); pent100H.displayNumExperiments();
% pent110H = pent.getExperimentsByExperimentName('pmadAB_brkAB_wg-ptcAB_110,5-111,5H'); pent110H.displayNumExperiments();

%% ------------------------------------------------------------------------
% 1D EXPRESSION PROFILES

% Here the examples can not be run directly but are there to provide a
% reference document to show how to use specific functions/methods.
% Test datasets can be downloaded on http://lis.epfl.ch/wingj.

% For more information on how to select specific experiments in a
% repository, please refer the the first example given in main_structure.m

% -------------------------------------------------------------------------
% EXAMPLE 1: Opens the expression profiles measured along the D/V boundary.
% The profiles should have been previously exported using WingJ. To
% generate these data, select the D/V boundary as reference boundary and
% set the translation offset to 0. See Supplementary Material for more
% detailed information about how expression profiles are generated. Here we
% take only the expression profiles of 'pmadAB'. The parameter [0 0]
% indicates to load only expression profiles obtained with translation
% offset set to 0. A range of offset can be defined, for instance [-10 10].
wt_pmadAB_80H_profiles = wt80H.get(:).getExpressionProfiles('pmadAB','D/V',[0,0]);
wt_pmadAB_90H_profiles = wt90H.get(:).getExpressionProfiles('pmadAB','D/V',[0,0]);
% wt_pmadAB_100H_profiles = wt100H.get(:).getExpressionProfiles('pmadAB','D/V',[0,0]);
% wt_pmadAB_110H_profiles = wt110H.get(:).getExpressionProfiles('pmadAB','D/V',[0,0]);
pent_pmadAB_80H_profiles = pent80H.get(:).getExpressionProfiles('pmadAB','D/V',[0,0]);
pent_pmadAB_90H_profiles = pent90H.get(:).getExpressionProfiles('pmadAB','D/V',[0,0]);
% pent_pmadAB_100H_profiles = pent100H.get(:).getExpressionProfiles('pmadAB','D/V',[0,0]);
% pent_pmadAB_110H_profiles = pent110H.get(:).getExpressionProfiles('pmadAB','D/V',[0,0]);

% Shows the identified structure for the experiments in the repository
% wt80H. More information on how to use
% Experiment.showStructurePreview() is given in main_structure.m.
figure; pent80H.get(:).showStructurePreview();

% Plots all expression profiles for wild type wings aged of 80H AEL on the
% same plot.
figure; wt_pmadAB_80H_profiles.get(:).plotExpressionProfiles();

title('Wild type');
xlabel('X (um)');
ylabel('[pmadAB] (a.u.)');
% set(gca,'XLim',[-120 100]);
% set(gca,'YLim',[0 140]);
figure; wt_pmadAB_90H_profiles.get(:).plotExpressionProfiles();
title('Pent2-5');
xlabel('X (um)');
ylabel('[pmadAB] (a.u.)');
% set(gca,'XLim',[-120 100]);
% set(gca,'YLim',[0 140]);

% -------------------------------------------------------------------------
% EXAMPLE 2: Computes the mean expression profile of multiple individual
% profiles. A mean expression profile is computed for each of the profile
% repository wt_pmadAB_80H_profiles, wt_pmadAB_90H_profiles, wt_pmadAB_100H_profiles
% and wt_pmadAB_110H_profiles. The four mean expression profiles are
% plotted in different colors and with error bars as obtained using the
% Matlab command 'errorbar' (error bars symmetric and 2*E(i) where E(i) is 
% the distance computed between every sample at location i; type 'help 
% errorbar' in Matlab for more information).
figure
wt_pmadAB_80H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,numInterpSamples,['''Color'',[' num2str(GOOGLEYELLOW) ']']);
hold on
wt_pmadAB_90H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,numInterpSamples,['''Color'',[' num2str(GOOGLEGREEN) ']']);
% wt_pmadAB_100H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,numInterpSamples,['''Color'',[' num2str(GOOGLEBLUE) ']']);
% wt_pmadAB_110H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,numInterpSamples,['''Color'',[' num2str(GOOGLERED) ']']);
% The next two lines can be used to specify the range of the x- and y-axis.
% set(gca,'XLim',[-160 160]);
% set(gca,'YLim',[0 180]);
title('Wild type');
xlabel('X (um)');
ylabel('[pmadAB] (a.u.)');
% legend({'80H','90H','100H','110H'});
legend({'80H','90H'});
set(gca,'XLim',[-120 80]); % sets the same limits to the x- and y-axis
set(gca,'YLim',[0 0.7]);

% Same figure as before but for pent2-5 mutant wings.
figure
pent_pmadAB_80H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,numInterpSamples,['''Color'',[' num2str(GOOGLEYELLOW) ']']);
hold on
pent_pmadAB_90H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,numInterpSamples,['''Color'',[' num2str(GOOGLEGREEN) ']']);
% pent_pmadAB_100H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,numInterpSamples,['''Color'',[' num2str(GOOGLEBLUE) ']']);
% pent_pmadAB_110H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,numInterpSamples,['''Color'',[' num2str(GOOGLERED) ']']);
title('Pent2-5');
xlabel('X (um)');
ylabel('[pmadAB] (a.u.)');
legend({'80H','90H','100H','110H'});
legend({'80H','90H'});
set(gca,'XLim',[-120 80]); % sets the same limits to the x- and y-axis
set(gca,'YLim',[0 0.7]);

% Finally, there are two parameters that can be set and which are defined
% in the Settings section of this file (numInterpSamples and
% numPlotSamples).
