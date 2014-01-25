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
% the package 'structure'.
%
% Author: Thomas Schaffter (thomas.schaff...@gmail.com)
% Version: September 10, 2012

close all; clear all; clc;

%% ------------------------------------------------------------------------
% WJSettings

addpath('lib');
addpath('structure');

WJSettings = WJSettings.getInstance;

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

% The wt and pent repository are then sorted by the age of the wings. The
% number of experiments for each new repository is displayed.
wt78H = wt.getExperimentsByExperimentName('pmadAB_brkAB_wg-ptcAB_78-79H'); wt78H.displayNumExperiments();
wt90H = wt.getExperimentsByExperimentName('pmadAB_brkAB_wg-ptcAB_90,5-91,5H'); wt90H.displayNumExperiments();
% wt99H = wt.getExperimentsByExperimentName('pmadAB_brkAB_wg-ptcAB_99-100H'); wt99H.displayNumExperiments();
% wt110H = wt.getExperimentsByExperimentName('pmadAB_brkAB_wg-ptcAB_110,5-111,5H'); wt110H.displayNumExperiments();
pent78H = pent.getExperimentsByExperimentName('pmadAB_brkAB_wg-ptcAB_78-79H'); pent78H.displayNumExperiments();
pent90H = pent.getExperimentsByExperimentName('pmadAB_brkAB_wg-ptcAB_90-91H'); pent90H.displayNumExperiments();
% pent99H = pent.getExperimentsByExperimentName('pmadAB_brkAB_wg-ptcAB_99-100H'); pent99H.displayNumExperiments();
% pent110H = pent.getExperimentsByExperimentName('pmadAB_brkAB_wg-ptcAB_110,5-111,5H'); pent110H.displayNumExperiments();

%% ------------------------------------------------------------------------
% ANALYZE MORPHOLOGICAL STRUCTURE

% Here the examples can not be run directly but are there to provide a
% reference document to show how to use specific functions/methods.
% Test datasets can be downloaded on http://lis.epfl.ch/wingj.

% -------------------------------------------------------------------------
% EXAMPLE 1: Show the Wg-Ptc projection used to detection the wing
% structure for wild type wings aged of 90H AEL (after egg laying). The
% detected structure is shown on top of it.
%wt90H.get(:).showStructurePreview();

% The part 'get(:)' means that every experiments included in wt90H are
% used. A selection can be obtained by specifying the index of the
% experiments to consider. For example:
% get(1): uses only the first experiment
% get([1 2 5]): uses the experiments 1, 2 and 5
% get(1:5): uses the experiments 1, 2, 3, 4 and 5
% get([1:5 10]): uses the experiments 1, 2, 3, 4, 5 and 10

