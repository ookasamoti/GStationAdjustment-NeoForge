package net.ookasamoti.gstationmod.tan;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.PathPackResources;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.AddPackFindersEvent;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;

public final class TanTagPackRegistrar {

    private TanTagPackRegistrar(){}

    private static final String PACK_ID = "gstationmod:generated_tan_tags";

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent e) {
        if (e.getPackType() != PackType.SERVER_DATA) return;

        Path root = TanTagPackBuilder.outputRoot(FMLPaths.GAMEDIR.get());

        TanTagPackBuilder.rebuild(FMLPaths.GAMEDIR.get());

        RepositorySource source = (Consumer<Pack> consumer) -> {
            if (!java.nio.file.Files.exists(root.resolve("pack.mcmeta"))) return;

            PackLocationInfo info = new PackLocationInfo(
                    PACK_ID,
                    Component.literal("G-Station: TAN Tags"),
                    PackSource.BUILT_IN,
                    Optional.empty()
            );

            Pack pack = Pack.readMetaAndCreate(
                    info,
                    new PathPackResources.PathResourcesSupplier(root),
                    e.getPackType(),
                    new PackSelectionConfig(true, Pack.Position.TOP, false)
            );
            if (pack != null) consumer.accept(pack);
        };
        e.addRepositorySource(source);
    }
}
