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
% SHOW_IMAGE_DIFFERENCE_3D Shows the difference between two images as a 2D
% image and a 3D representation on top of it.
%
% There are two ways to use this function. If img2 is not specify, the 2D
% image shown is img1 and the 3D representation plotted on top of it
% correspond to img1. If img2 is given, the 2D image is the different 
% img1-img2 and the 3D representation is associated to img1-img2.
%
% The thrid and fourth parameters 'range' and 'filename' are not
% supported for now. 'range' would allow to specify the range of the
% colormap used to show the 2D image and filename would be used to save
% automatically the figure in TIF format. The code to do that is
% currently commented at the end of the script but additional tests
% should be done. Have a look at SHOW_IMAGE_DIFFERENCE.
%
% Author: Thomas Schaffter (thomas.schaff...@gmail.com)
% Version: September 10, 2012
function imgDiff = show_image_difference_3D(img1, img2, range, filename)

    imgDiff = []; %#ok<NASGU>
    if isempty(img2)
        imgDiff = img1;
    else
        imgDiff = compute_image_difference(img1,img2);
    end
    
    imgSize = size(imgDiff);
    
    % the data that you want to plot as a 3D surface.
    x = 1:imgSize(1);
    y = 1:imgSize(2);
    z = imgDiff;
    
    hsize = [40 40];
    sigma = 5;
    h = fspecial('gaussian',hsize,sigma);
    
    z = imfilter(z,h);
 
    % get the corners of the domain in which the data occurs.
    min_x = min(min(x));
    min_y = min(min(y));
    max_x = max(max(x));
    max_y = max(max(y));

    % --- SCRIPT TAKEN FROM AN EXTERNAL SOURCE (START) ---
    % the image data you want to show as a plane.
    planeimg = imgDiff; %abs(z);

    % scale image between [0, 255] in order to use a custom color map for it.
    minplaneimg = min(min(planeimg)); % find the minimum
    scaledimg = (floor(((planeimg - minplaneimg) ./ ...
        (max(max(planeimg)) - minplaneimg)) * 255)); % perform scaling

    % convert the image to a true color image with the jet colormap.
    colorimg = ind2rgb(scaledimg,jet(256));

    % set hold on so we can show multiple plots / surfs in the figure.
    figure; hold on;

    % do a normal surface plot.
    surf(x,y,z,'edgecolor','none');

    % set a colormap for the surface
    colormap(gray);

    % desired z position of the image plane.
    imgzposition = -100;

    % plot the image plane using surf.
    surf([min_x max_x],[min_y max_y],repmat(imgzposition, [2 2]),...
        colorimg,'facecolor','texture')

    % set the view.
    view(45,30);
    % --- SCRIPT TAKEN FROM AN EXTERNAL SOURCE (END) ---

    % label the axes
    xlabel('x');
    ylabel('y');
    zlabel('z');

% %     img = imagesc(imgDiff); hold on;
% %     circle(imgSize/2,min(imgSize)/2,1000,'w--'); hold off;
%     set(gca,'XTick',[]);
%     set(gca,'YTick',[]);
%     
%     if nargin>2
%     	set(gca, 'CLim',range);
%     end
% %     clim = get(gca,'CLim');
% %     set(gca, 'CLim',[0 clim(2)]);
% 
%     axis square
%     cmap = colormap(hot(128));
%     cbh = colorbar;
%     set(get(cbh,'YLabel'),'String','Distance');
%     
%         surf(Z*ones(size(imgDiff)),imgDiff,'EdgeColor','none')
%     
%     % Converts and save the image to TIF if required
%     if nargin>3 && ~isempty(filename)
%         % I ranges from 0 - 1
%         I = []; %#ok<NASGU>
%         if isempty(range)
%             clim = get(gca,'CLim');
%             I=mat2gray(get(img,'cdata'),[clim(1) clim(2)]);
%         else
%             I=mat2gray(get(img,'cdata'),range);
%         end
% 
%         % Convert I to indices into the colormap,
%         %i.e map 0 -1 to 1 - size of colormap
%         IndI = ceil(I*size(cmap,1));
% 
%         imwrite(IndI,cmap,filename,'tif');
%     end
end

