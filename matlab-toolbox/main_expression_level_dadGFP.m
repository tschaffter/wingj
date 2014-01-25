close all; clear all; clc;

addpath('lib');
addpath('structure');
addpath('expression');
addpath('nuclei_detector');

%% ------------------------------------------------------------------------
% SETTINGS

settings = Settings.getInstance;

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
% EXPRESSION LEVEL IN RAW WING POUCHES

% Create lists of expression maps
wt_dadGFP_80H_raw = wt_dadGFP_80H.get(:).getExpressionMaps('dadGFP',[],{'raw' '-mask'});
wt_dadGFP_90H_raw = wt_dadGFP_90H.get(:).getExpressionMaps('dadGFP',[],{'raw' '-mask'});
wt_dadGFP_100H_raw = wt_dadGFP_100H.get(:).getExpressionMaps('dadGFP',[],{'raw' '-mask'});
wt_dadGFP_110H_raw = wt_dadGFP_110H.get(:).getExpressionMaps('dadGFP',[],{'raw' '-mask'});
pent_dadGFP_80H_raw = pent_dadGFP_80H.get(:).getExpressionMaps('dadGFP',[],{'raw' '-mask'});
pent_dadGFP_90H_raw = pent_dadGFP_90H.get(:).getExpressionMaps('dadGFP',[],{'raw' '-mask'});
pent_dadGFP_100H_raw = pent_dadGFP_100H.get(:).getExpressionMaps('dadGFP',[],{'raw' '-mask'});
pent_dadGFP_110H_raw = pent_dadGFP_110H.get(:).getExpressionMaps('dadGFP',[],{'raw' '-mask'});

figure; wt_dadGFP_110H_raw.get(:).showExpressionMaps();
figure; pent_dadGFP_110H_raw.get(:).showExpressionMaps();

wt_dadGFP_80H_mask = wt_dadGFP_80H.get(:).getExpressionMaps('dadGFP',[],{'raw','mask'});
wt_dadGFP_90H_mask = wt_dadGFP_90H.get(:).getExpressionMaps('dadGFP',[],{'raw' 'mask'});
wt_dadGFP_100H_mask = wt_dadGFP_100H.get(:).getExpressionMaps('dadGFP',[],{'raw' 'mask'});
wt_dadGFP_110H_mask = wt_dadGFP_110H.get(:).getExpressionMaps('dadGFP',[],{'raw' 'mask'});
pent_dadGFP_80H_mask = pent_dadGFP_80H.get(:).getExpressionMaps('dadGFP',[],{'raw','mask'});
pent_dadGFP_90H_mask = pent_dadGFP_90H.get(:).getExpressionMaps('dadGFP',[],{'raw' 'mask'});
pent_dadGFP_100H_mask = pent_dadGFP_100H.get(:).getExpressionMaps('dadGFP',[],{'raw' 'mask'});
pent_dadGFP_110H_mask = pent_dadGFP_110H.get(:).getExpressionMaps('dadGFP',[],{'raw' 'mask'});

wt_dadGFP_80H_exp_levels = wt_dadGFP_80H_raw.get(:).sumExpression(wt_dadGFP_80H_mask.get(:));
wt_dadGFP_90H_exp_levels = wt_dadGFP_90H_raw.get(:).sumExpression(wt_dadGFP_90H_mask.get(:));
wt_dadGFP_100H_exp_levels = wt_dadGFP_100H_raw.get(:).sumExpression(wt_dadGFP_100H_mask.get(:));
wt_dadGFP_110H_exp_levels = wt_dadGFP_110H_raw.get(:).sumExpression(wt_dadGFP_110H_mask.get(:));
pent_dadGFP_80H_exp_levels = pent_dadGFP_80H_raw.get(:).sumExpression(pent_dadGFP_80H_mask.get(:));
pent_dadGFP_90H_exp_levels = pent_dadGFP_90H_raw.get(:).sumExpression(pent_dadGFP_90H_mask.get(:));
pent_dadGFP_100H_exp_levels = pent_dadGFP_100H_raw.get(:).sumExpression(pent_dadGFP_100H_mask.get(:));
pent_dadGFP_110H_exp_levels = pent_dadGFP_110H_raw.get(:).sumExpression(pent_dadGFP_110H_mask.get(:));

% By age
a = figure;
h(1) = subplot(1,2,1);
G = boxplot_groups({wt_dadGFP_80H_exp_levels,wt_dadGFP_90H_exp_levels,wt_dadGFP_100H_exp_levels,wt_dadGFP_110H_exp_levels});
labels = {'wt_80H','wt_90H','wt_100H','wt_110H'};
boxplot([wt_dadGFP_80H_exp_levels;wt_dadGFP_90H_exp_levels;wt_dadGFP_100H_exp_levels;wt_dadGFP_110H_exp_levels],G,'labels',labels);
% title('Total expression level measured in the wing pouch')
xlabel('Experiment');
ylabel('Expression level (a.u.)');

