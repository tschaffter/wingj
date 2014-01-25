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
% MEXICAN_HAT_FILTER Implements a Mexican Hat filter.
%
% Version: September 16, 2011
% Author: Aitana Neves
function h = mexican_hat_filter(siz,sig)

if nargin<1
   clear all; clc;
   siz = 10;
   sig = 2;
end

n1 = linspace(-siz/2,siz/2,siz);
n2 = n1;

sumh = 0;
for i=1:siz
    for j=1:siz
        
            %- Gaussian filter : H = fspecial('gaussian',[siz siz],sig);
            %- h(i,j) = exp( -(n1(i)^2+n2(j)^2)/(2*sig^2) );
            
            %- Mexihat filter
            h(i,j) = (2*pi^(-0.25)/sqrt(3))*(1-(n1(i)^2+n2(j)^2)/(sig^2))*exp(-(n1(i)^2+n2(j)^2)/(2*sig^2));
            %h(i,j) = (2*pi^(-0.25)/sqrt(3))*(1-(n1(i)^2+n2(j)^2))*exp(-(n1(i)^2+n2(j)^2)/(2*sig^2));
            sumh = sumh + h(i,j);
        
    end
end
h = h/abs(sumh);
hmin = min(min(h));
h = h - hmin*0.8;

if nargin<1
    figure; 
    mesh(h);
    colormap(hsv);
end
