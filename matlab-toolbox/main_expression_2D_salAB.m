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
experimentRootDirectories = {'20101129_salAB_dadGFP_wg-ptcAB_79-80H'
                             '20101129_salAB_dadGFP_wg-ptcAB_89-90H'
                             '20101129_salAB_dadGFP_wg-ptcAB_100-101H'
                             '20101129_salAB_dadGFP_wg-ptcAB_110,5-111,5H'
                             '20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_79-80H'
                             '20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_90-91H'
                             '20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_99-101H'
                             '20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_114-115H'}; 
                              
experimentRootDirectories = strcat('/mnt/extra/wingj_benchmarks/',experimentRootDirectories);
outputDirectory = '/home/tschaffter/expression_maps/';

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
% 2D EXPRESSION ANALYSIS (PART I: mean expression)

% Create lists of expression profiles
wt_salAB_80H_maps = wt_salAB_80H.get(:).getExpressionMaps('salAB',[-100,-100]);
wt_salAB_90H_maps = wt_salAB_90H.get(:).getExpressionMaps('salAB',[-100,-100]);
wt_salAB_100H_maps = wt_salAB_100H.get(:).getExpressionMaps('salAB',[-100,-100]);

pent_salAB_80H_maps = pent_salAB_80H.get(:).getExpressionMaps('salAB',[-100,-100]);
pent_salAB_90H_maps = pent_salAB_90H.get(:).getExpressionMaps('salAB',[-100,-100]);
pent_salAB_100H_maps = pent_salAB_100H.get(:).getExpressionMaps('salAB',[-100,-100]);


% Shows various images
% figure; wt_salAB_90H.get(:).showStructurePreview();
% figure; wt_salAB_80H_maps.get(:).showExpressionMaps();


% Computes/plots/saves mean expression maps
wt_salAB_80H_maps_mean = wt_salAB_80H_maps.get(:).computeMeanExpressionMap();
wt_salAB_90H_maps_mean = wt_salAB_90H_maps.get(:).computeMeanExpressionMap();
wt_salAB_100H_maps_mean = wt_salAB_100H_maps.get(:).computeMeanExpressionMap();

pent_salAB_80H_maps_mean = pent_salAB_80H_maps.get(:).computeMeanExpressionMap();
pent_salAB_90H_maps_mean = pent_salAB_90H_maps.get(:).computeMeanExpressionMap();
pent_salAB_100H_maps_mean = pent_salAB_100H_maps.get(:).computeMeanExpressionMap();

imwrite(wt_salAB_80H_maps_mean,[outputDirectory 'wt_salAB_80H_maps_mean.tif'],'tif');
imwrite(wt_salAB_90H_maps_mean,[outputDirectory 'wt_salAB_90H_maps_mean.tif'],'tif');
imwrite(wt_salAB_100H_maps_mean,[outputDirectory 'wt_salAB_100H_maps_mean.tif'],'tif');

imwrite(pent_salAB_80H_maps_mean,[outputDirectory 'pent_salAB_80H_maps_mean.tif'],'tif');
imwrite(pent_salAB_90H_maps_mean,[outputDirectory 'pent_salAB_90H_maps_mean.tif'],'tif');
imwrite(pent_salAB_100H_maps_mean,[outputDirectory 'pent_salAB_100H_maps_mean.tif'],'tif');


% figure
% wt_salAB_80H_maps.get(:).showStdExpressionMap();


% Look at the difference between mean expression maps
wt_salAB_90H80H_maps_mean_dist = compute_image_difference(wt_salAB_90H_maps_mean,wt_salAB_80H_maps_mean);
wt_salAB_100H90H_maps_mean_dist = compute_image_difference(wt_salAB_100H_maps_mean,wt_salAB_90H_maps_mean);
wt_salAB_100H80H_maps_mean_dist = compute_image_difference(wt_salAB_100H_maps_mean,wt_salAB_80H_maps_mean);

pent_salAB_90H80H_maps_mean_dist = compute_image_difference(pent_salAB_90H_maps_mean,pent_salAB_80H_maps_mean);
pent_salAB_100H90H_maps_mean_dist = compute_image_difference(pent_salAB_100H_maps_mean,pent_salAB_90H_maps_mean);
pent_salAB_100H80H_maps_mean_dist = compute_image_difference(pent_salAB_100H_maps_mean,pent_salAB_80H_maps_mean);

wt_pent_salAB_80H_maps_mean_dist = compute_image_difference(pent_salAB_80H_maps_mean,wt_salAB_80H_maps_mean);
wt_pent_salAB_90H_maps_mean_dist = compute_image_difference(pent_salAB_90H_maps_mean,wt_salAB_90H_maps_mean);
wt_pent_salAB_100H_maps_mean_dist = compute_image_difference(pent_salAB_100H_maps_mean,wt_salAB_100H_maps_mean);

range = [-120 120];

show_image_difference(wt_salAB_90H80H_maps_mean_dist,[],range,[outputDirectory 'wt_salAB_90H80H_maps_mean_diff_2.tif']);
show_image_difference(wt_salAB_100H90H_maps_mean_dist,[],range,[outputDirectory 'wt_salAB_100H90H_maps_mean_diff_2.tif']);
show_image_difference(wt_salAB_100H80H_maps_mean_dist,[],range,[outputDirectory 'wt_salAB_100H80H_maps_mean_diff_2.tif']);

show_image_difference(pent_salAB_90H80H_maps_mean_dist,[],range,[outputDirectory 'pent_salAB_90H80H_maps_mean_diff_2.tif']);
show_image_difference(pent_salAB_100H90H_maps_mean_dist,[],range,[outputDirectory 'pent_salAB_100H90H_maps_mean_diff_2.tif']);
show_image_difference(pent_salAB_100H80H_maps_mean_dist,[],range,[outputDirectory 'pent_salAB_100H80H_maps_mean_diff_2.tif']);

show_image_difference(wt_pent_salAB_80H_maps_mean_dist,[],range,[outputDirectory 'wt_pent_salAB_80H_maps_mean_diff_2.tif']);
show_image_difference(wt_pent_salAB_90H_maps_mean_dist,[],range,[outputDirectory 'wt_pent_salAB_90H_maps_mean_diff_2.tif']);
show_image_difference(wt_pent_salAB_100H_maps_mean_dist,[],range,[outputDirectory 'wt_pent_salAB_100H_maps_mean_diff_2.tif']);

% min1 = min(min(wt_salAB_90H80H_maps_mean_dist));
% min2 = min(min(wt_salAB_100H90H_maps_mean_dist));
% min3 = min(min(pent_salAB_90H80H_maps_mean_dist));
% min4 = min(min(pent_salAB_100H90H_maps_mean_dist));
% 
% max1 = max(max(wt_salAB_90H80H_maps_mean_dist));
% max2 = max(max(wt_salAB_100H90H_maps_mean_dist));
% max3 = max(max(pent_salAB_90H80H_maps_mean_dist));
% max4 = max(max(pent_salAB_100H90H_maps_mean_dist));
% 
% min([min1 min2 min3 min4])
% max([max1 max2 max3 max4])