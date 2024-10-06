package github.kwizii.transport.compress;


import github.kwizii.extension.SPI;


@SPI
public interface Compress {

    byte[] compress(byte[] bytes);


    byte[] decompress(byte[] bytes);
}
