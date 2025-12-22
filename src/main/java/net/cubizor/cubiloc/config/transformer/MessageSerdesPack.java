package net.cubizor.cubiloc.config.transformer;

import eu.okaeri.configs.serdes.OkaeriSerdesPack;
import eu.okaeri.configs.serdes.SerdesRegistry;

/**
 * Okaeri serdes pack for message transformers.
 */
public class MessageSerdesPack implements OkaeriSerdesPack {

    @Override
    public void register(SerdesRegistry registry) {
        registry.register(new SingleMessageResultTransformer());
        registry.register(new ListMessageResultTransformer());
    }
}
