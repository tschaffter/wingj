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
% FIND_TOKENS Returns true if all the tokens are found in a given string.
%
% The function returns true if ALL given tokens are found in the given
% string.
%
% Example: find_tokens('There is an apple on the tree.',{'apple','tree'})
% return true.
%
% It is also possible to specify keywords that must NOT be in the string.
% Use '-' before a keyword to distinguish it from keywords that must be
% included in the string. If at least one 'negative' keyword is found,
% the function returns false.
%
% Example: find_tokens('There is an apple on the tree.',{'-apple','tree'})
% returns false because 'apple' is in the string.
%
% Author: Thomas Schaffter (thomas.schaff...@gmail.com)
% Version: September 10, 2012
function found = find_tokens(str, tokens)

    % build a vector where 1 means positive token, otherwise 0
    tokensSigns = zeros(length(tokens),1);
    for i=1:length(tokens)
        tokensSigns(i) = ~strcmp(tokens{i}(1),'-');
    end
    positiveTokens = tokens(tokensSigns==1);
    negativeTokens = tokens(tokensSigns==0);

    found = false;
    count = 0;
    % first check if there is one negative tokens
    for n=1:length(negativeTokens)
        if strfind(str,negativeTokens{n}(2:end)) % discard the '-'
            return
        end
    end
    % only if no positve tokens were found
    for p=1:length(positiveTokens)
       if strfind(str,positiveTokens{p})
            count = count+1;
        end
    end
    if count == length(positiveTokens)
        found = true;
        return
    end
end