package com.aleksandartokarev;


import com.google.common.collect.Lists;
import io.confluent.ksql.function.udaf.TableUdaf;
import io.confluent.ksql.function.udaf.UdafDescription;
import io.confluent.ksql.function.udaf.UdafFactory;
import org.apache.kafka.connect.data.Struct;

import java.util.List;

@UdafDescription(
        name = "collect_items",
        category = "test",
        author = "Aleksandar Tokarev",
        description = "User defined function")
public class CollectItemsUdaf {

    public static final String INPUT_SCHEMA_DESCRIPTOR = "STRUCT<`NAME` STRING, `DESCRIPTION` STRING, `AMOUNT` DOUBLE, `UPDATED` BIGINT>";

    public static final String AGGREGATE_SCHEMA_DESCRIPTOR = "ARRAY<STRUCT<`NAME` STRING, `DESCRIPTION` STRING, `AMOUNT` DOUBLE, `UPDATED` BIGINT>>";
    public static final String RETURN_SCHEMA_DESCRIPTOR = "ARRAY<STRUCT<`NAME` STRING, `DESCRIPTION` STRING, `AMOUNT` DOUBLE, `UPDATED` BIGINT>>";

    @UdafFactory(
            description = "Main function to do item aggregation",
            paramSchema = INPUT_SCHEMA_DESCRIPTOR,
            aggregateSchema = AGGREGATE_SCHEMA_DESCRIPTOR,
            returnSchema = RETURN_SCHEMA_DESCRIPTOR
    )
    public static TableUdaf<Struct, List<Struct>, List<Struct>> createUdaf() {
        return new ItemUdafImpl();
    }

    private static class ItemUdafImpl implements TableUdaf<Struct, List<Struct>, List<Struct>> {
        @Override
        public List<Struct> initialize() {
            // Called first
            return Lists.newArrayList();
        }

        @Override
        public List<Struct> aggregate(Struct current, List<Struct> aggregate) {
            // Called for each item - `current` is the current item, where `aggregate` is the list of all items for the aggregate function being done
            if ((long) current.get("UPDATED") > System.currentTimeMillis()) {
                aggregate.add(current);
            }
            return aggregate;
        }

        @Override
        public List<Struct> merge(final List<Struct> aggOne, final List<Struct> aggTwo) {
            // Used for merging session windows - not used in this example
            aggOne.addAll(aggTwo);
            return aggOne;
        }

        @Override
        public List<Struct> map(final List<Struct> agg) {
            // Called last - when data is returned
            return agg;
        }

        @Override
        public List<Struct> undo(Struct valueToUndo, List<Struct> aggregateValue) {
            final int lastIndex = aggregateValue.lastIndexOf(valueToUndo);
            // If we cannot find the value, that means that we hit the limit and never inserted it, so
            // just return.
            if (lastIndex < 0) {
                return aggregateValue;
            }
            aggregateValue.remove(lastIndex);
            return aggregateValue;
        }
    }
}