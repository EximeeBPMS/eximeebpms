package org.eximeebpms.bpm.client.topic.impl;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Supplier;

public interface ThreadPoolTaskExecutorSupplier extends Supplier<ThreadPoolExecutor> {
}
