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
% This class implements a scroll to indicate where the image slice is
% located in the stack, for instance. This scroll is used by the class
% NucleiDetector when the images and/or movie are exported.
%
% Version: September 19, 2011
% Author: Thomas Schaffter (thomas.schaff...@gmail.com)

classdef ZScroll < handle

    properties(GetAccess = 'public', SetAccess = 'private')
        % X coordinate of the frame
        x = 0;
        % Y coordinate of the frame
        y = 0;
        % Frame width 
        frameWidth = 9;
        % Frmae height
        frameHeight = 60;
        % Scroll height
        scrollHeight = 8;
        % Frame and scroll color
        color = 'w';
        % Label height (10) + space between label and frame
        labelSpace = 30;
        
        % True if using the default scrollHeight
        useDefaultScrollHeight = true;
        % If true top->down, otherwise bottom->up
        topDown = true;
    end
    
    % =====================================================================
	% PRIVATE METHODS
    
    methods (Access = private)
        % Return the scroll Y location according to the given state
        function scrollY = computeScrollY(obj, percent)
           
            if obj.topDown
                if obj.useDefaultScrollHeight
                    scrollY = obj.y + obj.frameHeight*percent;
                else
                    scrollY = obj.y + (obj.frameHeight-obj.scrollHeight)*percent;
                end
            else
                if obj.useDefaultScrollHeight
                    scrollY = obj.y + obj.frameHeight - obj.scrollHeight - obj.frameHeight*percent;
                else
                    scrollY = obj.y + obj.frameHeight - obj.scrollHeight - (obj.frameHeight-obj.scrollHeight)*percent;
                end
            end
            scrollY = floor(scrollY);
        end
    end
    
    % =====================================================================
	% PUBLIC METHODS
    
    methods
        % Constructor
        function obj = ZScroll(numStates, topDown)
            if nargin > 0
                obj.scrollHeight = obj.frameHeight/numStates;
                obj.topDown = topDown;
            end
        end
        
        % -----------------------------------------------------------------
        
        % Draw the scroll on the current figure
        function draw(obj, percent)
            
            % draw frame
            rectangle('Position', [obj.x obj.y...
                obj.frameWidth obj.frameHeight],...
                'EdgeColor', obj.color, 'FaceColor', 'k');
            
            % draw scroll at the given location
            scrollY = obj.computeScrollY(percent);
            
            rectangle('Position', [obj.x scrollY...
                obj.frameWidth obj.scrollHeight],...
                'EdgeColor', obj.color, 'FaceColor', obj.color);
            
            % Write label 'z'
            labelX = obj.x;
            labelY = obj.y + obj.frameHeight + obj.labelSpace;
            text(labelX, labelY, 'Z', 'Color', obj.color);
        end
      
        % =================================================================
        % SETTERS AND GETTERS
        
        function setX(obj, x)
            obj.x = x;
        end
        
        function setY(obj, y)
            obj.y = y;
        end
        
        function setFrameWidth(obj, frameWidth)
            obj.frameWidth = frameWidth;
        end
        
        function setFrameHeight(obj, frameHeight)
            obj.frameHeight = frameHeight;
        end
        
        function setScrollHeight(obj, scrollHeight)
            obj.scrollHeight = scrollHeight;
            obj.useDefaultScrollHeight = false;
        end
        
        function setColor(obj, color)
            obj.color = color;
        end
    end
end