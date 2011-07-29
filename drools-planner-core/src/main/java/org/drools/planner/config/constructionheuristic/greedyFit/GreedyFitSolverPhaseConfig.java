/*
 * Copyright 2011 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.planner.config.constructionheuristic.greedyFit;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import org.drools.planner.config.EnvironmentMode;
import org.drools.planner.config.phase.SolverPhaseConfig;
import org.drools.planner.core.constructionheuristic.greedyFit.DefaultGreedyFitSolverPhase;
import org.drools.planner.core.constructionheuristic.greedyFit.GreedyFitSolverPhase;
import org.drools.planner.core.constructionheuristic.greedyFit.decider.DefaultGreedyDecider;
import org.drools.planner.core.constructionheuristic.greedyFit.decider.GreedyDecider;
import org.drools.planner.core.constructionheuristic.greedyFit.decider.PickEarlyGreedyFitType;
import org.drools.planner.core.constructionheuristic.greedyFit.selector.GreedyPlanningEntitySelector;
import org.drools.planner.core.domain.solution.SolutionDescriptor;
import org.drools.planner.core.heuristic.selector.entity.PlanningEntitySelector;
import org.drools.planner.core.heuristic.selector.variable.PlanningValueWalker;
import org.drools.planner.core.heuristic.selector.variable.PlanningVariableWalker;
import org.drools.planner.core.score.definition.ScoreDefinition;
import org.drools.planner.core.termination.Termination;

@XStreamAlias("greedyFit")
public class GreedyFitSolverPhaseConfig extends SolverPhaseConfig {

    // Warning: all fields are null (and not defaulted) because they can be inherited
    // and also because the input config file should match the output config file

    @XStreamImplicit(itemFieldName = "greedyFitPlanningEntity")
    protected List<GreedyFitPlanningEntityConfig> greedyFitPlanningEntityConfigList = null;

    protected PickEarlyGreedyFitType pickEarlyGreedyFitType = null;

    public List<GreedyFitPlanningEntityConfig> getGreedyFitPlanningEntityConfigList() {
        return greedyFitPlanningEntityConfigList;
    }

    public void setGreedyFitPlanningEntityConfigList(List<GreedyFitPlanningEntityConfig> greedyFitPlanningEntityConfigList) {
        this.greedyFitPlanningEntityConfigList = greedyFitPlanningEntityConfigList;
    }

    public PickEarlyGreedyFitType getPickEarlyGreedyFitType() {
        return pickEarlyGreedyFitType;
    }

    public void setPickEarlyGreedyFitType(PickEarlyGreedyFitType pickEarlyGreedyFitType) {
        this.pickEarlyGreedyFitType = pickEarlyGreedyFitType;
    }

    // ************************************************************************
    // Builder methods
    // ************************************************************************

    public GreedyFitSolverPhase buildSolverPhase(EnvironmentMode environmentMode, SolutionDescriptor solutionDescriptor,
            ScoreDefinition scoreDefinition, Termination solverTermination) {
        DefaultGreedyFitSolverPhase greedySolverPhase = new DefaultGreedyFitSolverPhase();
        configureSolverPhase(greedySolverPhase, environmentMode, scoreDefinition, solverTermination);
        greedySolverPhase.setGreedyPlanningEntitySelector(buildGreedyPlanningEntitySelector(solutionDescriptor));
        greedySolverPhase.setGreedyDecider(buildGreedyDecider(solutionDescriptor, environmentMode));
        if (environmentMode == EnvironmentMode.DEBUG || environmentMode == EnvironmentMode.TRACE) {
            greedySolverPhase.setAssertStepScoreIsUncorrupted(true);
        }
        return greedySolverPhase;
    }

    private GreedyPlanningEntitySelector buildGreedyPlanningEntitySelector(SolutionDescriptor solutionDescriptor) {
        GreedyPlanningEntitySelector greedyPlanningEntitySelector = new GreedyPlanningEntitySelector();
        List<PlanningEntitySelector> planningEntitySelectorList = new ArrayList<PlanningEntitySelector>(greedyFitPlanningEntityConfigList.size());
        for (GreedyFitPlanningEntityConfig greedyFitPlanningEntityConfig : greedyFitPlanningEntityConfigList) {
            planningEntitySelectorList.add(greedyFitPlanningEntityConfig.buildPlanningEntitySelector(solutionDescriptor));
        }
        greedyPlanningEntitySelector.setPlanningEntitySelectorList(planningEntitySelectorList);
        return greedyPlanningEntitySelector;
    }

    private GreedyDecider buildGreedyDecider(SolutionDescriptor solutionDescriptor, EnvironmentMode environmentMode) {
        DefaultGreedyDecider greedyDecider = new DefaultGreedyDecider();
        PickEarlyGreedyFitType pickEarlyGreedyFitType = (this.pickEarlyGreedyFitType == null)
                ? PickEarlyGreedyFitType.NEVER : this.pickEarlyGreedyFitType;

        // TODO very broken =================
        PlanningVariableWalker planningVariableWalker = new PlanningVariableWalker(null); // TODO broken
        List<PlanningValueWalker> planningValueWalkerList = new ArrayList<PlanningValueWalker>();
        planningVariableWalker.setPlanningValueWalkerList(planningValueWalkerList);
        greedyDecider.setPlanningVariableWalker(planningVariableWalker); // TODO broken
        // TODO very broken =================
        
        greedyDecider.setPickEarlyGreedyFitType(pickEarlyGreedyFitType);
        if (environmentMode == EnvironmentMode.TRACE) {
            greedyDecider.setAssertMoveScoreIsUncorrupted(true);
        }
        return greedyDecider;
    }

    public void inherit(GreedyFitSolverPhaseConfig inheritedConfig) {
        super.inherit(inheritedConfig);
        if (greedyFitPlanningEntityConfigList == null) {
            greedyFitPlanningEntityConfigList = inheritedConfig.getGreedyFitPlanningEntityConfigList();
        } else if (inheritedConfig.getGreedyFitPlanningEntityConfigList() != null) {
            // The inherited greedyFitPlanningEntityConfigList should be before the non-inherited one
            List<GreedyFitPlanningEntityConfig> mergedList = new ArrayList<GreedyFitPlanningEntityConfig>(
                    inheritedConfig.getGreedyFitPlanningEntityConfigList());
            mergedList.addAll(greedyFitPlanningEntityConfigList);
            greedyFitPlanningEntityConfigList = mergedList;
        }
        if (pickEarlyGreedyFitType == null) {
            pickEarlyGreedyFitType = inheritedConfig.getPickEarlyGreedyFitType();
        }
    }

}
