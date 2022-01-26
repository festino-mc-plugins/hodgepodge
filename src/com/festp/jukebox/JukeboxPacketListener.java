package com.festp.jukebox;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.block.Jukebox;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.protocol.game.PacketPlayOutWorldEvent;

public class JukeboxPacketListener implements Listener {
	final JukeboxHandler handler;
	
	public JukeboxPacketListener(JukeboxHandler handler) {
		this.handler = handler;
	}
	
	@EventHandler
    public void onJoin(PlayerJoinEvent event){
        injectPlayer(event.getPlayer());
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event){
        removePlayer(event.getPlayer());
    }
    private void removePlayer(Player player) {
        Channel channel = ((CraftPlayer) player).getHandle().b.a.k; // .playerConnection.networkManager.channel
        channel.eventLoop().submit(() -> {
            channel.pipeline().remove(player.getName());
            return null;
        });
    }

    private void injectPlayer(Player player) {
        ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {

            @Override
            public void channelRead(ChannelHandlerContext channelHandlerContext, Object packet) throws Exception {
                //Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "PACKET READ: " + ChatColor.RED + packet.toString());
                super.channelRead(channelHandlerContext, packet);
            }

            @Override
            public void write(ChannelHandlerContext channelHandlerContext, Object packet, ChannelPromise channelPromise) throws Exception {
                if (packet instanceof PacketPlayOutWorldEvent) {
                	Object obj = getField(packet, "b");
                	if (!(obj instanceof BlockPosition)) {
                		// vanilla obfuscation has changed
                	}
                	BlockPosition pos = (BlockPosition) obj;
                	int x = pos.u(); // .getX()
                	int y = pos.v(); // .getY()
                	int z = pos.w(); // .getZ()
                	// world?
                	for (Jukebox jukebox : handler.getClickedJukeboxes()) {
                		if (jukebox.getX() == x && jukebox.getY() == y && jukebox.getZ() == z) {
                        	//Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "PACKET BLOCKED: " + ChatColor.GREEN + packetPlayOutWorldEvent.toString());
                        	return;
                		}
                	}
                	//Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "PACKET PASSED: " + ChatColor.GREEN + packetPlayOutWorldEvent.toString());
                }
                super.write(channelHandlerContext, packet, channelPromise);
            }


        };

        ChannelPipeline pipeline = ((CraftPlayer) player).getHandle().b.a.k.pipeline();
        pipeline.addBefore("packet_handler", player.getName(), channelDuplexHandler);

    }

    
    // https://github.com/frengor/PacketInjectorAPI/blob/master/src/main/java/com/fren_gor/packetInjectorAPI/ReflectionUtil.java
	private static final Map<String, Map<String, Field>> fields = new ConcurrentHashMap<>();
	
	public static Object getField(Object object, String field) {
		return getField(object, object.getClass(), field);
	}
	private static Object getField(Object object, Class<?> c, String field) {

		if (fields.containsKey(c.getCanonicalName())) {
			Map<String, Field> fs = fields.get(c.getCanonicalName());
			if (fs.containsKey(field)) {
				try {
					return fs.get(field).get(object);
				} catch (ReflectiveOperationException e) {
					return null;
				}
			}
		}

		Class<?> current = c;
		Field f;
		while (true)
			try {
				f = current.getDeclaredField(field);
				break;
			} catch (ReflectiveOperationException e1) {
				current = current.getSuperclass();
				if (current != null) {
					continue;
				}
				return null;
			}

		f.setAccessible(true);

		Map<String, Field> map;
		if (fields.containsKey(c.getCanonicalName())) {
			map = fields.get(c.getCanonicalName());
		} else {
			map = new ConcurrentHashMap<>();
			fields.put(c.getCanonicalName(), map);
		}

		map.put(f.getName(), f);

		try {
			return f.get(object);
		} catch (ReflectiveOperationException e) {
			return null;
		}

	}
}
