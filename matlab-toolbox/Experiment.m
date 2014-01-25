% Copyright (c) 2010-2013 Thomas Schaffter, Ricard Delgado-Gonzalo
%
% We release this software open source under a Creative Commons Attribution
% -NonCommercial 3.0 Unported License. Please cite the papers listed on 
% http://lis.epfl.ch/wingj when using WingJ in your publication.
%
% For commercial use, please contact Thomas Schaffter 
% (thomas.schaff...@gmail.com).
%
% A brief description of the license is available at 
% http://creativecommons.org/licenses/by-nc/3.0/ and the full license at 
% http://creativecommons.org/licenses/by-nc/3.0/legalcode.
%
% The above copyright notice and this permission notice shall be included 
% in all copies or substantial portions of the Software.
%
% THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS 
% OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
% MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
% IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY 
% CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
% OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR 
% THE USE OR OTHER DEALINGS IN THE SOFTWARE.
%
% This class represent a single experiment (e.g. a single wing).
%
% This high-level class is used to open morphological (structure) and
% expression dataset. This class can also be used to run operation such as
% nuclei detection (if nuclear staining is available), boxplot structure
% properties, etc. This class acts like a hub for analysing the available
% datasets.
%
% Please refer to the Supplementary Material for more information about the
% datasets exported using WingJ.
%
% TODO: Move the methods to boxplot structure datasets to the 'structure'
% package.
%
% Author: Thomas Schaffter (thomas.schaff...@gmail.com)
% Version: September 10, 2012
classdef Experiment < handle
    
    properties(Constant)
        % Structure properties: one subplot per batch of experiments.
        PER_REPOSITORY = 1;
        % Structure properties: one subplot per properties.
        PER_STRUCTURE_PROPERTY = 2;
    end
    
    properties(GetAccess = 'public', SetAccess = 'private')
        
        % Directory containing the experiment
        directory = '';
        % Name of the experiment
        name = '';
        % Gene/protein/marker/channel names
        channelNames = [];
        % Mutant names (if any)
        mutantNames = [];
        % Age of the structure in hour (H)
        age = [];
        % Date of the experiment
        date = '';
        
        % Image stacks (images{1} for ch00, images{2} for ch01, etc.)
        images = [];
        
        % Structural properties
        structureProperties = [];
        % Structure binary mask
        structureBinaryMask = [];
        % Structure preview with compartments labeled
        structurePreview = [];
        
        % 1D expression profiles (ExpressionProfileList)
        expressionProfiles = [];
        % 2D expression maps (ExpressionMapList)
        expressionMaps = [];
        
        % Number of nuclei (in EXPERIMENT/WingJ/Matlab/NucleiDetector/)
        numNuclei = [];
        
        % WingJ log
        log = [];
    end
    
    % =====================================================================
	% PRIVATE METHODS
    
    methods (Access = private)
        
        % Initialize
        function initialize(obj, directory)
            
            settings = WJSettings.getInstance;
            
            obj.setDirectory(directory);
            obj.setDate(obj.name);
            obj.setChannelNames(obj.name);
            obj.setAge(obj.name);
            obj.images = cell(1,settings.getNumChannels());
        end
        
        % -----------------------------------------------------------------
        
        % Return the index associated to the given channel name.
        % Otherwise return [].
        function channel = getChannelIndex(obj, name)
            
            channel = find(strcmp(name,obj.channelNames));
            if ~isempty(channel)
                channel = channel-1 ; % remove 1 to start at channel 0
            end
        end
        
        % -----------------------------------------------------------------
        
        % Open images associated to the given channel index.
        function openImageSequence(obj, channel)
            
            settings = WJSettings.getInstance;
            
            numChannels = settings.getNumChannels();
            if channel > numChannels-1
                error(['ERROR: channel must be in [0,' num2str(numChannels-1) '].']);
            end
            
            fprintf('Opening %s %s images\n', obj.name, obj.channelNames{channel+1});
            imagesDirectory = [obj.directory settings.imagesDirectoryName filesep];
            obj.images{channel+1} = open_image_sequence(imagesDirectory,...
                channel,settings.slicePrefix,settings.channelPrefix);
        end
    end
    
    % =====================================================================
	% PUBLIC METHODS
    
    methods
        
        % Constructor
        function obj = Experiment(directory)
            if nargin > 0
                obj.initialize(directory);
            end
        end
        
        % -----------------------------------------------------------------
        
        % Set the name of the experiment from the directory path
        function setDirectory(obj, directory)
            
            obj.directory = directory;
            % split the path
            tokens = regexp(obj.directory,filesep,'split');
            % remove empty entries where were the delimiters
            tokens(cellfun(@isempty,tokens)) = [];
            % the name of the folder containing the experiment is the name
            token = tokens(end);
            obj.name = token{1};
        end
        
        % -----------------------------------------------------------------
        
        % Set experiment index
        function setIndex(obj, name)
            
            % split the name with '_'
            tokens = regexp(name,'_','split');
            c = tokens(1,end);
            obj.index = str2num(['uint16(' c{1} ')']); %#ok<ST2NM>
        end
        
        % -----------------------------------------------------------------
        
        % Set the date of the experiment (e.g. '20110921')
        function setDate(obj, name)
                       
            % split the name with '_'
            tokens = regexp(name,'_','split');
            obj.date = tokens{1};
        end
        
        % -----------------------------------------------------------------
        
        % Set the names of the gene/protein/marker used for each channel
        function setChannelNames(obj, name)
            
            settings = WJSettings.getInstance;
            
            % split the name with '_'
            tokens = regexp(name,'_','split');
            for firstChannelIndex=2:length(tokens)
                token = tokens{firstChannelIndex};
                % is it a mutant tag ?
                if strcmp(token(end),'-')
                    obj.mutantNames{end+1} = token(1:end-1); % removes the '-' from the tag
                    continue
                else
                    numChannels = settings.getNumChannels();
                    try
                        obj.channelNames = tokens(1,firstChannelIndex:firstChannelIndex+numChannels-1);
                    catch err
                        if strcmp(err.identifier,'MATLAB:badsubscript')
                            msg='The experiment name doesn''t respect the format of the gene names. Please refer to the documentation.';
                            error('Experiment:setChannelNames:baddformat',msg);
                        else
                            rethrow(err);
                        end
                    end
                    break
                end
            end
        end
        
        % -----------------------------------------------------------------
        
        % Set the age of the structure (e.g. wing) from experiment name
        function setAge(obj, name)
            
            % split the name with '_'
            tokens = regexp(name,'_','split');
            ageNameIndex = 1+length(obj.mutantNames)+length(obj.channelNames)+1;
            ageStr = tokens(1, ageNameIndex);
            ageStr = ageStr{1};
            % replace commas (,) by period (.)
            ageStr = regexprep(ageStr, ',', '.');
            % remove the 'H'
            ageStr(end) = [];
            % converts cell array from regexp to vector
            obj.age = cellfun(@str2num,regexp(ageStr,'-','split'));
        end
        
        % -----------------------------------------------------------------
        
        % Open the structure properties from a Structure dataset exported
        % using WingJ.
        function openStructureProperties(obj)
            
            settings = WJSettings.getInstance;
            
            % Opens the structure properties of all given experiments
            N = length(obj);
            for i=1:N
                exp = obj(i);
                % Returns if the structure properties have already been
                % loaded.
                if ~isempty(exp.structureProperties)
                    continue
                end
                
                % Gets all filenames sharing the same correct suffix
                folder = [exp.directory settings.wingjDirectoryName filesep];
                regex = [folder '*' settings.structurePropertiesSuffix];
                filenames = dir(regex);

                if isempty(filenames)
                    disp(['Couldn''t find a match for ' regex '.']);
                else
                    % Only uses the first one, display a warning if more.
                    if (length(filenames) > 1)
                        disp(['WARNING: There are more than one filename match ' regex '.']);
                    end
                    exp.structureProperties = Structure([folder filenames(1).name]);
                end   
            end
        end
        
        % -----------------------------------------------------------------
        
        % Open the binary mask of the structure
        function openStructureBinaryMask(obj)
            
            settings = WJSettings.getInstance;
            
            % Gets all filenames sharing the same correct suffix
            folder = [obj.directory settings.wingjDirectoryName filesep];
            regex = [folder '*' settings.structureBinaryMaskSuffix];
            filenames = dir(regex);
            
            if isempty(filenames)
                error(['Couldn''t find a match for ' regex '.']);
            else
                % Only uses the first one, display a warning if more.
                if (length(filenames) > 1)
                    disp(['WARNING: There are more than one filename match ' regex '.']);
                end
                obj.structureBinaryMask = imread([folder filenames(1).name]);
            end
        end
        
        % -----------------------------------------------------------------
        
        % Open log file (from WingJ)
        function openLog(obj)
            
            settings = WJSettings.getInstance;
            
            % Gets all filenames sharing the same correct suffix
            folder = [obj.directory settings.wingjDirectoryName filesep];
            regex = [folder '*' settings.logSuffix];
            filenames = dir(regex);
            
            if isempty(filenames)
                error(['Couldn''t find a match for ' regex '.']);
            else
                % Only uses the first one, display a warning if more.
                if (length(filenames) > 1)
                    disp(['WARNING: There are more than one filename match ' regex '.']);
                end
                fid = fopen([folder filenames(1).name],'r');
                content = textscan(fid, '%s', 'delimiter', '\n');
                obj.log = content{1};
            end
        end
        
        % -----------------------------------------------------------------
        
        % Open structure image with detected and labeled compartments
        function openStructurePreview(obj)
            
            settings = WJSettings.getInstance;
            
            % Gets all filenames sharing the same correct suffix
            folder = [obj.directory settings.wingjDirectoryName filesep];
            regex = [folder '*' settings.structurePreviewSuffix];
            filenames = dir(regex);
            
            if isempty(filenames)
                error(['Couldn''t find a match for ' regex '.']);
            else
                % Only uses the first one, display a warning if more.
                if (length(filenames) > 1)
                    disp(['WARNING: There are more than one filename match ' regex '.']);
                end
                obj.structurePreview = imread([folder filenames(1).name]);
            end
        end
        
        % -----------------------------------------------------------------
        
        % Shows the structure images with detected and labeled compartments.
        function showStructurePreview(obj, galery, mytitles)
            
            if nargin < 2
                galery = true;
            end
