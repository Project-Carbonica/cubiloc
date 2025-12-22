package net.cubizor.cubiloc.config.transformer;

import eu.okaeri.configs.schema.GenericsPair;
import eu.okaeri.configs.serdes.BidirectionalTransformer;
import eu.okaeri.configs.serdes.SerdesContext;
import net.cubizor.cubiloc.message.SingleMessageResult;
import org.jetbrains.annotations.NotNull;

/**
 * Okaeri transformer for SingleMessageResult.
 */
public class SingleMessageResultTransformer extends BidirectionalTransformer<String, SingleMessageResult> {

    @Override
    public GenericsPair<String, SingleMessageResult> getPair() {
        return this.genericsPair(String.class, SingleMessageResult.class);
    }

    @Override
    public SingleMessageResult leftToRight(@NotNull String data, @NotNull SerdesContext serdesContext) {
        return SingleMessageResult.of(data);
    }

    @Override
    public String rightToLeft(@NotNull SingleMessageResult data, @NotNull SerdesContext serdesContext) {
        return data.asString();
    }
}
