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
experimentRootDirectories = {'20101129_salAB_dadGFP_wg-ptcAB_79-80H'
                             '20101129_salAB_dadGFP_wg-ptcAB_89-90H'
                             '20101129_salAB_dadGFP_wg-ptcAB_100-101H'
                             '20101129_salAB_dadGFP_wg-ptcAB_110,5-111,5H'
                             '20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_79-80H'
                             '20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_90-91H'
                             '20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_99-101H'
                             '20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_114-115H'};
                              
experimentRootDirectories = strcat('/mnt/extra/wingj_benchmarks/',experimentRootDirectories);

%% ------------------------------------------------------------------------
% LOAD EXPERIMENTS

repository = ExperimentList(experimentRootDirectories);

% Prepare batches of experiments to ease the analyses
% Sort by wt and pent2-5 mutant experiments
wt = repository.getWildTypeExperiments();
pent = repository.getExperimentsByMutantName('pent2-5');

% salAB wt/pent2-5
wt_salAB = wt.getExperimentsByGeneName('salAB'); wt_salAB.displayNumExperiments();
pent_salAB = pent.getExperimentsByGeneName('salAB'); pent_salAB.displayNumExperiments();

wt_salAB_80H = wt_salAB.getExperimentsByAge([79,80]); wt_salAB_80H.displayNumExperiments();
wt_salAB_90H = wt_salAB.getExperimentsByAge([89,90]); wt_salAB_90H.displayNumExperiments();
wt_salAB_100H = wt_salAB.getExperimentsByAge([100,101]); wt_salAB_100H.displayNumExperiments();
wt_salAB_110H = wt_salAB.getExperimentsByAge([110.5,111.5]); wt_salAB_110H.displayNumExperiments();

pent_salAB_80H = pent_salAB.getExperimentsByAge([79,80]); pent_salAB_80H.displayNumExperiments();
pent_salAB_90H = pent_salAB.getExperimentsByAge([90,91]); pent_salAB_90H.displayNumExperiments();
pent_salAB_100H = pent_salAB.getExperimentsByAge([99,101]); pent_salAB_100H.displayNumExperiments();
pent_salAB_110H = pent_salAB.getExperimentsByAge([114,115]); pent_salAB_110H.displayNumExperiments();

%% ------------------------------------------------------------------------
% 1D EXPRESSION ANALYSIS

% Create lists of expression profiles
wt_salAB_80H_profiles = wt_salAB_80H.get(:).getExpressionProfiles('salAB','D/V',[0,0]);
wt_salAB_90H_profiles = wt_salAB_90H.get(:).getExpressionProfiles('salAB','D/V',[0,0]);
wt_salAB_100H_profiles = wt_salAB_100H.get(:).getExpressionProfiles('salAB','D/V',[0,0]);
wt_salAB_110H_profiles = wt_salAB_110H.get(:).getExpressionProfiles('salAB','D/V',[0,0]);

pent_salAB_80H_profiles = pent_salAB_80H.get(:).getExpressionProfiles('salAB','D/V',[0,0]);
pent_salAB_90H_profiles = pent_salAB_90H.get(:).getExpressionProfiles('salAB','D/V',[0,0]);
pent_salAB_100H_profiles = pent_salAB_100H.get(:).getExpressionProfiles('salAB','D/V',[0,0]);
pent_salAB_110H_profiles = pent_salAB_110H.get(:).getExpressionProfiles('salAB','D/V',[0,0]);

% Show the structure inference
% figure
% wt_salAB_80H.get(:).showStructurePreview();

% Plots all expression profiles on the same plot
% wt_brkAB_80H_DV_0_profiles.get(:).plotExpressionProfiles();


figure
wt_salAB_80H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,numInterpSamples,['''Color'',[' num2str(GOOGLEYELLOW) ']']);
hold on
wt_salAB_90H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,numInterpSamples,['''Color'',[' num2str(GOOGLEGREEN) ']']);
wt_salAB_100H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,numInterpSamples,['''Color'',[' num2str(GOOGLEBLUE) ']']);
wt_salAB_110H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,numInterpSamples,['''Color'',[' num2str(GOOGLERED) ']']);
% set(gca,'XLim',[-180 160]);
% set(gca,'YLim',[0 165]);
set(gca,'XLim',[-180 160]);
set(gca,'YLim',[0 0.4]);
title('Wt');
xlabel('X (um)');
ylabel('[salAB] (a.u.)');
legend({'80H','90H','100H','110H'});

figure
pent_salAB_80H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,numInterpSamples,['''Color'',[' num2str(GOOGLEYELLOW) ']']);
hold on
pent_salAB_90H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,numInterpSamples,['''Color'',[' num2str(GOOGLEGREEN) ']']);
pent_salAB_100H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,numInterpSamples,['''Color'',[' num2str(GOOGLEBLUE) ']']);
pent_salAB_110H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,numInterpSamples,['''Color'',[' num2str(GOOGLERED) ']']);
% set(gca,'XLim',[-180 160]);
% set(gca,'YLim',[0 165]);
set(gca,'XLim',[-180 160]);
set(gca,'YLim',[0 0.4]);
title('Pent2-5');
xlabel('X (um)');
ylabel('[salAB] (a.u.)');
legend({'80H','90H','100H','110H'});