%             if nargin < 3
%                 mytitles = {};
%             end
            
            % Get all the images and place them in a cell array. Idem for
            % the image title. First load the images if required and count
            % the number of effective images.
            Neff = 0;
            for i=1:length(obj)
                e = obj(i);
                if isempty(e.structurePreview)
                    e.openStructurePreview();
                end
                if ~isempty(e.structurePreview)
                    Neff = Neff+1;
                end
            end
            
            % Build the list of images
            I = cell(Neff,1);
            titles = cell(Neff,1);
            for i=1:length(obj)
                e = obj(i);
                if ~isempty(e.structurePreview)
                    I{i} = e.structurePreview;
                    titles{i} = regexprep(e.name, '_', '\_');
                end
            end    
            
            % Use the given titles (if any given)
            if exist('mytitles','var')
                % If a single string is given, add indexes
                if size(mytitles,1)==1
                    indexesStr = arrayfun(@num2str, 1:Neff, 'unif', 0);
                    titles = cellfun(@(c)[mytitles c],indexesStr,'uni',false);
                else
                    titles = mytitles;
                end
            end
            
            % Show the galery
            if ~isempty(I)
                settings = WJSettings.getInstance;
                numImgPerLine = settings.numGaleryPicturesPerLine;
                show_image_galery(I,titles,galery,numImgPerLine);
            end
        end
        
        % -----------------------------------------------------------------
        
        % Runs nuclei detection.
        function detectNuclei(obj, marker, sliceIndexes, saveMedia)
            
            if ~ischar(marker)
                error('WingJ:Experiment:badmarker', 'Marker is not a string.');
            end
            
            if nargin < 3
                sliceIndexes = [];
            end
            if nargin < 4
                saveMedia = false;
            end
            
            settings = WJSettings.getInstance;
            
            % runs the nuclear detection for each experiment
            N = length(obj);
            for i=1:N
