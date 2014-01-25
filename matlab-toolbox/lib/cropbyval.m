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
% CROPBYVAL Crop the four borders of a given image or matrix IMG, whose
% elements only contain the given value VAL.
%
% Usage: croppedImage = cropbyval(img, val);
%
% Author: Thomas Schaffter (thomas.schaff...@gmail.com)
% Version: September 10, 2012
function [croppedImg left right top bottom] = cropbyval(img, val)

    croppedImg = img;

    % crop left margin
    A = sum(croppedImg);
    margin = find(A ~= val);
    margin = margin(1);
    croppedImg(:, 1:margin) = [];
    left = margin + 1;
    
    % crop right margin
    A = sum(croppedImg);
    margin = fliplr(find(A ~= val));
    margin = margin(1);
    croppedImg(:,margin:end) = [];
    right = left + margin - 2;

    % crop top margin
    A = sum(croppedImg,2);
    margin = find(A ~= val);
    margin = margin(1);
    croppedImg(1:margin,:) = [];
    top = margin + 1;

    % crop bottom margin
    A = sum(croppedImg,2);
    margin = flipud(find(A ~= val));
    margin = margin(1);
    croppedImg(margin:end,:) = [];
    bottom = top + margin - 2;
end