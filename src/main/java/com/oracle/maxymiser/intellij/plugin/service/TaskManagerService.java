/*
 * Copyright © 2019 Oracle America Inc. and its affiliates. All rights reserved.
 *
 * Licensed under the Universal Permissive License (UPL) v 1.0 as shown at http://oss.oracle.com/licenses/upl/.
 */

package com.oracle.maxymiser.intellij.plugin.service;

import org.jetbrains.concurrency.Promise;

import java.util.concurrent.Callable;

public interface TaskManagerService {
    <T> Promise<T> async(Callable<T> task);
}