%                 try
                    disp(['Running nuclei detection (' num2str(i) '/' num2str(N) ')']);
                    disp(['Marker: ' marker]);
                    exp = obj(i);
                    % run nuclei detection
                    detector = NucleiDetector(exp.getImageSequence(marker));
                    detector.setMask(exp.getStructureBinaryMask());
                    detector.setSliceIndexes(sliceIndexes);
                    detector.run();
                    % save results to file
                    detector.save([exp.directory settings.wingjDirectoryName filesep settings.matlabDirectoryName filesep],false,saveMedia);
                    % delete everything but keep results in detector
                    detector.delete();
                    clear detector;
%                 catch e
%                     msg = ['Unable to apply nuclei detection on ' exp.name ': ' e.message];
%                     disp(msg);
%                 end
            end
        end
        
        % -----------------------------------------------------------------
        
        % Open all 1D expression profiles found for this experiment.
        function openExpressionProfiles(obj)
            
            settings = WJSettings.getInstance;         
            N = length(obj);
            for i=1:N
                try
                    exp = obj(i);
                    exp.expressionProfiles = ExpressionProfileList([exp.directory settings.wingjDirectoryName filesep],exp.name);
                catch e
                    msg = ['Unable to open expression profile for ' exp.name ': ' e.message];
                    disp(msg);
                end
            end
        end
        
        % -----------------------------------------------------------------
        
        % Open specified expression maps found for this experiment.
        function openExpressionMaps(obj, geneName, offsetRange, tokens)
            
            settings = WJSettings.getInstance;         
            N = length(obj);
            for i=1:N
                try
                    exp=obj(i);    
                    exp.expressionMaps=ExpressionMapList([exp.directory settings.wingjDirectoryName filesep],exp.name,geneName,offsetRange,tokens);
                catch e
                    msg=['Unable to open expression map for ' exp.name ': ' e.message];
                    disp(msg);
                end
            end
        end
        
        % -----------------------------------------------------------------
        
        % Returns the given structure properties.
        function [values units] = getStructureProperty(obj, propertyName)
            
            obj.openStructureProperties();
            
            % Gets structure properties and groups them for the boxplot
            structProps=[obj.structureProperties];
            [values,units]=structProps.getProperty(propertyName);
        end
        
        % -----------------------------------------------------------------
        
        % Returns several properties from one group of experiments.
        % obj: Experiment[].
        % propertyNames: str array of property names.
        % X: vector of data to boxplot.
        % G: vector defining the groups.
        function [X G] = getStructurePropertiesFromOneRepository(obj, propertyNames)
           
            obj.openStructureProperties();
            
            X = []; G = [];
            for i=1:length(propertyNames)
                structProps = [obj.structureProperties];
                % Gets the values of the current property.
                [values,~] = structProps.getProperty(propertyNames{i});
                X = [X values]; %#ok<AGROW>
                G = [G i*ones(1,length(values))]; %#ok<AGROW>
            end
        end
        
        % -----------------------------------------------------------------
        
        % Returns one property for each group of experiments.
        % obj: Experiment[].
        % extraExperiments: cell array where each cell is an Experiment[].
        % propertyName: str.
        % X: vector of data to boxplot.
        % G: vector defining the groups.
        function [X G] = getOneStructurePropertyFromMultiRepositories(obj, additionalBatches, propertyName)
           
            obj.openStructureProperties();
            
            % Requires to convert obj to a cell
            experimentBatches = [{obj},additionalBatches];
            numExperimentBatches = length(experimentBatches);
            for i=1:numExperimentBatches
                experimentBatches{i}.openStructureProperties();
            end
            
            X = []; G = [];
            for i=1:numExperimentBatches
                eg=experimentBatches(i);
                structProps = [eg{1}.structureProperties];
                % Gets the values of the current property.
                [values,~] = structProps.getProperty(propertyName);
                X = [X values]; %#ok<AGROW>
                G = [G i*ones(1,length(values))]; %#ok<AGROW>
            end
        end
        
        % -----------------------------------------------------------------
        
        % Returns all the data required in boxplot input format. X is a
        % vector containing all the data. G is a vector of the same length
        % as X and contains for each element of X the index of the group it
        % belongs to.
        function [X G numGroups] = getStructureProperties(obj, propertyNames, additionalBatches, subplotStrategy)
            
            obj.openStructureProperties();
            
            % Gather all the batches of experiments together.
            % Requires to convert obj to a cell.
            experimentBatches = [{obj},additionalBatches];
            numExperimentBatches = length(experimentBatches);
            for i=1:numExperimentBatches
                experimentBatches{i}.openStructureProperties();
            end
            
            numProperties = length(propertyNames);
            switch subplotStrategy
                case Experiment.PER_REPOSITORY
                    numGroups = numExperimentBatches;
                case Experiment.PER_STRUCTURE_PROPERTY
                    numGroups = numProperties;
                otherwise
                    msg = 'subplotStrategy must be Experiment.PER_REPOSITORY or Experiment.PER_STRUCTURE_PROPERTY.';
                    error('Experiment:getStructureProperties:badinput',msg);
            end
            
            % Get data
            X = []; G = [];
            for i=1:numGroups
                switch subplotStrategy
                    case Experiment.PER_REPOSITORY
                        [x,g] = experimentBatches{i}.getStructurePropertiesFromOneRepository(propertyNames);
                    case Experiment.PER_STRUCTURE_PROPERTY
                        [x,g] = obj.getOneStructurePropertyFromMultiRepositories(additionalBatches,propertyNames{i});
                    otherwise
                        % Nothing to do: this case has been already
                        % tested.
                end
                X = [X x]; %#ok<AGROW>
                if i>1
                    g = g+G(end); % shift the group indexes
                end
                G = [G g]; %#ok<AGROW> 
            end
        end
 
        % -----------------------------------------------------------------
        
        % Boxplots the given structure properties and returns stats.
        function [X,G,numGroups,H,P,sh] = boxplotStructureProperty(obj, propertyNames, additionalBatches, subplotStrategy, boxplotNames, subplotNames, boxplotOptions)
            
            obj.openStructureProperties();
            
            % Get data grouped and almost ready to be plotted.
            [X,G,numGroups] = obj.getStructureProperties(propertyNames,additionalBatches,subplotStrategy);

            experimentBatches = [{obj},additionalBatches];
            numExperimentBatches = length(experimentBatches);
            
            % Format boxplot options
            options = '';
            % If no user-defined subplotNames, use default ones.
            if isempty(subplotNames)
                switch subplotStrategy
                    case Experiment.PER_REPOSITORY
                        % Surely there must be a way to do it in one line
                        a = arrayfun(@num2str, 1:numExperimentBatches, 'unif', 0);
                        subplotNames = cellfun(@(c)['Batch' c],a,'uni',false);
                    case Experiment.PER_STRUCTURE_PROPERTY
                        subplotNames = propertyNames;
                end
            end
            
            % If no user-defined boxplot group labels, use the name
            % of the properties.
            if isempty(boxplotNames)
                switch subplotStrategy
                    case Experiment.PER_REPOSITORY
                        boxplotNames = propertyNames;
                    case Experiment.PER_STRUCTURE_PROPERTY
                        % Surely there must be a way to do it in one line
                        a = arrayfun(@num2str, 1:numExperimentBatches, 'unif', 0);
                        boxplotNames = cellfun(@(c)['Batch' c],a,'uni',false);
                end
            end
            % Add single quotes around each name and a comma for
            % the elements 2 to end.
            boxplotNames = strcat('''',boxplotNames,'''');
            if length(boxplotNames)>1
                boxplotNames(2:end) = strcat(',',boxplotNames(2:end));
            end
            options = [options '''labels'',{' boxplotNames '}'];
            if ~isempty(boxplotOptions)
                if ~isemtpy(options)
                    options = [options ','];
                end
                options = [options boxplotOptions];
            end
            
            % Defines the positioning of the subplots
            settings = WJSettings.getInstance;
            n = settings.numStructurePropertySubplotsPerLine;
            m = ceil(numGroups/n); 
            numBoxplotsPerGroup = length(unique(G))/numGroups;
            figure;
            sh = [];
            for i=1:numGroups
                % Select data
                a = (i-1)*numBoxplotsPerGroup + 1;
                b = a + numBoxplotsPerGroup - 1;
                indexes = (G>=a & G<=b);
                x = X(indexes); %#ok<NASGU>
                g = G(indexes); %#ok<NASGU>
                % Plot
                if numGroups > 1 % Don't use subplot if there is only on subplot.
                    sh(i) = subplot(m,n,i); %#ok<AGROW>
                end
                evalStr = ['boxplot(x,g,' horzcat(options{:}) ');'];
                disp(evalStr);
                eval(evalStr);
                title(subplotNames(i));
            end
            % Synchronize y-axis of the subplots
            if ~isempty(sh)
                linkaxes(sh,'y');
                ylim(sh(1),[min(X) max(X)]);
            end
            
            
            % MANN-WHITNEY U-TEST
            % null hypothesis: two same distribution
            numDistributions = length(unique(G));
            if numDistributions > 1
                % p-values (reject null hypothesis if p_ij<0.05 by default)
                P=ones(numDistributions,numDistributions);
                % h_ij = 1 if groups i and j are significantly different (p<0.05).
                H=zeros(numDistributions,numDistributions);
                for i=1:numDistributions
                    for j=i+1:numDistributions
                        data1=X(G==i);
                        data2=X(G==j);
                        [p,h]=ranksum(data1,data2);
                        P(i,j)=p; P(j,i)=p;
                        H(i,j)=h; H(j,i)=h;
                        
                        iSubplotIndex = floor((i-1)/length(boxplotNames))+1;
                        iBoxplotIndex = mod((i-1),length(boxplotNames))+1;
                        jSubplotIndex = floor((j-1)/length(boxplotNames))+1;
                        jBoxplotIndex = mod((j-1),length(boxplotNames))+1;
                        
                        iSubplotName = char(subplotNames(iSubplotIndex));
                        iBoxplotName = char(boxplotNames(iBoxplotIndex));
                        jSubplotName = char(subplotNames(jSubplotIndex));
                        jBoxplotName = char(boxplotNames(jBoxplotIndex));
                        
                        % Remove commas added for boxplot options format
                        % compatibility
                        if iBoxplotIndex > 1
                            iBoxplotName = iBoxplotName(2:end);
                        end
                        if jBoxplotIndex > 1
                            jBoxplotName = jBoxplotName(2:end);
                        end
                       
                        % Report the significance
                        signStr = ['significantly different (p=' num2str(p) ' < 0.05)'];
                        if ~h
                            signStr = ['not significantly different (p=' num2str(p) ' >= 0.05)'];
                        end
                        disp(['Mann-Whitney U-Test: (' iSubplotName ',' iBoxplotName ') and (' jSubplotName ',' jBoxplotName '): ' signStr]);
                    end
                end
%                 disp('h_ij = 1 indicates that two groups of experiments are significantly different (at 5% significance level, i.e. p_ij < 0.05). p_ij is the p-value when considering groups i and j.');
%                 H %#ok<NOPRT>
%                 P %#ok<NOPRT>
            end
        end
        
        % -----------------------------------------------------------------
        
        % Returns the expression profiles matching the filter specifications.
        function profiles = getExpressionProfiles(obj, geneName, refBoundary, offsetRange)
            
            if nargin < 2
                geneName = [];
            end
            if nargin < 3
                refBoundary = [];
            end
            if nargin < 4
                offsetRange = [];
            end
            
            N=length(obj);
            profiles = ExpressionProfileList();
            
            % Opens expression profiles if required.
            for i=1:N
                exp = obj(i);
                if isempty(exp.expressionProfiles)
                    exp.openExpressionProfiles();
                end
                if ~isempty(exp.expressionProfiles)
                    profiles.addExpressionProfiles(exp.expressionProfiles.get(:));
                end
            end
            
            profiles = profiles.getExpressionProfiles(geneName,refBoundary,offsetRange);
        end
        
        % -----------------------------------------------------------------
        
        % Returns the expression maps match the filter specifications.
        % "tokens" can be an array of strings to find in the filename of
        % the image/map.
        function maps = getExpressionMaps(obj, geneName, offsetRange, tokens)
            
            if nargin < 2
                geneName = [];
            end
            if nargin < 3
                offsetRange = [];
            end
            if nargin < 4
                tokens = [];
            end
            
            N = length(obj);
            maps = ExpressionMapList();
            
            % Opens expression maps if required.
            for i=1:N
                exp = obj(i);
%                 if isempty(exp.expressionMaps)
                    exp.openExpressionMaps(geneName,offsetRange,tokens);
%                 end
                if ~isempty(exp.expressionMaps)
                    maps.addExpressionMaps(exp.expressionMaps.get(:));
                end
            end
            
            maps = maps.getExpressionMaps(geneName,offsetRange,tokens);
        end
        
        % -----------------------------------------------------------------
        
        % Load the number of nuclei previously detected from the file in
        % EXPERIMENT/WingJ/Matlab/NucleiDetection/nuclei_num.txt
        function openNumNuclei(obj)
            
            settings = WJSettings.getInstance;
            N = length(obj);
            for i=1:N
                e = obj(i);
                if isempty(e.numNuclei) || e.numNuclei==0
                    try
                        folder = [e.directory settings.wingjDirectoryName filesep settings.matlabDirectoryName filesep settings.nucleiDetectorDirectoryName filesep];
                        filename = [folder settings.numNucleiFilename];
                        data = importdata(filename);
                        obj.numNuclei(1) = data(1,1);
                    catch err
                        e.numNuclei = 0;
                        msg = ['Unable to open number of nuclei ' e.name ': ' err.message];
                        disp(msg);
                    end
                end
            end
        end
        
        % -----------------------------------------------------------------
        
        % Returns the number of nuclei.
        function numNuclei = getNumNuclei(obj)
            
            N = length(obj);
            numNuclei = zeros(N,1);
            for i=1:N
                e = obj(i);
                e.openNumNuclei();
                numNuclei(i) = e.numNuclei;
            end
            % Remove null elements
            numNuclei(numNuclei==0) = [];
        end
        
        % =================================================================
        % SETTERS AND GETTERS
        
        % Return the image sequence associated to the given channel.
        % Parameter 'channel' could be either a channel index or name.
        function images = getImageSequence(obj, channel)
            
            settings = WJSettings.getInstance;
            
            % get image sequence from channel index
            if isfloat(channel) % would be better to test integer
                % check channel index validity
                numChannels = settings.getNumChannels();
                if channel > numChannels-1
                    error(['ERROR: channel must be in [0,' num2str(numChannels-1) '].']);
                end
                % load image sequence if required
                if isempty(obj.images{channel+1})
                    obj.openImageSequence(channel);
                end
                images = obj.images{channel+1};
                
            % get image sequence from channel name
            elseif ischar(channel)
                index = obj.getChannelIndex(channel);
                if isempty(index)
                	error(['ERROR: Invalid channel name ''' channel '''.']);
                end
                images = obj.getImageSequence(index);
            else
                error('ERROR: Invalid parameter type.');
            end
        end
        
        % -----------------------------------------------------------------
        
        % Return the structure binary mask
        function mask = getStructureBinaryMask(obj)
            
            if isempty(obj.structureBinaryMask)
                obj.openStructureBinaryMask();
            end 
            mask = obj.structureBinaryMask;
        end
        
        % -----------------------------------------------------------------
        
        % Set the name of the mutant
        function setMutantName(obj, mutant)
            obj.mutantName = mutant;
        end
    end
end