h(2) = subplot(1,2,2);
G = boxplot_groups({pent_dadGFP_80H_exp_levels,pent_dadGFP_90H_exp_levels,pent_dadGFP_100H_exp_levels,pent_dadGFP_110H_exp_levels});
labels = {'pent_80H','pent_90H','pent_100H','pent_110H'};
boxplot([pent_dadGFP_80H_exp_levels;pent_dadGFP_90H_exp_levels;pent_dadGFP_100H_exp_levels;pent_dadGFP_110H_exp_levels],G,'labels',labels);
% title('Total expression level measured in the wing pouch')
xlabel('Experiment');
ylabel('Expression level (a.u.)');

linkaxes(h,'y');
minE = min([wt_dadGFP_80H_exp_levels;wt_dadGFP_90H_exp_levels;wt_dadGFP_100H_exp_levels;wt_dadGFP_110H_exp_levels;pent_dadGFP_80H_exp_levels;pent_dadGFP_90H_exp_levels;pent_dadGFP_100H_exp_levels;pent_dadGFP_110H_exp_levels]);
maxE = max([wt_dadGFP_80H_exp_levels;wt_dadGFP_90H_exp_levels;wt_dadGFP_100H_exp_levels;wt_dadGFP_110H_exp_levels;pent_dadGFP_80H_exp_levels;pent_dadGFP_90H_exp_levels;pent_dadGFP_100H_exp_levels;pent_dadGFP_110H_exp_levels]);
set(h(1),'Ylim',[minE maxE]);
set(h(1),'Ylim',[4041966 74779356]);
set(a, 'OuterPosition',[0 0 800 500]);

[p h]=ranksum(wt_dadGFP_80H_exp_levels,wt_dadGFP_90H_exp_levels);
disp(['[wt/wt] dadGFP 80H and 90H: p=' num2str(p) ', h=' num2str(h)]);
[p h]=ranksum(wt_dadGFP_90H_exp_levels,wt_dadGFP_100H_exp_levels);
disp(['[wt/wt] dadGFP 90H and 100H: p=' num2str(p) ', h=' num2str(h)]);
[p h]=ranksum(wt_dadGFP_100H_exp_levels,wt_dadGFP_110H_exp_levels);
disp(['[wt/wt] dadGFP 100H and 11H: p=' num2str(p) ', h=' num2str(h)]);

[p h]=ranksum(pent_dadGFP_80H_exp_levels,pent_dadGFP_90H_exp_levels);
disp(['[pent/pent] dadGFP 80H and 90H: p=' num2str(p) ', h=' num2str(h)]);
[p h]=ranksum(pent_dadGFP_90H_exp_levels,pent_dadGFP_100H_exp_levels);
disp(['[pent/pent] dadGFP 90H and 100H: p=' num2str(p) ', h=' num2str(h)]);
[p h]=ranksum(pent_dadGFP_100H_exp_levels,pent_dadGFP_110H_exp_levels);
disp(['[pent/pent] dadGFP 100H and 11H: p=' num2str(p) ', h=' num2str(h)]);

[p h]=ranksum(wt_dadGFP_80H_exp_levels,pent_dadGFP_80H_exp_levels);
disp(['[wt/pent] dadGFP 80H: p=' num2str(p) ', h=' num2str(h)]);
[p h]=ranksum(wt_dadGFP_90H_exp_levels,pent_dadGFP_90H_exp_levels);
disp(['[wt/pent] dadGFP 90H: p=' num2str(p) ', h=' num2str(h)]);
[p h]=ranksum(wt_dadGFP_100H_exp_levels,pent_dadGFP_100H_exp_levels);
disp(['[wt/pent] dadGFP 100H: p=' num2str(p) ', h=' num2str(h)]);
[p h]=ranksum(wt_dadGFP_110H_exp_levels,pent_dadGFP_110H_exp_levels);
disp(['[wt/pent] dadGFP 110H: p=' num2str(p) ', h=' num2str(h)]);


% Here I divide the expression level by the area of the pouch
[wt80H_area unit] = wt_dadGFP_80H.get(:).getStructureProperty('pouch.area');
[wt90H_area unit] = wt_dadGFP_90H.get(:).getStructureProperty('pouch.area');
[wt100H_area unit] = wt_dadGFP_100H.get(:).getStructureProperty('pouch.area');
[wt110H_area unit] = wt_dadGFP_110H.get(:).getStructureProperty('pouch.area');
[pent80H_area unit] = pent_dadGFP_80H.get(:).getStructureProperty('pouch.area');
[pent90H_area unit] = pent_dadGFP_90H.get(:).getStructureProperty('pouch.area');
[pent100H_area unit] = pent_dadGFP_100H.get(:).getStructureProperty('pouch.area');
[pent110H_area unit] = pent_dadGFP_110H.get(:).getStructureProperty('pouch.area');

wt_dadGFP_80H_exp_levels = wt_dadGFP_80H_exp_levels ./ wt80H_area';
wt_dadGFP_90H_exp_levels = wt_dadGFP_90H_exp_levels ./ wt90H_area';
wt_dadGFP_100H_exp_levels = wt_dadGFP_100H_exp_levels ./ wt100H_area';
wt_dadGFP_110H_exp_levels = wt_dadGFP_110H_exp_levels ./ wt110H_area';
pent_dadGFP_80H_exp_levels = pent_dadGFP_80H_exp_levels ./ pent80H_area';
pent_dadGFP_90H_exp_levels = pent_dadGFP_90H_exp_levels ./ pent90H_area';
pent_dadGFP_100H_exp_levels = pent_dadGFP_100H_exp_levels ./ pent100H_area';
pent_dadGFP_110H_exp_levels = pent_dadGFP_110H_exp_levels ./ pent110H_area';

