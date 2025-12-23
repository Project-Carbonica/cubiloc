package net.cubizor.cubiloc.config.transformer;

import eu.okaeri.configs.schema.GenericsPair;
import eu.okaeri.configs.serdes.BidirectionalTransformer;
import eu.okaeri.configs.serdes.SerdesContext;
import net.cubizor.cubiloc.message.ListMessageResult;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Okaeri transformer for ListMessageResult.
 */
public class ListMessageResultTransformer extends BidirectionalTransformer<ArrayList, ListMessageResult> {

    @Override
    public GenericsPair<ArrayList, ListMessageResult> getPair() {
        return this.genericsPair(ArrayList.class, ListMessageResult.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ListMessageResult leftToRight(@NotNull ArrayList data, @NotNull SerdesContext serdesContext) {
        return ListMessageResult.of((List<String>) data);
    }

    @Override
    public ArrayList rightToLeft(@NotNull ListMessageResult data, @NotNull SerdesContext serdesContext) {
        return new ArrayList<>(data.raw());
    }
}
