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
% Contains the measurements made on a WingJ Structure. This class was
% initially referring directly to the wing pouch structure.
%
% Author: Thomas Schaffter (thomas.schaff...@gmail.com)
% Version: September 10, 2012
classdef Structure < handle
    
    properties(GetAccess = 'public', SetAccess = 'private')
        name = '';      % name of the structure
        age = '';       % age of the structure (e.g. 114h AEL)
        DV = [];        % D/V boundary
        AP = [];        % A/P boundary
        CD = [];        % structure center to point D
        CV = [];        % structure center to point V
        CA = [];        % structure center to point A
        CP = [];        % structure center to point P
        structure = []; % structure
        da = [];        % DA compartment
        dp = [];        % DP compartment
        va = [];        % VA compartment
        vp = [];        % VP compartment
    end
    
    % =====================================================================
	% PUBLIC METHODS
    
    methods
        % Constructor
        function obj = Structure(filename)
            if nargin > 0
                try
                    xmldoc = xmlread(filename);
                catch %#ok<CTCH>
                    error('ERROR: Failed to read XML file %s', filename);
                end

                root = xmldoc.getDocumentElement;
                obj.name = char(root.getAttribute('name'));
                obj.age = char(root.getAttribute('age'));

                % list all boundary elements
                boundaries = root.getElementsByTagName('boundary');
                % read boundaries
                obj.DV = read_boundary('D/V', boundaries);
                obj.AP = read_boundary('A/P', boundaries);
                obj.CD = read_boundary('CD_axis', boundaries);
                obj.CV = read_boundary('CV_axis', boundaries);
                obj.CA = read_boundary('CA_axis', boundaries);
                obj.CP = read_boundary('CP_axis', boundaries);

                % list all compartment elements
                compartments = root.getElementsByTagName('compartment');
                % read compartments
                obj.structure = read_compartment('structure', compartments); % replace pouch by structure
                obj.da = read_compartment('DA', compartments);
                obj.dp = read_compartment('DP', compartments);
                obj.va = read_compartment('VA', compartments);
                obj.vp = read_compartment('VP', compartments);
            end
        end
        
        % -----------------------------------------------------------------
        
        % Returns the 'value' and 'unit' of the given property.
        function [values units] = getProperty(obj, propertyName) %#ok<INUSL>
            
            % Optionally one can specify an addition of properties
            tokens = regexp(propertyName,'\+','split');
            
            % 'DV.length' becomes the cell array {'DV','length'}
            tokens = regexp(tokens,'\.','split');
            
            try
                values = 0;
                units = '';
                for p=1:length(tokens)
                    property = [];
                    t=tokens{p};
                    for i=1:length(t)
                        if isempty(property)
                            evalStr = ['[obj.' t{1} ']'];
                            property = eval(evalStr);
                        else
                            evalStr = ['[property.' t{i} ']'];
                            property = eval(evalStr);
                        end
                    end

                    if ~isempty(property)
                        values = values + [property.value];    % returned as array
                        units = {property.unit};    % returned as cell array
                    end
                end
            catch %#ok<CTCH>
                msg = ['Unknown property: ' propertyName];
                error('Structure:getProperty:badproperty',msg);
            end
        end
    end
end