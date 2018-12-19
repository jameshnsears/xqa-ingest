/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
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
package org.apache.qpid.jms.message.facade.test;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jms.JMSException;

import org.apache.qpid.jms.JmsDestination;
import org.apache.qpid.jms.message.facade.JmsMessageFacade;

/**
 * A test implementation of the JmsMessageFaceade that provides a generic
 * message instance which can be used instead of implemented in Provider specific
 * version that maps to a Provider message object.
 */
public class JmsTestMessageFacade implements JmsMessageFacade {

    private static final Charset UTF8 = Charset.forName("UTF-8");
    private Map<String, Object> properties = new HashMap<>();
    private int priority = javax.jms.Message.DEFAULT_PRIORITY;
    private String groupId;
    private int groupSequence;
    private Object messageId;
    private long expiration;
    private long deliveryTime;
    private boolean deliveryTimeTransmitted;
    private long timestamp;
    private String correlationId;
    private boolean persistent = true;
    private int redeliveryCount;
    private String type;
    private JmsDestination destination;
    private JmsDestination replyTo;
    private String userId;

    public JmsMsgType getMsgType() {
        return JmsMsgType.MESSAGE;
    }

    @Override
    public JmsTestMessageFacade copy() {
        JmsTestMessageFacade copy = new JmsTestMessageFacade();
        copyInto(copy);
        return copy;
    }

    void copyInto(JmsTestMessageFacade target) {
        target.priority = this.priority;
        target.groupSequence = this.groupSequence;
        target.groupId = this.groupId;
        target.expiration = this.expiration;
        target.deliveryTime = this.deliveryTime;
        target.timestamp = this.timestamp;
        target.correlationId = this.correlationId;
        target.persistent = this.persistent;
        target.redeliveryCount = this.redeliveryCount;
        target.type = this.type;
        target.destination = this.destination;
        target.replyTo = this.replyTo;
        target.userId = this.userId;
        target.messageId = this.messageId;

        if (this.properties != null) {
            target.properties = new HashMap<>(this.properties);
        } else {
            target.properties = null;
        }
    }

    @Override
    public Set<String> getPropertyNames() {
        Set<String> names = new HashSet<>();
        if (properties != null) {
            names.addAll(properties.keySet());
        }

        return names;
    }

    @Override
    public boolean propertyExists(String key) {
        return this.properties.containsKey(key);
    }

    @Override
    public Object getProperty(String key) {
        return this.properties.get(key);
    }

    @Override
    public void setProperty(String key, Object value) {
        this.properties.put(key, value);
    }

    @Override
    public void onSend(long producerTtl) throws JMSException {
    }

    @Override
    public void onDispatch() {
    }

    @Override
    public void clearBody() {
    }

    @Override
    public void clearProperties() {
        properties.clear();
    }

    @Override
    public String getMessageId() {
        return messageId == null ? null : String.valueOf(messageId);
    }

    @Override
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    @Override
    public Object getProviderMessageIdObject() {
        return messageId;
    }

    @Override
    public void setProviderMessageIdObject(Object messageId) {
        this.messageId = messageId;
    }

    @Override
    public long getTimestamp() {
        return this.timestamp;
    }

    @Override
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String getCorrelationId() {
        return correlationId;
    }

    @Override
    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    @Override
    public byte[] getCorrelationIdBytes() {
        return correlationId.getBytes(UTF8);
    }

    @Override
    public void setCorrelationIdBytes(byte[] correlationId) {
        if (correlationId != null && correlationId.length > 0) {
            this.correlationId = new String(correlationId, UTF8);
        } else {
            this.correlationId = null;
        }
    }

    @Override
    public boolean isPersistent() {
        return this.persistent;
    }

    @Override
    public void setPersistent(boolean value) {
        this.persistent = value;
    }

    @Override
    public int getDeliveryCount() {
        return this.redeliveryCount + 1;
    }

    @Override
    public void setDeliveryCount(int deliveryCount) {
        this.redeliveryCount = deliveryCount - 1;
    }

    @Override
    public int getRedeliveryCount() {
        return this.redeliveryCount;
    }

    @Override
    public void setRedeliveryCount(int redeliveryCount) {
        this.redeliveryCount = redeliveryCount;
    }

    @Override
    public boolean isRedelivered() {
        return redeliveryCount > 0;
    }

    @Override
    public void setRedelivered(boolean redelivered) {
        if (redelivered) {
            if (!isRedelivered()) {
                setRedeliveryCount(1);
            }
        } else {
            if (isRedelivered()) {
                setRedeliveryCount(0);
            }
        }
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void setPriority(int priority) {
        if (priority < 0) {
            this.priority = 0;
        } else if (priority > 9) {
            this.priority = 9;
        } else {
            this.priority = priority;
        }
    }

    @Override
    public long getExpiration() {
        return expiration;
    }

    @Override
    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

    @Override
    public long getDeliveryTime() {
        return deliveryTime;
    }

    @Override
    public void setDeliveryTime(long deliveryTime, boolean transmit) {
        this.deliveryTime = deliveryTime;
        this.deliveryTimeTransmitted = transmit;
    }

    @Override
    public boolean isDeliveryTimeTransmitted() {
        return deliveryTimeTransmitted;
    }

    @Override
    public JmsDestination getDestination() {
        return this.destination;
    }

    @Override
    public void setDestination(JmsDestination destination) {
        this.destination = destination;
    }

    @Override
    public JmsDestination getReplyTo() {
        return this.replyTo;
    }

    @Override
    public void setReplyTo(JmsDestination replyTo) {
        this.replyTo = replyTo;
    }

    @Override
    public String getUserId() {
        return this.userId;
    }

    @Override
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public byte[] getUserIdBytes() {
        return userId != null ? userId.getBytes(Charset.forName("UTF-8")) : null;
    }

    @Override
    public void setUserIdBytes(byte[] userId) {
        if (userId != null) {
            this.userId = new String(userId, Charset.forName("UTF-8"));
        } else {
            this.userId = null;
        }
    }

    @Override
    public String getGroupId() {
        return this.groupId;
    }

    @Override
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @Override
    public int getGroupSequence() {
        return this.groupSequence;
    }

    @Override
    public void setGroupSequence(int groupSequence) {
        this.groupSequence = groupSequence;
    }

    @Override
    public boolean hasBody() {
        return false;
    }

    @Override
    public Object encodeMessage() {
        return this;
    }

    public enum JmsMsgType {
        MESSAGE("jms/message"),
        BYTES("jms/bytes-message"),
        MAP("jms/map-message"),
        OBJECT("jms/object-message"),
        STREAM("jms/stream-message"),
        TEXT("jms/text-message"),
        TEXT_NULL("jms/text-message-null");

        public final String buffer = this.name();
        final String mime;

        JmsMsgType(String mime) {
            this.mime = mime;
        }
    }
}
