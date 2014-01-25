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
% the package 'expression' for analyzing 2D expression maps.
%
% Author: Thomas Schaffter (thomas.schaff...@gmail.com)
% Version: September 10, 2012

close all; clear all; clc;

%% ------------------------------------------------------------------------
% SETTINGS

addpath('lib');
addpath('expression');

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
% 2D EXPRESSION MAPS

% Here the examples can not be run directly but are there to provide a
% reference document to show how to use specific functions/methods.
% Test datasets can be downloaded on http://lis.epfl.ch/wingj.

% For more information on how to select specific experiments in a
% repository, please refer the the first example given in main_structure.m

% -------------------------------------------------------------------------
% EXAMPLE 1: Opens the expression maps associated to the marker 'pmadAB'
% and obtained using the offset -100 in WingJ which correspond to
% expression maps conserving the A/P boundary. Below we create an expression
% map repository for different wing ages. Exceptions are used to discard
% the loading of 'expression_2D' files which are not expression maps.
exceptions = {'-raw','-density'};
wt_pmadAB_80H_maps = wt80H.get(:).getExpressionMaps('pmadAB',[-100,-100],exceptions);
wt_pmadAB_90H_maps = wt90H.get(:).getExpressionMaps('pmadAB',[-100,-100],exceptions);
% wt_pmadAB_100H_maps = wt100H.get(:).getExpressionMaps('pmadAB',[-100,-100],exceptions);
% Here the experiment 3 is discarded because the wing is damaged.
% pent_pmadAB_80H_maps = pent80H.get([1 2 4 5]).getExpressionMaps('pmadAB',[-100,-100],exceptions);
pent_pmadAB_80H_maps = pent80H.get(:).getExpressionMaps('pmadAB',[-100,-100],exceptions);
pent_pmadAB_90H_maps = pent90H.get(:).getExpressionMaps('pmadAB',[-100,-100],exceptions);
% pent_pmadAB_100H_maps = pent100H.get(:).getExpressionMaps('pmadAB',[-100,-100],exceptions);

% Shows the identified structure for the experiments in the
% repository wt90H. More information on how to use
% Experiment.showStructurePreview() is given in main_structure.m.
figure; wt90H.get(:).showStructurePreview();

% Shows expression maps
figure; wt_pmadAB_90H_maps.get(:).showExpressionMaps();

% -------------------------------------------------------------------------
% EXAMPLE 2: Computes the mean and std expression map from multple
% expression maps.
figure; wt_pmadAB_90H_maps.get(:).showMeanExpressionMap();
figure; wt_pmadAB_90H_maps.get(:).showStdExpressionMap();
