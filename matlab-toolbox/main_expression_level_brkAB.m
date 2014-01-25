close all; clear all; clc;

addpath('lib');
addpath('nuclei_detector');
addpath('structure');

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
experimentRootDirectories = {'20100716_pmadAB_brkAB_wg-ptcAB_78-79H/'
                             '20100716_pmadAB_brkAB_wg-ptcAB_90,5-91,5H/'
                             '20100716_pmadAB_brkAB_wg-ptcAB_99-100H/'
                             '20100716_pmadAB_brkAB_wg-ptcAB_110,5-111,5H/'
                             '20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_78-79H/'
                             '20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_90-91H/'
                             '20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_99-100H/'
                             '20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_110,5-111,5H/'}; 
                              
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

wt_brkAB_80H = wt_brkAB.getExperimentsByAge([78,79]); wt_brkAB_80H.displayNumExperiments();
wt_brkAB_90H = wt_brkAB.getExperimentsByAge([90.5,91.5]); wt_brkAB_90H.displayNumExperiments();
wt_brkAB_100H = wt_brkAB.getExperimentsByAge([99,100]); wt_brkAB_100H.displayNumExperiments();
wt_brkAB_110H = wt_brkAB.getExperimentsByAge([110.5,111.5]); wt_brkAB_110H.displayNumExperiments();

pent_brkAB_80H = pent_brkAB.getExperimentsByAge([78,79]); pent_brkAB_80H.displayNumExperiments();
pent_brkAB_90H = pent_brkAB.getExperimentsByAge([90,91]); pent_brkAB_90H.displayNumExperiments();
pent_brkAB_100H = pent_brkAB.getExperimentsByAge([99,100]); pent_brkAB_100H.displayNumExperiments();
pent_brkAB_110H = pent_brkAB.getExperimentsByAge([110.5,111.5]); pent_brkAB_110H.displayNumExperiments();

%% ------------------------------------------------------------------------
% EXPRESSION LEVEL IN RAW WING POUCHES

% Create lists of expression maps
wt_brkAB_80H_raw = wt_brkAB_80H.get(:).getExpressionMaps('brkAB',[],{'raw' '-mask'});
wt_brkAB_90H_raw = wt_brkAB_90H.get(:).getExpressionMaps('brkAB',[],{'raw' '-mask'});
wt_brkAB_100H_raw = wt_brkAB_100H.get(:).getExpressionMaps('brkAB',[],{'raw' '-mask'});
wt_brkAB_110H_raw = wt_brkAB_110H.get(:).getExpressionMaps('brkAB',[],{'raw' '-mask'});
pent_brkAB_80H_raw = pent_brkAB_80H.get(:).getExpressionMaps('brkAB',[],{'raw' '-mask'});
pent_brkAB_90H_raw = pent_brkAB_90H.get(:).getExpressionMaps('brkAB',[],{'raw' '-mask'});
pent_brkAB_100H_raw = pent_brkAB_100H.get(:).getExpressionMaps('brkAB',[],{'raw' '-mask'});
pent_brkAB_110H_raw = pent_brkAB_110H.get(:).getExpressionMaps('brkAB',[],{'raw' '-mask'});

wt_brkAB_80H_mask = wt_brkAB_80H.get(:).getExpressionMaps('brkAB',[],{'raw','mask'});
wt_brkAB_90H_mask = wt_brkAB_90H.get(:).getExpressionMaps('brkAB',[],{'raw' 'mask'});
wt_brkAB_100H_mask = wt_brkAB_100H.get(:).getExpressionMaps('brkAB',[],{'raw' 'mask'});
wt_brkAB_110H_mask = wt_brkAB_110H.get(:).getExpressionMaps('brkAB',[],{'raw' 'mask'});
pent_brkAB_80H_mask = pent_brkAB_80H.get(:).getExpressionMaps('brkAB',[],{'raw','mask'});
pent_brkAB_90H_mask = pent_brkAB_90H.get(:).getExpressionMaps('brkAB',[],{'raw' 'mask'});
pent_brkAB_100H_mask = pent_brkAB_100H.get(:).getExpressionMaps('brkAB',[],{'raw' 'mask'});
pent_brkAB_110H_mask = pent_brkAB_110H.get(:).getExpressionMaps('brkAB',[],{'raw' 'mask'});

wt_brkAB_80H_exp_levels = wt_brkAB_80H_raw.get(:).sumExpression(wt_brkAB_80H_mask.get(:));
wt_brkAB_90H_exp_levels = wt_brkAB_90H_raw.get(:).sumExpression(wt_brkAB_90H_mask.get(:));
wt_brkAB_100H_exp_levels = wt_brkAB_100H_raw.get(:).sumExpression(wt_brkAB_100H_mask.get(:));
wt_brkAB_110H_exp_levels = wt_brkAB_110H_raw.get(:).sumExpression(wt_brkAB_110H_mask.get(:));
pent_brkAB_80H_exp_levels = pent_brkAB_80H_raw.get(:).sumExpression(pent_brkAB_80H_mask.get(:));
pent_brkAB_90H_exp_levels = pent_brkAB_90H_raw.get(:).sumExpression(pent_brkAB_90H_mask.get(:));
pent_brkAB_100H_exp_levels = pent_brkAB_100H_raw.get(:).sumExpression(pent_brkAB_100H_mask.get(:));
pent_brkAB_110H_exp_levels = pent_brkAB_110H_raw.get(:).sumExpression(pent_brkAB_110H_mask.get(:));

