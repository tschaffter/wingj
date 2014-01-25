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

% dadGFP wt/pent2-5
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
% 2D EXPRESSION ANALYSIS (PART I: mean expression)

% Create lists of expression profiles
wt_dadGFP_80H_maps = wt_dadGFP_80H.get(:).getExpressionMaps('dadGFP',[-100,-100]);
wt_dadGFP_90H_maps = wt_dadGFP_90H.get(:).getExpressionMaps('dadGFP',[-100,-100]);
wt_dadGFP_100H_maps = wt_dadGFP_100H.get(:).getExpressionMaps('dadGFP',[-100,-100]);

pent_dadGFP_80H_maps = pent_dadGFP_80H.get(:).getExpressionMaps('dadGFP',[-100,-100]);
pent_dadGFP_90H_maps = pent_dadGFP_90H.get(:).getExpressionMaps('dadGFP',[-100,-100]);
pent_dadGFP_100H_maps = pent_dadGFP_100H.get(:).getExpressionMaps('dadGFP',[-100,-100]);


% Shows various images
% figure; wt_dadGFP_90H.get(:).showStructurePreview();
% figure; wt_dadGFP_80H_maps.get(:).showExpressionMaps();


% Computes/plots/saves mean expression maps
wt_dadGFP_80H_maps_mean = wt_dadGFP_80H_maps.get(:).computeMeanExpressionMap();
wt_dadGFP_90H_maps_mean = wt_dadGFP_90H_maps.get(:).computeMeanExpressionMap();
wt_dadGFP_100H_maps_mean = wt_dadGFP_100H_maps.get(:).computeMeanExpressionMap();

pent_dadGFP_80H_maps_mean = pent_dadGFP_80H_maps.get(:).computeMeanExpressionMap();
pent_dadGFP_90H_maps_mean = pent_dadGFP_90H_maps.get(:).computeMeanExpressionMap();
pent_dadGFP_100H_maps_mean = pent_dadGFP_100H_maps.get(:).computeMeanExpressionMap();

imwrite(wt_dadGFP_80H_maps_mean,[outputDirectory 'wt_dadGFP_80H_maps_mean.tif'],'tif');
imwrite(wt_dadGFP_90H_maps_mean,[outputDirectory 'wt_dadGFP_90H_maps_mean.tif'],'tif');
imwrite(wt_dadGFP_100H_maps_mean,[outputDirectory 'wt_dadGFP_100H_maps_mean.tif'],'tif');

imwrite(pent_dadGFP_80H_maps_mean,[outputDirectory 'pent_dadGFP_80H_maps_mean.tif'],'tif');
imwrite(pent_dadGFP_90H_maps_mean,[outputDirectory 'pent_dadGFP_90H_maps_mean.tif'],'tif');
imwrite(pent_dadGFP_100H_maps_mean,[outputDirectory 'pent_dadGFP_100H_maps_mean.tif'],'tif');


% figure
% wt_dadGFP_80H_maps.get(:).showStdExpressionMap();


% Look at the difference between mean expression maps
wt_dadGFP_90H80H_maps_mean_dist = compute_image_difference(wt_dadGFP_90H_maps_mean,wt_dadGFP_80H_maps_mean);
wt_dadGFP_100H90H_maps_mean_dist = compute_image_difference(wt_dadGFP_100H_maps_mean,wt_dadGFP_90H_maps_mean);
wt_dadGFP_100H80H_maps_mean_dist = compute_image_difference(wt_dadGFP_100H_maps_mean,wt_dadGFP_80H_maps_mean);

pent_dadGFP_90H80H_maps_mean_dist = compute_image_difference(pent_dadGFP_90H_maps_mean,pent_dadGFP_80H_maps_mean);
pent_dadGFP_100H90H_maps_mean_dist = compute_image_difference(pent_dadGFP_100H_maps_mean,pent_dadGFP_90H_maps_mean);
pent_dadGFP_100H80H_maps_mean_dist = compute_image_difference(pent_dadGFP_100H_maps_mean,pent_dadGFP_80H_maps_mean);

wt_pent_dadGFP_80H_maps_mean_dist = compute_image_difference(pent_dadGFP_80H_maps_mean,wt_dadGFP_80H_maps_mean);
wt_pent_dadGFP_90H_maps_mean_dist = compute_image_difference(pent_dadGFP_90H_maps_mean,wt_dadGFP_90H_maps_mean);
wt_pent_dadGFP_100H_maps_mean_dist = compute_image_difference(pent_dadGFP_100H_maps_mean,wt_dadGFP_100H_maps_mean);

range = [-120 120];

show_image_difference(wt_dadGFP_90H80H_maps_mean_dist,[],range,[outputDirectory 'wt_dadGFP_90H80H_maps_mean_diff_2.tif']);
show_image_difference(wt_dadGFP_100H90H_maps_mean_dist,[],range,[outputDirectory 'wt_dadGFP_100H90H_maps_mean_diff_2.tif']);
show_image_difference(wt_dadGFP_100H80H_maps_mean_dist,[],range,[outputDirectory 'wt_dadGFP_100H80H_maps_mean_diff_2.tif']);

show_image_difference(pent_dadGFP_90H80H_maps_mean_dist,[],range,[outputDirectory 'pent_dadGFP_90H80H_maps_mean_diff_2.tif']);
show_image_difference(pent_dadGFP_100H90H_maps_mean_dist,[],range,[outputDirectory 'pent_dadGFP_100H90H_maps_mean_diff_2.tif']);
show_image_difference(pent_dadGFP_100H80H_maps_mean_dist,[],range,[outputDirectory 'pent_dadGFP_100H80H_maps_mean_diff_2.tif']);

show_image_difference(wt_pent_dadGFP_80H_maps_mean_dist,[],range,[outputDirectory 'wt_pent_dadGFP_80H_maps_mean_diff_2.tif']);
show_image_difference(wt_pent_dadGFP_90H_maps_mean_dist,[],range,[outputDirectory 'wt_pent_dadGFP_90H_maps_mean_diff_2.tif']);
show_image_difference(wt_pent_dadGFP_100H_maps_mean_dist,[],range,[outputDirectory 'wt_pent_dadGFP_100H_maps_mean_diff_2.tif']);

% min1 = min(min(wt_dadGFP_90H80H_maps_mean_dist));
% min2 = min(min(wt_dadGFP_100H90H_maps_mean_dist));
% min3 = min(min(pent_dadGFP_90H80H_maps_mean_dist));
% min4 = min(min(pent_dadGFP_100H90H_maps_mean_dist));
% 
% max1 = max(max(wt_dadGFP_90H80H_maps_mean_dist));
% max2 = max(max(wt_dadGFP_100H90H_maps_mean_dist));
% max3 = max(max(pent_dadGFP_90H80H_maps_mean_dist));
% max4 = max(max(pent_dadGFP_100H90H_maps_mean_dist));
% 
% min([min1 min2 min3 min4])
% max([max1 max2 max3 max4])