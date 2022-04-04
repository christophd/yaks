/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package demo;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.kie.api.event.process.ProcessNodeTriggeredEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.event.process.ProcessVariableChangedEvent;
import org.kie.kogito.internal.process.runtime.KogitoNodeInstance;
import org.kie.kogito.internal.process.runtime.KogitoProcessInstance;
import org.kie.kogito.internal.process.event.DefaultKogitoProcessEventListener;
import org.kie.kogito.process.impl.DefaultProcessEventListenerConfig;

@ApplicationScoped
public class ListenerConfig extends DefaultProcessEventListenerConfig {

    public ListenerConfig() {
    }

    @PostConstruct
    public void setup() {
        register(new DefaultKogitoProcessEventListener() {
            public void beforeProcessStarted(ProcessStartedEvent event) {
                System.out.println("Starting workflow " + event.getProcessInstance().getProcessId() + " (" + ((KogitoProcessInstance) event.getProcessInstance()).getStringId() + ")");
            }
            public void afterProcessStarted(ProcessStartedEvent event) {
                System.out.println("Workflow " + event.getProcessInstance().getProcessId() + " (" + ((KogitoProcessInstance) event.getProcessInstance()).getStringId() + ") was started, now " + getStatus(event.getProcessInstance().getState()));
            }
            public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
                String nodeName = event.getNodeInstance().getNodeName();
                if (!"EmbeddedStart".equals(nodeName) && !"EmbeddedEnd".equals(nodeName) && !"Script".equals(nodeName)) {
                    System.out.println("Triggered node " + nodeName + " (" + ((KogitoNodeInstance) event.getNodeInstance()).getStringId() + ") for process " + event.getProcessInstance().getProcessId() + " (" + ((KogitoProcessInstance) event.getProcessInstance()).getStringId() + ")");
                }
            }
            public void afterVariableChanged(ProcessVariableChangedEvent event) {
                System.out.println("Data changed: " + event.getNewValue());
            }
        });
    }

    private static String getStatus(int status) {
        switch (status) {
            case 0: return "PENDING";
            case 1: return "ACTIVE";
            case 2: return "COMPLETED";
            case 3: return "ABORTED";
            case 4: return "SUSPENDED";
            default: return "UNKNOWN " + status;
        }
    }

}