% By age
a = figure;
h(1) = subplot(1,2,1);
G = boxplot_groups({wt_brkAB_80H_exp_levels,wt_brkAB_90H_exp_levels,wt_brkAB_100H_exp_levels,wt_brkAB_110H_exp_levels});
labels = {'wt_80H','wt_90H','wt_100H','wt_110H'};
boxplot([wt_brkAB_80H_exp_levels;wt_brkAB_90H_exp_levels;wt_brkAB_100H_exp_levels;wt_brkAB_110H_exp_levels],G,'labels',labels);
% title('Total expression level measured in the wing pouch')
xlabel('Experiment');
ylabel('Expression level (a.u.)');

h(2) = subplot(1,2,2);
G = boxplot_groups({pent_brkAB_80H_exp_levels,pent_brkAB_90H_exp_levels,pent_brkAB_100H_exp_levels,pent_brkAB_110H_exp_levels});
labels = {'pent_80H','pent_90H','pent_100H','pent_110H'};
boxplot([pent_brkAB_80H_exp_levels;pent_brkAB_90H_exp_levels;pent_brkAB_100H_exp_levels;pent_brkAB_110H_exp_levels],G,'labels',labels);
% title('Total expression level measured in the wing pouch')
xlabel('Experiment');
ylabel('Expression level (a.u.)');

linkaxes(h,'y');
minE = min([wt_brkAB_80H_exp_levels;wt_brkAB_90H_exp_levels;wt_brkAB_100H_exp_levels;wt_brkAB_110H_exp_levels;pent_brkAB_80H_exp_levels;pent_brkAB_90H_exp_levels;pent_brkAB_100H_exp_levels;pent_brkAB_110H_exp_levels]);
maxE = max([wt_brkAB_80H_exp_levels;wt_brkAB_90H_exp_levels;wt_brkAB_100H_exp_levels;wt_brkAB_110H_exp_levels;pent_brkAB_80H_exp_levels;pent_brkAB_90H_exp_levels;pent_brkAB_100H_exp_levels;pent_brkAB_110H_exp_levels]);
set(h(1),'Ylim',[minE maxE]);
set(h(1),'Ylim',[4041966 74779356]);
set(a, 'OuterPosition',[0 0 800 500]);

[p h]=ranksum(wt_brkAB_80H_exp_levels,pent_brkAB_80H_exp_levels);
disp(['[wt/pent] brkAB 80H: p=' num2str(p) ', h=' num2str(h)]);
[p h]=ranksum(wt_brkAB_90H_exp_levels,pent_brkAB_90H_exp_levels);
disp(['[wt/pent] brkAB 90H: p=' num2str(p) ', h=' num2str(h)]);
[p h]=ranksum(wt_brkAB_100H_exp_levels,pent_brkAB_100H_exp_levels);
disp(['[wt/pent] brkAB 100H: p=' num2str(p) ', h=' num2str(h)]);
[p h]=ranksum(wt_brkAB_110H_exp_levels,pent_brkAB_110H_exp_levels);
disp(['[wt/pent] brkAB 110H: p=' num2str(p) ', h=' num2str(h)]);


% Here I divide the expression level by the area of the pouch
[wt80H_area unit] = wt_brkAB_80H.get(:).getStructureProperty('pouch.area');
[wt90H_area unit] = wt_brkAB_90H.get(:).getStructureProperty('pouch.area');
[wt100H_area unit] = wt_brkAB_100H.get(:).getStructureProperty('pouch.area');
[wt110H_area unit] = wt_brkAB_110H.get(:).getStructureProperty('pouch.area');
[pent80H_area unit] = pent_brkAB_80H.get(:).getStructureProperty('pouch.area');
[pent90H_area unit] = pent_brkAB_90H.get(:).getStructureProperty('pouch.area');
[pent100H_area unit] = pent_brkAB_100H.get(:).getStructureProperty('pouch.area');
[pent110H_area unit] = pent_brkAB_110H.get(:).getStructureProperty('pouch.area');

% Remove damaged wing not taken into account for expression (but structure ok)
pent80H_area(2) = [];

