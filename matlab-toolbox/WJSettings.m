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
% This class contains the general settings (Singleton pattern).
%
% Author: Thomas Schaffter (thomas.schaff...@gmail.com)
% Version: September 10, 2012
classdef WJSettings < handle
    
    properties(GetAccess = 'public', SetAccess = 'private')
        
        % =================================================================
        % See WingJ User Manual for file naming convention
        
        % =================================================================
        % GENERAL
        
        % In each experiment, name of the folder containing the images.
        imagesDirectoryName = 'images';
        % In each experiment, name of the folder containing the WingJ files.
        wingjDirectoryName = 'WingJ'; % 'WingJ'
        % In each WingJ folder, name of the folder containing the output Matlab files.
        matlabDirectoryName = 'Matlab';
        % Name of the folder containing the output of the nuclei detector
        % (in the Matlab folder).
        nucleiDetectorDirectoryName = 'NucleiDetector';
        
        % Slice prefix in image filenames.
        slicePrefix = '_z';
        % Channel prefix in image filenames.
        channelPrefix = '_ch';

        % Suffix for log file.
        logSuffix = '_log.txt';
        
        % Defines which image channel to load.
        numChannels = 3;
        
        % Number of images per line in a picture galery.
        numGaleryPicturesPerLine = 4;
        
        % =================================================================
        % STRUCTURE DETECTION
        
        % Name of the channel containing the structure (see User Manual).
        structureChannelName = 'wg-ptcAB';
        
        % Suffix for wing structure measurements dataset.
        structurePropertiesSuffix = 'structure_measurements.xml'; % replace with structure_measurements.xml
        % Suffix for the binary mask of the wing structure.
        structureBinaryMaskSuffix = 'structure_mask.tif';
        % Suffix for structure preview with detected and labeled compartments.
        structurePreviewSuffix = 'structure.tif';
        
        % Number of subplots per line for boxplotting/barplotting structure properties.
        numStructurePropertySubplotsPerLine = 2;
        
        % =================================================================
        % EXPRESSION QUANTIFICATION
        
        % String to identify expression profiles (default: expression_profile).
        expressionProfileStr = 'expression_profile'; % expression_1D
        
        % String to identify expression maps (default: expression_map).
        expressionMapStr = 'expression_map'; % expression_2D
        
        % Normalize expression profile (divides values by 255.)
        expressionProfileNormalize = true;
        
        % Number of samples when computing mean 1D expression profiles (interpolation).
        expressionProfileNumInterpSamples = 200;
        
        % =================================================================
        % NUCLEI DETECTOR
        
        % Filename of the file containing the number of nuclei.
        numNucleiFilename = 'nuclei_num.txt';
        
        % Watershed movie file FPS (frame per second).
        watershedMovieFps = 2;
        
        % 1 px = X um (only valid for x and y)
        % IMPORTANT: Structure data exported by WingJ are already in um.
        scale = 0.37841796604334377;
        % Z distance between two image slices (default: 1um)
        zScale = 1;
    end
    
    % =====================================================================
	% PRIVATE METHODS
    
    methods (Access = private)
        % Default constructor
        function obj = WJSettings
        end
    end
    
    % =====================================================================
	% PUBLIC METHODS
    
    methods (Static)
        % Get instance
        function instance = getInstance()
            persistent instance_;
            if isempty(instance_)
                instance_ = WJSettings;
            end
            instance = instance_;
        end
    end
    
    % -----------------------------------------------------------------
    
    methods
        % Get number of channels
        function numChannels = getNumChannels(obj)
            numChannels = obj.numChannels;
        end
        
        % -----------------------------------------------------------------
        
        % Return true if channel index is active (i.e. must be loaded)
        function b = isActiveChannel(obj, index)
            b = obj.channels(index);
        end
    end
end