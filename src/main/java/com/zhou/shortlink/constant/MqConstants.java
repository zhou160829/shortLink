package com.zhou.shortlink.constant;

public interface MqConstants {
    interface Exchange {

        String SHORT_EXCHANGE = "short.topic";
        /*异常信息的交换机*/
        String ERROR_EXCHANGE = "error.topic";
        String SHORT_DELETE_EXCHANGE = "short.delete.topic";
    }

    interface Queue {
        String SHORT_QUEUE_TEMPLATE= "short.{}.queue";
        String ERROR_QUEUE_TEMPLATE = "error.{}.queue";
    }

    interface Key {

        String SHORT_KEY_PREFIX = "short.";
        String SHORT_COUNT_KEY_PREFIX = "short.count";
        String SHORT_DELETE_COUNT_KEY_PREFIX = "short.delete.count";
        /*异常RoutingKey的前缀*/
        String ERROR_KEY_PREFIX = "error.";
        String DEFAULT_ERROR_KEY = "error.#";

    }
}