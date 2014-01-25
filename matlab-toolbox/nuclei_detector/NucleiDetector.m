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
% Identifies the nuclein in stacks of fluorescent confocal images (3D
% images).
%
% Several pre-processing filters are applied to every image before
% running the 3D watershed algorithm implemented in Matlab. See our paper
% for a visual representation of the output of every filters applied.
%
% In addition to count the number of nuclei, this class can export every
% slice of the stack where the nuclei are highlighted. A binary mask can
% be provided to indicate to the present algorithm where the nuclei must
% be identified. This class can also export a movie (avi; working fine on
% linux) using the images where the detected nuclei are labelled in
% different colors.
%
% Author: Thomas Schaffter (thomas.schaff...@gmail.com)
% Version: September 15, 2011
classdef NucleiDetector < handle
    
    properties(GetAccess = 'private', SetAccess = 'private')
        
        % Directory name where NucleiDetector files are saved
        directoryName = 'NucleiDetector';
        % Filename where the detected number of nuclei is saved
        numNucleiFilename = 'nuclei_num.txt';
        % Filename where minimum distances between nuclei are saved
        minNucleiDistancesFilename = 'nuclei_min_distances.txt'
%         % Index of the z-slices to use
%         sliceIndexes = [];
        
        % Truncated max number of nuclei to consider for computing min
        % distances
        minNucleiDistancesLimit = 500;
        % Directory name where each watershed slice is saved
        % (used to make the avi file)
        imagesDirectoryName = 'images';
        % Name of the watershed movie
        watershedVideoFilename = 'watershed.avi';
    end
    
    properties(GetAccess = 'public', SetAccess = 'private')
        
        % Image sequence
        images = [];
        % Structure binary mask
        mask = [];
        % Margins used to crop the image slice (if mask provided)
        % [left right top bottom]
        cropMargins = [];
        
        % Graysame image sequence
        GS3 = [];
        % B/W image sequence
        BW3 = [];
        % Watershed output
        W = [];
        
        % Number of nuclei detected
        numNuclei = 0;
        
        % statistics (elements area, centroid, pixel list, etc.)
        stats = [];
    end
    
    % =====================================================================
	% PRIVATE METHODS
    
    methods (Access = private)
        
        % Pre-process images before applying watershed
        function preProcess(obj)
            
            if isempty(obj.images)
                error('ERROR: obj.images is empty.');
            end
            
            % crop tightly around the mask (if procided)
            if ~isempty(obj.mask)
                [obj.mask,left,right,top,bottom] = cropbyval(obj.mask, 0);
                obj.cropMargins = [left right top bottom];
            end

            % for each slice
            for i=1:length(obj.images)
                
                J = obj.images{i};
                % force conversion to uint8
%                 if ~isa(J,'uint8')
%                     J = im2uint8(J);
%                 end
                
                % if a mask is provided, crop the slice accordingly
                if ~isempty(obj.mask)
                    % crop the image as done for the mask
                    J = J(top:bottom,left:right);
                    % apply the mask
                    J = immultiply(obj.mask/255, J);
                end
%                 if i==24
%                     figure
%                     imshow(J, 'Border', 'tight')
%                     save_tiff(gcf, '/home/tschaffter/watershed_pp_1_original_with_mask.tif');
%                 end
                
                % remove "salt and pepper" noise
                J = medfilt2(J,[2,2]);
                obj.GS3(:,:,i) = J;
%                 if i==24
%                     figure
%                     imshow(J, 'Border', 'tight')
%                     save_tiff(gcf, '/home/tschaffter/watershed_pp_2_noise_removal.tif');
%                 end
                % remove uneven illumination (top-hat filtering)
                J = imtophat(J, strel('disk', 8));
%                 if i==24
%                     figure
%                     imshow(J, 'Border', 'tight')
%                     save_tiff(gcf, '/home/tschaffter/watershed_pp_3_uneven_illumination_removal.tif');
%                 end
                % blur image (point-spread function)
                PSF = mexican_hat_filter(3, 1.7); % 3, 1.8
                J = imfilter(J, PSF, 'symmetric', 'conv');
