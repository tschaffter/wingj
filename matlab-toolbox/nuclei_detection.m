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

addpath('lib');
addpath('nuclei_detector');

settings = Settings.getInstance;

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
                             '20100511_pent2-5-_ombLACZ_wg-ptcAB_TOPRO_65-66H'
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

% Create a repository for each gene
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

% pent_TOPRO_65H = pent_TOPRO.getExperimentsByAge([65,66]); pent_TOPRO_65H.displayNumExperiments();
pent_TOPRO_72H = pent_TOPRO.getExperimentsByAge([72.5,73.5]); pent_TOPRO_72H.displayNumExperiments();
pent_TOPRO_79H = pent_TOPRO.getExperimentsByAge([79,80]); pent_TOPRO_79H.displayNumExperiments();
pent_TOPRO_91H = pent_TOPRO.getExperimentsByAge([91,92]); pent_TOPRO_91H.displayNumExperiments();
pent_TOPRO_100H = pent_TOPRO.getExperimentsByAge([100,101]); pent_TOPRO_100H.displayNumExperiments();
pent_TOPRO_112H = pent_TOPRO.getExperimentsByAge([112,113]); pent_TOPRO_112H.displayNumExperiments();

%% ------------------------------------------------------------------------
% NUCLEI DETECTION

% wt_TOPRO_salLACZ_73H.get(:).detectNuclei('TOPRO');
% wt_TOPRO_salLACZ_96H.get(:).detectNuclei('TOPRO');
% wt_TOPRO_salLACZ_114H.get(:).detectNuclei('TOPRO');

% wt_TOPRO_pentGFP_72H.get(:).detectNuclei('TOPRO');
% wt_TOPRO_pentGFP_95H.get(:).detectNuclei('TOPRO');
% wt_TOPRO_pentGFP_111H.get(1).detectNuclei('TOPRO');

wt_TOPRO_salLACZ_73H_num_nuclei = wt_TOPRO_salLACZ_73H.get(:).getNumNuclei();
wt_TOPRO_salLACZ_96H_num_nuclei = wt_TOPRO_salLACZ_96H.get(:).getNumNuclei();
wt_TOPRO_salLACZ_114H_num_nuclei = wt_TOPRO_salLACZ_114H.get(:).getNumNuclei();

wt_TOPRO_pentGFP_72H_num_nuclei = wt_TOPRO_pentGFP_72H.get(:).getNumNuclei();
wt_TOPRO_pentGFP_95H_num_nuclei = wt_TOPRO_pentGFP_95H.get(:).getNumNuclei();
wt_TOPRO_pentGFP_111H_num_nuclei = wt_TOPRO_pentGFP_111H.get(:).getNumNuclei();

pent_TOPRO_72H_num_nuclei = pent_TOPRO_72H.get(:).getNumNuclei();
pent_TOPRO_79H_num_nuclei = pent_TOPRO_79H.get(:).getNumNuclei();
pent_TOPRO_91H_num_nuclei = pent_TOPRO_91H.get(:).getNumNuclei();
pent_TOPRO_100H_num_nuclei = pent_TOPRO_100H.get(:).getNumNuclei();
pent_TOPRO_112H_num_nuclei = pent_TOPRO_112H.get(:).getNumNuclei();

data = [wt_TOPRO_salLACZ_73H_num_nuclei;wt_TOPRO_salLACZ_96H_num_nuclei;wt_TOPRO_salLACZ_114H_num_nuclei;...
    pent_TOPRO_72H_num_nuclei;pent_TOPRO_91H_num_nuclei;...
    pent_TOPRO_100H_num_nuclei;pent_TOPRO_112H_num_nuclei];
%wt_TOPRO_pentGFP_72H_num_nuclei;wt_TOPRO_pentGFP_95H_num_nuclei;wt_TOPRO_pentGFP_111H_num_nuclei;...
G = boxplot_groups({wt_TOPRO_salLACZ_73H_num_nuclei,wt_TOPRO_salLACZ_96H_num_nuclei,wt_TOPRO_salLACZ_114H_num_nuclei,...
    pent_TOPRO_72H_num_nuclei,pent_TOPRO_91H_num_nuclei,...
    pent_TOPRO_100H_num_nuclei,pent_TOPRO_112H_num_nuclei});
    %wt_TOPRO_pentGFP_72H_num_nuclei,wt_TOPRO_pentGFP_95H_num_nuclei,wt_TOPRO_pentGFP_111H_num_nuclei,...
labels = {'wt_73H','wt_96H','wt_114H',...
    'pent_72H','pent_91H','pent_100H','pent_112H'};
%'B:wt_72H','wt_95H','wt_111H',...


% [p,h] = ranksum(wt_TOPRO_salLACZ_96H_num_nuclei,pent_TOPRO_100H_num_nuclei)
[p,h] = ranksum(wt_TOPRO_salLACZ_73H_num_nuclei,pent_TOPRO_72H_num_nuclei)


boxplot(data,G,'labels',labels);
set(gcf, 'OuterPosition',[0 0 600 500]);
xlabel('Experiment');
ylabel('Number of nuclei detected (wing pouch)')

