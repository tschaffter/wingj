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

% Number of interpolation points required when combining expression
% profiles.
numInterpSamples = settings.expressionProfileNumInterpSamples;

% Number of samples per mean expression profile.
numPlotSamples = 50;

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

% Sort age
wt78H = wt.getExperimentsByExperimentName('pmadAB_brkAB_wg-ptcAB_78-79H'); wt78H.displayNumExperiments();
wt90H = wt.getExperimentsByExperimentName('pmadAB_brkAB_wg-ptcAB_90,5-91,5H'); wt90H.displayNumExperiments();
wt99H = wt.getExperimentsByExperimentName('pmadAB_brkAB_wg-ptcAB_99-100H'); wt99H.displayNumExperiments();
wt110H = wt.getExperimentsByExperimentName('pmadAB_brkAB_wg-ptcAB_110,5-111,5H'); wt110H.displayNumExperiments();
pent78H = pent.getExperimentsByExperimentName('pmadAB_brkAB_wg-ptcAB_78-79H'); pent78H.displayNumExperiments();
pent90H = pent.getExperimentsByExperimentName('pmadAB_brkAB_wg-ptcAB_90-91H'); pent90H.displayNumExperiments();
pent99H = pent.getExperimentsByExperimentName('pmadAB_brkAB_wg-ptcAB_99-100H'); pent99H.displayNumExperiments();
pent110H = pent.getExperimentsByExperimentName('pmadAB_brkAB_wg-ptcAB_110,5-111,5H'); pent110H.displayNumExperiments();

wt_sal = wt.getExperimentsByGeneName('salAB'); wt_sal.displayNumExperiments();
pent_sal = pent.getExperimentsByGeneName('salAB'); pent_sal.displayNumExperiments();
wt_dad = wt.getExperimentsByGeneName('dadGFP'); wt_dad.displayNumExperiments();
pent_dad = pent.getExperimentsByGeneName('dadGFP'); pent_dad.displayNumExperiments();
wt_brk = wt.getExperimentsByGeneName('brkAB'); wt_brk.displayNumExperiments();
pent_brk = pent.getExperimentsByGeneName('brkAB'); pent_brk.displayNumExperiments();
wt_omb = wt.getExperimentsByGeneName('ombAB'); wt_omb.displayNumExperiments();
pent_omb = pent.getExperimentsByGeneName('ombAB'); pent_omb.displayNumExperiments();

% salAB wt/pent2-5
wt_sal_79H = wt_sal.getExperimentsByAge([79,80]); wt_sal_79H.displayNumExperiments();
wt_sal_89H = wt_sal.getExperimentsByAge([89,90]); wt_sal_89H.displayNumExperiments();
wt_sal_100H = wt_sal.getExperimentsByAge([100,101]); wt_sal_100H.displayNumExperiments();
wt_sal_110H = wt_sal.getExperimentsByAge([110.5,111.5]); wt_sal_110H.displayNumExperiments();
pent_sal_79H = pent_sal.getExperimentsByAge([79,80]); pent_sal_79H.displayNumExperiments();
pent_sal_90H = pent_sal.getExperimentsByAge([90,91]); pent_sal_90H.displayNumExperiments();
pent_sal_99H = pent_sal.getExperimentsByAge([99,101]); pent_sal_99H.displayNumExperiments();
pent_sal_114H = pent_sal.getExperimentsByAge([114,115]); pent_sal_114H.displayNumExperiments();

% dadGFP wt/pent2-5
wt_dad_79H = wt_dad.getExperimentsByAge([79,80]); wt_dad_79H.displayNumExperiments();
wt_dad_89H = wt_dad.getExperimentsByAge([89,90]); wt_dad_89H.displayNumExperiments();
wt_dad_100H = wt_dad.getExperimentsByAge([100,101]); wt_dad_100H.displayNumExperiments();
wt_dad_110H = wt_dad.getExperimentsByAge([110.5,111.5]); wt_dad_110H.displayNumExperiments();
pent_dad_79H = pent_dad.getExperimentsByAge([79,80]); pent_dad_79H.displayNumExperiments();
pent_dad_90H = pent_dad.getExperimentsByAge([90,91]); pent_dad_90H.displayNumExperiments();
pent_dad_99H = pent_dad.getExperimentsByAge([99,101]); pent_dad_99H.displayNumExperiments();
pent_dad_114H = pent_dad.getExperimentsByAge([114,115]); pent_dad_114H.displayNumExperiments();

