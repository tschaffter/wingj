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
experimentRootDirectories = {'20100716_pmadAB_brkAB_wg-ptcAB_78-79H/'
                             '20100716_pmadAB_brkAB_wg-ptcAB_90,5-91,5H/'
                             '20100716_pmadAB_brkAB_wg-ptcAB_99-100H/'
                             '20100716_pmadAB_brkAB_wg-ptcAB_110,5-111,5H/'
                             '20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_78-79H/'
                             '20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_90-91H/'
                             '20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_99-100H/'
                             '20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_110,5-111,5H/'
%                              '20100511_pent2-5-_ombLACZ_wg-ptcAB_TOPRO_65-66H'
%                              '20100426_pent2-5-_ombLACZ_wg-ptcAB_TOPRO_72,5-73,5H'
%                              '20100426_pent2-5-_ombLACZ_wg-ptcAB_TOPRO_79-80H'
%                              '20100422_pent2-5-_ombLACZ_wg-ptcAB_TOPRO_91-92H'
%                              '20100421_pent2-5-_ombLACZ_wg-ptcAB_TOPRO_100-101H'
%                              '20100430_pent2-5-_ombLACZ_wg-ptcAB_TOPRO_112-113H'
                             '20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_79-80H'
                             '20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_90-91H'
                             '20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_99-101H'
                             '20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_114-115H'
                             '20101129_salAB_dadGFP_wg-ptcAB_79-80H'
                             '20101129_salAB_dadGFP_wg-ptcAB_89-90H'
                             '20101129_salAB_dadGFP_wg-ptcAB_100-101H'
                             '20101129_salAB_dadGFP_wg-ptcAB_110,5-111,5H'
                             '20110302_brkAB_ombAB_wg-ptcAB_79-80H'
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
% 1D EXPRESSION ANALYSIS

% Create lists of expression profiles
wt_pmadAB_80H_profiles = wt_pmadAB_80H.get(:).getExpressionProfiles('pmadAB','D/V',[0,0]);
wt_pmadAB_90H_profiles = wt_pmadAB_90H.get(:).getExpressionProfiles('pmadAB','D/V',[0,0]);
wt_pmadAB_100H_profiles = wt_pmadAB_100H.get(:).getExpressionProfiles('pmadAB','D/V',[0,0]);
wt_pmadAB_110H_profiles = wt_pmadAB_110H.get(:).getExpressionProfiles('pmadAB','D/V',[0,0]);

pent_pmadAB_80H_profiles = pent_pmadAB_80H.get(:).getExpressionProfiles('pmadAB','D/V',[0,0]);
pent_pmadAB_90H_profiles = pent_pmadAB_90H.get(:).getExpressionProfiles('pmadAB','D/V',[0,0]);
pent_pmadAB_100H_profiles = pent_pmadAB_100H.get(:).getExpressionProfiles('pmadAB','D/V',[0,0]);
pent_pmadAB_110H_profiles = pent_pmadAB_110H.get(:).getExpressionProfiles('pmadAB','D/V',[0,0]);

% Shows the structure inference
% figure
% wt_pmadAB_90H.get(:).showStructurePreview(); return

% Plots all expression profiles on the same plot
% wt_pmadAB_80H_DV_0_profiles.get(:).plotExpressionProfiles();

% Computes and plots mean expression profile
figure
[X Ymean] = wt_pmadAB_80H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,numInterpSamples,['''Color'',[' num2str(GOOGLEYELLOW) ']']);
hold on
wt_pmadAB_90H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,numInterpSamples,['''Color'',[' num2str(GOOGLEGREEN) ']']);
wt_pmadAB_100H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,numInterpSamples,['''Color'',[' num2str(GOOGLEBLUE) ']']);
wt_pmadAB_110H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,numInterpSamples,['''Color'',[' num2str(GOOGLERED) ']']);
% set(gca,'XLim',[-180 160]);
% set(gca,'YLim',[0 165]);
set(gca,'XLim',[-180 160]);
set(gca,'YLim',[0 0.4]);
title('Wt');
xlabel('X (um)');
ylabel('[pmadAB] (a.u.)');
legend({'80H','90H','100H','110H'});
wtXlim = get(gca,'XLim');
wtYlim = get(gca,'YLim');

figure
pent_pmadAB_80H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,numInterpSamples,['''Color'',[' num2str(GOOGLEYELLOW) ']']);
hold on
pent_pmadAB_90H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,numInterpSamples,['''Color'',[' num2str(GOOGLEGREEN) ']']);
pent_pmadAB_100H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,numInterpSamples,['''Color'',[' num2str(GOOGLEBLUE) ']']);
pent_pmadAB_110H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,numInterpSamples,['''Color'',[' num2str(GOOGLERED) ']']);
% set(gca,'XLim',[-180 160]);
% set(gca,'YLim',[0 165]);
set(gca,'XLim',[-180 160]);
set(gca,'YLim',[0 0.4]);
title('Pent2-5');
xlabel('X (um)');
ylabel('[pmadAB] (a.u.)');
legend({'80H','90H','100H','110H'});
pentXlim = get(gca,'XLim');
pentYlim = get(gca,'YLim');

disp(['xmin: ' num2str(min(wtXlim(1),pentXlim(1)))]);
disp(['xmax: ' num2str(max(wtXlim(2),pentXlim(2)))]);
disp(['ymin: ' num2str(min(wtYlim(1),pentYlim(1)))]);
disp(['ymax: ' num2str(max(wtYlim(2),pentYlim(2)))]);
