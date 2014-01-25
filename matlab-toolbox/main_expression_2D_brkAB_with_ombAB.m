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
outputDirectory = '/home/tschaffter/expression_maps/';

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
% 2D EXPRESSION ANALYSIS (PART I: mean expression)

% Create lists of expression profiles
wt_brkAB2_80H_maps = wt_brkAB_80H.get(:).getExpressionMaps('brkAB',[-100,-100]);
wt_brkAB2_90H_maps = wt_brkAB_90H.get(:).getExpressionMaps('brkAB',[-100,-100]);
wt_brkAB2_100H_maps = wt_brkAB_100H.get(:).getExpressionMaps('brkAB',[-100,-100]);

pent_brkAB2_80H_maps = pent_brkAB_80H.get(:).getExpressionMaps('brkAB',[-100,-100]);
pent_brkAB2_90H_maps = pent_brkAB_90H.get(:).getExpressionMaps('brkAB',[-100,-100]);
pent_brkAB2_100H_maps = pent_brkAB_100H.get(:).getExpressionMaps('brkAB',[-100,-100]);


% Shows various images
% figure; wt_brkAB2_90H.get(:).showStructurePreview();
% figure; wt_brkAB2_80H_maps.get(:).showExpressionMaps();


% Computes/plots/saves mean expression maps
wt_brkAB2_80H_maps_mean = wt_brkAB2_80H_maps.get(:).computeMeanExpressionMap();
wt_brkAB2_90H_maps_mean = wt_brkAB2_90H_maps.get(:).computeMeanExpressionMap();
wt_brkAB2_100H_maps_mean = wt_brkAB2_100H_maps.get(:).computeMeanExpressionMap();

pent_brkAB2_80H_maps_mean = pent_brkAB2_80H_maps.get(:).computeMeanExpressionMap();
pent_brkAB2_90H_maps_mean = pent_brkAB2_90H_maps.get(:).computeMeanExpressionMap();
pent_brkAB2_100H_maps_mean = pent_brkAB2_100H_maps.get(:).computeMeanExpressionMap();

imwrite(wt_brkAB2_80H_maps_mean,[outputDirectory 'wt_brkAB2_80H_maps_mean.tif'],'tif');
imwrite(wt_brkAB2_90H_maps_mean,[outputDirectory 'wt_brkAB2_90H_maps_mean.tif'],'tif');
imwrite(wt_brkAB2_100H_maps_mean,[outputDirectory 'wt_brkAB2_100H_maps_mean.tif'],'tif');

imwrite(pent_brkAB2_80H_maps_mean,[outputDirectory 'pent_brkAB2_80H_maps_mean.tif'],'tif');
imwrite(pent_brkAB2_90H_maps_mean,[outputDirectory 'pent_brkAB2_90H_maps_mean.tif'],'tif');
imwrite(pent_brkAB2_100H_maps_mean,[outputDirectory 'pent_brkAB2_100H_maps_mean.tif'],'tif');


% figure
% wt_brkAB2_80H_maps.get(:).showStdExpressionMap();


% Look at the difference between mean expression maps
wt_brkAB2_90H80H_maps_mean_dist = compute_image_difference(wt_brkAB2_90H_maps_mean,wt_brkAB2_80H_maps_mean);
wt_brkAB2_100H90H_maps_mean_dist = compute_image_difference(wt_brkAB2_100H_maps_mean,wt_brkAB2_90H_maps_mean);
wt_brkAB2_100H80H_maps_mean_dist = compute_image_difference(wt_brkAB2_100H_maps_mean,wt_brkAB2_80H_maps_mean);

pent_brkAB2_90H80H_maps_mean_dist = compute_image_difference(pent_brkAB2_90H_maps_mean,pent_brkAB2_80H_maps_mean);
pent_brkAB2_100H90H_maps_mean_dist = compute_image_difference(pent_brkAB2_100H_maps_mean,pent_brkAB2_90H_maps_mean);
pent_brkAB2_100H80H_maps_mean_dist = compute_image_difference(pent_brkAB2_100H_maps_mean,pent_brkAB2_80H_maps_mean);

wt_pent_brkAB2_80H_maps_mean_dist = compute_image_difference(pent_brkAB2_80H_maps_mean,wt_brkAB2_80H_maps_mean);
wt_pent_brkAB2_90H_maps_mean_dist = compute_image_difference(pent_brkAB2_90H_maps_mean,wt_brkAB2_90H_maps_mean);
wt_pent_brkAB2_100H_maps_mean_dist = compute_image_difference(pent_brkAB2_100H_maps_mean,wt_brkAB2_100H_maps_mean);

range = [-120 120];

show_image_difference(wt_brkAB2_90H80H_maps_mean_dist,[],range,[outputDirectory 'wt_brkAB2_90H80H_maps_mean_diff.tif']);
show_image_difference(wt_brkAB2_100H90H_maps_mean_dist,[],range,[outputDirectory 'wt_brkAB2_100H90H_maps_mean_diff.tif']);
show_image_difference(wt_brkAB2_100H80H_maps_mean_dist,[],range,[outputDirectory 'wt_brkAB2_100H80H_maps_mean_diff.tif']);

show_image_difference(pent_brkAB2_90H80H_maps_mean_dist,[],range,[outputDirectory 'pent_brkAB2_90H80H_maps_mean_diff.tif']);
show_image_difference(pent_brkAB2_100H90H_maps_mean_dist,[],range,[outputDirectory 'pent_brkAB2_100H90H_maps_mean_diff.tif']);
show_image_difference(pent_brkAB2_100H80H_maps_mean_dist,[],range,[outputDirectory 'pent_brkAB2_100H80H_maps_mean_diff.tif']);

show_image_difference(wt_pent_brkAB2_80H_maps_mean_dist,[],range,[outputDirectory 'wt_pent_brkAB2_80H_maps_mean_diff.tif']);
show_image_difference(wt_pent_brkAB2_90H_maps_mean_dist,[],range,[outputDirectory 'wt_pent_brkAB2_90H_maps_mean_diff.tif']);
show_image_difference(wt_pent_brkAB2_100H_maps_mean_dist,[],range,[outputDirectory 'wt_pent_brkAB2_100H_maps_mean_diff.tif']);

% min1 = min(min(wt_brkAB2_90H80H_maps_mean_dist));
% min2 = min(min(wt_brkAB2_100H90H_maps_mean_dist));
% min3 = min(min(pent_brkAB2_90H80H_maps_mean_dist));
% min4 = min(min(pent_brkAB2_100H90H_maps_mean_dist));
% 
% max1 = max(max(wt_brkAB2_90H80H_maps_mean_dist));
% max2 = max(max(wt_brkAB2_100H90H_maps_mean_dist));
% max3 = max(max(pent_brkAB2_90H80H_maps_mean_dist));
% max4 = max(max(pent_brkAB2_100H90H_maps_mean_dist));
% 
% min([min1 min2 min3 min4])
% max([max1 max2 max3 max4])