%                 if i==24
%                     figure
%                     imshow(J, 'Border', 'tight')
%                     save_tiff(gcf, '/home/tschaffter/watershed_pp_4_mexican_hat_filtering.tif');
%                 end
                % suppress _local_ minima to avoid over-segmentation (H-minima transform)
                J = imhmin(J, 8);
%                 if i==24
%                     figure
%                     imshow(J, 'Border', 'tight')
%                     save_tiff(gcf, '/home/tschaffter/watershed_pp_5_local_minima_suppression.tif');
%                 end
                % adjust levels
                % corresponds to min/max 0 200
                J = imadjust(J, [0.1 1], [0 1]);
%                 if i==24
%                     figure
%                     imshow(J, 'Border', 'tight')
%                     save_tiff(gcf, '/home/tschaffter/watershed_pp_6_adjust_levels.tif');
%                 end
                % convert to binary image
                level = graythresh(J);
                BW = im2bw(J, level);
                obj.BW3(:,:,i) = BW;
%                 if i==24
%                     figure
%                     imshow(BW, 'Border', 'tight')
%                     save_tiff(gcf, '/home/tschaffter/watershed_pp_7_binary.tif');
%                 end

                clear J;
                clear BW;
                clear level;
            end
        end
        
        % -----------------------------------------------------------------
        
        % Run watershed in order to detect nuclei location
        function detectNuclei(obj)
            
            if isempty(obj.BW3)
                error('ERROR: obj.BW3 is empty.');
            end
            
            % compute distance
            D = -bwdist(~obj.BW3);
            D(~obj.BW3) = -Inf;
            
            % 3D watershed
            obj.W = watershed(D);
            % remove first region found by watershed (background)
            obj.W = obj.W-1;
            obj.W(obj.W<0) = 0;
            
            % count the nuclei = higher element number
            obj.numNuclei = max(max(max(obj.W)));     
            % get stats
            % 'PixelIdxList', 'PixelList', 'Area'
            obj.stats = regionprops(obj.W, obj.GS3, 'Centroid');
        end
    end
    
    % =====================================================================
	% PUBLIC METHODS
    
    methods
        
        % Constructor
        function obj = NucleiDetector(images)
            if nargin > 0
                obj.images = images;
            end
        end
        
        % -----------------------------------------------------------------
        
        % Set a mask
        function setMask(obj, mask)
            
            if isempty(obj.images)
                error('ERROR: obj.images must first be set.');
            end
            if sum(size(obj.images{1}) == size(mask)) ~= ndims(mask)
                error('ERROR: Mask dimensions don''t fit those of obj.images.');
            end
            % set mask
            obj.mask = mask;
        end
        
        % -----------------------------------------------------------------
        
        % Sets the indexes of the slices to consider. obj.images must have
        % already been loaded by the constructor. If indexes is empty, use
        % all the slices available.
        function setSliceIndexes(obj, indexes)
            
            if isempty(indexes)
                return
            end
            
            if (indexes(1) < 1)
                error(['ERROR: Minimum slice index ' num2str(indexes(1)) ' must be >= 1.']);
            elseif (indexes(1) > length(obj.images))
                error(['ERROR: Minimum slice index ' num2str(indexes(1)) ' must be <= ' num2str(length(obj.images)) '.']);
            elseif (indexes(end) < 1)
                error(['ERROR: Maximum slice index ' num2str(indexes(end)) ' must be >= 1.']);
            elseif (indexes(1) > length(obj.images))
                error(['ERROR: Maximum slice index ' num2str(indexes(end)) ' must be <= ' num2str(length(obj.images)) '.']);
            elseif (indexes(1) >= indexes(end))
                error(['ERROR: Minimum slice index ' num2str(indexes(1)) ' must be < ' num2str(indexes(end)) '.']);
            end
            
            disp('Truncating image stack for nuclei detection.');
            obj.images = obj.images(indexes);
        end
        
        % -----------------------------------------------------------------
        
        % Run the nuclei detection
        function run(obj)
            
            fprintf('Running nuclei detection\n');
            obj.preProcess();
            obj.detectNuclei();
        end
        
        % -----------------------------------------------------------------
        
        % Delete everything but results
        function detete(obj) %#ok<MANU>
            
            clear obj.images;
            clear obj.mask;
            clear obj.cropMargins;
            clear obj.GS3;
            clear obj.BW3;
            clear obj.W;
        end
        
        % -----------------------------------------------------------------
          
        % Compute minimum distances between two nuclei (for each nucleus)
        function D = computeMinNucleiDistances(obj, truncatedNumNuclei)
            
            if isempty(obj.stats)
                error('ERROR: obj.stats is empty.');
            end
            
            fprintf('Computing minimum nuclei distances\n');
            settings = WJSettings.getInstance;
            
            % compute distance matrix
            D = Inf*ones(truncatedNumNuclei, truncatedNumNuclei);
            % scale vector
            S = [settings.scale settings.scale settings.zScale]; 
            for i=1:truncatedNumNuclei
                for j=1:truncatedNumNuclei
                    if i~=j
                        % convert to [um]
                        C1 = obj.stats(i).Centroid .* S;
                        C2 = obj.stats(j).Centroid .* S;
                        % compute Euclidian distance
                        D(j,i) = pdist([C1; C2]);
                    end
                end
            end
            
            % keep only the distance with the closest nucleus neighbour
            D = min(D);
        end
        
        % -----------------------------------------------------------------
        
        % Save the number of detected nuclei to file (txt)
        function saveNumNuclei(obj, filename)
            
            if obj.numNuclei == 0
                fprintf('ERROR: numNuclei is 0. Does that make sense ?');
            end
            
            fid = fopen(filename, 'w');
            fprintf(fid, '%i\n', obj.numNuclei);
            fclose(fid);
        end
        
        % -----------------------------------------------------------------
        
        % Save watershed images and movie
        % IMPORTANT: Both dimensions of the images must be multiple of 8 px
        % otherwise the avi movie can be affected by a diagonal tearing.
        function saveWatershedMedia(obj, directory, useMask, fps)
            
            if isempty(obj.images)
                error('ERROR: obj.images is empty.')
            end
            if isempty(obj.W)
                error('ERROR: obj.W is empty.');
            end
            
            fprintf('Exporting watershed media (tif, avi)\n');

            % create folder to save watershed slices
            imagesDirectory = [directory obj.imagesDirectoryName filesep];
            if ~mkdir(imagesDirectory)
                error(['ERROR: Can not mkdir ' imagesDirectory]);
            end
            
            % open the stream to the avi file
            % Used in Matlab < 2012a
            aviobj = avifile([directory 'watershed.avi'], 'fps', fps, 'quality', 100);
            % Suggested by Matlab 2012b
            % EDIT: for now it appears that there is no way to make a video
            % without having to display the frames.
            %vwobj = VideoWriter([directory 'watershed.avi']);
            %vwobj.FrameRate = fps;
            %vwobj.Quality = 100;
            %open(vwobj);
            warning('off', 'Images:initSize:adjustingMag');

            % instantiate the z-scroll displayed on each slice
            numSlices = length(obj.images);
            imageHeight = size(obj.images{1}, 1);
            margin = 25;
            zscroll = ZScroll(numSlices, false);
            zscroll.setScrollHeight(8);
            zscroll.setX(margin);
            zscroll.setY(imageHeight-zscroll.frameHeight-zscroll.labelSpace-margin);
            
            % save each original slice with the detected nuclei labeled
            for i=1:numSlices

                figure('Name', ['Watershed slice ' num2str(i)],...
                    'Visible', 'off');
                J = obj.images{i};

                % crop original slice (if mask has been provided)
                % be sure that the width and height of the images
                % are multiple of 8
                if useMask && ~isempty(obj.mask)
                    validDims = (floor(size(obj.mask)/8)+1) * 8; % now multiples of 8
                    left = obj.cropMargins(1);
                    top = obj.cropMargins(3);
                    J = J(top:top+validDims(1)-1, left:left+validDims(2)-1);
                end

                % show original image
                imshow(J, 'Border', 'tight');

                % show elements labeled
                % zerocolor is white (elements labeled 0)
                WRGB = label2rgb(obj.W(:,:,i), 'jet', 'w', 'shuffle');

                % if WRGB dimensions different from J (mask used) and
                % if mask must not be used, place WRGB in a matrix that
                % has the same size than the original image
                overlay = 255*ones([size(J), 3]); % white image
                if useMask && ~isempty(obj.mask)
                    overlay(1:size(WRGB,1),1:size(WRGB,2),:) = WRGB;
                else
                    % place WRGB at the correct place
                    left = obj.cropMargins(1);
                    top = obj.cropMargins(3); 
                    overlay(top:top+size(WRGB,1)-1,left:left+size(WRGB,2)-1,:) = WRGB;
                end

                hold on;
                h = imshow(overlay, 'Border', 'tight');
                % [255 255 255] pixel opacity set to 0, otherwise 1
                alphaData = sum(overlay~=255,3)~=0;
                % set opacity of the elements labeled
                set(h, 'AlphaData', alphaData);
                % add scroll
                % XXX: comment next line to remove z-scroll bar
                zscroll.draw((i-1)/numSlices);
                % save image
                save_tiff(gcf, [imagesDirectory 'watershed_z' sprintf('%02.f',i-1) '.tif']);
                % add images to video
                aviobj = addframe(aviobj, gcf);
                %writeVideo(vwobj,gcf);
                
                close gcf;
                clear J;
                clear WRGB;
                clear overlay;
            end
            % close video stream
            aviobj = close(aviobj); %#ok<NASGU>
            %close(vwobj);
            warning('on', 'Images:initSize:adjustingMag');
            
            clear aviobj;
            %clear vwobj;
        end
        
        % -----------------------------------------------------------------
        
        % Save minimum distances between two nuclei (for each nucleus)
        function saveMinNucleiDistances(obj, filename)
            
            truncatedNumNuclei = min([obj.numNuclei obj.minNucleiDistancesLimit]);            
            D = obj.computeMinNucleiDistances(truncatedNumNuclei);
            % save to file
            dlmwrite(filename, D', 'delimiter', '\t', 'precision', 6);
        end
        
        % -----------------------------------------------------------------
        
        % Save all files
        function save(obj, directory, useMask, saveMedia)
            
            if nargin < 4
                saveMedia = false;
            end
            
            settings = WJSettings.getInstance();
            
            % remove old output directory (if it exists),
            folder = [directory obj.directoryName];
            try
                rmdir(folder, 's');
            catch err %#ok<NASGU>
                % nothing to do
            end
            % then re-create it
            try
                mkdir(folder);
            catch err %#ok<NASGU>
                % nothing to do
            end
            
            % save the number of detected nuclei to file
            try
                obj.saveNumNuclei([directory obj.directoryName filesep obj.numNucleiFilename]);
                fprintf('[x] Number of nuclei (txt)\n');
            catch err
                fprintf('ERROR: %s\n', err.message);
                fprintf('[ ] Number of nuclei (txt)\n');
            end
            
            % save the minimum distances between nuclei to file
%             try
%                 obj.saveMinNucleiDistances([directory obj.directoryName '/' obj.minNucleiDistancesFilename]);
%                 fprintf('[x] Minimum nuclei distances (txt)\n');
%             catch err
%                 fprintf('ERROR: %s\n', err.message);
%                 fprintf('[ ] Minimum nuclei distances (txt)\n');
%             end
            
            % save watershed images and movie
            if saveMedia
                try
                    fps = settings.watershedMovieFps;
                    obj.saveWatershedMedia([directory obj.directoryName filesep], useMask, fps)
                    fprintf('[x] Watershed images (tif)\n');
                    fprintf('[x] Watershed movie (avi)\n');
                catch err
                    fprintf('ERROR: %s\n', err.message);
                    fprintf('[ ] Watershed images (tif)\n');
                    fprintf('[ ] Watershed movie (avi)\n');
                end
            end
        end
    end
end