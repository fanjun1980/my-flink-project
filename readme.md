### 一、配置信息
- org.apache.flink.configuration.ConfigConstants:配置名、默认值的静态变量
- org.apache.flink.configuration.*Options:各类型的配置项和默认值

### 二、例子
#### 2.1、Batch

|类型|说明|类|
|------|------|-----|
|基础|基于CollectExecution例子|org.apache.flink.examples.java.misc.CollectionExecutionExample|
|&nbsp;|wordcount|org.apache.flink.examples.java.wordcount.WordCount/WordCountPojo|
|迭代|Bulk Iterations|org.apache.flink.examples.java.graph.PageRank|
|&nbsp;|Broadcast variables in bulk iterations|org.apache.flink.examples.java.clustering.KMeans<br>org.apache.flink.examples.java.ml.LinearRegression|
|&nbsp;|增量迭代 Delta Iterations|org.apache.flink.examples.java.graph.ConnectedComponents|
|**关系运算**|custom data type derived from tuple data types<br>projection and join projection|org.apache.flink.examples.java.relational.TPCHQuery3<br>org.apache.flink.examples.java.relational.TPCHQuery10|
|&nbsp;|projection and join projection<br>the CoGroup transformation for an anti-join |org.apache.flink.examples.java.relational.WebLogAnalysis|
|&nbsp;|custom accumulators|org.apache.flink.examples.java.relational.EmptyFieldsCountAccumulator|

#### 2.2、Stream
1. ProcessWindowFunction
    - 使用ProcessWindowFunction进行简单的聚合(如count)是非常低效的。
    - ProcessWindowFunction可以与ReduceFunction、AggregateFunction或FoldFunction组合，以便在元素到达窗口时增量地聚合它们。当窗口关闭时，ProcessWindowFunction将提供聚合结果。
    - 可以使用旧版的WindowFunction而不是ProcessWindowFunction进行增量窗口聚合。(它提供的上下文信息较少，并且没有一些高级特性)
2. 触发器(Trigger)
    - 触发器决定窗口函数何时准备好处理窗口(由窗口分配程序形成)。每个WindowAssigner都带有一个默认触发器。
    - 一旦触发器确定窗口已准备好进行处理，就会触发，即返回FIRE或FIRE_AND_PURGE。这是窗口操作符发出当前窗口结果的信号。给定一个带有ProcessWindowFunction的窗口，所有元素都被传递给ProcessWindowFunction(可能在将它们传递给回收器之后)。具有ReduceFunction、AggregateFunction或FoldFunction的窗口只发出它们聚合的结果
3. 驱逐器(Evictors)
    - Flink的窗口模型允许在指定WindowAssigner和Trigger之外指定一个可选的Evictor。Evictor能够在Trigger触发之后以及在应用窗口函数之前和/或之后从窗口中删除元素。
    - 在evictBefore()包含窗口函数之前被施加驱逐逻辑，而evictAfter() 包含窗口函数之后要施加的逻辑。在应用窗口函数之前被逐出的元素将不会被它处理。
4. TypeInformation
    - stream.getType():Gets the type of the stream.
    - BasicTypeInfo:Type information for primitive types
    - TypeInformation.of(class)
    - TypeInformation.of(new TypeHint<AbstractStatisticsWrapper<AisMessage>>() {})
    - new TupleTypeInfo(TypeInformation.of(String.class), TypeInformation.of(Integer.class)) <br>
      TupleTypeInfo<Tuple2<String, Integer>> tupleType = new TupleTypeInfo<>(Types.STRING(),Types.INT());
      

|类型|说明|类|
|------|------|-----|
|基础|wordcount|org.apache.flink.streaming.examples.wordcount.WordCount|
|window|wordcount|org.apache.flink.streaming.examples.windowing.WindowWordCount<br>org.apache.flink.streaming.examples.socket.SocketWindowWordCount|
|&nbsp;|自定义source、Timestamp、Watermark|org.apache.flink.streaming.examples.windowing.SessionWindowing|
|&nbsp;|RichParallelSourceFunction、自定义sink|org.apache.flink.streaming.examples.windowing.GroupedProcessingTimeWindowExample|
|&nbsp;|驱逐器(Evictors)、触发器(Trigger)|org.apache.flink.streaming.examples.windowing.TopSpeedWindowing|
|Join|&nbsp;|org.apache.flink.streaming.examples.join.WindowJoin|
|SideOutput|&nbsp;|org.apache.flink.streaming.examples.sideoutput.SideOutputExample|
|AsyncIO|&nbsp;|org.apache.flink.streaming.examples.async.AsyncIOExample|
|statemachine|event pattern detection,详见readme<br>CEP更完整|org.apache.flink.streaming.examples.statemachine.StateMachineExample|
|gpu|how to use GPU resources in Flink|org.apache.flink.streaming.examples.gpu|

#### 2.3、Table
1.批处理Table只能BatchTableSink写入，而流式处理Table需要AppendStreamTableSink，RetractStreamTableSink或UpsertStreamTableSink

    - AppendStreamTableSink：只支持插入变更，如果Table对象同时有更新和删除的变更，那么将会抛出TableException；
    - RetractStreamTableSink：支持输出一个streaming模式的表，该表上可以有插入和删除变更；(A retract stream of type X is a DataStream<Tuple2<Boolean, X>>. The boolean field indicates the type of the change. True is INSERT, false is DELETE.)
    - UpsertStreamTableSink：支持输出一个streaming模式的表，该表上可以有插入、更新和删除变更，且还要求表要么有唯一的键字段要么是append-only模式的，如果两者都不满足，将抛出TableException；

2.XXX

#### 2.4、CEP

