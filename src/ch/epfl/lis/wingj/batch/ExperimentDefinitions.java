/*
Copyright (c) 2010-2013 Thomas Schaffter & Ricard Delgado-Gonzalo

We release this software open source under a Creative Commons Attribution
-NonCommercial 3.0 Unported License. Please cite the papers listed on 
http://lis.epfl.ch/wingj when using WingJ in your publication.

For commercial use, please contact Thomas Schaffter 
(thomas.schaff...@gmail.com).

A brief description of the license is available at 
http://creativecommons.org/licenses/by-nc/3.0/ and the full license at 
http://creativecommons.org/licenses/by-nc/3.0/legalcode.

The above copyright notice and this permission notice shall be included 
in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package ch.epfl.lis.wingj.batch;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains definitions of experiments for batch processing.
 * <p>
 * The methods below have been written before the introduction of
 * the slice dataset to WingJ. To enable batch processing, the experiments
 * below are created from an experiment directory and a liste of Gene
 * objects which correspond each to a gene/channel/marker, i.e. to
 * a specific stack of images. Each gene is defined by a name (e.g. "pmadAB"),
 * a selection tag for selecting the images part of the stack (e.g. "ch00")
 * and optionally a minimum and maximum slice indexes.
 * <p>
 * Now that the slice dataset have been introduced, these definitions
 * are not required anymore because this dataset contains all the
 * information required to load the stack of images (also includes
 * the min and max slice indexes to consider). See ExperimentBatch
 * for a concrete example of how to add experiments for batch processing.
 * 
 * @see Gene
 * @see ExperimentBatch
 * 
 * @version December 3, 2012
 * 
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 */
public class ExperimentDefinitions {
	