wt_brkAB_80H_exp_levels = wt_brkAB_80H_exp_levels ./ wt80H_area';
wt_brkAB_90H_exp_levels = wt_brkAB_90H_exp_levels ./ wt90H_area';
wt_brkAB_100H_exp_levels = wt_brkAB_100H_exp_levels ./ wt100H_area';
wt_brkAB_110H_exp_levels = wt_brkAB_110H_exp_levels ./ wt110H_area';
pent_brkAB_80H_exp_levels = pent_brkAB_80H_exp_levels ./ pent80H_area';
pent_brkAB_90H_exp_levels = pent_brkAB_90H_exp_levels ./ pent90H_area';
pent_brkAB_100H_exp_levels = pent_brkAB_100H_exp_levels ./ pent100H_area';
pent_brkAB_110H_exp_levels = pent_brkAB_110H_exp_levels ./ pent110H_area';

% By age
a = figure;
hh(1) = subplot(1,2,1);
G = boxplot_groups({wt_brkAB_80H_exp_levels,wt_brkAB_90H_exp_levels,wt_brkAB_100H_exp_levels,wt_brkAB_110H_exp_levels});
labels = {'wt_80H','wt_90H','wt_100H','wt_110H'};
boxplot([wt_brkAB_80H_exp_levels;wt_brkAB_90H_exp_levels;wt_brkAB_100H_exp_levels;wt_brkAB_110H_exp_levels],G,'labels',labels);
% title('Total expression level measured in the wing pouch')
xlabel('Experiment');
ylabel('Expression level (a.u.) / pouch area (um^2)');

hh(2) = subplot(1,2,2);
G = boxplot_groups({pent_brkAB_80H_exp_levels,pent_brkAB_90H_exp_levels,pent_brkAB_100H_exp_levels,pent_brkAB_110H_exp_levels});
labels = {'pent_80H','pent_90H','pent_100H','pent_110H'};
boxplot([pent_brkAB_80H_exp_levels;pent_brkAB_90H_exp_levels;pent_brkAB_100H_exp_levels;pent_brkAB_110H_exp_levels],G,'labels',labels);
% title('Total expression level measured in the wing pouch')
xlabel('Experiment');
ylabel('Expression level (a.u.) / pouch area (um^2)');

linkaxes(hh,'y');
minE = min([wt_brkAB_80H_exp_levels;wt_brkAB_90H_exp_levels;wt_brkAB_100H_exp_levels;wt_brkAB_110H_exp_levels;pent_brkAB_80H_exp_levels;pent_brkAB_90H_exp_levels;pent_brkAB_100H_exp_levels;pent_brkAB_110H_exp_levels]);
maxE = max([wt_brkAB_80H_exp_levels;wt_brkAB_90H_exp_levels;wt_brkAB_100H_exp_levels;wt_brkAB_110H_exp_levels;pent_brkAB_80H_exp_levels;pent_brkAB_90H_exp_levels;pent_brkAB_100H_exp_levels;pent_brkAB_110H_exp_levels]);
% set(hh(1),'Ylim',[minE maxE]);
set(hh(1),'Ylim',[508.8245 2.4977e+03]);
set(a, 'OuterPosition',[0 0 800 500]);

[p h]=ranksum(wt_brkAB_80H_exp_levels,wt_brkAB_90H_exp_levels);
disp(['[wt/wt] brkAB 80H and 90H: p=' num2str(p) ', h=' num2str(h)]);
[p h]=ranksum(wt_brkAB_90H_exp_levels,wt_brkAB_100H_exp_levels);
disp(['[wt/wt] brkAB 90H and 100H: p=' num2str(p) ', h=' num2str(h)]);
[p h]=ranksum(wt_brkAB_100H_exp_levels,wt_brkAB_110H_exp_levels);
disp(['[wt/wt] brkAB 100H and 110H: p=' num2str(p) ', h=' num2str(h)]);

[p h]=ranksum(pent_brkAB_80H_exp_levels,pent_brkAB_90H_exp_levels);
disp(['[pent/pent] brkAB 80H and 90H: p=' num2str(p) ', h=' num2str(h)]);
[p h]=ranksum(pent_brkAB_90H_exp_levels,pent_brkAB_100H_exp_levels);
disp(['[pent/pent] brkAB 90H and 100H: p=' num2str(p) ', h=' num2str(h)]);
[p h]=ranksum(pent_brkAB_100H_exp_levels,pent_brkAB_110H_exp_levels);
disp(['[pent/pent] brkAB 100H and 110H: p=' num2str(p) ', h=' num2str(h)]);

[p h]=ranksum(wt_brkAB_80H_exp_levels,pent_brkAB_80H_exp_levels);
disp(['[wt/pent] brkAB 80H: p=' num2str(p) ', h=' num2str(h)]);
[p h]=ranksum(wt_brkAB_90H_exp_levels,pent_brkAB_90H_exp_levels);
disp(['[wt/pent] brkAB 90H: p=' num2str(p) ', h=' num2str(h)]);
[p h]=ranksum(wt_brkAB_100H_exp_levels,pent_brkAB_100H_exp_levels);
disp(['[wt/pent] brkAB 100H: p=' num2str(p) ', h=' num2str(h)]);
[p h]=ranksum(wt_brkAB_110H_exp_levels,pent_brkAB_110H_exp_levels);
disp(['[wt/pent] brkAB 110H: p=' num2str(p) ', h=' num2str(h)]);