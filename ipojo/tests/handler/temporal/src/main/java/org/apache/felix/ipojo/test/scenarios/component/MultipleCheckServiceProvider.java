/* 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.felix.ipojo.test.scenarios.component;

import java.util.Properties;

import org.apache.felix.ipojo.test.scenarios.temporal.service.CheckService;
import org.apache.felix.ipojo.test.scenarios.temporal.service.FooService;

public class MultipleCheckServiceProvider implements CheckService {
    
    /**
     * Temporal dependency.
     */
    private FooService[] fs;

    public boolean check() {
        boolean result = true;
        //Use a local variable to avoid to wait at each access.
        FooService[] array = fs;
        for (int i = 0; array != null && i < array.length; i++) {
            result = result && array[i].foo();
            System.out.println("Result : " + result);
        }
        return result;
    }

    public Properties getProps() {
        return fs[0].fooProps();
    }

}