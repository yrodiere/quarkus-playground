/*
 * Sofis Solutions
 */
package io.quarkus.playground;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import java.io.IOException;
import org.hibernate.Hibernate;
import com.fasterxml.jackson.databind.type.CollectionType;
import java.util.logging.Logger;
import org.jboss.logmanager.Level;

public class CustomLazySerializer extends BeanSerializerModifier {

    private static final Logger LOGGER = Logger.getLogger(CustomLazySerializer.class.getName());

    @Override
    public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc, JsonSerializer<?> serializer) {
        LOGGER.log(Level.SEVERE, "Serializer:" + beanDesc.getBeanClass().getCanonicalName());
        return super.modifySerializer(config, beanDesc, serializer);
    }


    @Override
    public JsonSerializer<?> modifyCollectionSerializer(SerializationConfig config,
            CollectionType valueType, BeanDescription beanDesc, JsonSerializer<?> serializer) {
        LOGGER.log(Level.SEVERE, "Collection Serializer:" + beanDesc.getBeanClass().getCanonicalName());

        return new MyLazyCollectionJsonSerializer((JsonSerializer<Object>) serializer);
    }

    private class MyLazyCollectionJsonSerializer extends JsonSerializer<Object> {

        private final JsonSerializer<Object> serializer;

        public MyLazyCollectionJsonSerializer(JsonSerializer<Object> serializer) {
            this.serializer = serializer;
        }

        @Override
        public void serialize(Object value,
                JsonGenerator jgen,
                SerializerProvider provider) throws IOException {

            LOGGER.log(Level.SEVERE, "Value:" + value.getClass().getCanonicalName());

            if (!Hibernate.isInitialized(value)) {
                LOGGER.log(Level.SEVERE, "NOT INITIALIZED");
                provider.defaultSerializeNull(jgen);
                return;
            } else {
                LOGGER.log(Level.SEVERE, "INITIALIZED");
                serializer.serialize(value, jgen, provider);
            }
        }
    }

}
