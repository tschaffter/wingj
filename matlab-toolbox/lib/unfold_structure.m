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
% UNFOLD_STRUCTURE Unfolds and shows the content of the given structure.
%
% UNFOLD_STRUCTURE(STRUCT, ROOT) shows recursively the content of the
% given structure STRUCT. ROOT is a string which must be set with the
% variable name of STRUCT. Clean and generic implementation which can be
% easily extended, e.g. for structures containing cells.
%
% Usage: unfold_structure(myStructure, 'myStructure');
%
% Author: Thomas Schaffter (thomas.schaff...@gmail.com)
% Version: June 13, 2011
function unfold_structure(struct, root)

    names = fieldnames(struct);
    for i=1:length(names)
        
        value = struct.(names{i});
        if isstruct(value)
            unfold_structure(value, [root '.' names{i}])
        else
            disp([root '.' names{i} ': ' num2str(value)]);
        end
    end
end