package com.loohp.interactivechatdiscordsrvaddon.wrappers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapView;

import com.loohp.interactivechat.utils.FilledMapUtils;

public class ItemMapWrapper {
	
	@SuppressWarnings("deprecation")
	private static final Comparator<MapCursor> ICON_ORDER = Comparator.comparing(each -> each.getRawType());
	
	private ItemStack itemStack;
	private byte[] colors;
	private List<MapCursor> icons;
	
	public ItemMapWrapper(ItemStack itemStack, Player player) throws Exception {
		this.itemStack = itemStack;
		this.icons = new ArrayList<>();
		update(player);
	}
	
	public void update(Player player) throws Exception {
		if (!FilledMapUtils.isFilledMap(itemStack)) {
			throw new IllegalArgumentException("Provided item is not a filled map");
		}
		MapView mapView = FilledMapUtils.getMapView(itemStack);
		colors = FilledMapUtils.getColors(mapView, player);
		icons = FilledMapUtils.getCursors(mapView, player).stream().sorted(ICON_ORDER).collect(Collectors.toList());
	}
	
	public byte[] getColors() {
		return colors;
	}
	
	public List<MapCursor> getMapCursors() {
		return Collections.unmodifiableList(icons);
	}

}