% brkAB wt/pent
wt_brk_79H = wt_brk.getExperimentsByAge([79,80]); wt_brk_79H.displayNumExperiments();
wt_brk_87H = wt_brk.getExperimentsByAge([87,88]); wt_brk_87H.displayNumExperiments();
wt_brk_100H = wt_brk.getExperimentsByAge([100,101]); wt_brk_100H.displayNumExperiments();
wt_brk_111H = wt_brk.getExperimentsByAge([111,112]); wt_brk_111H.displayNumExperiments();
pent_brk_79H = pent_brk.getExperimentsByAge([79,80]); pent_brk_79H.displayNumExperiments();
pent_brk_87H = pent_brk.getExperimentsByAge([87,88]); pent_brk_87H.displayNumExperiments();
pent_brk_100H = pent_brk.getExperimentsByAge([100,101]); pent_brk_100H.displayNumExperiments();
pent_brk_109H = pent_brk.getExperimentsByAge([109,110.5]); pent_brk_109H.displayNumExperiments();

% ombAB wt/pent
wt_omb_79H = wt_omb.getExperimentsByAge([79,80]); wt_omb_79H.displayNumExperiments();
wt_omb_87H = wt_omb.getExperimentsByAge([87,88]); wt_omb_87H.displayNumExperiments();
wt_omb_100H = wt_omb.getExperimentsByAge([100,101]); wt_omb_100H.displayNumExperiments();
wt_omb_111H = wt_omb.getExperimentsByAge([111,112]); wt_omb_111H.displayNumExperiments();
pent_omb_79H = pent_omb.getExperimentsByAge([79,80]); pent_omb_79H.displayNumExperiments();
pent_omb_87H = pent_omb.getExperimentsByAge([87,88]); pent_omb_87H.displayNumExperiments();
pent_omb_100H = pent_omb.getExperimentsByAge([100,101]); pent_omb_100H.displayNumExperiments();
pent_omb_109H = pent_omb.getExperimentsByAge([109,110.5]); pent_omb_109H.displayNumExperiments();

%% ------------------------------------------------------------------------
% 1D EXPRESSION ANALYSIS

