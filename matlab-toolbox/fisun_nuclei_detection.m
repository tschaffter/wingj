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
% Version: November 8, 2012

close all; clear all; clc;

%% ------------------------------------------------------------------------
% SETTINGS

addpath('lib');
addpath('nuclei_detector');

settings = Settings.getInstance;

%% ------------------------------------------------------------------------
% LOCATE EXPERIMENTS

% Defines a list including the experiments root directories. See
% User Manual for an example of the files and folders organization.
% Typically, each of the folder name included below should contain one or
% more experiment folder. An experiment folder contains a single
% experiment (e.g. a single wing). Don't forget the final '/'.
% For the nuclei detection, we use the nuclear staining TO-PRO
% experimentRootDirectories = {'20121106_histoneGFP_histoneRFP_Topro_0H/'};
% experimentRootDirectories = {'20091222_wg-ptcAB_pentGFP_TOPRO_111-112H/'};
experimentRootDirectories = {'20121113_histoneGFP_histoneRFP_wg-ptcAB_0H/'};
                              
% Adds a prefix to the above directory names to obtain complete paths.
experimentRootDirectories = strcat('benchmarks/',experimentRootDirectories);

%% ------------------------------------------------------------------------
% OUTPUT

% Output files are saved in the folder WingJ/Matlab/NucleiDetection
% included in each experiment directoy. The output files are a text file
% containing the number of nuclei detected, each TOPRO images labeled with
% the detected nuclei and a movie in AVI format made out of these images.

%% ------------------------------------------------------------------------
% LOAD EXPERIMENTS

% Creates the Experiment objets by listing every experiment folder contained
% in the above 'experiments root folders'. Append '_IGNORE' to the name of
% an experiment folder to discard it.
repository = ExperimentList(experimentRootDirectories);
wt = repository.getWildTypeExperiments();

%% ------------------------------------------------------------------------
% NUCLEI DETECTION
% wt.get(1).detectNuclei('Topro',11:64);
wt.get(6:10).detectNuclei('histoneGFP',[],true);
numNuclei = wt.get(6:10).getNumNuclei()