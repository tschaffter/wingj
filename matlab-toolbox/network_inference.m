close all; clear all; clc;

addpath('lib');
addpath('nuclei_detector');
addpath('structure');

%% ------------------------------------------------------------------------
% EXPERIMENT LOCATIONS

outputDirectory = '/home/tschaffter/expression_maps/';

%% ------------------------------------------------------------------------
% LOAD EXPERIMENTS

wt_dadGFP_80H = imread([outputDirectory 'wt_dadGFP_80H_maps_mean.tif']);
wt_dadGFP_90H = imread([outputDirectory 'wt_dadGFP_90H_maps_mean.tif']);
wt_dadGFP_100H = imread([outputDirectory 'wt_dadGFP_100H_maps_mean.tif']);
pent_dadGFP_80H = imread([outputDirectory 'pent_dadGFP_80H_maps_mean.tif']);
pent_dadGFP_90H = imread([outputDirectory 'pent_dadGFP_90H_maps_mean.tif']);
pent_dadGFP_100H = imread([outputDirectory 'pent_dadGFP_100H_maps_mean.tif']);

wt_pmadAB_80H = imread([outputDirectory 'wt_pmadAB_80H_maps_mean.tif']);
wt_pmadAB_90H = imread([outputDirectory 'wt_pmadAB_90H_maps_mean.tif']);
wt_pmadAB_100H = imread([outputDirectory 'wt_pmadAB_100H_maps_mean.tif']);
pent_pmadAB_80H = imread([outputDirectory 'pent_pmadAB_80H_maps_mean.tif']);
pent_pmadAB_90H = imread([outputDirectory 'pent_pmadAB_90H_maps_mean.tif']);
pent_pmadAB_100H = imread([outputDirectory 'pent_pmadAB_100H_maps_mean.tif']);

wt_brkAB_80H = imread([outputDirectory 'wt_brkAB_80H_maps_mean.tif']);
wt_brkAB_90H = imread([outputDirectory 'wt_brkAB_90H_maps_mean.tif']);
wt_brkAB_100H = imread([outputDirectory 'wt_brkAB_100H_maps_mean.tif']);
pent_brkAB_80H = imread([outputDirectory 'pent_brkAB_80H_maps_mean.tif']);
pent_brkAB_90H = imread([outputDirectory 'pent_brkAB_90H_maps_mean.tif']);
pent_brkAB_100H = imread([outputDirectory 'pent_brkAB_100H_maps_mean.tif']);

wt_salAB_80H = imread([outputDirectory 'wt_salAB_80H_maps_mean.tif']);
wt_salAB_90H = imread([outputDirectory 'wt_salAB_90H_maps_mean.tif']);
wt_salAB_100H = imread([outputDirectory 'wt_salAB_100H_maps_mean.tif']);
pent_salAB_80H = imread([outputDirectory 'pent_salAB_80H_maps_mean.tif']);
pent_salAB_90H = imread([outputDirectory 'pent_salAB_90H_maps_mean.tif']);
pent_salAB_100H = imread([outputDirectory 'pent_salAB_100H_maps_mean.tif']);

wt_ombAB_80H = imread([outputDirectory 'wt_ombAB_80H_maps_mean.tif']);
wt_ombAB_90H = imread([outputDirectory 'wt_ombAB_90H_maps_mean.tif']);
wt_ombAB_100H = imread([outputDirectory 'wt_ombAB_100H_maps_mean.tif']);
pent_ombAB_80H = imread([outputDirectory 'pent_ombAB_80H_maps_mean.tif']);
pent_ombAB_90H = imread([outputDirectory 'pent_ombAB_90H_maps_mean.tif']);
pent_ombAB_100H = imread([outputDirectory 'pent_ombAB_100H_maps_mean.tif']);

wt_wgAB_90H = imread([outputDirectory 'wt_wgAB_90H_maps_mean.tif']);

wt_pmadAB_80H_d = double(wt_pmadAB_80H);
wt_pmadAB_90H_d = double(wt_pmadAB_90H);
wt_pmadAB_100H_d = double(wt_pmadAB_100H);
pent_pmadAB_80H_d = double(pent_pmadAB_80H);
pent_pmadAB_90H_d = double(pent_pmadAB_90H);
pent_pmadAB_100H_d = double(pent_pmadAB_100H);

