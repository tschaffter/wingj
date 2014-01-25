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
% This class represents a list or repository containing ExpressionMap
% objects. Please refer to the description of ExpressionMap for more
% detailed information.
%
% See the Supplementary Material for more information about how expression
% maps are generated in WingJ.
%
% Author: Thomas Schaffter (thomas.schaff...@gmail.com)
% Version: September 10, 2012
classdef ExpressionMapList < handle
    
    properties(GetAccess = 'public', SetAccess = 'private')
        % List of ExpressionMap objects.
        maps = [];
    end
    
    % =====================================================================
	% PRIVATE METHODS
    
    methods (Access = private)
        
        % Initialize
        function initialize(obj, directory, experimentName, geneName, offsetRange, tokens)
            
            % For simplicity, defined here
            expressionMapSubStr = 'expression_map';
            
            % first get all files assiciated to 1D expression datasets
            filenames = list_expression_datasets(directory,expressionMapSubStr,geneName);
            
            % remove extension (must be always 4 chars '.xxx')
            for iFile=1:length(filenames)
                ext = filenames{iFile}(end-3:end);
                if ~strcmp(ext(1), '.')
                    error('WingJ:ExpressionMap:badextension', ['Is not a 3-letter extension: ' ext]);
                end
                % delete extension
                filenames{iFile}(end-3:end) = [];
            end
            
            % discard maps based on the offset
            if nargin > 4 && ~isempty(offsetRange)
                keep = zeros(length(filenames),1);
                for iFile=1:length(filenames)
                    offset_d = ExpressionMap.getOffset(filenames{iFile});
                    keep(iFile) = (offset_d >= offsetRange(1) && offset_d <= offsetRange(2));
                end
                filenames(keep==0) = [];
            end
            
            % discard files based on the given tokens
            if nargin > 5 && ~isempty(tokens)
                keep = zeros(length(filenames),1);
                for iFile=1:length(filenames)
                    keep(iFile) = find_tokens(filenames{iFile},tokens);
                end
                filenames(keep==0) = [];
            end
            
%             keepFile = ones(size(filenames,1),1);
% 
%             % if geneName specified, keep only filename containing it
%             if nargin>4
%                 for iFile = find(keepFile)'
%                     if isempty(strfind(filenames{iFile}, geneName))
%                         keepFile(iFile) = 0;
%                     end
%                     disp([filenames{iFile} ': ' num2str(keepFile(iFile))])
%                 end
%             end
%             
%             
%             
%             filenames(~keepFile) = [];
            
%             for iFile = find(keepFile)'
%                 filenames{iFile}
%             end
            
            % remove repetitions
            filenames = unique(filenames);
            
            % instantiate and add datasets
            disp(['Experiment ' experimentName ':']);
            for iFile=1:length(filenames)
                try
                    disp(['Opening expression map ' filenames{iFile}]);
                    map=ExpressionMap(experimentName,filenames{iFile});
                    obj.addExpressionMaps(map);
                catch err
                    msg=['Unable to open expression map: ' err.message];
                    disp(msg);
                end
            end
                
            disp(['=> ' num2str(length(obj.maps)) ' expression maps successfully opened.'])
        end
    end
    
    % =====================================================================
	% PUBLIC METHODS
    
    methods
        
        % Constructor.
        function obj = ExpressionMapList(experimentName, directory, geneName, offsetRange, tokens)
            if nargin > 0
                obj.initialize(experimentName,directory,geneName,offsetRange,tokens);
            end
        end
        
        % -----------------------------------------------------------------
        
        % Adds the given expression maps to a single vector.
        function addExpressionMaps(obj, maps)
            for i=1:length(maps)
                obj.maps = [obj.maps maps(i)];
            end
        end
        
        % -----------------------------------------------------------------
        
        % Returns the expression maps matching the given gene name and offset.
        function maps = getExpressionMaps(obj, geneName, offsetRange, tokens)
           
            maps = ExpressionMapList();
            maps.addExpressionMaps(obj.get(:));
            
            % If no arguments, then just return
            if nargin < 2
                return
            end
            
            if isempty(maps)
                return
            end
            
            % Filters
            if ~isempty(geneName)
                maps=maps.getExpressionMapsByGeneName(geneName);
            end
            if ~isempty(offsetRange)
                maps=maps.getExpressionMapsByOffset(offsetRange);
            end
            if ~isempty(tokens)
                maps=maps.getExpressionMapsFromFilenameTokens(tokens);
            end
        end
        
        % -----------------------------------------------------------------
        
        % Returns expression maps matching the given gene name.
        function maps = getExpressionMapsByGeneName(obj, geneName)
            
            maps = ExpressionMapList();
            for i=1:length(obj.maps)
                m = obj.maps(i);
                if strcmp(m.geneName,geneName)
                    maps.addExpressionMaps(m);
                end
            end
        end
        
        % -----------------------------------------------------------------
        
        % Returns the images (loosely called expression maps) whose
        % filename contains all the given tokens.
        % If a token starts with '-', this token should NOT be included.
        function maps = getExpressionMapsFromFilenameTokens(obj, tokens)
            
            maps = ExpressionMapList();
            for i=1:length(obj.maps)
                m = obj.maps(i);
                if find_tokens(m.filename,tokens)
                    maps.addExpressionMaps(m);
                end
            end
        end
        
        % -----------------------------------------------------------------
        
        % Returns expression maps with offset in [minOffset,maxOffset].
        function maps = getExpressionMapsByOffset(obj, offsetRange)
             
            maps = ExpressionMapList();
            for i=1:length(obj.maps)
                m = obj.maps(i);
                if m.offset >= offsetRange(1) && m.offset <= offsetRange(2)
                    maps.addExpressionMaps(m);
                end
            end
        end
        
        % =================================================================
        % SETTERS AND GETTERS
        
        % Returns the experiment map(s) from index(es).
        function e = get(obj, index)
            e = obj.maps(index);
        end
        
        % -----------------------------------------------------------------
        
        % Returns the total number of expression maps.
        function num = getNumExpressionMaps(obj)
            num = length(obj.maps);
        end
        
        % -----------------------------------------------------------------
        
        % Returns a cell array containing all the maps experiment names.
        function names = getMapNames(obj)
            names = {obj.maps.name};
            names = names';
        end
    end
end