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
%
% This class represents a list or repository containing ExpressionProfile
% objects. Please refer to the description of ExpressionProfile for more
% detailed information.
%
% See the Supplementary Material for more information about how expression
% maps are generated in WingJ.
%
% Author: Thomas Schaffter (thomas.schaff...@gmail.com)
% Version: September 10, 2012
classdef ExpressionProfileList < handle
    
    properties(GetAccess = 'public', SetAccess = 'private')
        % List of ExpressionProfile objects
        profiles = [];
    end
    
    % =====================================================================
	% PRIVATE METHODS
    
    methods (Access = private)
        
        % Initialize
        function initialize(obj, directory, experimentName, expressionProfileSubStr)
            
            settings = WJSettings.getInstance;
            
            if nargin < 4
                expressionProfileSubStr = settings.expressionProfileStr;
            end
 
            % first get all files assiciated to 1D expression datasets
            filenames = list_expression_datasets(directory, expressionProfileSubStr);
            
            disp(['Loading expression profiles for ' experimentName]);
            disp(['Directory: ' directory]);
            
            % remove extension (must be always 4 chars '.xxx')
            for iFile=1:length(filenames)
                ext = filenames{iFile}(end-3:end);
                if ~strcmp(ext(1), '.')
                    error('WingJ:ExpressionProfile:badextension', ['Is not a 3-letter extension: ' ext]);
                end
                % delete extension
                filenames{iFile}(end-3:end) = [];
            end
            
            % remove repetitions
            filenames = unique(filenames);
            
            % instantiate and add datasets
            for iFile=1:length(filenames)
                try
                    disp(['Opening expression profile ' filenames{iFile}]);
                    profile = ExpressionProfile(filenames{iFile},experimentName);
                    obj.addExpressionProfiles(profile);
                catch err
                    msg=['Unable to open expression profile: ' err.message];
                    disp(msg);
                end
            end
            
            disp(['=> ' num2str(length(obj.profiles)) ' expression profiles successfully opened.'])
        end
    end
    
    % =====================================================================
	% PUBLIC METHODS
    
    methods
        
        % Constructor
        function obj = ExpressionProfileList(directory, experimentName)
            if nargin > 0
                obj.initialize(directory,experimentName);
            end
        end
        
        % -----------------------------------------------------------------
        
        % Adds the given expression profiles to a single vector.
        function addExpressionProfiles(obj, profiles)
            for i=1:length(profiles)
                obj.profiles = [obj.profiles profiles(i)];
            end
        end
        
        % -----------------------------------------------------------------
        
        % Returns the expression profiles matching the given gene name,
        % reference boundary and offset.
        function profiles = getExpressionProfiles(obj, geneName, refBoundary, offsetRange)
           
            profiles = ExpressionProfileList();
            profiles.addExpressionProfiles(obj.get(:));
            
            % If no arguments, then just return
            if nargin < 2
                return
            end
            
            if isempty(profiles)
                return
            end
            
            % Filters
            if ~isempty(geneName)
                profiles=profiles.getExpressionProfilesByGeneName(geneName);
            end
            if ~isempty(refBoundary)
                profiles=profiles.getExpressionProfilesByReferenceBoundary(refBoundary);
            end
            if ~isempty(offsetRange)
                profiles=profiles.getExpressionProfilesByOffset(offsetRange);
            end
        end
        
        % -----------------------------------------------------------------
        
        % Returns expression profiles matching the given gene name.
        function profiles = getExpressionProfilesByGeneName(obj, geneName)
            
            profiles = ExpressionProfileList();
            for i=1:length(obj.profiles)
                p = obj.profiles(i);
                if strcmp(p.geneName,geneName)
                    profiles.addExpressionProfiles(p);
                end
            end
        end
        
        % -----------------------------------------------------------------
        
        % Returns expression profiles matching the given reference
        % boundary.
        function profiles = getExpressionProfilesByReferenceBoundary(obj, refBoundary)
             
            profiles = ExpressionProfileList();
            for i=1:length(obj.profiles)
                p = obj.profiles(i);
                if strcmp(p.referenceBoundary,refBoundary)
                    profiles.addExpressionProfiles(p);
                end
            end
        end
        
        % -----------------------------------------------------------------
        
        % Returns expression profiles with offset in [minOffset,maxOffset].
        function profiles = getExpressionProfilesByOffset(obj, offsetRange)
             
            profiles = ExpressionProfileList();
            for i=1:length(obj.profiles)
                p = obj.profiles(i);
                if p.offset >= offsetRange(1) && p.offset <= offsetRange(2)
                    profiles.addExpressionProfiles(p);
                end
            end
        end
        
        % -----------------------------------------------------------------
        
        % Prints all the name of the expression profiles contained in this
        % ExpressionProfileList.
        function displayExpressionProfileNames(obj)
            
            numProfiles = obj.getNumExpressionProfiles();
            for i=1:numProfiles
                disp([inputname(1) '.get(' num2str(i) '): ' obj.get(i).name]);
            end
        end
        
        % =================================================================
        % SETTERS AND GETTERS
        
        % Returns the experiment profile(s) from index(es).
        function e = get(obj, index)
            e = obj.profiles(index);
        end
        
        % -----------------------------------------------------------------
        
        % Returns the total number of expression profiles.
        function num = getNumExpressionProfiles(obj)
            num = length(obj.profiles);
        end
        
        % -----------------------------------------------------------------
        
        % Returns a cell array containing all the profile experiment names.
        function names = getProfileNames(obj)
            names = {obj.profiles.name};
            names = names';
        end
    end
end