wt_brkAB_80H_d = double(wt_brkAB_80H);
wt_brkAB_90H_d = double(wt_brkAB_90H);
wt_brkAB_100H_d = double(wt_brkAB_100H);
pent_brkAB_80H_d = double(pent_brkAB_80H);
pent_brkAB_90H_d = double(pent_brkAB_90H);
pent_brkAB_100H_d = double(pent_brkAB_100H);

wt_dadGFP_80H_d = double(wt_dadGFP_80H);
wt_dadGFP_90H_d = double(wt_dadGFP_80H);
wt_dadGFP_100H_d = double(wt_dadGFP_80H);
pent_dadGFP_80H_d = double(pent_dadGFP_80H);
pent_dadGFP_90H_d = double(pent_dadGFP_80H);
pent_dadGFP_100H_d = double(pent_dadGFP_80H);

wt_salAB_80H_d = double(wt_salAB_80H);
wt_salAB_90H_d = double(wt_salAB_90H);
wt_salAB_100H_d = double(wt_salAB_100H);
pent_salAB_80H_d = double(pent_salAB_80H);
pent_salAB_90H_d = double(pent_salAB_90H);
pent_salAB_100H_d = double(pent_salAB_100H);

wt_ombAB_80H_d = double(wt_ombAB_80H);
wt_ombAB_90H_d = double(wt_ombAB_90H);
wt_ombAB_100H_d = double(wt_ombAB_100H);
pent_ombAB_80H_d = double(pent_ombAB_80H);
pent_ombAB_90H_d = double(pent_ombAB_90H);
pent_ombAB_100H_d = double(pent_ombAB_100H);

wt_wgAB_90H_d = double(wt_wgAB_90H);

wt_80H = {wt_dadGFP_80H_d,wt_pmadAB_80H_d,wt_brkAB_80H_d,wt_salAB_80H_d,wt_ombAB_80H_d};
wt_90H = {wt_dadGFP_90H_d,wt_pmadAB_90H_d,wt_brkAB_90H_d,wt_salAB_90H_d,wt_ombAB_90H_d,wt_wgAB_90H_d};
wt_100H = {wt_dadGFP_100H_d,wt_pmadAB_100H_d,wt_brkAB_100H_d,wt_salAB_100H_d,wt_ombAB_100H_d};
pent_80H = {pent_dadGFP_80H_d,pent_pmadAB_80H_d,pent_brkAB_80H_d,pent_salAB_80H_d,pent_ombAB_80H_d};
pent_90H = {pent_dadGFP_90H_d,pent_pmadAB_90H_d,pent_brkAB_90H_d,pent_salAB_90H_d,pent_ombAB_90H_d};
pent_100H = {pent_dadGFP_100H_d,pent_pmadAB_100H_d,pent_brkAB_100H_d,pent_salAB_100H_d,pent_ombAB_100H_d};

%% ------------------------------------------------------------------------
% ANALYSIS

% C = normxcorr2(wt_pmadAB_80H,wt_brkAB_80H);
% r = corr2(double(wt_pmadAB_80H),double(wt_brkAB_80H));

% input = wt_80H;
% output = zeros(length(input));
% for i=1:length(input)
%     for j=i:length(input)
%         output(i,j) = corr2_disc(input{i},input{j});
%     end
% end

input2 = wt_90H;
output2 = zeros(length(input2));
for i=1:length(input2)
    for j=i:length(input2)
        output2(i,j) = corr2_disc(input2{i},input2{j});
    end
end

% input3 = wt_100H;
% output3 = zeros(length(input3));
% for i=1:length(input3)
%     for j=i:length(input3)
%         output3(i,j) = corr2_disc(input3{i},input3{j});
%     end
% end

% input4 = pent_80H;
% output4 = zeros(length(input4));
% for i=1:length(input4)
%     for j=i:length(input4)
%         output4(i,j) = corr2_disc(input4{i},input4{j});
%     end
% end

% input5 = pent_90H;
% output5 = zeros(length(input5));
% for i=1:length(input5)
%     for j=i:length(input5)
%         output5(i,j) = corr2_disc(input5{i},input5{j});
%     end
% end

% input6 = pent_100H;
% output6 = zeros(length(input6));
% for i=1:length(input6)
%     for j=i:length(input6)
%         output6(i,j) = corr2_disc(input6{i},input6{j});
%     end
% end

% a = conv2(wt_pmadAB_80H_d,wt_brkAB_80H_d)