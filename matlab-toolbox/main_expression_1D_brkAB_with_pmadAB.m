close all; clear all; clc;

addpath('lib');
addpath('nuclei_detector');
addpath('structure');
addpath('expression');

%% ------------------------------------------------------------------------
% SETTINGS

settings = WJSettings.getInstance;

GOOGLEBLUE=[64,93,170]/255;
GOOGLERED=[222,30,50]/255;
GOOGLEYELLOW=[255,199,56]/255;
GOOGLEGREEN=[5,165,74]/255;

% Number of samples per mean expression profile.
numPlotSamples = 50;
numInterpSamples = settings.expressionProfileNumInterpSamples;

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

% brkAB wt/pent2-5
wt_brkAB = wt.getExperimentsByGeneName('brkAB'); wt_brkAB.displayNumExperiments();
pent_brkAB = pent.getExperimentsByGeneName('brkAB'); pent_brkAB.displayNumExperiments();

wt_brkAB_80H = wt_brkAB.getExperimentsByAge([78,79]); wt_brkAB_80H.displayNumExperiments();
wt_brkAB_90H = wt_brkAB.getExperimentsByAge([90.5,91.5]); wt_brkAB_90H.displayNumExperiments();
wt_brkAB_100H = wt_brkAB.getExperimentsByAge([99,100]); wt_brkAB_100H.displayNumExperiments();
wt_brkAB_110H = wt_brkAB.getExperimentsByAge([110.5,111.5]); wt_brkAB_110H.displayNumExperiments();

pent_brkAB_80H = pent_brkAB.getExperimentsByAge([78,79]); pent_brkAB_80H.displayNumExperiments();
pent_brkAB_90H = pent_brkAB.getExperimentsByAge([90,91]); pent_brkAB_90H.displayNumExperiments();
pent_brkAB_100H = pent_brkAB.getExperimentsByAge([99,100]); pent_brkAB_100H.displayNumExperiments();
pent_brkAB_110H = pent_brkAB.getExperimentsByAge([110.5,111.5]); pent_brkAB_110H.displayNumExperiments();

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
% wt_brkAB_80H.get(:).showStructurePreview();

% Plots all expression profiles on the same plot
% wt_brkAB_80H_DV_0_profiles.get(:).plotExpressionProfiles();


% figure
wt_brkAB_80H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,numInterpSamples,['''Color'',[' num2str(GOOGLEYELLOW) ']']);
hold on
wt_brkAB_90H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,numInterpSamples,['''Color'',[' num2str(GOOGLEGREEN) ']']);
wt_brkAB_100H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,numInterpSamples,['''Color'',[' num2str(GOOGLEBLUE) ']']);
wt_brkAB_110H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,numInterpSamples,['''Color'',[' num2str(GOOGLERED) ']']);
% set(gca,'XLim',[-180 160]);
% set(gca,'YLim',[0 165]);
set(gca,'XLim',[-180 160]);
set(gca,'YLim',[0 0.4]);
title('Wt');
xlabel('X (um)');
ylabel('[brkAB] (a.u.)');
legend({'80H','90H','100H','110H'});

figure
pent_brkAB_80H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,numInterpSamples,['''Color'',[' num2str(GOOGLEYELLOW) ']']);
hold on
pent_brkAB_90H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,numInterpSamples,['''Color'',[' num2str(GOOGLEGREEN) ']']);
pent_brkAB_100H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,numInterpSamples,['''Color'',[' num2str(GOOGLEBLUE) ']']);
pent_brkAB_110H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,numInterpSamples,['''Color'',[' num2str(GOOGLERED) ']']);
% set(gca,'XLim',[-180 160]);
% set(gca,'YLim',[0 165]);
set(gca,'XLim',[-180 160]);
set(gca,'YLim',[0 0.4]);
title('Pent2-5');
xlabel('X (um)');
ylabel('[brkAB] (a.u.)');
legend({'80H','90H','100H','110H'});
