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
wt_dadGFP = wt.getExperimentsByGeneName('dadGFP'); wt_dadGFP.displayNumExperiments();
pent_dadGFP = pent.getExperimentsByGeneName('dadGFP'); pent_dadGFP.displayNumExperiments();

wt_dadGFP_80H = wt_dadGFP.getExperimentsByAge([79,80]); wt_dadGFP_80H.displayNumExperiments();
wt_dadGFP_90H = wt_dadGFP.getExperimentsByAge([89,90]); wt_dadGFP_90H.displayNumExperiments();
wt_dadGFP_100H = wt_dadGFP.getExperimentsByAge([100,101]); wt_dadGFP_100H.displayNumExperiments();
wt_dadGFP_110H = wt_dadGFP.getExperimentsByAge([110.5,111.5]); wt_dadGFP_110H.displayNumExperiments();

pent_dadGFP_80H = pent_dadGFP.getExperimentsByAge([79,80]); pent_dadGFP_80H.displayNumExperiments();
pent_dadGFP_90H = pent_dadGFP.getExperimentsByAge([90,91]); pent_dadGFP_90H.displayNumExperiments();
pent_dadGFP_100H = pent_dadGFP.getExperimentsByAge([99,101]); pent_dadGFP_100H.displayNumExperiments();
pent_dadGFP_110H = pent_dadGFP.getExperimentsByAge([114,115]); pent_dadGFP_110H.displayNumExperiments();

%% ------------------------------------------------------------------------
% 1D EXPRESSION ANALYSIS

% Create lists of expression profiles
wt_dadGFP_80H_profiles = wt_dadGFP_80H.get(:).getExpressionProfiles('dadGFP','D/V',[0,0]);
wt_dadGFP_90H_profiles = wt_dadGFP_90H.get(:).getExpressionProfiles('dadGFP','D/V',[0,0]);
wt_dadGFP_100H_profiles = wt_dadGFP_100H.get(:).getExpressionProfiles('dadGFP','D/V',[0,0]);
wt_dadGFP_110H_profiles = wt_dadGFP_110H.get(:).getExpressionProfiles('dadGFP','D/V',[0,0]);

pent_dadGFP_80H_profiles = pent_dadGFP_80H.get(:).getExpressionProfiles('dadGFP','D/V',[0,0]);
pent_dadGFP_90H_profiles = pent_dadGFP_90H.get(:).getExpressionProfiles('dadGFP','D/V',[0,0]);
pent_dadGFP_100H_profiles = pent_dadGFP_100H.get(:).getExpressionProfiles('dadGFP','D/V',[0,0]);
pent_dadGFP_110H_profiles = pent_dadGFP_110H.get(:).getExpressionProfiles('dadGFP','D/V',[0,0]);

% Show the structure inference
% figure
% wt_dadGFP_80H.get(:).showStructurePreview();

% Plots all expression profiles on the same plot
% wt_brkAB_80H_DV_0_profiles.get(:).plotExpressionProfiles();


wt_dadGFP_80H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,numInterpSamples,['''Color'',[' num2str(GOOGLEYELLOW) ']']);
hold on
wt_dadGFP_90H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,numInterpSamples,['''Color'',[' num2str(GOOGLEGREEN) ']']);
wt_dadGFP_100H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,numInterpSamples,['''Color'',[' num2str(GOOGLEBLUE) ']']);
wt_dadGFP_110H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,numInterpSamples,['''Color'',[' num2str(GOOGLERED) ']']);
% set(gca,'XLim',[-180 160]);
% set(gca,'YLim',[0 165]);
set(gca,'XLim',[-180 160]);
set(gca,'YLim',[0 0.4]);
title('Wt');
xlabel('X (um)');
ylabel('[dadGFP] (a.u.)');
legend({'80H','90H','100H','110H'});

figure
pent_dadGFP_80H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,numInterpSamples,['''Color'',[' num2str(GOOGLEYELLOW) ']']);
hold on
pent_dadGFP_90H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,numInterpSamples,['''Color'',[' num2str(GOOGLEGREEN) ']']);
pent_dadGFP_100H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,numInterpSamples,['''Color'',[' num2str(GOOGLEBLUE) ']']);
pent_dadGFP_110H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,numInterpSamples,['''Color'',[' num2str(GOOGLERED) ']']);
% set(gca,'XLim',[-180 160]);
% set(gca,'YLim',[0 165]);
set(gca,'XLim',[-180 160]);
set(gca,'YLim',[0 0.4]);
title('Pent2-5');
xlabel('X (um)');
ylabel('[dadGFP] (a.u.)');
legend({'80H','90H','100H','110H'});
