package org.apache.flink.cep.examples.demo;

import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.examples.demo.entity.LoginEvent;
import org.apache.flink.cep.examples.demo.entity.LoginWarning;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.IterativeCondition;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class FlinkLoginFail {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlinkLoginFail.class);

    public static class DataSource implements Iterator<LoginEvent>, Serializable {
        private final AtomicInteger atomicInteger = new AtomicInteger(0);
        private final List<LoginEvent> LoginEventList = Arrays.asList(
                new LoginEvent("1", "192.168.0.1", "fail"),
                new LoginEvent("1", "192.168.0.2", "fail"),
                new LoginEvent("1", "192.168.0.3", "fail"),
                new LoginEvent("2", "192.168.10,10", "success"),
                new LoginEvent("2", "192.168.0.4", "fail"),
                new LoginEvent("1", "192.168.0.5", "fail")
        );

        @Override
        public boolean hasNext() {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        public LoginEvent next() {
            return LoginEventList.get(atomicInteger.getAndIncrement() % LoginEventList.size());
        }
    }

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

//        DataStream<LoginEvent> loginEventStream = env.fromCollection(Arrays.asList(
//                new LoginEvent("1","192.168.0.1","fail"),
//                new LoginEvent("1","192.168.0.2","fail"),
//                new LoginEvent("1","192.168.0.3","fail"),
//                new LoginEvent("2","192.168.10,10","success"),
//                new LoginEvent("2","192.168.0.4","fail"),
//                new LoginEvent("2","192.168.0.5","fail")
//        ));
        DataStream<LoginEvent> loginEventStream = env.fromCollection(new DataSource(), LoginEvent.class);

        Pattern<LoginEvent, LoginEvent> loginFailPattern = Pattern.<LoginEvent>
                begin("begin")
                .where(new IterativeCondition<LoginEvent>() {
                    @Override
                    public boolean filter(LoginEvent loginEvent, Context context) throws Exception {
                        return loginEvent.getType().equals("fail");
                    }
                })
                .next("next")
                .where(new IterativeCondition<LoginEvent>() {
                    @Override
                    public boolean filter(LoginEvent loginEvent, Context context) throws Exception {
                        return loginEvent.getType().equals("fail");
                    }
                })
                .within(Time.seconds(3));

        PatternStream<LoginEvent> patternStream = CEP.pattern(
                loginEventStream.keyBy(LoginEvent::getUserId),
                loginFailPattern);

        DataStream<LoginWarning> loginFailDataStream = patternStream.select((Map<String, List<LoginEvent>> pattern) -> {
            List<LoginEvent> second = pattern.get("next");
            return new LoginWarning(second.get(0).getUserId(),second.get(0).getIp(), second.get(0).getType());
        });

        loginFailDataStream.print();

        env.execute();

        LOGGER.info("finish");
    }

}
