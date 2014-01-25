function r = corr2_disc(varargin)
%CORR2 2-D correlation coefficient.
%   R = CORR2(A,B) computes the correlation coefficient between A
%   and B, where A and B are matrices or vectors of the same size.
%
%   Class Support
%   -------------
%   A and B can be numeric or logical. 
%   R is a scalar double.
%
%   Example
%   -------
%   I = imread('pout.tif');
%   J = medfilt2(I);
%   R = corr2(I,J)
%
%   See also CORRCOEF, STD2.

%   Copyright 1992-2005 The MathWorks, Inc.
%   $Revision: 5.18.4.5 $  $Date: 2006/06/15 20:08:33 $

%   Modified by Thomas Schaffter to discard part of the image ouside of a
%   disc centered on the image and whose radius is equal to min(w,h)/2
%   where w=width and h=height of the image in pixels (actually the image
%   must be squared: no additional test are done).
%
%   Author: Thomas Schaffter (thomas.schaff...@gmail.com)
%   Version: September 10, 2012

[a,b] = ParseInputs(varargin{:});

% create a binary disc mask (a and b have the same size/tested)
mask = create_disc_mask(size(a));

% stretch the data
a_v = a(:);
b_v = b(:);
mask_v = mask(:) > 0;

% discard data outside of the mask
a_v(~mask_v) = [];
b_v(~mask_v) = [];

a_v = a_v - mean(a_v);
b_v = b_v - mean(b_v);
r = sum(sum(a_v.*b_v))/sqrt(sum(sum(a_v.*a_v))*sum(sum(b_v.*b_v)));

% a = a - mean2(a);
% b = b - mean2(b);
% r = sum(sum(a.*b))/sqrt(sum(sum(a.*a))*sum(sum(b.*b)));

%--------------------------------------------------------
function [A,B] = ParseInputs(varargin)

iptchecknargin(2,2,nargin, mfilename);

A = varargin{1};
B = varargin{2};

iptcheckinput(A, {'logical' 'numeric'}, {'real'}, mfilename, 'A', 1);
iptcheckinput(B, {'logical' 'numeric'}, {'real'}, mfilename, 'B', 2);

if any(size(A)~=size(B))
    messageId = 'Images:corr2:notSameSize';
    message1 = 'A and B must be the same size.';
    error(messageId, '%s', message1);
end

if (~isa(A,'double'))
    A = double(A);
end

if (~isa(B,'double'))
    B = double(B);
end