% % Plots all pmadAB and brkAB expression profiles of wt78H.
% wt78H.displayExperimentNames();
% 
% % Create lists or groups of expression profiles
% wt78H_pmadAB_DV_0_profiles = wt78H.get(:).getExpressionProfiles('pmadAB','D/V',[0,0]);
% wt90H_pmadAB_DV_0_profiles = wt90H.get(:).getExpressionProfiles('pmadAB','D/V',[0,0]);
% wt99H_pmadAB_DV_0_profiles = wt99H.get(:).getExpressionProfiles('pmadAB','D/V',[0,0]);
% wt110H_pmadAB_DV_0_profiles = wt110H.get(:).getExpressionProfiles('pmadAB','D/V',[0,0]);
% 
wt78H_brkAB_DV_0_profiles = wt78H.get(:).getExpressionProfiles('brkAB','D/V',[0,0]);
wt90H_brkAB_DV_0_profiles = wt90H.get(:).getExpressionProfiles('brkAB','D/V',[0,0]);
wt99H_brkAB_DV_0_profiles = wt99H.get(:).getExpressionProfiles('brkAB','D/V',[0,0]);
wt110H_brkAB_DV_0_profiles = wt110H.get(:).getExpressionProfiles('brkAB','D/V',[0,0]);
% 
% % % Display all expression profiles contained in wt78H_pmadAB_DV_0_profiles
% % wt78H_pmadAB_DV_0_profiles.displayExpressionProfileNames();
% 
% % % Plots all expression profiles contained in wt78H_pmadAB_DV_0_profiles on
% % % a single plot.
% % wt78H_pmadAB_DV_0_profiles.get(:).plotExpressionProfiles();
% 
% % % Plots the mean expression profile from the profiles in
% % % wt78H_pmadAB_DV_0_profiles.
% % % Data only
% % % [X Ymean Ystd] = wt78H_pmadAB_DV_0_profiles.get(:).computeMeanExpressionProfile();
% % % Plot directly
% % figure
% % [X Ymean Ystd] = wt78H_pmadAB_DV_0_profiles.get(:).plotMeanExpressionProfile(numPlotSamples);
% % figure
% % wt78H_brkAB_DV_0_profiles.get(:).plotMeanExpressionProfile(numPlotSamples);
% 
% % Plots a batch of mean expression profiles on the same figure.
% figure
% wt78H_pmadAB_DV_0_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,['''Color'',[' num2str(GOOGLEYELLOW) ']']);
% hold on
% wt90H_pmadAB_DV_0_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,['''Color'',[' num2str(GOOGLEGREEN) ']']);
% wt99H_pmadAB_DV_0_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,['''Color'',[' num2str(GOOGLEBLUE) ']']);
% wt110H_pmadAB_DV_0_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,['''Color'',[' num2str(GOOGLERED) ']']);
% set(gca,'YLim',[0 180]);
% title('Wt');
% xlabel('X (um)');
% ylabel('[P-Mad] (a.u.)');
% legend({'78-79H','90-91H','99-100H','110-111H'});
% 
figure
wt78H_brkAB_DV_0_profiles.get(:).plotMeanExpressionProfile(numInterpSamples,numPlotSamples,['''Color'',[' num2str(GOOGLEYELLOW) ']']);
hold on
wt90H_brkAB_DV_0_profiles.get(:).plotMeanExpressionProfile(numInterpSamples,numPlotSamples,['''Color'',[' num2str(GOOGLEGREEN) ']']);
wt99H_brkAB_DV_0_profiles.get(:).plotMeanExpressionProfile(numInterpSamples,numPlotSamples,['''Color'',[' num2str(GOOGLEBLUE) ']']);
wt110H_brkAB_DV_0_profiles.get(:).plotMeanExpressionProfile(numInterpSamples,numPlotSamples,['''Color'',[' num2str(GOOGLERED) ']']);
set(gca,'YLim',[0 180]);
title('Wt');
xlabel('X (um)');
ylabel('[Brk] (a.u.)');
legend({'78-79H','90-91H','99-100H','110-111H'});
% 
% % % Computes the correlations between any pair of mean expression profiles.
% % p1 = wt78H_pmadAB_DV_0_profiles.get(:).getMeanExpressionProfile();
% % p1.setName('Mean expression profile for wt78H_pmadAB_DV_0_profiles');
% % p2 = wt78H_brkAB_DV_0_profiles.get(:).getMeanExpressionProfile();
% % p2.setName('Mean expression profile for wt78H_brkAB_DV_0_profiles');
% % profiles = [p1,p2];
% % [R P] = profiles.computeExpressionProfileCorrelations();
% % disp(['Correlation between pmadAB and brkAB: corr coeff = ' num2str(R(1,2)) ', p-value = ' num2str(P(1,2))]);
% 
% 
% 
% pent78H_pmadAB_DV_0_profiles = pent78H.get(:).getExpressionProfiles('pmadAB','D/V',[0,0]);
% pent90H_pmadAB_DV_0_profiles = pent90H.get(:).getExpressionProfiles('pmadAB','D/V',[0,0]);
% pent99H_pmadAB_DV_0_profiles = pent99H.get(:).getExpressionProfiles('pmadAB','D/V',[0,0]);
% pent110H_pmadAB_DV_0_profiles = pent110H.get(:).getExpressionProfiles('pmadAB','D/V',[0,0]);
% 
% pent78H_brkAB_DV_0_profiles = pent78H.get(:).getExpressionProfiles('brkAB','D/V',[0,0]);
% pent90H_brkAB_DV_0_profiles = pent90H.get(:).getExpressionProfiles('brkAB','D/V',[0,0]);
% pent99H_brkAB_DV_0_profiles = pent99H.get(:).getExpressionProfiles('brkAB','D/V',[0,0]);
% pent110H_brkAB_DV_0_profiles = pent110H.get(:).getExpressionProfiles('brkAB','D/V',[0,0]);
% 
% figure
% pent78H_pmadAB_DV_0_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,['''Color'',[' num2str(GOOGLEYELLOW) ']']);
% hold on
% pent90H_pmadAB_DV_0_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,['''Color'',[' num2str(GOOGLEGREEN) ']']);
% pent99H_pmadAB_DV_0_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,['''Color'',[' num2str(GOOGLEBLUE) ']']);
% pent110H_pmadAB_DV_0_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,['''Color'',[' num2str(GOOGLERED) ']']);
% set(gca,'YLim',[0 180]);
% title('Pent2-5 mutants');
% xlabel('X (um)');
% ylabel('[P-Mad] (a.u.)');
% legend({'78-79H','90-91H','99-100H','110-111H'});
% 
% figure
% pent78H_brkAB_DV_0_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,['''Color'',[' num2str(GOOGLEYELLOW) ']']);
% hold on
% pent90H_brkAB_DV_0_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,['''Color'',[' num2str(GOOGLEGREEN) ']']);
% pent99H_brkAB_DV_0_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,['''Color'',[' num2str(GOOGLEBLUE) ']']);
% pent110H_brkAB_DV_0_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,['''Color'',[' num2str(GOOGLERED) ']']);
% set(gca,'YLim',[0 180]);
% title('Pent2-5 mutants');
% xlabel('X (um)');
% ylabel('[Brk] (a.u.)');
% legend({'78-79H','90-91H','99-100H','110-111H'});

% -------------------------------------------------------------------------
% salAB wt/pent2-5

% wt_dad_79H.get(:).showStructurePreview(true,'wt_dad_79H ');
% figure; wt_dad_89H.get(:).showStructurePreview(true,'wt_dad_89H ');
% figure; wt_dad_100H.get(:).showStructurePreview(true,'wt_dad_100H ');
% figure; wt_dad_110H.get(:).showStructurePreview(true,'wt_dad_110H ');

% pent_dad_79H.get(:).showStructurePreview(true,'pent_dad_79H ');
% pent_dad_90H.get(:).showStructurePreview(true,'pent_dad_90H ');
% pent_dad_99H.get(:).showStructurePreview(true,'pent_dad_99H ');
% pent_dad_114H.get(:).showStructurePreview(true,'pent_dad_114H ');
% pent_dad_114H.get(:).showStructurePreview();

% % Profiles
% wt_sal_73H_profile = wt_sal_73H.get(:).getExpressionProfiles('salLACZ','D/V',[0 0]);
% wt_sal_96H_profile = wt_sal_96H.get(:).getExpressionProfiles('salLACZ','D/V',[0 0]);
% wt_sal_114H_profile = wt_sal_114H.get(:).getExpressionProfiles('salLACZ','D/V',[0 0]);
% 
% figure
% wt_sal_114H_profile.get(:).plotExpressionProfiles();
% 
% figure
% wt_sal_73H_profile.get(:).plotMeanExpressionProfile(numPlotSamples,['''Color'',[' num2str(GOOGLEYELLOW) ']']);
% hold on
% % pent_omb_91H_profile.get(:).plotMeanExpressionProfile(numPlotSamples,['''Color'',[' num2str(GOOGLEGREEN) ']']);
% wt_sal_96H_profile.get(:).plotMeanExpressionProfile(numPlotSamples,['''Color'',[' num2str(GOOGLEBLUE) ']']);
% wt_sal_114H_profile.get(:).plotMeanExpressionProfile(numPlotSamples,['''Color'',[' num2str(GOOGLERED) ']']);
% % set(gca,'YLim',[0 180]);
% title('Wt');
% xlabel('X (um)');
% ylabel('[Sal] (a.u.)');
% legend({'73-74H','96-97H','114-115H'});

% wt_dad_79H = wt_dad.getExperimentsByAge([79,80]); wt_dad_79H.displayNumExperiments();
% wt_dad_89H = wt_dad.getExperimentsByAge([89,90]); wt_dad_89H.displayNumExperiments();
% wt_dad_100H = wt_dad.getExperimentsByAge([100,101]); wt_dad_100H.displayNumExperiments();
% wt_dad_110H = wt_dad.getExperimentsByAge([110.5,111.5]); wt_dad_110H.displayNumExperiments();
% pent_dad_79H = pent_dad.getExperimentsByAge([79,80]); pent_dad_79H.displayNumExperiments();
% pent_dad_90H = pent_dad.getExperimentsByAge([90,91]); pent_dad_90H.displayNumExperiments();
% pent_dad_99H = pent_dad.getExperimentsByAge([100,101]); pent_dad_99H.displayNumExperiments();
% pent_dad_114H = pent_dad.getExperimentsByAge([110.5,111.5]); pent_dad_114H.displayNumExperiments();

% -------------------------------------------------------------------------
% Pent

% figure
% wt_pent_95H.get(:).showStructurePreview();
% figure
% wt_pent_111H.get(:).showStructurePreview();
% 
% % Profiles
% wt_pent_72H_profile = wt_pent_72H.get(:).getExpressionProfiles('pentGFP','D/V',[0 0]);
% wt_pent_95H_profile = wt_pent_95H.get(:).getExpressionProfiles('pentGFP','D/V',[0 0]);
% wt_pent_111H_profile = wt_pent_111H.get(:).getExpressionProfiles('pentGFP','D/V',[0 0]);
% 
% % figure
% % wt_pent_72H_profile.get(:).plotExpressionProfiles();
% 
% figure
% wt_pent_72H_profile.get(:).plotMeanExpressionProfile(numPlotSamples,['''Color'',[' num2str(GOOGLEYELLOW) ']']);
% hold on
% % pent_omb_91H_profile.get(:).plotMeanExpressionProfile(numPlotSamples,['''Color'',[' num2str(GOOGLEGREEN) ']']);
% wt_pent_95H_profile.get(:).plotMeanExpressionProfile(numPlotSamples,['''Color'',[' num2str(GOOGLEBLUE) ']']);
% wt_pent_111H_profile.get(:).plotMeanExpressionProfile(numPlotSamples,['''Color'',[' num2str(GOOGLERED) ']']);
% % set(gca,'YLim',[0 180]);
% title('Wt');
% xlabel('X (um)');
% ylabel('[Pent] (a.u.)');
% legend({'72-73H','95.5-96.5H','111-112H'});
% 
% % wt_pent_72H = wt_pent.getExperimentsByAge([72,73]); wt_pent_72H.displayNumExperiments();
% % wt_pent_95H = wt_pent.getExperimentsByAge([95.5,96.5]); wt_pent_95H.displayNumExperiments();
% % wt_pent_111H = wt_pent.getExperimentsByAge([111,112]); wt_pent_111H.displayNumExperiments();

% -------------------------------------------------------------------------
% brkAB wt/pent2-5

% % figure; wt_brk_79H.get(:).showStructurePreview();
% % figure; wt_brk_87H.get(:).showStructurePreview();
% % figure; wt_brk_100H.get(:).showStructurePreview();
% % figure; wt_brk_111H.get(:).showStructurePreview();
% 
% % figure; pent_brk_79H.get(:).showStructurePreview();
% % figure; pent_brk_87H.get(:).showStructurePreview();
% % figure; pent_brk_100H.get(:).showStructurePreview();
% % figure; pent_brk_109H.get(:).showStructurePreview();
% 
% % Profiles
% wt_brk_79H_profiles = wt_brk_79H.get(:).getExpressionProfiles('brkAB','D/V',[0 0]);
% wt_brk_87H_profiles = wt_brk_87H.get(:).getExpressionProfiles('brkAB','D/V',[0 0]);
% wt_brk_100H_profiles = wt_brk_100H.get(:).getExpressionProfiles('brkAB','D/V',[0 0]);
% wt_brk_111H_profiles = wt_brk_111H.get(:).getExpressionProfiles('brkAB','D/V',[0 0]);
% pent_brk_79H_profiles = pent_brk_79H.get(:).getExpressionProfiles('brkAB','D/V',[0 0]);
% pent_brk_87H_profiles = pent_brk_87H.get(:).getExpressionProfiles('brkAB','D/V',[0 0]);
% pent_brk_100H_profiles = pent_brk_100H.get(:).getExpressionProfiles('brkAB','D/V',[0 0]);
% pent_brk_109H_profiles = pent_brk_109H.get(:).getExpressionProfiles('brkAB','D/V',[0 0]);
% 
% % figure
% % figure; wt_brk_111H_profiles.get(8:12).plotExpressionProfiles();
% 
% % Wild-type
% figure
% wt_brk_79H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,['''Color'',[' num2str(GOOGLEYELLOW) ']']);
% hold on
% wt_brk_87H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,['''Color'',[' num2str(GOOGLEGREEN) ']']);
% wt_brk_100H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,['''Color'',[' num2str(GOOGLEBLUE) ']']);
% wt_brk_111H_profiles.get(8:12).plotMeanExpressionProfile(numPlotSamples,['''Color'',[' num2str(GOOGLERED) ']']);
% set(gca,'YLim',[0 180]);
% title('Wt');
% xlabel('X (um)');
% ylabel('[Brk] (a.u.)');
% legend({'79-80H','87-88H','100-101H','111-112H'});
% 
% % Pent2-5 mutants
% figure
% pent_brk_79H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,['''Color'',[' num2str(GOOGLEYELLOW) ']']);
% hold on
% pent_brk_87H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,['''Color'',[' num2str(GOOGLEGREEN) ']']);
% pent_brk_100H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,['''Color'',[' num2str(GOOGLEBLUE) ']']);
% pent_brk_109H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,['''Color'',[' num2str(GOOGLERED) ']']);
% set(gca,'YLim',[0 180]);
% title('Pent2-5 mutants');
% xlabel('X (um)');
% ylabel('[Brk] (a.u.)');
% legend({'79-80H','87-88H','100-101H','109-110.5H'});

% -------------------------------------------------------------------------
% ombAB wt/pent2-5

% % figure; wt_omb_79H.get(:).showStructurePreview();
% % figure; wt_omb_87H.get(:).showStructurePreview();
% % figure; wt_omb_100H.get(:).showStructurePreview();
% % figure; wt_omb_111H.get(:).showStructurePreview();
% 
% % figure; pent_omb_79H.get(:).showStructurePreview();
% % figure; pent_omb_87H.get(:).showStructurePreview();
% % figure; pent_omb_100H.get(:).showStructurePreview();
% % figure; pent_omb_109H.get(:).showStructurePreview();
% 
% % Profiles
% wt_omb_79H_profiles = wt_omb_79H.get(:).getExpressionProfiles('ombAB','D/V',[0 0]);
% wt_omb_87H_profiles = wt_omb_87H.get(:).getExpressionProfiles('ombAB','D/V',[0 0]);
% wt_omb_100H_profiles = wt_omb_100H.get(:).getExpressionProfiles('ombAB','D/V',[0 0]);
% wt_omb_111H_profiles = wt_omb_111H.get(:).getExpressionProfiles('ombAB','D/V',[0 0]);
% pent_omb_79H_profiles = pent_omb_79H.get(:).getExpressionProfiles('ombAB','D/V',[0 0]);
% pent_omb_87H_profiles = pent_omb_87H.get(:).getExpressionProfiles('ombAB','D/V',[0 0]);
% pent_omb_100H_profiles = pent_omb_100H.get(:).getExpressionProfiles('ombAB','D/V',[0 0]);
% pent_omb_109H_profiles = pent_omb_109H.get(:).getExpressionProfiles('ombAB','D/V',[0 0]);
% 
% % figure
% % figure; wt_brk_111H_profiles.get(8:12).plotExpressionProfiles();
% 
% % Wild-type
% figure
% wt_omb_79H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,['''Color'',[' num2str(GOOGLEYELLOW) ']']);
% hold on
% wt_omb_87H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,['''Color'',[' num2str(GOOGLEGREEN) ']']);
% wt_omb_100H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,['''Color'',[' num2str(GOOGLEBLUE) ']']);
% wt_omb_111H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,['''Color'',[' num2str(GOOGLERED) ']']);
% set(gca,'YLim',[0 180]);
% title('Wt');
% xlabel('X (um)');
% ylabel('[Omb] (a.u.)');
% legend({'79-80H','87-88H','100-101H','111-112H'});
% 
% % Pent2-5 mutants
% figure
% pent_omb_79H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,['''Color'',[' num2str(GOOGLEYELLOW) ']']);
% hold on
% pent_omb_87H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,['''Color'',[' num2str(GOOGLEGREEN) ']']);
% pent_omb_100H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,['''Color'',[' num2str(GOOGLEBLUE) ']']);
% pent_omb_109H_profiles.get(:).plotMeanExpressionProfile(numPlotSamples,['''Color'',[' num2str(GOOGLERED) ']']);
% set(gca,'YLim',[0 180]);
% title('Pent2-5 mutants');
% xlabel('X (um)');
% ylabel('[Omb] (a.u.)');
% legend({'79-80H','87-88H','100-101H','109-110.5H'});
