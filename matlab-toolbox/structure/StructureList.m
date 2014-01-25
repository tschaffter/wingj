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
% Represents a list of Structure objects (structure measurements).
%
% Author: Thomas Schaffter (thomas.schaff...@gmail.com)
% Version: September 10, 2012
classdef StructureList < handle
    
    properties(GetAccess = 'private', SetAccess = 'private')
        structures = [];     % structure references
    end
    
    methods
        
        % Constructor.
        function obj = StructureList(n)
            obj.structures = Structure.empty(n,0);
        end
        
        % -----------------------------------------------------------------
        
        % Adds a structure to the list.
        function obj = set(obj, i, structure)
            obj.structures(i) = structure;
        end
        
        % -----------------------------------------------------------------
        
        % Returns an array containing all the experiment/structure names.
        function names = getExperimentNames(obj)
            names = {obj.structures.name};
        end
        
        % =================================================================
        % GETTERS BOUNDARIES
        
        % Returns an array containing all the D/V boundary lengths.
        function values = getDvBoundaryLengths(obj)
            dv_boundaries = [obj.structures.DV];
            lengths = [dv_boundaries.length];
            values = [lengths.value];
        end
        
        % -----------------------------------------------------------------
        
        % Returns an array containing all the D/V boundary lengths.
        function values = getApBoundaryLengths(obj)
            dv_boundaries = [obj.structures.AP];
            lengths = [dv_boundaries.length];
            values = [lengths.value];
        end
        
        % =================================================================
        % GETTERS BRANCHES
        
        % Returns an array containing all the CD branch lengths.
        function values = getCdBranchLengths(obj)
            dv_boundaries = [obj.structures.CD];
            lengths = [dv_boundaries.length];
            values = [lengths.value];
        end
        
        % -----------------------------------------------------------------
        
        % Returns an array containing all the CV branch lengths.
        function values = getCvBranchLengths(obj)
            dv_boundaries = [obj.structures.CV];
            lengths = [dv_boundaries.length];
            values = [lengths.value];
        end
        
        % -----------------------------------------------------------------
        
        % Returns an array containing all the CA branch lengths.
        function values = getCaBranchLengths(obj)
            dv_boundaries = [obj.structures.CA];
            lengths = [dv_boundaries.length];
            values = [lengths.value];
        end
        
        % -----------------------------------------------------------------
        
        % Returns an array containing all the CP branch lengths.
        function values = getCpBranchLengths(obj)
            dv_boundaries = [obj.structures.CP];
            lengths = [dv_boundaries.length];
            values = [lengths.value];
        end
        
        % =================================================================
        % GETTERS STRUCTURE
        
        % Return an array containing all the structure perimeters.
        function values = getStructurePerimeters(obj)
            structs = [obj.structures.structure];
            perimeters = [structs.perimeter];
            values = [perimeters.value];
        end
        
        % -----------------------------------------------------------------
        
        % Returns an array containing all the structure areas.
        function values = getPouchAreas(obj)
            structs = [obj.structures.structure];
            areas = [structs.area];
            values = [areas.value];
        end
        
        % =================================================================
        % GETTERS COMPARTMENTS
        
        % Returns an array containing all the DA compartment perimeters.
        function values = getDaCompartmentPerimeters(obj)
            compartments = [obj.structures.da];
            perimeters = [compartments.perimeter];
            values = [perimeters.value];
        end
        
        % -----------------------------------------------------------------
        
        % Returns an array containing all the DA compartment areas.
        function values = getDaCompartmentAreas(obj)
            compartments = [obj.structures.da];
            areas = [compartments.area];
            values = [areas.value];
        end
        
        % -----------------------------------------------------------------
        
        % Returns an array containing all the DP compartment perimeters.
        function values = getDpCompartmentPerimeters(obj)
            compartments = [obj.structures.dp];
            perimeters = [compartments.perimeter];
            values = [perimeters.value];
        end
        
        % -----------------------------------------------------------------
        
        % Returns an array containing all the DP compartment areas.
        function values = getDpCompartmentAreas(obj)
            compartments = [obj.structures.dp];
            areas = [compartments.area];
            values = [areas.value];
        end
        
        % -----------------------------------------------------------------
        
        % Returns an array containing all the VA compartment perimeters.
        function values = getVaCompartmentPerimeters(obj)
            compartments = [obj.structures.va];
            perimeters = [compartments.perimeter];
            values = [perimeters.value];
        end
        
        % -----------------------------------------------------------------
        
        % Returns an array containing all the VA compartment areas.
        function values = getVaCompartmentAreas(obj)
            compartments = [obj.structures.va];
            areas = [compartments.area];
            values = [areas.value];
        end
        
        % -----------------------------------------------------------------
        
        % Returns an array containing all the VP compartment perimeters.
        function values = getVpCompartmentPerimeters(obj)
            compartments = [obj.structures.vp];
            perimeters = [compartments.perimeter];
            values = [perimeters.value];
        end
        
        % -----------------------------------------------------------------
        
        % Returns an array containing all the VP compartment areas.
        function values = getVpCompartmentAreas(obj)
            compartments = [obj.structures.vp];
            areas = [compartments.area];
            values = [areas.value];
        end
    end
end

% [Vehicles(:).Is_Active] = deal( true );