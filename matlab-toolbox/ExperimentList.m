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
% This class represents a list or repository containing Experiment
% objects. Please refer to the description of Experiment for more
% detailed information.
%
% The goal of its implementation is that the user can create instance of
% ExperimentList containing several experiment before calling a method that
% will be applied to every Experiment object.
%
% Author: Thomas Schaffter (thomas.schaff...@gmail.com)
% Version: September 10, 2012
classdef ExperimentList < handle
    
    properties(GetAccess = 'public', SetAccess = 'private')
        % experiment references
        experiments = [];
    end
    
    % =====================================================================
	% PUBLIC METHODS
    
    methods
        
        % Constructor
        % Open all the experiments located in the given root directories
        function obj = ExperimentList(rootDirectories)
            if nargin > 0
                % for each root directory
                numExperiments = 0;
                for i=1:length(rootDirectories)
                    directories = list_experiments(rootDirectories{i});
                    % for each experiment directory
                    for j=1:length(directories)
                        try
                            % create and add experiment
                            fprintf('Adding experiment %s\n', directories{j});
                            e = Experiment(directories{j});
                            obj.addExperiment(e);
                            numExperiments = numExperiments+1;
                        catch err
                            disp(['WARNING: Unable to load ' directories{j} ': ' err.message]);
                            disp('Please check that the naming convention introduced in the WingJ User Manual is respected.')
                        end
                    end
                end
                disp(['=> ' num2str(numExperiments) ' experiment successfully added.'])
            end
        end
        
        % -----------------------------------------------------------------
        
        % Add experiment
        function addExperiment(obj, e)
            obj.experiments = [obj.experiments e];
        end
        
        % =================================================================
        % SETTERS AND GETTERS
        
        % Returns the experiment matching the given index(es).
        function e = get(obj, index)
            if size(obj.experiments,1) < 1
                error('WingJ:ExperimentList:empty', 'This list of experiments is empty.');
            end
            try
                e = obj.experiments(index);
            catch err
                if (strcmp(err.identifier,'MATLAB:badsubscript'))
                    N = obj.getNumExperiments();
                    msg = ['There are only ' num2str(N) ' experiments... listed (index starts from 1 to ' num2str(N) ').'];
                    error('WingJ:ExperimentList:dimensions', msg);
                else
                    rethrow(err);
                end
            end
        end
        
        % -----------------------------------------------------------------
        
        % Returns the total number of experiments.
        function num = getNumExperiments(obj)
            num = length(obj.experiments);
        end
        
        % -----------------------------------------------------------------
        
        % Display the number of experiments.
        function displayNumExperiments(obj)
            disp([inputname(1) ' contains ' num2str(obj.getNumExperiments) ' experiments.']);
        end
        
        % -----------------------------------------------------------------
        
        % Display the name of the experiments.
        function displayExperimentNames(obj)
            
            numExperiments = obj.getNumExperiments();
            for i=1:numExperiments
                disp([inputname(1) '.get(' num2str(i) '): ' obj.get(i).name]);
            end
        end
        
        % -----------------------------------------------------------------
        
        % Return an array containing all the experiment names
        function names = getExperimentNames(obj)
            names = {obj.experiments.name};
            names = names';
        end
        
        % -----------------------------------------------------------------
        
        % Get experiments with age inside the given range
        % For example age = [minAge maxAge]
        function experiments = getExperimentsByAge(obj, age)
             
            experiments = ExperimentList();
            for i=1:length(obj.experiments)
                e = obj.experiments(i);
                meanAge = mean(e.age);
                if age(1) <= meanAge && meanAge <= age(2)
                    experiments.addExperiment(e);
                end
            end
        end
        
        % -----------------------------------------------------------------
        
        % Get experiments done on one of the given dates
        function experiments = getExperimentsByDate(obj, dates)
                   
            experiments = ExperimentList();
            for i=1:length(obj.experiments)
                e = obj.experiments(i);
                if sum(strcmp(dates,e.date)) > 0 % found a match
                    experiments.addExperiment(e);
                end
            end
        end
        
        % -----------------------------------------------------------------
        
        % Returns the experiments that correspond to the given mutant name.
        function experiments = getExperimentsByMutantName(obj, mutantName)
            
            experiments = ExperimentList();
            for i=1:length(obj.experiments)
                e = obj.experiments(i);
                if ~isempty(mutantName)
                    for j=1:length(e.mutantNames)
                        if strcmp(e.mutantNames(j),mutantName);
                            experiments.addExperiment(e);
                            break;
                        end
                    end
                elseif isempty(e.mutantNames) % wild-type experiments
                    experiments.addExperiment(e);
                end
            end
        end
        
        % -----------------------------------------------------------------
        
        % Returns wild-type experiments.
        function experiments = getWildTypeExperiments(obj)
            
            experiments = obj.getExperimentsByMutantName([]);
        end
        
        % -----------------------------------------------------------------
        
        % Returns the experiments that correspond to the given gene name.
        function experiments = getExperimentsByGeneName(obj, geneName)
            
            experiments = ExperimentList();
            for i=1:length(obj.experiments)
                e = obj.experiments(i);
                for j=1:length(e.channelNames)
                    if strcmp(e.channelNames(j),geneName);
                        experiments.addExperiment(e);
                        break;
                    end
                end
            end
        end
        
        % -----------------------------------------------------------------
        
        % Returns the experiments whose name contains the given substring.
        function experiments = getExperimentsByExperimentName(obj, substr)
           
            experiments = ExperimentList();
            for i=1:length(obj.experiments)
                e = obj.experiments(i);
                if ~isempty(strfind(e.name,substr))
                    experiments.addExperiment(e);
                end
            end
        end
    end
end