import com.aleksandartokarev.CollectItemsUdaf;
import io.confluent.ksql.function.udaf.TableUdaf;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class CollectItemsUdafTest {

    public static final Schema INPUT_SCHEMA = SchemaBuilder.struct().optional()
            .field("NAME", Schema.OPTIONAL_STRING_SCHEMA)
            .field("DESCRIPTION", Schema.OPTIONAL_STRING_SCHEMA)
            .field("AMOUNT", Schema.OPTIONAL_FLOAT64_SCHEMA)
            .field("UPDATED", Schema.OPTIONAL_INT64_SCHEMA)
            .build();

    @Test
    public void shouldCollectLatestItemsWith2Items1Duplicate() {
        final TableUdaf<Struct, List<Struct>, List<Struct>> udaf = CollectItemsUdaf.createUdaf();
        Struct initializer = new Struct(INPUT_SCHEMA);
        initializer.put("NAME", "Tomatoes");
        initializer.put("DESCRIPTION", "They are tasty");
        initializer.put("AMOUNT", 1d);
        initializer.put("UPDATED", System.currentTimeMillis() + 1000);


        Struct initializer2 = new Struct(INPUT_SCHEMA);
        initializer2.put("NAME", "Potatoes");
        initializer2.put("DESCRIPTION", "They are tasty too");
        initializer2.put("AMOUNT", 2d);
        initializer2.put("UPDATED", System.currentTimeMillis() + 1000);

        Struct initializer3 = new Struct(INPUT_SCHEMA);
        initializer3.put("NAME", "Tomatoes");
        initializer3.put("DESCRIPTION", "They are tasty");
        initializer3.put("AMOUNT", 3d);
        initializer3.put("UPDATED", System.currentTimeMillis() - 1000); // some item in the past

        final Struct[] values = new Struct[] {initializer, initializer2, initializer3};


        List<Struct> runningList = udaf.initialize();
        for (final Struct i : values) {
            runningList = udaf.aggregate(i, runningList);
        }

        List<Struct> resultList = udaf.map(runningList);

        Assertions.assertEquals(2, resultList.size());
    }
}
