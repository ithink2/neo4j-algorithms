/*
 * Copyright (c) 2014 GraphAware
 *
 * This file is part of GraphAware.
 *
 * GraphAware is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.graphaware.module.algo.generator.relationship;

import com.graphaware.common.util.SameTypePair;
import com.graphaware.common.util.UnorderedPair;
import com.graphaware.module.algo.generator.config.DistributionBasedConfig;
import com.graphaware.module.algo.generator.distribution.DegreeDistribution;
import com.graphaware.module.algo.generator.distribution.MutableDegreeDistribution;
import com.graphaware.module.algo.generator.distribution.MutableSimpleDegreeDistribution;
import com.graphaware.module.algo.generator.utils.WeightedReservoirSampler;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple minded {@link RelationshipGenerator} based on a {@link DegreeDistribution}
 * <p/>
 * Please note that the distribution of randomly generated graphs isn't exactly uniform (see the paper below)
 * <p/>
 * Uses Blitzstein-Diaconis algorithm Ref:
 * <p/>
 * A SEQUENTIAL IMPORTANCE SAMPLING ALGORITHM FOR GENERATING RANDOM GRAPHS WITH PRESCRIBED DEGREES
 * By Joseph Blitzstein and Persi Diaconis (Stanford University). (Harvard, June 2006)
 */
public class SimpleGraphRelationshipGenerator extends BaseRelationshipGenerator<DistributionBasedConfig> {

    public SimpleGraphRelationshipGenerator(DistributionBasedConfig configuration) {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Returns an edge-set corresponding to a randomly chosen simple graph.
     */
    @Override
    protected List<SameTypePair<Integer>> doGenerateEdges() {
        List<SameTypePair<Integer>> edges = new ArrayList<>();
        MutableDegreeDistribution distribution = new MutableSimpleDegreeDistribution(getConfiguration().getDegrees());

        while (!distribution.isZeroList()) {
            // int length = distribution.size();
            int index = 0;
            int min = Integer.MAX_VALUE;

            // find minimal nonzero element
            for (int i = 0; i < distribution.size(); ++i) {
                int elem = distribution.get(i);
                if (elem != 0 && elem < min) {
                    min = elem;
                    index = i;
                }
            }

            WeightedReservoirSampler sampler = new WeightedReservoirSampler();

            // Obtain a candidate list:
            while (true) {
                MutableDegreeDistribution temp = new MutableSimpleDegreeDistribution(distribution.getDegrees());
                int candidateIndex = sampler.randomIndexChoice(temp.getDegrees(), index);

                SameTypePair<Integer> edgeCandidate = new UnorderedPair<>(candidateIndex, index);

                //  Checks if edge has already been added.
                boolean skip = false;
                for (SameTypePair<Integer> edge : edges) {
                    if (edge.equals(edgeCandidate)) {
                        skip = true;
                        break;
                    }
                }

                if (skip) {
                    continue;
                }

                // Prepare the candidate set and test if it is graphical
                temp.decrease(index);
                temp.decrease(candidateIndex);

                if (temp.isValid()) {
                    distribution = temp;
                    edges.add(edgeCandidate); // edge is allowed, add it.
                    break;
                }
            }
        }

        return edges;
    }
}
