package github.kwizii.transport.serializer;

import github.kwizii.extension.SPI;

@SPI
public interface Serializer {
    byte[] serialize(Object obj);

    <T> T deserialize(byte[] bytes, Class<T> clazz);
}
