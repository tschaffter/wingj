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

function experimentDirectories = list_experiments(rootDirectory)
%LIST_EXPERIMENTS Lists all the WingJ experiment folders located in the
%   given 'rootDirectory' and returns the list of experiment directories.
%
%   This function uses two variables from the WJSettings object:
%   WJSettings.imagesDirectoryName (e.g. 'images')
%   WJSettings.wingjDirectoryName (e.g. 'WingJ')
%
%   Author: Thomas Schaffter (thomas.schaff...@gmail.com)
%   Version: September 10, 2012

    % load WJSettings
    WJSettings = WJSettings.getInstance;
    
    % Add final directory separator (system dependent)
    if ~strcmp(rootDirectory(end),filesep)
        rootDirectory = [rootDirectory filesep];
    end

    % list folders
    folders = dir(rootDirectory);
    if isempty(folders)
       msg=['The directory ' rootDirectory ' doesn''t exist or is empty.'];
       error(msg);
    end
    
    % the first two entries in folders are current and parent dir
    % so we'll ignore them
    folders(1:2) = [];

    % keep only the folders
    isExperimentFolder = cat(1,folders.isdir);

    % remove hidden files
    for iFile = find(isExperimentFolder)'
       % on OSX, hidden files start with a dot
       isExperimentFolder(iFile) = ~strcmp(folders(iFile).name(1),'.');
       if isExperimentFolder(iFile) && ispc
           %# check for hidden Windows files - only works on Windows
           [~,stats] = fileattrib(fullfile(rootDirectory,folders(iFile).name));
           if stats.hidden
              isExperimentFolder(iFile) = false;
           end
       end
    end
    
    % remove folders that contain the substring "IGNORE"
    for iFile = find(isExperimentFolder)'
        if ~isempty(strfind(folders(iFile).name,'IGNORE'))
            isExperimentFolder(iFile) = false;
        end
    end

    % an experiment folder must contain a folder called "images"
    for iFile = find(isExperimentFolder)'
        listing = dir([rootDirectory folders(iFile).name]);
        isExperimentFolder(iFile) = false; % set to false for now
        for i=1:length(listing)
            if strcmp(listing(i).name,WJSettings.imagesDirectoryName)
                isExperimentFolder(iFile) = true; % has "images" folder
                break
            end
        end
    end
    
    % an experiment folder must contain a folder called "WingJ"
    for iFile = find(isExperimentFolder)'
        listing = dir([rootDirectory folders(iFile).name]);
        %isExperimentFolder(iFile) = false; % set to false for now
        for i=1:length(listing)
            % only consider this if the folder already has an "images"
            % folder (isExperimentFolder(iFile) is true)
            if isExperimentFolder(iFile) && strcmp(listing(i).name,WJSettings.wingjDirectoryName)
                isExperimentFolder(iFile) = true; % has "images" folder
                break
            end
        end
    end

    % keep only experiment folders
    folders(~isExperimentFolder) = [];
    
    % build experiment directory paths
    experimentDirectories = strcat(rootDirectory,{folders.name}',filesep);
end