% By age
a = figure;
hh(1) = subplot(1,2,1);
G = boxplot_groups({wt_dadGFP_80H_exp_levels,wt_dadGFP_90H_exp_levels,wt_dadGFP_100H_exp_levels,wt_dadGFP_110H_exp_levels});
labels = {'wt_80H','wt_90H','wt_100H','wt_110H'};
boxplot([wt_dadGFP_80H_exp_levels;wt_dadGFP_90H_exp_levels;wt_dadGFP_100H_exp_levels;wt_dadGFP_110H_exp_levels],G,'labels',labels);
% title('Total expression level measured in the wing pouch')
xlabel('Experiment');
ylabel('Expression level (a.u.) / pouch area (um^2)');

hh(2) = subplot(1,2,2);
G = boxplot_groups({pent_dadGFP_80H_exp_levels,pent_dadGFP_90H_exp_levels,pent_dadGFP_100H_exp_levels,pent_dadGFP_110H_exp_levels});
labels = {'pent_80H','pent_90H','pent_100H','pent_110H'};
boxplot([pent_dadGFP_80H_exp_levels;pent_dadGFP_90H_exp_levels;pent_dadGFP_100H_exp_levels;pent_dadGFP_110H_exp_levels],G,'labels',labels);
% title('Total expression level measured in the wing pouch')
xlabel('Experiment');
ylabel('Expression level (a.u.) / pouch area (um^2)');

linkaxes(hh,'y');
minE = min([wt_dadGFP_80H_exp_levels;wt_dadGFP_90H_exp_levels;wt_dadGFP_100H_exp_levels;wt_dadGFP_110H_exp_levels;pent_dadGFP_80H_exp_levels;pent_dadGFP_90H_exp_levels;pent_dadGFP_100H_exp_levels;pent_dadGFP_110H_exp_levels]);
maxE = max([wt_dadGFP_80H_exp_levels;wt_dadGFP_90H_exp_levels;wt_dadGFP_100H_exp_levels;wt_dadGFP_110H_exp_levels;pent_dadGFP_80H_exp_levels;pent_dadGFP_90H_exp_levels;pent_dadGFP_100H_exp_levels;pent_dadGFP_110H_exp_levels]);
% set(hh(1),'Ylim',[minE maxE]);
set(hh(1),'Ylim',[508.8245 2.4977e+03]);
set(a, 'OuterPosition',[0 0 800 500]);

[p h]=ranksum(wt_dadGFP_80H_exp_levels,wt_dadGFP_90H_exp_levels);
disp(['[wt/wt] dadGFP 80H and 90H: p=' num2str(p) ', h=' num2str(h)]);
[p h]=ranksum(wt_dadGFP_90H_exp_levels,wt_dadGFP_100H_exp_levels);
disp(['[wt/wt] dadGFP 90H and 100H: p=' num2str(p) ', h=' num2str(h)]);
[p h]=ranksum(wt_dadGFP_100H_exp_levels,wt_dadGFP_110H_exp_levels);
disp(['[wt/wt] dadGFP 100H and 110H: p=' num2str(p) ', h=' num2str(h)]);

[p h]=ranksum(pent_dadGFP_80H_exp_levels,pent_dadGFP_90H_exp_levels);
disp(['[pent/pent] dadGFP 80H and 90H: p=' num2str(p) ', h=' num2str(h)]);
[p h]=ranksum(pent_dadGFP_90H_exp_levels,pent_dadGFP_100H_exp_levels);
disp(['[pent/pent] dadGFP 90H and 100H: p=' num2str(p) ', h=' num2str(h)]);
[p h]=ranksum(pent_dadGFP_100H_exp_levels,pent_dadGFP_110H_exp_levels);
disp(['[pent/pent] dadGFP 100H and 110H: p=' num2str(p) ', h=' num2str(h)]);

[p h]=ranksum(wt_dadGFP_80H_exp_levels,pent_dadGFP_80H_exp_levels);
disp(['[wt/pent] dadGFP 80H: p=' num2str(p) ', h=' num2str(h)]);
[p h]=ranksum(wt_dadGFP_90H_exp_levels,pent_dadGFP_90H_exp_levels);
disp(['[wt/pent] dadGFP 90H: p=' num2str(p) ', h=' num2str(h)]);
[p h]=ranksum(wt_dadGFP_100H_exp_levels,pent_dadGFP_100H_exp_levels);
disp(['[wt/pent] dadGFP 100H: p=' num2str(p) ', h=' num2str(h)]);
[p h]=ranksum(wt_dadGFP_110H_exp_levels,pent_dadGFP_110H_exp_levels);
disp(['[wt/pent] dadGFP 110H: p=' num2str(p) ', h=' num2str(h)]);