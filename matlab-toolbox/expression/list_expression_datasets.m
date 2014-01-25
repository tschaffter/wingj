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
% Lists the filenames of the expression datasets contained in the given
% folder.
%
% This function lists all the expression datasets (e.g. 1D and 2D)
% included in the directory 'rootDirectory'. Usually, WingJ output data
% including expression datasets are exported to EXPERIMENT/WingJ where
% EXPERIMENT is the path to an experiment directory.
%
% The type of the experiment can be 'expression_1D' or 'expression_2D'
% that are substrings included in the expression filenames.
%
% The gene name allows to load only data that correspond to a specific
% gene.
%
% See documentation for examples of expression data filename.
%
% Author: Thomas Schaffter (thomas.schaff...@gmail.com)
% Version: September 10, 2012
function datasetFilenames = list_expression_datasets(rootDirectory, type, geneName)

    % list folders
    files = dir(rootDirectory);

    % the first two entries in folders are current and parent dir
    % so we'll ignore them
    files(1:2) = [];

    % keep only files
    isBadFile = cat(1,files.isdir);

    % remove hidden files
    for iFile = find(~isBadFile)'
       % on OSX, hidden files start with a dot
       isBadFile(iFile) = strcmp(files(iFile).name(1),'.');
       if ~isBadFile(iFile) && ispc
           %# check for hidden Windows files - only works on Windows
           [~,stats] = fileattrib(fullfile(rootDirectory,files(iFile).name));
           if stats.hidden
              isBadFile(iFile) = true;
           end
       end
    end

    % a dataset filename must contain ['expression_' dimension];
    for iFile = find(~isBadFile)'
        if isempty(strfind(files(iFile).name, type))
            isBadFile(iFile) = true;
        end
    end
    
    % if gene name is provided
    if nargin>2
        for iFile = find(~isBadFile)'
            if isempty(strfind(files(iFile).name, geneName))
                isBadFile(iFile) = true;
            end
        end
    end
    
    % keep only expression dataset files
    files(isBadFile) = [];
    
    % build experiment directory paths
    datasetFilenames = [];
    if length(files)>0 %#ok<ISMT>
        datasetFilenames = strcat(rootDirectory,{files.name}');
    end
end