	public static List<ExperimentBatch> getAggExperimentBatches() throws Exception {
		
		List<ExperimentBatch> batches = new ArrayList<ExperimentBatch>();
		List<Experiment> experimentsForAggStructureModel = null;
		List<Experiment> experimentsForExpressionQuantification = null;
		List<String> rootDirectories = new ArrayList<String>();
		
		// ============================================================================
		// wt80h structures
		
		rootDirectories.clear();
		rootDirectories.add("/mnt/extra/wingj_benchmarks/20100716_pmadAB_brkAB_wg-ptcAB_78-79H/");
		rootDirectories.add("/mnt/extra/wingj_benchmarks/20101129_salAB_dadGFP_wg-ptcAB_79-80H/");
		rootDirectories.add("/mnt/extra/wingj_benchmarks/20110302_brkAB_ombAB_wg-ptcAB_79-80H/");
		
		experimentsForAggStructureModel = new ArrayList<Experiment>();
		for (int i = 0; i < 2; i++)
			experimentsForAggStructureModel.addAll(ExperimentBatch.getExperiments(rootDirectories.get(i)));
		
		for (int i = 0; i < rootDirectories.size(); i++) {
			experimentsForExpressionQuantification = new ArrayList<Experiment>();
			experimentsForExpressionQuantification.addAll(ExperimentBatch.getExperiments(rootDirectories.get(i)));
			batches.add(new ExperimentBatch(experimentsForAggStructureModel, experimentsForExpressionQuantification));
		}
		
		// ============================================================================
		// wt90h structures
		
//		rootDirectories.clear();
//		rootDirectories.add("/mnt/extra/wingj_benchmarks/20100716_pmadAB_brkAB_wg-ptcAB_90,5-91,5H/");
//		rootDirectories.add("/mnt/extra/wingj_benchmarks/20101129_salAB_dadGFP_wg-ptcAB_89-90H/");
//		rootDirectories.add("/mnt/extra/wingj_benchmarks/20110302_brkAB_ombAB_wg-ptcAB_87-88H/");
//		
//		experimentsForAggStructureModel = new ArrayList<Experiment>();
//		for (int i = 0; i < 2; i++)
//			experimentsForAggStructureModel.addAll(ExperimentBatch.getExperiments(rootDirectories.get(i)));
//		
//		for (int i = 0; i < rootDirectories.size(); i++) {
//			experimentsForExpressionQuantification = new ArrayList<Experiment>();
//			experimentsForExpressionQuantification.addAll(ExperimentBatch.getExperiments(rootDirectories.get(i)));
//			batches.add(new ExperimentBatch(experimentsForAggStructureModel, experimentsForExpressionQuantification));
//		}
		
		// ============================================================================
		// wt100h structures
		
//		rootDirectories.clear();
//		rootDirectories.add("/mnt/extra/wingj_benchmarks/20100716_pmadAB_brkAB_wg-ptcAB_99-100H/");
//		rootDirectories.add("/mnt/extra/wingj_benchmarks/20101129_salAB_dadGFP_wg-ptcAB_100-101H/");
//		rootDirectories.add("/mnt/extra/wingj_benchmarks/20110302_brkAB_ombAB_wg-ptcAB_100-101H/");
//		
//		experimentsForAggStructureModel = new ArrayList<Experiment>();
//		for (int i = 0; i < 2; i++)
//			experimentsForAggStructureModel.addAll(ExperimentBatch.getExperiments(rootDirectories.get(i)));
//		
//		for (int i = 0; i < rootDirectories.size(); i++) {
//			experimentsForExpressionQuantification = new ArrayList<Experiment>();
//			experimentsForExpressionQuantification.addAll(ExperimentBatch.getExperiments(rootDirectories.get(i)));
//			batches.add(new ExperimentBatch(experimentsForAggStructureModel, experimentsForExpressionQuantification));
//		}
		
		// ============================================================================
		// wt110h structures

//		rootDirectories.clear();
//		rootDirectories.add("/mnt/extra/wingj_benchmarks/20100716_pmadAB_brkAB_wg-ptcAB_110,5-111,5H/");
//		rootDirectories.add("/mnt/extra/wingj_benchmarks/20101129_salAB_dadGFP_wg-ptcAB_110,5-111,5H/");
//		rootDirectories.add("/mnt/extra/wingj_benchmarks/20110302_brkAB_ombAB_wg-ptcAB_111-112H/");
//		
//		experimentsForAggStructureModel = new ArrayList<Experiment>();
//		for (int i = 0; i < 2; i++)
//			experimentsForAggStructureModel.addAll(ExperimentBatch.getExperiments(rootDirectories.get(i)));
//		
//		for (int i = 0; i < rootDirectories.size(); i++) {
//			experimentsForExpressionQuantification = new ArrayList<Experiment>();
//			experimentsForExpressionQuantification.addAll(ExperimentBatch.getExperiments(rootDirectories.get(i)));
//			batches.add(new ExperimentBatch(experimentsForAggStructureModel, experimentsForExpressionQuantification));
//		}
		
		// ============================================================================
		// pent80h structures

//		rootDirectories.clear();
//		rootDirectories.add("/mnt/extra/wingj_benchmarks/20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_78-79H/");
//		rootDirectories.add("/mnt/extra/wingj_benchmarks/20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_79-80H/");
//		rootDirectories.add("/mnt/extra/wingj_benchmarks/20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_79-80H/");
//		
//		experimentsForAggStructureModel = new ArrayList<Experiment>();
//		for (int i = 0; i < 2; i++)
//			experimentsForAggStructureModel.addAll(ExperimentBatch.getExperiments(rootDirectories.get(i)));
//		
//		for (int i = 0; i < rootDirectories.size(); i++) {
//			experimentsForExpressionQuantification = new ArrayList<Experiment>();
//			experimentsForExpressionQuantification.addAll(ExperimentBatch.getExperiments(rootDirectories.get(i)));
//			batches.add(new ExperimentBatch(experimentsForAggStructureModel, experimentsForExpressionQuantification));
//		}
		
		// ============================================================================
		// pent90h structures

//		rootDirectories.clear();
//		rootDirectories.add("/mnt/extra/wingj_benchmarks/20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_90-91H/");
//		rootDirectories.add("/mnt/extra/wingj_benchmarks/20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_90-91H/");
//		rootDirectories.add("/mnt/extra/wingj_benchmarks/20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_87-88H/");
//		
//		experimentsForAggStructureModel = new ArrayList<Experiment>();
//		for (int i = 0; i < 2; i++)
//			experimentsForAggStructureModel.addAll(ExperimentBatch.getExperiments(rootDirectories.get(i)));
//		
//		for (int i = 0; i < rootDirectories.size(); i++) {
//			experimentsForExpressionQuantification = new ArrayList<Experiment>();
//			experimentsForExpressionQuantification.addAll(ExperimentBatch.getExperiments(rootDirectories.get(i)));
//			batches.add(new ExperimentBatch(experimentsForAggStructureModel, experimentsForExpressionQuantification));
//		}
		
		// ============================================================================
		// pent100h structures

//		rootDirectories.clear();
//		rootDirectories.add("/mnt/extra/wingj_benchmarks/20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_99-100H/");
//		rootDirectories.add("/mnt/extra/wingj_benchmarks/20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_99-101H/");
//		rootDirectories.add("/mnt/extra/wingj_benchmarks/20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_100-101H/");
//		
//		experimentsForAggStructureModel = new ArrayList<Experiment>();
//		for (int i = 0; i < 2; i++)
//			experimentsForAggStructureModel.addAll(ExperimentBatch.getExperiments(rootDirectories.get(i)));
//		
//		for (int i = 0; i < rootDirectories.size(); i++) {
//			experimentsForExpressionQuantification = new ArrayList<Experiment>();
//			experimentsForExpressionQuantification.addAll(ExperimentBatch.getExperiments(rootDirectories.get(i)));
//			batches.add(new ExperimentBatch(experimentsForAggStructureModel, experimentsForExpressionQuantification));
//		}
		
		// ============================================================================
		// pent110h structures

		rootDirectories.clear();
		rootDirectories.add("/mnt/extra/wingj_benchmarks/20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_110,5-111,5H/");
		rootDirectories.add("/mnt/extra/wingj_benchmarks/20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_114-115H/");
		rootDirectories.add("/mnt/extra/wingj_benchmarks/20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_109-110.5H/");
		
		experimentsForAggStructureModel = new ArrayList<Experiment>();
		for (int i = 0; i < 2; i++)
			experimentsForAggStructureModel.addAll(ExperimentBatch.getExperiments(rootDirectories.get(i)));
		
		for (int i = 0; i < rootDirectories.size(); i++) {
			experimentsForExpressionQuantification = new ArrayList<Experiment>();
			experimentsForExpressionQuantification.addAll(ExperimentBatch.getExperiments(rootDirectories.get(i)));
			batches.add(new ExperimentBatch(experimentsForAggStructureModel, experimentsForExpressionQuantification));
		}
		
		return batches;
	}
	
	// ============================================================================
	// EXPERIMENT DEFINITIONS BEFORE THE INTRODUCTION OF THE SLICE DATASET
	
	public static List<Experiment> getWt80hPmadBrkWgPtc() {
		
		List<Experiment> experiments = new ArrayList<Experiment>();
		Experiment e = null;
		
		// settings
		Gene pmadAB = new Gene("pmadAB", "ch00");
		Gene brkAB = new Gene("brkAB", "ch01");
		Gene wgPtcAB = new Gene("wg-ptcAB", "ch02");
		String rootDirectory = "/mnt/extra/wingj_benchmarks/20100716_pmadAB_brkAB_wg-ptcAB_78-79H/";
		
		e = new Experiment(rootDirectory + "20100716_pmadAB_brkAB_wg-ptcAB_78-79H_M_1/");
		e.addGene(pmadAB); e.addGene(brkAB, 1, 16); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20100716_pmadAB_brkAB_wg-ptcAB_78-79H_M_3/");
		e.addGene(pmadAB); e.addGene(brkAB, 1, 16); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20100716_pmadAB_brkAB_wg-ptcAB_78-79H_M_4/");
		e.addGene(pmadAB); e.addGene(brkAB, 14, 31); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20100716_pmadAB_brkAB_wg-ptcAB_78-79H_M_5/");
		e.addGene(pmadAB); e.addGene(brkAB, 1, 18); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20100716_pmadAB_brkAB_wg-ptcAB_78-79H_M_8/");
		e.addGene(pmadAB); e.addGene(brkAB, 1, 16); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20100716_pmadAB_brkAB_wg-ptcAB_78-79H_M_9/");
		e.addGene(pmadAB); e.addGene(brkAB, 14, 31); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20100716_pmadAB_brkAB_wg-ptcAB_78-79H_M_10/");
		e.addGene(pmadAB); e.addGene(brkAB, 16, 28); e.addGene(wgPtcAB);
		experiments.add(e);
		
		return experiments;
	}
	
