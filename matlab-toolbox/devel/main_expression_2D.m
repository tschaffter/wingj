close all; clear all; clc;

addpath('lib');
addpath('nuclei_detector');
addpath('structure');
addpath('expression');

%% ------------------------------------------------------------------------
% SETTINGS

settings = Settings.getInstance;

GOOGLEBLUE=[64,93,170]/255;
GOOGLERED=[222,30,50]/255;
GOOGLEYELLOW=[255,199,56]/255;
GOOGLEGREEN=[5,165,74]/255;

%% ------------------------------------------------------------------------
% EXPERIMENT LOCATIONS

% Define a list of root directories
experimentRootDirectories = {'20100716_pmadAB_brkAB_wg-ptcAB_78-79H/'
                             '20100716_pmadAB_brkAB_wg-ptcAB_90,5-91,5H/'
                             '20100716_pmadAB_brkAB_wg-ptcAB_99-100H/'
                             '20100716_pmadAB_brkAB_wg-ptcAB_110,5-111,5H/'
                             '20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_78-79H/'
                             '20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_90-91H/'
                             '20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_99-100H/'
                             '20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_110,5-111,5H/'}; 
                              
experimentRootDirectories = strcat('/mnt/extra/wingj_benchmarks/',experimentRootDirectories);

%% ------------------------------------------------------------------------
% LOAD EXPERIMENTS

repository = ExperimentList(experimentRootDirectories);

% Prepare batches of experiments to ease the analyses
% Sort by wt and pent2-5 mutant experiments
wt = repository.getWildTypeExperiments();
pent = repository.getExperimentsByMutantName('pent2-5');

% pmadAB wt/pent2-5
wt_pmadAB = wt.getExperimentsByGeneName('pmadAB'); wt_pmadAB.displayNumExperiments();
pent_pmadAB = pent.getExperimentsByGeneName('pmadAB'); pent_pmadAB.displayNumExperiments();

wt_pmadAB_80H = wt_pmadAB.getExperimentsByAge([78,79]); wt_pmadAB_80H.displayNumExperiments();
wt_pmadAB_90H = wt_pmadAB.getExperimentsByAge([90.5,91.5]); wt_pmadAB_90H.displayNumExperiments();
wt_pmadAB_100H = wt_pmadAB.getExperimentsByAge([99,100]); wt_pmadAB_100H.displayNumExperiments();
wt_pmadAB_110H = wt_pmadAB.getExperimentsByAge([110.5,111.5]); wt_pmadAB_110H.displayNumExperiments();

pent_pmadAB_80H = pent_pmadAB.getExperimentsByAge([78,79]); pent_pmadAB_80H.displayNumExperiments();
pent_pmadAB_90H = pent_pmadAB.getExperimentsByAge([90,91]); pent_pmadAB_90H.displayNumExperiments();
pent_pmadAB_100H = pent_pmadAB.getExperimentsByAge([99,100]); pent_pmadAB_100H.displayNumExperiments();
pent_pmadAB_110H = pent_pmadAB.getExperimentsByAge([110.5,111.5]); pent_pmadAB_110H.displayNumExperiments();

%% ------------------------------------------------------------------------
% 2D EXPRESSION ANALYSIS

% Create lists of expression profiles
% wt_pmadAB_80H_maps = wt_pmadAB_80H.get(:).getExpressionMaps('pmadAB',[-100,-100]);
% wt_pmadAB_90H_maps = wt_pmadAB_90H.get(:).getExpressionMaps('pmadAB',[-100,-100]);
% wt_pmadAB_100H_maps = wt_pmadAB_100H.get(:).getExpressionMaps('pmadAB',[-100,-100]);

% pent_pmadAB_80H_maps = pent_pmadAB_80H.get([1 2 4 5]).getExpressionMaps('pmadAB',[-100,-100]);
% pent_pmadAB_90H_maps = pent_pmadAB_90H.get(:).getExpressionMaps('pmadAB',[-100,-100]);
pent_pmadAB_100H_maps = pent_pmadAB_100H.get(:).getExpressionMaps('pmadAB',[-100,-100]);

% Shows the structure inference
% figure
% wt_pmadAB_90H.get(:).showStructurePreview();

% Show all expression maps
% wt_pmadAB_80H_maps.get(:).showExpressionMaps();

% Computes and plots mean expression map
% figure
% wt_pmadAB_80H_maps.get(:).showMeanExpressionMap();
% figure
% wt_pmadAB_80H_maps.get(:).showStdExpressionMap();

figure
pent_pmadAB_100H_maps.get(:).showMeanExpressionMap();
figure
pent_pmadAB_100H_maps.get(:).showStdExpressionMap();







% Plots all pmadAB and brkAB expression profiles of wt78H.
% wt78H.displayExperimentNames();

% Create lists or groups of expression profiles
% wt78H_pmadAB_DV_0_profiles = wt78H.get(:).getExpressionProfiles('pmadAB','D/V',[0,0]);
% wt90H_pmadAB_DV_0_profiles = wt90H.get(:).getExpressionProfiles('pmadAB','D/V',[0,0]);
% wt99H_pmadAB_DV_0_profiles = wt99H.get(:).getExpressionProfiles('pmadAB','D/V',[0,0]);
% wt110H_pmadAB_DV_0_profiles = wt110H.get(:).getExpressionProfiles('pmadAB','D/V',[0,0]);
% 
% wt78H_brkAB_DV_0_profiles = wt78H.get(:).getExpressionProfiles('brkAB','D/V',[0,0]);
% wt90H_brkAB_DV_0_profiles = wt90H.get(:).getExpressionProfiles('brkAB','D/V',[0,0]);
% wt99H_brkAB_DV_0_profiles = wt99H.get(:).getExpressionProfiles('brkAB','D/V',[0,0]);
% wt110H_brkAB_DV_0_profiles = wt110H.get(:).getExpressionProfiles('brkAB','D/V',[0,0]);
