package com.mcjava20.mid_player;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.GameShuttingDownEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.lwjgl.glfw.GLFW;

@SuppressWarnings("unused")
@Mod(MIDPLAYER.MODID)
public class MIDPLAYER {
    public static final String MODID = "midplayer";
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static KeyMapping openGuiKey;

    // 独立客户端事件监听类（专门放ClientTick，不会总线冲突崩溃）
    public static class ClientEventListener {
        private final Minecraft mc = Minecraft.getInstance();

        @SubscribeEvent
        public void onClientTick(ClientTickEvent.Post tick) {
            while (openGuiKey.consumeClick()) {
                if (mc.screen instanceof MidPlayerGuiScreen) {
                    mc.setScreen(null);
                }
                mc.setScreen(new MidPlayerGuiScreen());
            }
        }
        
        @SubscribeEvent
        public void onGameShutdown(GameShuttingDownEvent event) {
            MidiPlayerManager.getInstance().savePlaylist();
        }
    }

    public MIDPLAYER(ModContainer container, IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        modEventBus.addListener(this::registerKeybind);
        modEventBus.addListener(this::clientInit);
    }

    // 注册按键绑定
    private void registerKeybind(RegisterKeyMappingsEvent event) {
        openGuiKey = new KeyMapping(
                "key.midplayer.open_gui",
                GLFW.GLFW_KEY_F8,
                "category.midplayer"
        );
        event.register(openGuiKey);
    }

    // 客户端初始化：直接注册监听类，不需要获取clientBus方法
    private void clientInit(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // 自动识别客户端事件，不会报总线类型错误
            ClientEventListener listener = new ClientEventListener();
            NeoForge.EVENT_BUS.register(listener);
            // 加载保存的歌单
            MidiPlayerManager.getInstance().loadPlaylist();
        });
    }
}