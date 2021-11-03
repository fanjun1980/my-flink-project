package org.apache.flink.table.examples.java.fanjun;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.typeutils.TupleTypeInfo;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.Types;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

/**
 * Author : fanjun
 * Date   : 2019/4/30
 * Desc   : explain; table -> DataStream
 */
public class TableExplain {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tEnv = StreamTableEnvironment.create(env); //TableEnvironment.getTableEnvironment(env);

        DataStream<Tuple2<Integer, String>> stream1 = env.fromElements(new Tuple2<>(1, "hello"));
        DataStream<Tuple2<Integer, String>> stream2 = env.fromElements(new Tuple2<>(1, "hello"));

        Table table1 = tEnv.fromDataStream(stream1, "count, word");
        Table table2 = tEnv.fromDataStream(stream2, "count, word");
        Table table = table1
                .where("LIKE(word, 'F%')")
                .unionAll(table2);

        String explanation = tEnv.explain(table);
        System.out.println(explanation);

        System.out.println("=================================");
        tEnv.toAppendStream(table, new TupleTypeInfo<>(Types.INT(),Types.STRING())).print();
        env.execute();
    }
}
