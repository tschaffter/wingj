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
% OPEN_IMAGE_SEQUENCE Opens a and returns a stack of images associated to a
% specified channel.
%
% Opens a stack of images located in the given directory (final '/'
% required). The index of the channel must be specified, for instance 1,
% 2, 3, etc. Example of image filenames:
%
% Series002_z00_ch00.tif,Series002_z01_ch00.tif,Series002_z03_ch00.tif,..
% Series002_z00_ch01.tif,Series002_z01_ch01.tif,Series002_z02_ch01.tif,..
% ...
%
% To open the image stack associated to 'ch00':
% open_image_sequence(directory,0)
%
% By default, slicePrefix='_z' and channelPrefix='_ch'. 'ch' is always
% directly followed by a '0' and then by the channel index like in
% 'ch01'.
%
% Since image stack may be quite heavy in term of memory, don't forget to
% free the data when they are not useful anymore.
%
% Author: Thomas Schaffter (thomas.schaff...@gmail.com)
% Version: September 10, 2012
function stack = open_image_sequence(directory, channel, slicePrefix, channelPrefix)

    if nargin < 3
        slicePrefix = '_z';
    end
    if nargin < 4
        channelPrefix = '_ch';
    end

    % list images
    images = dir(directory);

    % the first two entries in images are current and parent dir
    % so we'll ignore them
    images(1:2) = [];

    % keep only the files
    isBadFile = cat(1,images.isdir);

    % remove hidden files
    for iFile = find(~isBadFile)'
       % on OSX, hidden files start with a dot
       isBadFile(iFile) = strcmp(images(iFile).name(1),'.');
       if ~isBadFile(iFile) && ispc
           %# check for hidden Windows files - only works on Windows
           [~,stats] = fileattrib(fullfile(directory,images(iFile).name));
           if stats.hidden
              isBadFile(iFile) = true;
           end
       end
    end

    % remove images which don't contain specific substring in their filename
    for iFile = find(~isBadFile)'
        isBadFile(iFile) = isempty(strfind(images(iFile).name,slicePrefix)) |...
            isempty(strfind(images(iFile).name,[channelPrefix '0' num2str(channel)]));
    end

    % keep only valid images
    images(isBadFile) = [];

    % build image paths
    filenames = strcat(directory,{images.name}');

    % load images in unit8
    numImages = length(images);
    stack = cell(1,numImages);
    for i=1:numImages
        stack{i} = imread(filenames{i});
    end
end