/*
 * Copyright 2013 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.core.impl.heuristic.selector.value.decorator;

import java.util.Iterator;

import com.google.common.collect.Iterators;
import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import org.optaplanner.core.impl.heuristic.selector.value.AbstractValueSelector;
import org.optaplanner.core.impl.heuristic.selector.value.ValueSelector;
import org.optaplanner.core.impl.phase.scope.AbstractPhaseScope;
import org.optaplanner.core.impl.score.director.ScoreDirector;

/**
 * Prevents reassigning of already initialized variables during Construction Heuristics and Exhaustive Search.
 * <p>
 * Returns no values for an entity's variable if the variable is already initialized.
 */
public class ReinitializeVariableValueSelector extends AbstractValueSelector {

    protected final ValueSelector childValueSelector;
    protected final SelectionFilter reinitializeVariableEntityFilter;

    protected ScoreDirector scoreDirector = null;

    public ReinitializeVariableValueSelector(ValueSelector childValueSelector) {
        this.childValueSelector = childValueSelector;
        this.reinitializeVariableEntityFilter = childValueSelector.getVariableDescriptor()
                .getReinitializeVariableEntityFilter();
        phaseLifecycleSupport.addEventListener(childValueSelector);
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public void phaseStarted(AbstractPhaseScope phaseScope) {
        super.phaseStarted(phaseScope);
        scoreDirector = phaseScope.getScoreDirector();
    }

    @Override
    public void phaseEnded(AbstractPhaseScope phaseScope) {
        super.phaseEnded(phaseScope);
        scoreDirector = null;
    }

    public GenuineVariableDescriptor getVariableDescriptor() {
        return childValueSelector.getVariableDescriptor();
    }

    public boolean isCountable() {
        return childValueSelector.isCountable();
    }

    public boolean isNeverEnding() {
        return childValueSelector.isNeverEnding();
    }

    public long getSize(Object entity) {
        if (!reinitializeVariableEntityFilter.accept(scoreDirector, entity)) {
            return 0L;
        }
        return childValueSelector.getSize(entity);
    }

    public Iterator<Object> iterator(Object entity) {
        if (!reinitializeVariableEntityFilter.accept(scoreDirector, entity)) {
            return Iterators.emptyIterator();
        }
        return childValueSelector.iterator(entity);
    }

    public Iterator<Object> endingIterator(Object entity) {
        if (!reinitializeVariableEntityFilter.accept(scoreDirector, entity)) {
            return Iterators.emptyIterator();
        }
        return childValueSelector.endingIterator(entity);
    }

    @Override
    public String toString() {
        return "Reinitialize(" + childValueSelector + ")";
    }

}
