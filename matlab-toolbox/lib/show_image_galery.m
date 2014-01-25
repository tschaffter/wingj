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
% SHOW_IMAGE_GALERY Displays a list of images in a single figure.
%
% The first arguments is a list of images to display next to each other
% on several lines and columns. A list containing the title of each image
% can be provided. The titles are plotted on the top-left corner of each
% image. The third argument, galery, can be true or false. True: images
% are plotted in a galery. False: each image is displayed on a different
% figure. The number of images per line can be specified using the fourth
% argument (default: 4 images per line).
%
% Note that images are converted to 'uint8'
%
% Author: Thomas Schaffter (thomas.schaff...@gmail.com)
% Version: September 10, 2012
function show_image_galery(images,titles,galery,numImgPerLine)

    TITLE_LOCATION_OFFSET_X = 20;
    TITLE_LOCATION_OFFSET_Y = 0;
    TITLE_MAX_LENGTH = 30;

    if nargin < 2
        titles = {};
    end
    if nargin < 3
        galery = true;
    end
    if nargin < 4
        numImgPerLine = 4;
    end
    
    numImages = length(images);
    S = size(images{1}); % size of an image (all images must have the same size)
    if galery && numImages > 1
        % Get the horizontal dimension in pixels
        if numImages < numImgPerLine
            w = S(2)*numImages;
        else
            w = S(2)*numImgPerLine;
        end
        h = S(1)*ceil(numImages/numImgPerLine);
        if length(S) == 3
            d = S(3);
            galery = zeros([h,w,d],'uint8');
        elseif length(S) == 2
            galery = zeros([h,w],'uint8');    
        end
        
        % Display
        index = 1;
        for i=1:numImages
            x1 = (mod((index-1),numImgPerLine))*S(2)+1;
            y1 = floor((index-1)/numImgPerLine)*S(1)+1;
            x2 = x1+S(2)-1;
            y2 = y1+S(1)-1;
            galery(y1:y2,x1:x2,:) = images{i};
            index = index+1;
        end
        imshow(galery);
    else
        for i=1:numImages
            if i<=length(titles) && ~isempty(titles(i))
                figure('name',titles{i});
            else
                figure;
            end
            imshow(images{i});
        end
    end
    
    % Adds titles (if any)
    if ~isempty(titles)
        index = 1;
        for i=1:numImages
            x1 = (mod((index-1),numImgPerLine))*S(2)+1;
            y1 = floor((index-1)/numImgPerLine)*S(1)+1;
            if i <= length(titles)
                str = regexprep(titles{i}, '_', '\\_');
                str = str2strarray_length(str,TITLE_MAX_LENGTH);
                htext = text(x1+TITLE_LOCATION_OFFSET_X,y1+TITLE_LOCATION_OFFSET_Y,str,'Color','w');
                pos = get(htext,'Extent');
                set(htext,'position', [pos(1), pos(2)])
                
%                 pos = get(gca, 'currentpoint');
%                 pos
%                 set(htext,'position', [pos(1), pos(3)]);
            end
            index = index+1;
        end
    end
end