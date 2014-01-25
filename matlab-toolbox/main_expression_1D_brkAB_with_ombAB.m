close all; clear all; clc;

addpath('lib');
addpath('nuclei_detector');
addpath('structure');

%% ------------------------------------------------------------------------
% SETTINGS

settings = WJSettings.getInstance;

GOOGLEBLUE=[64,93,170]/255;
GOOGLERED=[222,30,50]/255;
GOOGLEYELLOW=[255,199,56]/255;
GOOGLEGREEN=[5,165,74]/255;

% Number of samples per mean expression profile.
numPlotSamples = 50;

%% ------------------------------------------------------------------------
% EXPERIMENT LOCATIONS

% Define a list of root directories
experimentRootDirectories = {'20110302_brkAB_ombAB_wg-ptcAB_79-80H'
                             '20110302_brkAB_ombAB_wg-ptcAB_87-88H'
                             '20110302_brkAB_ombAB_wg-ptcAB_100-101H'
                             '20110302_brkAB_ombAB_wg-ptcAB_111-112H'
                             '20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_79-80H'
                             '20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_87-88H'
                             '20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_100-101H'
                             '20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_109-110.5H'};
                              
experimentRootDirectories = strcat('/mnt/extra/wingj_benchmarks/',experimentRootDirectories);

%% ------------------------------------------------------------------------
% LOAD EXPERIMENTS

repository = ExperimentList(experimentRootDirectories);

% Prepare batches of experiments to ease the analyses
% Sort by wt and pent2-5 mutant experiments
wt = repository.getWildTypeExperiments();
pent = repository.getExperimentsByMutantName('pent2-5');

% brkAB wt/pent2-5
wt_brkAB = wt.getExperimentsByGeneName('brkAB'); wt_brkAB.displayNumExperiments();
pent_brkAB = pent.getExperimentsByGeneName('brkAB'); pent_brkAB.displayNumExperiments();

wt_brkAB_80H = wt_brkAB.getExperimentsByAge([79,80]); wt_brkAB_80H.displayNumExperiments();
wt_brkAB_90H = wt_brkAB.getExperimentsByAge([87,88]); wt_brkAB_90H.displayNumExperiments();
wt_brkAB_100H = wt_brkAB.getExperimentsByAge([100,101]); wt_brkAB_100H.displayNumExperiments();
wt_brkAB_110H = wt_brkAB.getExperimentsByAge([111,112]); wt_brkAB_110H.displayNumExperiments();

pent_brkAB_80H = pent_brkAB.getExperimentsByAge([79,80]); pent_brkAB_80H.displayNumExperiments();
pent_brkAB_90H = pent_brkAB.getExperimentsByAge([87,88]); pent_brkAB_90H.displayNumExperiments();
pent_brkAB_100H = pent_brkAB.getExperimentsByAge([100,101]); pent_brkAB_100H.displayNumExperiments();
pent_brkAB_110H = pent_brkAB.getExperimentsByAge([109,110.5]); pent_brkAB_110H.displayNumExperiments();

%% ------------------------------------------------------------------------
% 1D EXPRESSION ANALYSIS

% Create lists of expression profiles
wt_brkAB_80H_profiles = wt_brkAB_80H.get(:).getExpressionProfiles('brkAB','D/V',[0,0]);
wt_brkAB_90H_profiles = wt_brkAB_90H.get(:).getExpressionProfiles('brkAB','D/V',[0,0]);
wt_brkAB_100H_profiles = wt_brkAB_100H.get(:).getExpressionProfiles('brkAB','D/V',[0,0]);
wt_brkAB_110H_profiles = wt_brkAB_110H.get(:).getExpressionProfiles('brkAB','D/V',[0,0]);

pent_brkAB_80H_profiles = pent_brkAB_80H.get(:).getExpressionProfiles('brkAB','D/V',[0,0]);
pent_brkAB_90H_profiles = pent_brkAB_90H.get(:).getExpressionProfiles('brkAB','D/V',[0,0]);
pent_brkAB_100H_profiles = pent_brkAB_100H.get(:).getExpressionProfiles('brkAB','D/V',[0,0]);
pent_brkAB_110H_profiles = pent_brkAB_110H.get(:).getExpressionProfiles('brkAB','D/V',[0,0]);

% Show the structure inference
figure
wt_brkAB_80H.get(:).showStructurePreview();

% Plots all expression profiles on the same plot
% wt_brkAB_80H_DV_0_profiles.get(:).plotExpressionProfiles();


figure
wt_brkAB_80H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,['''Color'',[' num2str(GOOGLEYELLOW) ']']);
hold on
wt_brkAB_90H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,['''Color'',[' num2str(GOOGLEGREEN) ']']);
wt_brkAB_100H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,['''Color'',[' num2str(GOOGLEBLUE) ']']);
wt_brkAB_110H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,['''Color'',[' num2str(GOOGLERED) ']']);
% set(gca,'YLim',[0 180]);
title('Wt');
xlabel('X (um)');
ylabel('[brkAB] (a.u.)');
legend({'80H','90H','100H','110H'});

figure
pent_brkAB_80H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,['''Color'',[' num2str(GOOGLEYELLOW) ']']);
hold on
pent_brkAB_90H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,['''Color'',[' num2str(GOOGLEGREEN) ']']);
pent_brkAB_100H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,['''Color'',[' num2str(GOOGLEBLUE) ']']);
pent_brkAB_110H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,['''Color'',[' num2str(GOOGLERED) ']']);
% set(gca,'YLim',[0 180]);
title('Pent2-5');
xlabel('X (um)');
ylabel('[brkAB] (a.u.)');
legend({'80H','90H','100H','110H'});