	// ----------------------------------------------------------------------------
	
	public static List<Experiment> getWt90hPmadBrkWgPtc() {
		
		List<Experiment> experiments = new ArrayList<Experiment>();
		Experiment e = null;
		
		// settings
		Gene pmadAB = new Gene("pmadAB", "ch00");
		Gene brkAB = new Gene("brkAB", "ch01");
		Gene wgPtcAB = new Gene("wg-ptcAB", "ch02");
		String rootDirectory = "/mnt/extra/wingj_benchmarks/20100716_pmadAB_brkAB_wg-ptcAB_90,5-91,5H/";
		
		e = new Experiment(rootDirectory+ "20100716_pmadAB_brkAB_wg-ptcAB_90,5-91,5H_M_1/");
		e.addGene(pmadAB); e.addGene(brkAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20100716_pmadAB_brkAB_wg-ptcAB_90,5-91,5H_M_2/");
		e.addGene(pmadAB); e.addGene(brkAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20100716_pmadAB_brkAB_wg-ptcAB_90,5-91,5H_M_3/");
		e.addGene(pmadAB); e.addGene(brkAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20100716_pmadAB_brkAB_wg-ptcAB_90,5-91,5H_M_4/");
		e.addGene(pmadAB); e.addGene(brkAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20100716_pmadAB_brkAB_wg-ptcAB_90,5-91,5H_M_5/");
		e.addGene(pmadAB); e.addGene(brkAB, 1, 20); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20100716_pmadAB_brkAB_wg-ptcAB_90,5-91,5H_M_6/");
		e.addGene(pmadAB); e.addGene(brkAB, 1, 20); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20100716_pmadAB_brkAB_wg-ptcAB_90,5-91,5H_M_7/");
		e.addGene(pmadAB); e.addGene(brkAB, 1, 16); e.addGene(wgPtcAB);
		experiments.add(e);
		
		return experiments;
	}
	
	// ----------------------------------------------------------------------------
	
	public static List<Experiment> getWt100hPmadBrkWgPtc() {
		
		List<Experiment> experiments = new ArrayList<Experiment>();
		Experiment e = null;
		
		// settings
		Gene pmadAB = new Gene("pmadAB", "ch00");
		Gene brkAB = new Gene("brkAB", "ch01");
		Gene wgPtcAB = new Gene("wg-ptcAB", "ch02");
		String rootDirectory = "/mnt/extra/wingj_benchmarks/20100716_pmadAB_brkAB_wg-ptcAB_99-100H/";
		
		e = new Experiment(rootDirectory+ "20100716_pmadAB_brkAB_wg-ptcAB_99-100H_M_1/");
		e.addGene(pmadAB); e.addGene(brkAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20100716_pmadAB_brkAB_wg-ptcAB_99-100H_M_2/");
		e.addGene(pmadAB); e.addGene(brkAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20100716_pmadAB_brkAB_wg-ptcAB_99-100H_M_3/");
		e.addGene(pmadAB); e.addGene(brkAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20100716_pmadAB_brkAB_wg-ptcAB_99-100H_M_4/");
		e.addGene(pmadAB); e.addGene(brkAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20100716_pmadAB_brkAB_wg-ptcAB_99-100H_M_5/");
		e.addGene(pmadAB); e.addGene(brkAB, 10, 33); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20100716_pmadAB_brkAB_wg-ptcAB_99-100H_M_6/");
		e.addGene(pmadAB); e.addGene(brkAB, 1, 28); e.addGene(wgPtcAB);
		experiments.add(e);
		
		return experiments;
	}
	
	// ----------------------------------------------------------------------------
	
	public static List<Experiment> getWt110hPmadBrkWgPtc() {
		
		List<Experiment> experiments = new ArrayList<Experiment>();
		Experiment e = null;
		
		// settings
		Gene pmadAB = new Gene("pmadAB", "ch00");
		Gene brkAB = new Gene("brkAB", "ch01");
		Gene wgPtcAB = new Gene("wg-ptcAB", "ch02");
		String rootDirectory = "/mnt/extra/wingj_benchmarks/20100716_pmadAB_brkAB_wg-ptcAB_110,5-111,5H/";
		
		e = new Experiment(rootDirectory+ "20100716_pmadAB_brkAB_wg-ptcAB_110,5-111,5H_M_1/");
		e.addGene(pmadAB); e.addGene(brkAB); e.addGene(wgPtcAB);
		experiments.add(e);

		e = new Experiment(rootDirectory+ "20100716_pmadAB_brkAB_wg-ptcAB_110,5-111,5H_M_2/");
		e.addGene(pmadAB); e.addGene(brkAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory+ "20100716_pmadAB_brkAB_wg-ptcAB_110,5-111,5H_M_3/");
		e.addGene(pmadAB); e.addGene(brkAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory+ "20100716_pmadAB_brkAB_wg-ptcAB_110,5-111,5H_M_4/");
		e.addGene(pmadAB); e.addGene(brkAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory+ "20100716_pmadAB_brkAB_wg-ptcAB_110,5-111,5H_M_5/");
		e.addGene(pmadAB); e.addGene(brkAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory+ "20100716_pmadAB_brkAB_wg-ptcAB_110,5-111,5H_M_6/");
		e.addGene(pmadAB); e.addGene(brkAB, 12, 38); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory+ "20100716_pmadAB_brkAB_wg-ptcAB_110,5-111,5H_M_7/");
		e.addGene(pmadAB); e.addGene(brkAB, 10, 39); e.addGene(wgPtcAB);
		experiments.add(e);
		
		return experiments;
	}
	
	// ----------------------------------------------------------------------------
	
	public static List<Experiment> getPent80hPmadBrkWgPtc() {
		
		List<Experiment> experiments = new ArrayList<Experiment>();
		Experiment e = null;
		
		// settings
		Gene pmadAB = new Gene("pmadAB", "ch00");
		Gene brkAB = new Gene("brkAB", "ch01");
		Gene wgPtcAB = new Gene("wg-ptcAB", "ch02");
		String rootDirectory = "/mnt/extra/wingj_benchmarks/20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_78-79H/";
		
		e = new Experiment(rootDirectory+ "20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_78-79H_F_1/");
		e.addGene(pmadAB); e.addGene(brkAB, 1, 20); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory+ "20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_78-79H_F_2/");
		e.addGene(pmadAB); e.addGene(brkAB, 12, 38); e.addGene(wgPtcAB);
		experiments.add(e);

		e = new Experiment(rootDirectory+ "20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_78-79H_F_3/");
		e.addGene(pmadAB); e.addGene(brkAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory+ "20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_78-79H_F_4/");
		e.addGene(pmadAB); e.addGene(brkAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory+ "20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_78-79H_F_5/");
		e.addGene(pmadAB); e.addGene(brkAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		return experiments;
	}
	
	// ----------------------------------------------------------------------------
	
	public static List<Experiment> getPent90hPmadBrkWgPtc() {
		
		List<Experiment> experiments = new ArrayList<Experiment>();
		Experiment e = null;
		
		// settings
		Gene pmadAB = new Gene("pmadAB", "ch00");
		Gene brkAB = new Gene("brkAB", "ch01");
		Gene wgPtcAB = new Gene("wg-ptcAB", "ch02");
		String rootDirectory = "/mnt/extra/wingj_benchmarks/20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_90-91H/";
		
		e = new Experiment(rootDirectory+ "20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_90-91H_M_1/");
		e.addGene(pmadAB); e.addGene(brkAB, 10, 33); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory+ "20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_90-91H_M_2/");
		e.addGene(pmadAB); e.addGene(brkAB, 1, 23); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory+ "20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_90-91H_M_3/");
		e.addGene(pmadAB); e.addGene(brkAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory+ "20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_90-91H_M_4/");
		e.addGene(pmadAB); e.addGene(brkAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory+ "20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_90-91H_M_5/");
		e.addGene(pmadAB); e.addGene(brkAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		return experiments;
	}
	
	// ----------------------------------------------------------------------------
	
	public static List<Experiment> getPent100hPmadBrkWgPtc() {
		
		List<Experiment> experiments = new ArrayList<Experiment>();
		Experiment e = null;
		
		// settings
		Gene pmadAB = new Gene("pmadAB", "ch00");
		Gene brkAB = new Gene("brkAB", "ch01");
		Gene wgPtcAB = new Gene("wg-ptcAB", "ch02");
		String rootDirectory = "/mnt/extra/wingj_benchmarks/20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_99-100H/";
		
		e = new Experiment(rootDirectory+ "20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_99-100H_M_1/");
		e.addGene(pmadAB); e.addGene(brkAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory+ "20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_99-100H_M_2/");
		e.addGene(pmadAB); e.addGene(brkAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory+ "20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_99-100H_M_3/");
		e.addGene(pmadAB); e.addGene(brkAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory+ "20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_99-100H_M_4/");
		e.addGene(pmadAB); e.addGene(brkAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory+ "20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_99-100H_M_5/");
		e.addGene(pmadAB); e.addGene(brkAB); e.addGene(wgPtcAB);
		experiments.add(e);

		return experiments;
	}
	public static List<Experiment> getPent110hPmadBrkWgPtc() {
		
		List<Experiment> experiments = new ArrayList<Experiment>();
		Experiment e = null;
		
		// settings
		Gene pmadAB = new Gene("pmadAB", "ch00");
		Gene brkAB = new Gene("brkAB", "ch01");
		Gene wgPtcAB = new Gene("wg-ptcAB", "ch02");
		String rootDirectory = "/mnt/extra/wingj_benchmarks/20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_110,5-111,5H/";
		
		e = new Experiment(rootDirectory+ "20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_110,5-111,5H_M_1/");
		e.addGene(pmadAB); e.addGene(brkAB, 12, 38); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory+ "20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_110,5-111,5H_M_2/");
		e.addGene(pmadAB); e.addGene(brkAB, 12, 39); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory+ "20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_110,5-111,5H_M_3/");
		e.addGene(pmadAB); e.addGene(brkAB, 12, 39); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory+ "20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_110,5-111,5H_M_4/");
		e.addGene(pmadAB); e.addGene(brkAB, 1, 30); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory+ "20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_110,5-111,5H_M_5/");
		e.addGene(pmadAB); e.addGene(brkAB, 1, 30); e.addGene(wgPtcAB);
		experiments.add(e);
		
		return experiments;
	}
	
	// ----------------------------------------------------------------------------

	public static List<Experiment> getWt80hSalDadWgPtc() {
		
		List<Experiment> experiments = new ArrayList<Experiment>();
		Experiment e = null;
		
		// settings
		Gene salAB = new Gene("salAB", "ch00");
		Gene dadGFP = new Gene("dadGFP", "ch01");
		Gene wgPtcAB = new Gene("wg-ptcAB", "ch02");
		String rootDirectory = "/mnt/extra/wingj_benchmarks/20101129_salAB_dadGFP_wg-ptcAB_79-80H/";
		
		e = new Experiment(rootDirectory + "20101129_salAB_dadGFP_wg-ptcAB_79-80H_M_1/");
		e.addGene(salAB); e.addGene(dadGFP, 10, 34); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20101129_salAB_dadGFP_wg-ptcAB_79-80H_M_2/");
		e.addGene(salAB); e.addGene(dadGFP, 1, 22); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20101129_salAB_dadGFP_wg-ptcAB_79-80H_M_3/");
		e.addGene(salAB); e.addGene(dadGFP, 10, 35); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20101129_salAB_dadGFP_wg-ptcAB_79-80H_M_4/");
		e.addGene(salAB); e.addGene(dadGFP, 1, 28); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20101129_salAB_dadGFP_wg-ptcAB_79-80H_M_5/");
		e.addGene(salAB); e.addGene(dadGFP, 10, 34); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20101129_salAB_dadGFP_wg-ptcAB_79-80H_M_6/");
		e.addGene(salAB); e.addGene(dadGFP, 1, 24); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20101129_salAB_dadGFP_wg-ptcAB_79-80H_M_7/");
		e.addGene(salAB); e.addGene(dadGFP, 10, 34); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20101129_salAB_dadGFP_wg-ptcAB_79-80H_M_8/");
		e.addGene(salAB); e.addGene(dadGFP, 10, 33); e.addGene(wgPtcAB);
		experiments.add(e);
		
		return experiments;
	}
	
	// ----------------------------------------------------------------------------
	
	public static List<Experiment> getWt90hSalDadWgPtc() {
		
		List<Experiment> experiments = new ArrayList<Experiment>();
		Experiment e = null;
		
		// settings
		Gene salAB = new Gene("salAB", "ch00");
		Gene dadGFP = new Gene("dadGFP", "ch01");
		Gene wgPtcAB = new Gene("wg-ptcAB", "ch02");
		String rootDirectory = "/mnt/extra/wingj_benchmarks/20101129_salAB_dadGFP_wg-ptcAB_89-90H/";
		
		e = new Experiment(rootDirectory + "20101129_salAB_dadGFP_wg-ptcAB_89-90H_M_1/");
		e.addGene(salAB); e.addGene(dadGFP, 1, 22); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20101129_salAB_dadGFP_wg-ptcAB_89-90H_M_2/");
		e.addGene(salAB); e.addGene(dadGFP, 10, 35); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20101129_salAB_dadGFP_wg-ptcAB_89-90H_M_3/");
		e.addGene(salAB); e.addGene(dadGFP, 1, 22); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20101129_salAB_dadGFP_wg-ptcAB_89-90H_M_4/");
		e.addGene(salAB); e.addGene(dadGFP, 10, 36); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20101129_salAB_dadGFP_wg-ptcAB_89-90H_M_5/");
		e.addGene(salAB); e.addGene(dadGFP, 10, 35); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20101129_salAB_dadGFP_wg-ptcAB_89-90H_M_6/");
		e.addGene(salAB); e.addGene(dadGFP, 1, 23); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20101129_salAB_dadGFP_wg-ptcAB_89-90H_M_7/");
		e.addGene(salAB); e.addGene(dadGFP, 15, 34); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20101129_salAB_dadGFP_wg-ptcAB_89-90H_M_8/");
		e.addGene(salAB); e.addGene(dadGFP, 10, 33); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20101129_salAB_dadGFP_wg-ptcAB_89-90H_M_9/");
		e.addGene(salAB); e.addGene(dadGFP, 1, 20); e.addGene(wgPtcAB);
		experiments.add(e);
		
		return experiments;
	}
	
	// ----------------------------------------------------------------------------
	
	public static List<Experiment> getWt100hSalDadWgPtc() {
		
		List<Experiment> experiments = new ArrayList<Experiment>();
		Experiment e = null;
		
		// settings
		Gene salAB = new Gene("salAB", "ch00");
		Gene dadGFP = new Gene("dadGFP", "ch01");
		Gene wgPtcAB = new Gene("wg-ptcAB", "ch02");
		String rootDirectory = "/mnt/extra/wingj_benchmarks/20101129_salAB_dadGFP_wg-ptcAB_100-101H/";
		
		e = new Experiment(rootDirectory + "20101129_salAB_dadGFP_wg-ptcAB_100-101H_M_1/");
		e.addGene(salAB); e.addGene(dadGFP, 14, 34); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20101129_salAB_dadGFP_wg-ptcAB_100-101H_M_2/");
		e.addGene(salAB); e.addGene(dadGFP, 12, 33); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20101129_salAB_dadGFP_wg-ptcAB_100-101H_M_3/");
		e.addGene(salAB); e.addGene(dadGFP, 1, 24); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20101129_salAB_dadGFP_wg-ptcAB_100-101H_M_4/");
		e.addGene(salAB); e.addGene(dadGFP, 12, 33); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20101129_salAB_dadGFP_wg-ptcAB_100-101H_M_5/");
		e.addGene(salAB); e.addGene(dadGFP, 12, 35); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20101129_salAB_dadGFP_wg-ptcAB_100-101H_M_6/");
		e.addGene(salAB); e.addGene(dadGFP, 12, 37); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20101129_salAB_dadGFP_wg-ptcAB_100-101H_M_7/");
		e.addGene(salAB); e.addGene(dadGFP, 1, 25); e.addGene(wgPtcAB);
		experiments.add(e);
		
		return experiments;
	}
	
	// ----------------------------------------------------------------------------
	
	public static List<Experiment> getWt110hSalDadWgPtc() {
		
		List<Experiment> experiments = new ArrayList<Experiment>();
		Experiment e = null;
		
		// settings
		Gene salAB = new Gene("salAB", "ch00");
		Gene dadGFP = new Gene("dadGFP", "ch01");
		Gene wgPtcAB = new Gene("wg-ptcAB", "ch02");
		String rootDirectory = "/mnt/extra/wingj_benchmarks/20101129_salAB_dadGFP_wg-ptcAB_110,5-111,5H/";
		
		e = new Experiment(rootDirectory + "20101129_salAB_dadGFP_wg-ptcAB_110,5-111,5H_M_1/");
		e.addGene(salAB); e.addGene(dadGFP, 12, 31); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20101129_salAB_dadGFP_wg-ptcAB_110,5-111,5H_M_2/");
		e.addGene(salAB); e.addGene(dadGFP, 12, 30); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20101129_salAB_dadGFP_wg-ptcAB_110,5-111,5H_M_3/");
		e.addGene(salAB); e.addGene(dadGFP, 12, 31); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20101129_salAB_dadGFP_wg-ptcAB_110,5-111,5H_M_4/");
		e.addGene(salAB); e.addGene(dadGFP, 12, 33); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20101129_salAB_dadGFP_wg-ptcAB_110,5-111,5H_M_5/");
		e.addGene(salAB); e.addGene(dadGFP, 12, 32); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20101129_salAB_dadGFP_wg-ptcAB_110,5-111,5H_M_6/");
		e.addGene(salAB); e.addGene(dadGFP, 1, 20); e.addGene(wgPtcAB);
		experiments.add(e);
		
		return experiments;
	}
	
	// ----------------------------------------------------------------------------
	
	public static List<Experiment> getPent80hSalDadWgPtc() {
		
		List<Experiment> experiments = new ArrayList<Experiment>();
		Experiment e = null;
		
		// settings
		Gene salAB = new Gene("salAB", "ch00");
		Gene dadGFP = new Gene("dadGFP", "ch01");
		Gene wgPtcAB = new Gene("wg-ptcAB", "ch02");
		String rootDirectory = "/mnt/extra/wingj_benchmarks/20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_79-80H/";
		
		e = new Experiment(rootDirectory + "20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_79-80H_1/");
		e.addGene(salAB); e.addGene(dadGFP, 1, 24); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_79-80H_2/");
		e.addGene(salAB); e.addGene(dadGFP, 1, 32); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_79-80H_3/");
		e.addGene(salAB); e.addGene(dadGFP, 12, 37); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_79-80H_4/");
		e.addGene(salAB); e.addGene(dadGFP, 1, 25); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_79-80H_5/");
		e.addGene(salAB); e.addGene(dadGFP, 1, 24); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_79-80H_7/");
		e.addGene(salAB); e.addGene(dadGFP, 12, 36); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_79-80H_8/");
		e.addGene(salAB); e.addGene(dadGFP, 12, 34); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_79-80H_9/");
		e.addGene(salAB); e.addGene(dadGFP, 12, 37); e.addGene(wgPtcAB);
		experiments.add(e);
		
		return experiments;
	}
	
	// ----------------------------------------------------------------------------
	
	public static List<Experiment> getPent90hSalDadWgPtc() {
		
		List<Experiment> experiments = new ArrayList<Experiment>();
		Experiment e = null;
		
		// settings
		Gene salAB = new Gene("salAB", "ch00");
		Gene dadGFP = new Gene("dadGFP", "ch01");
		Gene wgPtcAB = new Gene("wg-ptcAB", "ch02");
		String rootDirectory = "/mnt/extra/wingj_benchmarks/20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_90-91H/";
		
		e = new Experiment(rootDirectory + "20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_90-91H_M_2/");
		e.addGene(salAB); e.addGene(dadGFP, 12, 42); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_90-91H_M_3/");
		e.addGene(salAB); e.addGene(dadGFP, 12, 41); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_90-91H_M_4/");
		e.addGene(salAB); e.addGene(dadGFP, 14, 41); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_90-91H_M_5/");
		e.addGene(salAB); e.addGene(dadGFP, 1, 25); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_90-91H_M_6/");
		e.addGene(salAB); e.addGene(dadGFP, 1, 29); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_90-91H_M_7/");
		e.addGene(salAB); e.addGene(dadGFP, 1, 32); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_90-91H_M_8/");
		e.addGene(salAB); e.addGene(dadGFP, 10, 41); e.addGene(wgPtcAB);
		experiments.add(e);
		
		return experiments;
	}
	
	// ----------------------------------------------------------------------------
	
	public static List<Experiment> getPent100hSalDadWgPtc() {
		
		List<Experiment> experiments = new ArrayList<Experiment>();
		Experiment e = null;
		
		// settings
		Gene salAB = new Gene("salAB", "ch00");
		Gene dadGFP = new Gene("dadGFP", "ch01");
		Gene wgPtcAB = new Gene("wg-ptcAB", "ch02");
		String rootDirectory = "/mnt/extra/wingj_benchmarks/20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_99-101H/";
		
		e = new Experiment(rootDirectory + "20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_99-101H_1/");
		e.addGene(salAB); e.addGene(dadGFP, 12, 51); e.addGene(wgPtcAB);
		experiments.add(e);

		e = new Experiment(rootDirectory + "20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_99-101H_2/");
		e.addGene(salAB); e.addGene(dadGFP, 1, 40); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_99-101H_3/");
		e.addGene(salAB); e.addGene(dadGFP, 1, 42); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_99-101H_4/");
		e.addGene(salAB); e.addGene(dadGFP, 1, 38); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_99-101H_5/");
		e.addGene(salAB); e.addGene(dadGFP, 14, 54); e.addGene(wgPtcAB);
		experiments.add(e);
		
		return experiments;
	}
	
	// ----------------------------------------------------------------------------
	
	public static List<Experiment> getPent110hSalDadWgPtc() {
		
		List<Experiment> experiments = new ArrayList<Experiment>();
		Experiment e = null;
		
		// settings
		Gene salAB = new Gene("salAB", "ch00");
		Gene dadGFP = new Gene("dadGFP", "ch01");
		Gene wgPtcAB = new Gene("wg-ptcAB", "ch02");
		String rootDirectory = "/mnt/extra/wingj_benchmarks/20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_114-115H/";
		
		e = new Experiment(rootDirectory + "20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_114-115H_1/");
		e.addGene(salAB); e.addGene(dadGFP, 1, 30); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_114-115H_2/");
		e.addGene(salAB); e.addGene(dadGFP, 1, 30); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_114-115H_3/");
		e.addGene(salAB); e.addGene(dadGFP, 15, 48); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_114-115H_4/");
		e.addGene(salAB); e.addGene(dadGFP, 1, 30); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_114-115H_5/");
		e.addGene(salAB); e.addGene(dadGFP, 16, 46); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_114-115H_6/");
		e.addGene(salAB); e.addGene(dadGFP, 16, 48); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_114-115H_7/");
		e.addGene(salAB); e.addGene(dadGFP, 20, 45); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_114-115H_9/");
		e.addGene(salAB); e.addGene(dadGFP, 18, 42); e.addGene(wgPtcAB);
		experiments.add(e);

		return experiments;
	}

	// ----------------------------------------------------------------------------
	
	public static List<Experiment> getWt80hBrkOmbWgPtc() {
		
		List<Experiment> experiments = new ArrayList<Experiment>();
		Experiment e = null;
		
		// settings
		Gene brkAB = new Gene("brkAB", "ch00");
		Gene ombAB = new Gene("ombAB", "ch01");
		Gene wgPtcAB = new Gene("wg-ptcAB", "ch02");
		String rootDirectory = "/mnt/extra/wingj_benchmarks/20110302_brkAB_ombAB_wg-ptcAB_79-80H/";
		
		e = new Experiment(rootDirectory + "20110302_brkAB_ombAB_wg-ptcAB_79-80H_M_1/");
		e.addGene(brkAB, 10, 25); e.addGene(ombAB, 10, 25); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20110302_brkAB_ombAB_wg-ptcAB_79-80H_M_2/");
		e.addGene(brkAB, 10, 26); e.addGene(ombAB, 10, 26); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20110302_brkAB_ombAB_wg-ptcAB_79-80H_M_3/");
		e.addGene(brkAB, 1, 16); e.addGene(ombAB, 1, 16); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20110302_brkAB_ombAB_wg-ptcAB_79-80H_M_4/");
		e.addGene(brkAB, 5, 27); e.addGene(ombAB, 5, 27); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20110302_brkAB_ombAB_wg-ptcAB_79-80H_M_5/");
		e.addGene(brkAB, 1, 15); e.addGene(ombAB, 1, 15); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20110302_brkAB_ombAB_wg-ptcAB_79-80H_M_6/");
		e.addGene(brkAB); e.addGene(ombAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20110302_brkAB_ombAB_wg-ptcAB_79-80H_M_7/");
		e.addGene(brkAB, 8, 31); e.addGene(ombAB, 8, 31); e.addGene(wgPtcAB);
		experiments.add(e);
		
		return experiments;
	}
	
	// ----------------------------------------------------------------------------
	
	public static List<Experiment> getWt90hBrkOmbWgPtc() {
		
		List<Experiment> experiments = new ArrayList<Experiment>();
		Experiment e = null;
		
		// settings
		Gene brkAB = new Gene("brkAB", "ch00");
		Gene ombAB = new Gene("ombAB", "ch01");
		Gene wgPtcAB = new Gene("wg-ptcAB", "ch02");
		String rootDirectory = "/mnt/extra/wingj_benchmarks/20110302_brkAB_ombAB_wg-ptcAB_87-88H/";
		
		e = new Experiment(rootDirectory + "20110302_brkAB_ombAB_wg-ptcAB_87-88H_F_1/");
		e.addGene(brkAB, 18, 32); e.addGene(ombAB, 18, 32); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20110302_brkAB_ombAB_wg-ptcAB_87-88H_F_2/");
		e.addGene(brkAB, 1, 18); e.addGene(ombAB, 1, 18); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20110302_brkAB_ombAB_wg-ptcAB_87-88H_F_3/");
		e.addGene(brkAB, 1, 20); e.addGene(ombAB, 1, 20); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20110302_brkAB_ombAB_wg-ptcAB_87-88H_F_4/");
		e.addGene(brkAB, 1, 20); e.addGene(ombAB, 1, 20); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20110302_brkAB_ombAB_wg-ptcAB_87-88H_F_6/");
		e.addGene(brkAB, 1, 18); e.addGene(ombAB, 1, 18); e.addGene(wgPtcAB);
		experiments.add(e);
		
		return experiments;
	}
	
	// ----------------------------------------------------------------------------
	
	public static List<Experiment> getWt100hBrkOmbWgPtc() {
		
		List<Experiment> experiments = new ArrayList<Experiment>();
		Experiment e = null;
		
		// settings
		Gene brkAB = new Gene("brkAB", "ch00");
		Gene ombAB = new Gene("ombAB", "ch01");
		Gene wgPtcAB = new Gene("wg-ptcAB", "ch02");
		String rootDirectory = "/mnt/extra/wingj_benchmarks/20110302_brkAB_ombAB_wg-ptcAB_100-101H/";
		
		e = new Experiment(rootDirectory + "20110302_brkAB_ombAB_wg-ptcAB_100-101H_M_1/");
		e.addGene(brkAB, 15, 35); e.addGene(ombAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20110302_brkAB_ombAB_wg-ptcAB_100-101H_M_2/");
		e.addGene(brkAB); e.addGene(ombAB, 10, 37); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20110302_brkAB_ombAB_wg-ptcAB_100-101H_M_3/");
		e.addGene(brkAB); e.addGene(ombAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20110302_brkAB_ombAB_wg-ptcAB_100-101H_M_4/");
		e.addGene(brkAB); e.addGene(ombAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20110302_brkAB_ombAB_wg-ptcAB_100-101H_M_5/");
		e.addGene(brkAB); e.addGene(ombAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20110302_brkAB_ombAB_wg-ptcAB_100-101H_M_6/");
		e.addGene(brkAB); e.addGene(ombAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20110302_brkAB_ombAB_wg-ptcAB_100-101H_M_7/");
		e.addGene(brkAB, 12, 39); e.addGene(ombAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		return experiments;
	}
	
	// ----------------------------------------------------------------------------
	
	public static List<Experiment> getWt110hBrkOmbWgPtc() {
		
		List<Experiment> experiments = new ArrayList<Experiment>();
		Experiment e = null;
		
		// settings
		Gene brkAB = new Gene("brkAB", "ch00");
		Gene ombAB = new Gene("ombAB", "ch01");
		Gene wgPtcAB = new Gene("wg-ptcAB", "ch02");
		String rootDirectory = "/mnt/extra/wingj_benchmarks/20110302_brkAB_ombAB_wg-ptcAB_111-112H/";
		
		e = new Experiment(rootDirectory + "20110302_brkAB_ombAB_wg-ptcAB_111-112H_M_1/");
		e.addGene(brkAB, 10, 38); e.addGene(ombAB, 10, 38); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20110302_brkAB_ombAB_wg-ptcAB_111-112H_M_2/");
		e.addGene(brkAB); e.addGene(ombAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20110302_brkAB_ombAB_wg-ptcAB_111-112H_M_3/");
		e.addGene(brkAB); e.addGene(ombAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20110302_brkAB_ombAB_wg-ptcAB_111-112H_M_4/");
		e.addGene(brkAB); e.addGene(ombAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20110302_brkAB_ombAB_wg-ptcAB_111-112H_M_5/");
		e.addGene(brkAB); e.addGene(ombAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		return experiments;
	}
	
	// ----------------------------------------------------------------------------
	
	public static List<Experiment> getPent80hBrkOmbWgPtc() {
		
		List<Experiment> experiments = new ArrayList<Experiment>();
		Experiment e = null;
		
		// settings
		Gene brkAB = new Gene("brkAB", "ch00");
		Gene ombAB = new Gene("ombAB", "ch01");
		Gene wgPtcAB = new Gene("wg-ptcAB", "ch02");
		String rootDirectory = "/mnt/extra/wingj_benchmarks/20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_79-80H/";
		
		e = new Experiment(rootDirectory + "20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_79-80H_M_1/");
		e.addGene(brkAB); e.addGene(ombAB); e.addGene(wgPtcAB);
		experiments.add(e);
	
		e = new Experiment(rootDirectory + "20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_79-80H_M_2/");
		e.addGene(brkAB); e.addGene(ombAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_79-80H_M_3/");
		e.addGene(brkAB); e.addGene(ombAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_79-80H_M_4/");
		e.addGene(brkAB); e.addGene(ombAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_79-80H_M_5/");
		e.addGene(brkAB); e.addGene(ombAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_79-80H_M_6/");
		e.addGene(brkAB); e.addGene(ombAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_79-80H_M_7/");
		e.addGene(brkAB); e.addGene(ombAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_79-80H_M_8/");
		e.addGene(brkAB, 1, 12); e.addGene(ombAB, 1, 12); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_79-80H_M_9/");
		e.addGene(brkAB); e.addGene(ombAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		return experiments;
	}
	
	// ----------------------------------------------------------------------------
	
	public static List<Experiment> getPent90hBrkOmbWgPtc() {
		
		List<Experiment> experiments = new ArrayList<Experiment>();
		Experiment e = null;
		
		// settings
		Gene brkAB = new Gene("brkAB", "ch00");
		Gene ombAB = new Gene("ombAB", "ch01");
		Gene wgPtcAB = new Gene("wg-ptcAB", "ch02");
		String rootDirectory = "/mnt/extra/wingj_benchmarks/20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_87-88H/";
		
		e = new Experiment(rootDirectory + "20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_87-88H_M_1/");
		e.addGene(brkAB); e.addGene(ombAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_87-88H_M_2/");
		e.addGene(brkAB); e.addGene(ombAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_87-88H_M_3/");
		e.addGene(brkAB); e.addGene(ombAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_87-88H_M_4/");
		e.addGene(brkAB); e.addGene(ombAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_87-88H_M_5/");
		e.addGene(brkAB); e.addGene(ombAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_87-88H_M_6/");
		e.addGene(brkAB); e.addGene(ombAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		return experiments;
	}
	
	// ----------------------------------------------------------------------------
	
	public static List<Experiment> getPent100hBrkOmbWgPtc() {
		
		List<Experiment> experiments = new ArrayList<Experiment>();
		Experiment e = null;
		
		// settings
		Gene brkAB = new Gene("brkAB", "ch00");
		Gene ombAB = new Gene("ombAB", "ch01");
		Gene wgPtcAB = new Gene("wg-ptcAB", "ch02");
		String rootDirectory = "/mnt/extra/wingj_benchmarks/20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_100-101H/";
		
		e = new Experiment(rootDirectory + "20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_100-101H_M_1/");
		e.addGene(brkAB); e.addGene(ombAB); e.addGene(wgPtcAB);
		experiments.add(e);

		e = new Experiment(rootDirectory + "20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_100-101H_M_2/");
		e.addGene(brkAB); e.addGene(ombAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_100-101H_M_3/");
		e.addGene(brkAB); e.addGene(ombAB); e.addGene(wgPtcAB);
		experiments.add(e);

		e = new Experiment(rootDirectory + "20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_100-101H_M_4/");
		e.addGene(brkAB); e.addGene(ombAB); e.addGene(wgPtcAB);
		experiments.add(e);

		e = new Experiment(rootDirectory + "20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_100-101H_M_5/");
		e.addGene(brkAB); e.addGene(ombAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_100-101H_M_6/");
		e.addGene(brkAB); e.addGene(ombAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		return experiments;
	}
	
	// ----------------------------------------------------------------------------
	
	public static List<Experiment> getPent110hBrkOmbWgPtc() {
		
		List<Experiment> experiments = new ArrayList<Experiment>();
		Experiment e = null;
		
		// settings
		Gene brkAB = new Gene("brkAB", "ch00");
		Gene ombAB = new Gene("ombAB", "ch01");
		Gene wgPtcAB = new Gene("wg-ptcAB", "ch02");
		String rootDirectory = "/mnt/extra/wingj_benchmarks/20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_109-110.5H/";
		
		e = new Experiment(rootDirectory + "20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_109-110,5H_M_1/");
		e.addGene(brkAB); e.addGene(ombAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_109-110,5H_M_2/");
		e.addGene(brkAB); e.addGene(ombAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_109-110,5H_M_3/");
		e.addGene(brkAB); e.addGene(ombAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_109-110,5H_M_4/");
		e.addGene(brkAB); e.addGene(ombAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_109-110,5H_M_5/");
		e.addGene(brkAB); e.addGene(ombAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_109-110,5H_M_6/");
		e.addGene(brkAB); e.addGene(ombAB); e.addGene(wgPtcAB);
		experiments.add(e);
		
		e = new Experiment(rootDirectory + "20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_109-110,5H_M_7/");
		e.addGene(brkAB); e.addGene(ombAB); e.addGene(wgPtcAB);
		experiments.add(e);

		return experiments;
	}
}
