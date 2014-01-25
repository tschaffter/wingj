close all; clear all; clc;

addpath('lib');
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

% ombAB wt/pent2-5
wt_ombAB = wt.getExperimentsByGeneName('ombAB'); wt_ombAB.displayNumExperiments();
pent_ombAB = pent.getExperimentsByGeneName('ombAB'); pent_ombAB.displayNumExperiments();

wt_ombAB_80H = wt_ombAB.getExperimentsByAge([79,80]); wt_ombAB_80H.displayNumExperiments();
wt_ombAB_90H = wt_ombAB.getExperimentsByAge([87,88]); wt_ombAB_90H.displayNumExperiments();
wt_ombAB_100H = wt_ombAB.getExperimentsByAge([100,101]); wt_ombAB_100H.displayNumExperiments();
wt_ombAB_110H = wt_ombAB.getExperimentsByAge([111,112]); wt_ombAB_110H.displayNumExperiments();

pent_ombAB_80H = pent_ombAB.getExperimentsByAge([79,80]); pent_ombAB_80H.displayNumExperiments();
pent_ombAB_90H = pent_ombAB.getExperimentsByAge([87,88]); pent_ombAB_90H.displayNumExperiments();
pent_ombAB_100H = pent_ombAB.getExperimentsByAge([100,101]); pent_ombAB_100H.displayNumExperiments();
pent_ombAB_110H = pent_ombAB.getExperimentsByAge([109,110.5]); pent_ombAB_110H.displayNumExperiments();

%% ------------------------------------------------------------------------
% 1D EXPRESSION ANALYSIS

% Create lists of expression profiles
wt_ombAB_80H_profiles = wt_ombAB_80H.get(:).getExpressionProfiles('ombAB','D/V',[0,0]);
wt_ombAB_90H_profiles = wt_ombAB_90H.get(:).getExpressionProfiles('ombAB','D/V',[0,0]);
wt_ombAB_100H_profiles = wt_ombAB_100H.get(:).getExpressionProfiles('ombAB','D/V',[0,0]);
wt_ombAB_110H_profiles = wt_ombAB_110H.get(:).getExpressionProfiles('ombAB','D/V',[0,0]);

pent_ombAB_80H_profiles = pent_ombAB_80H.get(:).getExpressionProfiles('ombAB','D/V',[0,0]);
pent_ombAB_90H_profiles = pent_ombAB_90H.get(:).getExpressionProfiles('ombAB','D/V',[0,0]);
pent_ombAB_100H_profiles = pent_ombAB_100H.get(:).getExpressionProfiles('ombAB','D/V',[0,0]);
pent_ombAB_110H_profiles = pent_ombAB_110H.get(:).getExpressionProfiles('ombAB','D/V',[0,0]);

% Show the structure inference
% figure
% wt_ombAB_80H.get(:).showStructurePreview();

% Plots all expression profiles on the same plot
% wt_brkAB_80H_DV_0_profiles.get(:).plotExpressionProfiles();
% n = size(pent_ombAB_100H_profiles.get(:));
% for i=1:n
%     figure
%     pent_ombAB_100H_profiles.get(i).plotExpressionProfiles();
%     set(gca,'XLim',[-180 160]);
%     set(gca,'YLim',[0 0.4]);
% end
% return




figure
wt_ombAB_80H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,numInterpSamples,['''Color'',[' num2str(GOOGLEYELLOW) ']']);
hold on
wt_ombAB_90H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,numInterpSamples,['''Color'',[' num2str(GOOGLEGREEN) ']']);
wt_ombAB_100H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,numInterpSamples,['''Color'',[' num2str(GOOGLEBLUE) ']']);
wt_ombAB_110H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,numInterpSamples,['''Color'',[' num2str(GOOGLERED) ']']);
% set(gca,'XLim',[-180 160]);
% set(gca,'YLim',[0 165]);
% set(gca,'YLim',[0 165]);
set(gca,'XLim',[-180 160]);
set(gca,'YLim',[0 0.4]);
title('Wt');
xlabel('X (um)');
ylabel('[ombAB] (a.u.)');
legend({'80H','90H','100H','110H'});

figure
pent_ombAB_80H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,numInterpSamples,['''Color'',[' num2str(GOOGLEYELLOW) ']']);
hold on
pent_ombAB_90H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,numInterpSamples,['''Color'',[' num2str(GOOGLEGREEN) ']']);
pent_ombAB_100H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,numInterpSamples,['''Color'',[' num2str(GOOGLEBLUE) ']']);
pent_ombAB_110H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,numInterpSamples,['''Color'',[' num2str(GOOGLERED) ']']);
% set(gca,'XLim',[-180 160]);
% set(gca,'YLim',[0 165]);
% set(gca,'YLim',[0 165]);
set(gca,'XLim',[-180 160]);
set(gca,'YLim',[0 0.4]);
title('Pent2-5');
xlabel('X (um)');
ylabel('[ombAB] (a.u.)');
legend({'80H','90H','100H','110H'});
