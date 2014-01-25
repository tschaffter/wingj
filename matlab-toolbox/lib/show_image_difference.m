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
% SHOW_IMAGE_DIFFERENCE Shows the different between img1 and img2.
%
% Implemented to compare two expression maps, this function can be used
% in two different ways. If img2 is not given, img1 is shown. This allows
% to display different data such as the variance computed from multiple
% images computed before calling this function. If img2 is specified,
% img1-img2 is shown. 'range' is used to specify the range of the
% colormap (e.g. [-120 120]). If 'filename' is specified, the figure is
% saved in TIF format.
%
% A white dashed circle is plotted around the disc-shape expression map
% to separate the zero data in the four corners of the image with the
% zero data included in the expression map.
%
% Author: Thomas Schaffter (thomas.schaff...@gmail.com)
% Version: September 10, 2012
function imgDiff = show_image_difference(img1, img2, range, filename)

    imgDiff = []; %#ok<NASGU>
    if isempty(img2)
        imgDiff = img1;
    else
        imgDiff = compute_image_difference(img1,img2);
    end
    
    imgSize = size(imgDiff);

    img = imagesc(imgDiff); hold on;
    circle(imgSize/2,min(imgSize)/2,1000,'w--'); hold off;
    set(gca,'XTick',[]);
    set(gca,'YTick',[]);
    
    if nargin>2
    	set(gca, 'CLim',range);
    end
%     clim = get(gca,'CLim');
%     set(gca, 'CLim',[0 clim(2)]);

    axis square
    cmap = colormap(jet(128));
    cbh = colorbar;
    set(get(cbh,'YLabel'),'String','Distance');
    
    % Converts and save the image to TIF if required
    if nargin>3 && ~isempty(filename)
        % I ranges from 0 - 1
        I = []; %#ok<NASGU>
        if isempty(range)
            clim = get(gca,'CLim');
            I=mat2gray(get(img,'cdata'),[clim(1) clim(2)]);
        else
            I=mat2gray(get(img,'cdata'),range);
        end

        % Convert I to indices into the colormap,
        %i.e map 0 -1 to 1 - size of colormap
        IndI = ceil(I*size(cmap,1));

        imwrite(IndI,cmap,filename,'tif');
    end
end