/*
 * Copyright © 2019 Oracle America Inc. and its affiliates. All rights reserved.
 *
 * Licensed under the Universal Permissive License (UPL) v 1.0 as shown at http://oss.oracle.com/licenses/upl/.
 */

package com.oracle.maxymiser.intellij.plugin.service.impl;

import com.oracle.maxymiser.intellij.plugin.service.TaskManagerService;
import org.jetbrains.concurrency.AsyncPromise;
import org.jetbrains.concurrency.Promise;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskManagerServiceImpl implements TaskManagerService {
    private final ExecutorService executorService;

    public TaskManagerServiceImpl() {
        this.executorService = Executors.newCachedThreadPool();
    }

    @Override
    public <T> Promise<T> async(Callable<T> task) {
        AsyncPromise<T> promise = new AsyncPromise<>();

        this.executorService.submit(() -> {
            try {
                promise.setResult(task.call());
            } catch (Exception e) {
                promise.setError(e);
            }
        });

        return promise;
    }
}
