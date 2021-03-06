/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.elasticsearch.index.fielddata.fieldcomparator;

import org.elasticsearch.ElasticSearchIllegalArgumentException;

import java.util.Locale;

/**
 * Defines what values to pick in the case a document contains multiple values for a particular field.
 */
public enum SortMode {

    /**
     * Sum of all the values.
     */
    SUM {
        /**
         * Returns the sum of the two values
         */
        @Override
        public double apply(double a, double b) {
            return a + b;
        }

        /**
         * Returns the sum of the two values
         */
        @Override
        public long apply(long a, long b) {
            return a + b;
        }
    },

    /**
     * Average of all the values.
     */
    AVG {

        /**
         * Returns the sum of the two values
         */
        @Override
        public double apply(double a, double b) {
            return a + b;
        }

        /**
         * Returns the sum of the two values
         */
        @Override
        public long apply(long a, long b) {
            return a + b;
        }

        /**
         * Returns <code>a / Math.max(1.0d, numValues)</code>
         */
        @Override
        public double reduce(double a, int numValues) {
            return a / Math.max(1.0d, (double) numValues);
        }

        /**
         * Returns <code>Math.round(a / Math.max(1.0, numValues))</code>
         */
        @Override
        public long reduce(long a, int numValues) {
            return Math.round(a / Math.max(1.0, numValues));
        }
    },

    /**
     * Pick the lowest value.
     */
    MIN {
        /**
         * Equivalent to {@link Math#min(double, double)}
         */
        @Override
        public double apply(double a, double b) {
            return Math.min(a, b);
        }

        /**
         * Equivalent to {@link Math#min(long, long)}
         */
        @Override
        public long apply(long a, long b) {
            return Math.min(a, b);
        }

        /**
         * Returns {@link Double#POSITIVE_INFINITY}
         */
        @Override
        public double startDouble() {
            return Double.POSITIVE_INFINITY;
        }

        /**
         * Returns {@link Long#MAX_VALUE}
         */
        @Override
        public long startLong() {
            return Long.MAX_VALUE;
        }
    },

    /**
     * Pick the highest value.
     */
    MAX {
        /**
         * Equivalent to {@link Math#max(double, double)}
         */
        @Override
        public double apply(double a, double b) {
            return Math.max(a, b);
        }

        /**
         * Equivalent to {@link Math#max(long, long)}
         */
        @Override
        public long apply(long a, long b) {
            return Math.max(a, b);
        }

        /**
         * Returns {@link Double#NEGATIVE_INFINITY}
         */
        @Override
        public double startDouble() {
            return Double.NEGATIVE_INFINITY;
        }


        /**
         * Returns {@link Long#MIN_VALUE}
         */
        @Override
        public long startLong() {
            return Long.MIN_VALUE;
        }
    };

    /**
     * Applies the sort mode and returns the result. This method is meant to be
     * a binary function that is commonly used in a loop to find the relevant
     * value for the sort mode in a list of values. For instance if the sort mode
     * is {@link SortMode#MAX} this method is equivalent to {@link Math#max(double, double)}.
     *
     * Note: all implementations are idempotent.
     *
     * @param a an argument
     * @param b another argument
     * @return the result of the function.
     */
    public abstract double apply(double a, double b);

    /**
     * Applies the sort mode and returns the result. This method is meant to be
     * a binary function that is commonly used in a loop to find the relevant
     * value for the sort mode in a list of values. For instance if the sort mode
     * is {@link SortMode#MAX} this method is equivalent to {@link Math#max(long, long)}.
     *
     * Note: all implementations are idempotent.
     *
     * @param a an argument
     * @param b another argument
     * @return the result of the function.
     */
    public abstract long apply(long a, long b);

    /**
     * Returns an initial value for the sort mode that is guaranteed to have no impact if passed
     * to {@link #apply(double, double)}. This value should be used as the initial value if the
     * sort mode is applied to a non-empty list of values. For instance:
     * <pre>
     *     double relevantValue = sortMode.startDouble();
     *     for (int i = 0; i < array.length; i++) {
     *         relevantValue = sortMode.apply(array[i], relevantValue);
     *     }
     * </pre>
     *
     * Note: This method return <code>0</code> by default.
     *
     * @return an initial value for the sort mode.
     */
    public double startDouble() {
        return 0;
    }

    /**
     * Returns an initial value for the sort mode that is guaranteed to have no impact if passed
     * to {@link #apply(long, long)}. This value should be used as the initial value if the
     * sort mode is applied to a non-empty list of values. For instance:
     * <pre>
     *     long relevantValue = sortMode.startLong();
     *     for (int i = 0; i < array.length; i++) {
     *         relevantValue = sortMode.apply(array[i], relevantValue);
     *     }
     * </pre>
     *
     * Note: This method return <code>0</code> by default.
     * @return an initial value for the sort mode.
     */
    public long startLong() {
        return 0;
    }

    /**
     * Returns the aggregated value based on the sort mode. For instance if {@link SortMode#AVG} is used
     * this method divides the given value by the number of values. The default implementation returns
     * the first argument.
     *
     * Note: all implementations are idempotent.
     */
    public double reduce(double a, int numValues) {
        return a;
    }

    /**
     * Returns the aggregated value based on the sort mode. For instance if {@link SortMode#AVG} is used
     * this method divides the given value by the number of values. The default implementation returns
     * the first argument.
     *
     * Note: all implementations are idempotent.
     */
    public long reduce(long a, int numValues) {
        return a;
    }

    /**
     * A case insensitive version of {@link #valueOf(String)}
     *
     * @throws ElasticSearchIllegalArgumentException if the given string doesn't match a sort mode or is <code>null</code>.
     */
    public static SortMode fromString(String sortMode) {
        try {
            return valueOf(sortMode.toUpperCase(Locale.ROOT));
        } catch (Throwable t) {
            throw new ElasticSearchIllegalArgumentException("Illegal sort_mode " + sortMode);
        }
    }

}
