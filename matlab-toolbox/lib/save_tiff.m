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
% SAVE_TIFF Saves the given figure in TIFF format (image dimension conserved).
%
% Author: Thomas Schaffter (thomas.schaff...@gmail.com)
% Version: September 17, 2011
function save_tiff(fh, filename)

    oldscreenunits = get(fh, 'Units');
    oldpaperunits = get(fh, 'PaperUnits');
    oldpaperpos = get(fh, 'PaperPosition');
    set(fh, 'Units', 'pixels');
    scrpos = get(fh, 'Position');
    newpos = scrpos/100;
    set(fh, 'PaperUnits', 'inches',...
        'PaperPosition', newpos)
    print('-dtiff', filename, '-r100');
    drawnow
    set(fh, 'Units', oldscreenunits,...
        'PaperUnits', oldpaperunits,...
        'PaperPosition', oldpaperpos)
end