% Custom titles can be specified (e.g. shorter than the experiment name).
% Replace true by false to display each image in a different figure.
%titles = generate_titles('Experiment ',1:3);
%wt90H.get(:).showStructurePreview(true,titles'); % for custom titles
wt90H.get(:).showStructurePreview(true); % for experiment names

% -------------------------------------------------------------------------
% EXAMPLE 2: Get structure properties
% Get the length of the D/V boundary for the first experiment only.
[value,unit] = wt90H.get(1).getStructureProperty('DV.length');

% Get the wing pouch perimeter for all the experiments in wt90H.
[values,units] = wt90H.get(:).getStructureProperty('structure.perimeter');

% Get the area of the DA and DV compartments in wt90H (compartments are da,
% dp, va and vp).
properties = {'da.area','dp.area'};
[X,G] = wt90H.get(:).getStructurePropertiesFromOneRepository(properties); %#ok<*NASGU,*ASGLU>

% Get the area of the wing pouch for all the experiments in wt78H and
% and wt90H. X contains the values and G contains the repository index of
% each experiment. This is useful to boxplot the data, for instance. The
% same approach can be used to compare wild type and mutant experiments.
extraRepositories = {wt90H.get(:)};
[X,G] = wt78H.get(:).getOneStructurePropertyFromMultiRepositories(extraRepositories,'structure.area');

% Get the perimeter of the DA and DV compartments in wt90H and wt99H. Here
% data are grouped by repositories.
properties = {'da.perimeter','dp.perimeter'};
extraRepositories = {wt90H.get(:)};
organizer = Experiment.PER_REPOSITORY;
[X,G] = wt78H.get(:).getStructureProperties(properties,extraRepositories,organizer);

% Same as above but data are grouped per structure properties.
properties = {'da.perimeter','dp.perimeter'};
extraRepositories = {wt90H.get(:)};
organizer = Experiment.PER_STRUCTURE_PROPERTY;
[X,G] = wt78H.get(:).getStructureProperties(properties,extraRepositories,organizer);

% An efficient way to plot the structure properties are using boxplots
% boxplot(X,G);

% -------------------------------------------------------------------------
% EXPERIMENT 3: Boxplot structure properties
% The previous section shows how to get the structure properties before
% doing further analysis with Matlab. Specific methods are implemented to
% easily boxplot a selection of data using the same approach as before.
% These methods aim to ease the comparison between different experiments
% (contained in different repositories). Mann-Whitney U-test are performed
% at the same time to report which data are significantly different from
% others.

% Compare the length of the wild type D/V and A/P boundaries in wild type
% wings as a function of age. The example below shows four plots. One
% plot per experiment repository provided (here function of age). In each
% plot, the different properties are reported (here D/V and A/P length).
properties = {'DV.length','AP.length'};
% extraRepositories = {wt90H.get(:),wt99H.get(:),wt110H.get(:)};
extraRepositories = {wt90H.get(:)};
organizer = Experiment.PER_REPOSITORY;
% boxplotNames = []; subplotNames = {'wt78H','wt90H','wt99H','wt110H'};
boxplotNames = []; subplotNames = {'wt78H','wt90H'};
boxplotOptions = [];
[X,G,numGroups,H,P,sh] = wt78H.get(:).boxplotStructureProperty(properties,...
    extraRepositories,organizer,boxplotNames,subplotNames,boxplotOptions);

% X: structure properties data
% G: organization of the data into group (used by boxplot)
% numGroups: the number of repositories (here 4). If organizer=Experiment.PER_
%   STRUCTURE_PROPERTY, numGroups would be 2 (two structure properties)
% H: Matrix containing the result of the Mann-Whitney U-test for every pair
%   of data. H(i,j)=1 means that the dataset i and j are significantly
%   different. H(i,j)=0 means that the dataset i and j are NOT
%   significantly different.
% P: Same format as H but contains the p-values returned by the
%   Mann-Whitney U-test. P(i,j)<0.05 indicates a rejection of the null 
%   hypothesis at the 5% significance level.
% sh: vector containing the subplot handlers. sh(i) returns the handler of
%   the plot i.

% Same example as before but instead of comparing the data in function of
% the age, we compare the A/P to the D/V data. Note that 'subplotNames' can
% be defined as we do for 'boxplotNames'. Here by not defining them, the
% default name of the structure properties ('AP.length' and 'DV.length')
% are used. Here only two plots are generated, one per structure property.
properties = {'DV.length','AP.length'};
% extraRepositories = {wt90H.get(:),wt99H.get(:),wt110H.get(:)};
extraRepositories = {wt90H.get(:)};
organizer = Experiment.PER_STRUCTURE_PROPERTY;
% boxplotNames = {'wt78H','wt90H','wt99H','wt110H'}; subplotNames = [];
boxplotNames = {'wt78H','wt90H'}; subplotNames = [];
boxplotOptions = [];
[X,G,numGroups,H,P,sh] = wt78H.get(:).boxplotStructureProperty(properties,...
    extraRepositories,organizer,boxplotNames,subplotNames,boxplotOptions);

% The same approach can be applied to compare wild type and mutant wings.
% This is achieved by specifying the different repositories defined at the
% top of this file.
