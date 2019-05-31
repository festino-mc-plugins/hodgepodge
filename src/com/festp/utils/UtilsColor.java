package com.festp.utils;

import org.bukkit.DyeColor;
import org.bukkit.Material;

public class UtilsColor {


	//(wall) banner, bed, carpet, concrete, concrete powder, glass(pane), wool, shulker box, terracotta, glazed, dye or null
	public static DyeColor colorFromMaterial(Material m) {
		switch(m) {
		//banner
		case WHITE_BANNER: return DyeColor.WHITE;
		case ORANGE_BANNER: return DyeColor.ORANGE;
		case MAGENTA_BANNER: return DyeColor.MAGENTA;
		case LIGHT_BLUE_BANNER: return DyeColor.LIGHT_BLUE;
		case YELLOW_BANNER: return DyeColor.YELLOW;
		case LIME_BANNER: return DyeColor.LIME;
		case PINK_BANNER: return DyeColor.PINK;
		case GRAY_BANNER: return DyeColor.GRAY;
		case LIGHT_GRAY_BANNER: return DyeColor.LIGHT_GRAY;
		case CYAN_BANNER: return DyeColor.CYAN;
		case PURPLE_BANNER: return DyeColor.PURPLE;
		case BLUE_BANNER: return DyeColor.BLUE;
		case BROWN_BANNER: return DyeColor.BROWN;
		case GREEN_BANNER: return DyeColor.GREEN;
		case RED_BANNER: return DyeColor.RED;
		case BLACK_BANNER: return DyeColor.BLACK;
		//wall_banner
		case WHITE_WALL_BANNER: return DyeColor.WHITE;
		case ORANGE_WALL_BANNER: return DyeColor.ORANGE;
		case MAGENTA_WALL_BANNER: return DyeColor.MAGENTA;
		case LIGHT_BLUE_WALL_BANNER: return DyeColor.LIGHT_BLUE;
		case YELLOW_WALL_BANNER: return DyeColor.YELLOW;
		case LIME_WALL_BANNER: return DyeColor.LIME;
		case PINK_WALL_BANNER: return DyeColor.PINK;
		case GRAY_WALL_BANNER: return DyeColor.GRAY;
		case LIGHT_GRAY_WALL_BANNER: return DyeColor.LIGHT_GRAY;
		case CYAN_WALL_BANNER: return DyeColor.CYAN;
		case PURPLE_WALL_BANNER: return DyeColor.PURPLE;
		case BLUE_WALL_BANNER: return DyeColor.BLUE;
		case BROWN_WALL_BANNER: return DyeColor.BROWN;
		case GREEN_WALL_BANNER: return DyeColor.GREEN;
		case RED_WALL_BANNER: return DyeColor.RED;
		case BLACK_WALL_BANNER: return DyeColor.BLACK;
		//bed
		case WHITE_BED: return DyeColor.WHITE;
		case ORANGE_BED: return DyeColor.ORANGE;
		case MAGENTA_BED: return DyeColor.MAGENTA;
		case LIGHT_BLUE_BED: return DyeColor.LIGHT_BLUE;
		case YELLOW_BED: return DyeColor.YELLOW;
		case LIME_BED: return DyeColor.LIME;
		case PINK_BED: return DyeColor.PINK;
		case GRAY_BED: return DyeColor.GRAY;
		case LIGHT_GRAY_BED: return DyeColor.LIGHT_GRAY;
		case CYAN_BED: return DyeColor.CYAN;
		case PURPLE_BED: return DyeColor.PURPLE;
		case BLUE_BED: return DyeColor.BLUE;
		case BROWN_BED: return DyeColor.BROWN;
		case GREEN_BED: return DyeColor.GREEN;
		case RED_BED: return DyeColor.RED;
		case BLACK_BED: return DyeColor.BLACK;
		//carpet
		case WHITE_CARPET: return DyeColor.WHITE;
		case ORANGE_CARPET: return DyeColor.ORANGE;
		case MAGENTA_CARPET: return DyeColor.MAGENTA;
		case LIGHT_BLUE_CARPET: return DyeColor.LIGHT_BLUE;
		case YELLOW_CARPET: return DyeColor.YELLOW;
		case LIME_CARPET: return DyeColor.LIME;
		case PINK_CARPET: return DyeColor.PINK;
		case GRAY_CARPET: return DyeColor.GRAY;
		case LIGHT_GRAY_CARPET: return DyeColor.LIGHT_GRAY;
		case CYAN_CARPET: return DyeColor.CYAN;
		case PURPLE_CARPET: return DyeColor.PURPLE;
		case BLUE_CARPET: return DyeColor.BLUE;
		case BROWN_CARPET: return DyeColor.BROWN;
		case GREEN_CARPET: return DyeColor.GREEN;
		case RED_CARPET: return DyeColor.RED;
		case BLACK_CARPET: return DyeColor.BLACK;
		//concrete_powder
		case WHITE_CONCRETE_POWDER: return DyeColor.WHITE;
		case ORANGE_CONCRETE_POWDER: return DyeColor.ORANGE;
		case MAGENTA_CONCRETE_POWDER: return DyeColor.MAGENTA;
		case LIGHT_BLUE_CONCRETE_POWDER: return DyeColor.LIGHT_BLUE;
		case YELLOW_CONCRETE_POWDER: return DyeColor.YELLOW;
		case LIME_CONCRETE_POWDER: return DyeColor.LIME;
		case PINK_CONCRETE_POWDER: return DyeColor.PINK;
		case GRAY_CONCRETE_POWDER: return DyeColor.GRAY;
		case LIGHT_GRAY_CONCRETE_POWDER: return DyeColor.LIGHT_GRAY;
		case CYAN_CONCRETE_POWDER: return DyeColor.CYAN;
		case PURPLE_CONCRETE_POWDER: return DyeColor.PURPLE;
		case BLUE_CONCRETE_POWDER: return DyeColor.BLUE;
		case BROWN_CONCRETE_POWDER: return DyeColor.BROWN;
		case GREEN_CONCRETE_POWDER: return DyeColor.GREEN;
		case RED_CONCRETE_POWDER: return DyeColor.RED;
		case BLACK_CONCRETE_POWDER: return DyeColor.BLACK;
		//concrete
		case WHITE_CONCRETE: return DyeColor.WHITE;
		case ORANGE_CONCRETE: return DyeColor.ORANGE;
		case MAGENTA_CONCRETE: return DyeColor.MAGENTA;
		case LIGHT_BLUE_CONCRETE: return DyeColor.LIGHT_BLUE;
		case YELLOW_CONCRETE: return DyeColor.YELLOW;
		case LIME_CONCRETE: return DyeColor.LIME;
		case PINK_CONCRETE: return DyeColor.PINK;
		case GRAY_CONCRETE: return DyeColor.GRAY;
		case LIGHT_GRAY_CONCRETE: return DyeColor.LIGHT_GRAY;
		case CYAN_CONCRETE: return DyeColor.CYAN;
		case PURPLE_CONCRETE: return DyeColor.PURPLE;
		case BLUE_CONCRETE: return DyeColor.BLUE;
		case BROWN_CONCRETE: return DyeColor.BROWN;
		case GREEN_CONCRETE: return DyeColor.GREEN;
		case RED_CONCRETE: return DyeColor.RED;
		case BLACK_CONCRETE: return DyeColor.BLACK;
		//glass
		case WHITE_STAINED_GLASS: return DyeColor.WHITE;
		case ORANGE_STAINED_GLASS: return DyeColor.ORANGE;
		case MAGENTA_STAINED_GLASS: return DyeColor.MAGENTA;
		case LIGHT_BLUE_STAINED_GLASS: return DyeColor.LIGHT_BLUE;
		case YELLOW_STAINED_GLASS: return DyeColor.YELLOW;
		case LIME_STAINED_GLASS: return DyeColor.LIME;
		case PINK_STAINED_GLASS: return DyeColor.PINK;
		case GRAY_STAINED_GLASS: return DyeColor.GRAY;
		case LIGHT_GRAY_STAINED_GLASS: return DyeColor.LIGHT_GRAY;
		case CYAN_STAINED_GLASS: return DyeColor.CYAN;
		case PURPLE_STAINED_GLASS: return DyeColor.PURPLE;
		case BLUE_STAINED_GLASS: return DyeColor.BLUE;
		case BROWN_STAINED_GLASS: return DyeColor.BROWN;
		case GREEN_STAINED_GLASS: return DyeColor.GREEN;
		case RED_STAINED_GLASS: return DyeColor.RED;
		case BLACK_STAINED_GLASS: return DyeColor.BLACK;
		//glass_pane
		case WHITE_STAINED_GLASS_PANE: return DyeColor.WHITE;
		case ORANGE_STAINED_GLASS_PANE: return DyeColor.ORANGE;
		case MAGENTA_STAINED_GLASS_PANE: return DyeColor.MAGENTA;
		case LIGHT_BLUE_STAINED_GLASS_PANE: return DyeColor.LIGHT_BLUE;
		case YELLOW_STAINED_GLASS_PANE: return DyeColor.YELLOW;
		case LIME_STAINED_GLASS_PANE: return DyeColor.LIME;
		case PINK_STAINED_GLASS_PANE: return DyeColor.PINK;
		case GRAY_STAINED_GLASS_PANE: return DyeColor.GRAY;
		case LIGHT_GRAY_STAINED_GLASS_PANE: return DyeColor.LIGHT_GRAY;
		case CYAN_STAINED_GLASS_PANE: return DyeColor.CYAN;
		case PURPLE_STAINED_GLASS_PANE: return DyeColor.PURPLE;
		case BLUE_STAINED_GLASS_PANE: return DyeColor.BLUE;
		case BROWN_STAINED_GLASS_PANE: return DyeColor.BROWN;
		case GREEN_STAINED_GLASS_PANE: return DyeColor.GREEN;
		case RED_STAINED_GLASS_PANE: return DyeColor.RED;
		case BLACK_STAINED_GLASS_PANE: return DyeColor.BLACK;
		//wool
		case WHITE_WOOL: return DyeColor.WHITE;
		case ORANGE_WOOL: return DyeColor.ORANGE;
		case MAGENTA_WOOL: return DyeColor.MAGENTA;
		case LIGHT_BLUE_WOOL: return DyeColor.LIGHT_BLUE;
		case YELLOW_WOOL: return DyeColor.YELLOW;
		case LIME_WOOL: return DyeColor.LIME;
		case PINK_WOOL: return DyeColor.PINK;
		case GRAY_WOOL: return DyeColor.GRAY;
		case LIGHT_GRAY_WOOL: return DyeColor.LIGHT_GRAY;
		case CYAN_WOOL: return DyeColor.CYAN;
		case PURPLE_WOOL: return DyeColor.PURPLE;
		case BLUE_WOOL: return DyeColor.BLUE;
		case BROWN_WOOL: return DyeColor.BROWN;
		case GREEN_WOOL: return DyeColor.GREEN;
		case RED_WOOL: return DyeColor.RED;
		case BLACK_WOOL: return DyeColor.BLACK;
		//shulker_box
		case WHITE_SHULKER_BOX: return DyeColor.WHITE;
		case ORANGE_SHULKER_BOX: return DyeColor.ORANGE;
		case MAGENTA_SHULKER_BOX: return DyeColor.MAGENTA;
		case LIGHT_BLUE_SHULKER_BOX: return DyeColor.LIGHT_BLUE;
		case YELLOW_SHULKER_BOX: return DyeColor.YELLOW;
		case LIME_SHULKER_BOX: return DyeColor.LIME;
		case PINK_SHULKER_BOX: return DyeColor.PINK;
		case GRAY_SHULKER_BOX: return DyeColor.GRAY;
		case LIGHT_GRAY_SHULKER_BOX: return DyeColor.LIGHT_GRAY;
		case CYAN_SHULKER_BOX: return DyeColor.CYAN;
		case PURPLE_SHULKER_BOX: return DyeColor.PURPLE;
		case BLUE_SHULKER_BOX: return DyeColor.BLUE;
		case BROWN_SHULKER_BOX: return DyeColor.BROWN;
		case GREEN_SHULKER_BOX: return DyeColor.GREEN;
		case RED_SHULKER_BOX: return DyeColor.RED;
		case BLACK_SHULKER_BOX: return DyeColor.BLACK;
		//terracotta
		case WHITE_TERRACOTTA: return DyeColor.WHITE;
		case ORANGE_TERRACOTTA: return DyeColor.ORANGE;
		case MAGENTA_TERRACOTTA: return DyeColor.MAGENTA;
		case LIGHT_BLUE_TERRACOTTA: return DyeColor.LIGHT_BLUE;
		case YELLOW_TERRACOTTA: return DyeColor.YELLOW;
		case LIME_TERRACOTTA: return DyeColor.LIME;
		case PINK_TERRACOTTA: return DyeColor.PINK;
		case GRAY_TERRACOTTA: return DyeColor.GRAY;
		case LIGHT_GRAY_TERRACOTTA: return DyeColor.LIGHT_GRAY;
		case CYAN_TERRACOTTA: return DyeColor.CYAN;
		case PURPLE_TERRACOTTA: return DyeColor.PURPLE;
		case BLUE_TERRACOTTA: return DyeColor.BLUE;
		case BROWN_TERRACOTTA: return DyeColor.BROWN;
		case GREEN_TERRACOTTA: return DyeColor.GREEN;
		case RED_TERRACOTTA: return DyeColor.RED;
		case BLACK_TERRACOTTA: return DyeColor.BLACK;
		//glazed_terracotta
		case WHITE_GLAZED_TERRACOTTA: return DyeColor.WHITE;
		case ORANGE_GLAZED_TERRACOTTA: return DyeColor.ORANGE;
		case MAGENTA_GLAZED_TERRACOTTA: return DyeColor.MAGENTA;
		case LIGHT_BLUE_GLAZED_TERRACOTTA: return DyeColor.LIGHT_BLUE;
		case YELLOW_GLAZED_TERRACOTTA: return DyeColor.YELLOW;
		case LIME_GLAZED_TERRACOTTA: return DyeColor.LIME;
		case PINK_GLAZED_TERRACOTTA: return DyeColor.PINK;
		case GRAY_GLAZED_TERRACOTTA: return DyeColor.GRAY;
		case LIGHT_GRAY_GLAZED_TERRACOTTA: return DyeColor.LIGHT_GRAY;
		case CYAN_GLAZED_TERRACOTTA: return DyeColor.CYAN;
		case PURPLE_GLAZED_TERRACOTTA: return DyeColor.PURPLE;
		case BLUE_GLAZED_TERRACOTTA: return DyeColor.BLUE;
		case BROWN_GLAZED_TERRACOTTA: return DyeColor.BROWN;
		case GREEN_GLAZED_TERRACOTTA: return DyeColor.GREEN;
		case RED_GLAZED_TERRACOTTA: return DyeColor.RED;
		case BLACK_GLAZED_TERRACOTTA: return DyeColor.BLACK;
		//dyes
		case WHITE_DYE: return DyeColor.WHITE;
		case ORANGE_DYE: return DyeColor.ORANGE;
		case MAGENTA_DYE: return DyeColor.MAGENTA;
		case LIGHT_BLUE_DYE: return DyeColor.LIGHT_BLUE;
		case YELLOW_DYE: return DyeColor.YELLOW;
		case LIME_DYE: return DyeColor.LIME;
		case PINK_DYE: return DyeColor.PINK;
		case GRAY_DYE: return DyeColor.GRAY;
		case LIGHT_GRAY_DYE: return DyeColor.LIGHT_GRAY;
		case CYAN_DYE: return DyeColor.CYAN;
		case PURPLE_DYE: return DyeColor.PURPLE;
		case BLUE_DYE: return DyeColor.BLUE;
		case BROWN_DYE: return DyeColor.BROWN;
		case GREEN_DYE: return DyeColor.GREEN;
		case RED_DYE: return DyeColor.RED;
		case BLACK_DYE: return DyeColor.BLACK;
		default: return null;
		}
	}
	
	public static Material fromColor_banner(DyeColor color) {
		switch(color) {
		case WHITE: return Material.WHITE_BANNER; 
		case ORANGE: return Material.ORANGE_BANNER;
		case MAGENTA: return Material.MAGENTA_BANNER;
		case LIGHT_BLUE: return Material.LIGHT_BLUE_BANNER;
		case YELLOW: return Material.YELLOW_BANNER;
		case LIME: return Material.LIME_BANNER;
		case PINK: return Material.PINK_BANNER;
		case GRAY: return Material.GRAY_BANNER;
		case LIGHT_GRAY: return Material.LIGHT_GRAY_BANNER;
		case CYAN: return Material.CYAN_BANNER;
		case PURPLE: return Material.PURPLE_BANNER;
		case BLUE: return Material.BLUE_BANNER;
		case BROWN: return Material.BROWN_BANNER;
		case GREEN: return Material.GREEN_BANNER;
		case RED: return Material.RED_BANNER;
		case BLACK: return Material.BLACK_BANNER;
		default: return null;
		}
	}
	
	public static Material fromColor_wall_banner(DyeColor color) {
		switch(color) {
		case WHITE: return Material.WHITE_WALL_BANNER; 
		case ORANGE: return Material.ORANGE_WALL_BANNER;
		case MAGENTA: return Material.MAGENTA_WALL_BANNER;
		case LIGHT_BLUE: return Material.LIGHT_BLUE_WALL_BANNER;
		case YELLOW: return Material.YELLOW_WALL_BANNER;
		case LIME: return Material.LIME_WALL_BANNER;
		case PINK: return Material.PINK_WALL_BANNER;
		case GRAY: return Material.GRAY_WALL_BANNER;
		case LIGHT_GRAY: return Material.LIGHT_GRAY_WALL_BANNER;
		case CYAN: return Material.CYAN_WALL_BANNER;
		case PURPLE: return Material.PURPLE_WALL_BANNER;
		case BLUE: return Material.BLUE_WALL_BANNER;
		case BROWN: return Material.BROWN_WALL_BANNER;
		case GREEN: return Material.GREEN_WALL_BANNER;
		case RED: return Material.RED_WALL_BANNER;
		case BLACK: return Material.BLACK_WALL_BANNER;
		default: return null;
		}
	}
	
	public static Material fromColor_bed(DyeColor color) {
		switch(color) {
		case WHITE: return Material.WHITE_BED; 
		case ORANGE: return Material.ORANGE_BED;
		case MAGENTA: return Material.MAGENTA_BED;
		case LIGHT_BLUE: return Material.LIGHT_BLUE_BED;
		case YELLOW: return Material.YELLOW_BED;
		case LIME: return Material.LIME_BED;
		case PINK: return Material.PINK_BED;
		case GRAY: return Material.GRAY_BED;
		case LIGHT_GRAY: return Material.LIGHT_GRAY_BED;
		case CYAN: return Material.CYAN_BED;
		case PURPLE: return Material.PURPLE_BED;
		case BLUE: return Material.BLUE_BED;
		case BROWN: return Material.BROWN_BED;
		case GREEN: return Material.GREEN_BED;
		case RED: return Material.RED_BED;
		case BLACK: return Material.BLACK_BED;
		default: return null;
		}
	}
	
	public static Material fromColor_carpet(DyeColor color) {
		switch(color) {
		case WHITE: return Material.WHITE_CARPET; 
		case ORANGE: return Material.ORANGE_CARPET;
		case MAGENTA: return Material.MAGENTA_CARPET;
		case LIGHT_BLUE: return Material.LIGHT_BLUE_CARPET;
		case YELLOW: return Material.YELLOW_CARPET;
		case LIME: return Material.LIME_CARPET;
		case PINK: return Material.PINK_CARPET;
		case GRAY: return Material.GRAY_CARPET;
		case LIGHT_GRAY: return Material.LIGHT_GRAY_CARPET;
		case CYAN: return Material.CYAN_CARPET;
		case PURPLE: return Material.PURPLE_CARPET;
		case BLUE: return Material.BLUE_CARPET;
		case BROWN: return Material.BROWN_CARPET;
		case GREEN: return Material.GREEN_CARPET;
		case RED: return Material.RED_CARPET;
		case BLACK: return Material.BLACK_CARPET;
		default: return null;
		}
	}
	
	public static Material fromColor_concrete(DyeColor color) {
		switch(color) {
		case WHITE: return Material.WHITE_CONCRETE; 
		case ORANGE: return Material.ORANGE_CONCRETE;
		case MAGENTA: return Material.MAGENTA_CONCRETE;
		case LIGHT_BLUE: return Material.LIGHT_BLUE_CONCRETE;
		case YELLOW: return Material.YELLOW_CONCRETE;
		case LIME: return Material.LIME_CONCRETE;
		case PINK: return Material.PINK_CONCRETE;
		case GRAY: return Material.GRAY_CONCRETE;
		case LIGHT_GRAY: return Material.LIGHT_GRAY_CONCRETE;
		case CYAN: return Material.CYAN_CONCRETE;
		case PURPLE: return Material.PURPLE_CONCRETE;
		case BLUE: return Material.BLUE_CONCRETE;
		case BROWN: return Material.BROWN_CONCRETE;
		case GREEN: return Material.GREEN_CONCRETE;
		case RED: return Material.RED_CONCRETE;
		case BLACK: return Material.BLACK_CONCRETE;
		default: return null;
		}
	}
	
	public static Material fromColor_concrete_powder(DyeColor color) {
		switch(color) {
		case WHITE: return Material.WHITE_CONCRETE_POWDER; 
		case ORANGE: return Material.ORANGE_CONCRETE_POWDER;
		case MAGENTA: return Material.MAGENTA_CONCRETE_POWDER;
		case LIGHT_BLUE: return Material.LIGHT_BLUE_CONCRETE_POWDER;
		case YELLOW: return Material.YELLOW_CONCRETE_POWDER;
		case LIME: return Material.LIME_CONCRETE_POWDER;
		case PINK: return Material.PINK_CONCRETE_POWDER;
		case GRAY: return Material.GRAY_CONCRETE_POWDER;
		case LIGHT_GRAY: return Material.LIGHT_GRAY_CONCRETE_POWDER;
		case CYAN: return Material.CYAN_CONCRETE_POWDER;
		case PURPLE: return Material.PURPLE_CONCRETE_POWDER;
		case BLUE: return Material.BLUE_CONCRETE_POWDER;
		case BROWN: return Material.BROWN_CONCRETE_POWDER;
		case GREEN: return Material.GREEN_CONCRETE_POWDER;
		case RED: return Material.RED_CONCRETE_POWDER;
		case BLACK: return Material.BLACK_CONCRETE_POWDER;
		default: return null;
		}
	}
	
	public static Material fromColor_stained_glass(DyeColor color) {
		switch(color) {
		case WHITE: return Material.WHITE_STAINED_GLASS; 
		case ORANGE: return Material.ORANGE_STAINED_GLASS;
		case MAGENTA: return Material.MAGENTA_STAINED_GLASS;
		case LIGHT_BLUE: return Material.LIGHT_BLUE_STAINED_GLASS;
		case YELLOW: return Material.YELLOW_STAINED_GLASS;
		case LIME: return Material.LIME_STAINED_GLASS;
		case PINK: return Material.PINK_STAINED_GLASS;
		case GRAY: return Material.GRAY_STAINED_GLASS;
		case LIGHT_GRAY: return Material.LIGHT_GRAY_STAINED_GLASS;
		case CYAN: return Material.CYAN_STAINED_GLASS;
		case PURPLE: return Material.PURPLE_STAINED_GLASS;
		case BLUE: return Material.BLUE_STAINED_GLASS;
		case BROWN: return Material.BROWN_STAINED_GLASS;
		case GREEN: return Material.GREEN_STAINED_GLASS;
		case RED: return Material.RED_STAINED_GLASS;
		case BLACK: return Material.BLACK_STAINED_GLASS;
		default: return null;
		}
	}
	
	public static Material fromColor_stained_glass_pane(DyeColor color) {
		switch(color) {
		case WHITE: return Material.WHITE_STAINED_GLASS_PANE; 
		case ORANGE: return Material.ORANGE_STAINED_GLASS_PANE;
		case MAGENTA: return Material.MAGENTA_STAINED_GLASS_PANE;
		case LIGHT_BLUE: return Material.LIGHT_BLUE_STAINED_GLASS_PANE;
		case YELLOW: return Material.YELLOW_STAINED_GLASS_PANE;
		case LIME: return Material.LIME_STAINED_GLASS_PANE;
		case PINK: return Material.PINK_STAINED_GLASS_PANE;
		case GRAY: return Material.GRAY_STAINED_GLASS_PANE;
		case LIGHT_GRAY: return Material.LIGHT_GRAY_STAINED_GLASS_PANE;
		case CYAN: return Material.CYAN_STAINED_GLASS_PANE;
		case PURPLE: return Material.PURPLE_STAINED_GLASS_PANE;
		case BLUE: return Material.BLUE_STAINED_GLASS_PANE;
		case BROWN: return Material.BROWN_STAINED_GLASS_PANE;
		case GREEN: return Material.GREEN_STAINED_GLASS_PANE;
		case RED: return Material.RED_STAINED_GLASS_PANE;
		case BLACK: return Material.BLACK_STAINED_GLASS_PANE;
		default: return null;
		}
	}
	
	public static Material fromColor_wool(DyeColor color) {
		switch(color) {
		case WHITE: return Material.WHITE_WOOL; 
		case ORANGE: return Material.ORANGE_WOOL;
		case MAGENTA: return Material.MAGENTA_WOOL;
		case LIGHT_BLUE: return Material.LIGHT_BLUE_WOOL;
		case YELLOW: return Material.YELLOW_WOOL;
		case LIME: return Material.LIME_WOOL;
		case PINK: return Material.PINK_WOOL;
		case GRAY: return Material.GRAY_WOOL;
		case LIGHT_GRAY: return Material.LIGHT_GRAY_WOOL;
		case CYAN: return Material.CYAN_WOOL;
		case PURPLE: return Material.PURPLE_WOOL;
		case BLUE: return Material.BLUE_WOOL;
		case BROWN: return Material.BROWN_WOOL;
		case GREEN: return Material.GREEN_WOOL;
		case RED: return Material.RED_WOOL;
		case BLACK: return Material.BLACK_WOOL;
		default: return null;
		}
	}
	
	public static Material fromColor_shulker_box(DyeColor color) {
		switch(color) {
		case WHITE: return Material.WHITE_SHULKER_BOX; 
		case ORANGE: return Material.ORANGE_SHULKER_BOX;
		case MAGENTA: return Material.MAGENTA_SHULKER_BOX;
		case LIGHT_BLUE: return Material.LIGHT_BLUE_SHULKER_BOX;
		case YELLOW: return Material.YELLOW_SHULKER_BOX;
		case LIME: return Material.LIME_SHULKER_BOX;
		case PINK: return Material.PINK_SHULKER_BOX;
		case GRAY: return Material.GRAY_SHULKER_BOX;
		case LIGHT_GRAY: return Material.LIGHT_GRAY_SHULKER_BOX;
		case CYAN: return Material.CYAN_SHULKER_BOX;
		case PURPLE: return Material.PURPLE_SHULKER_BOX;
		case BLUE: return Material.BLUE_SHULKER_BOX;
		case BROWN: return Material.BROWN_SHULKER_BOX;
		case GREEN: return Material.GREEN_SHULKER_BOX;
		case RED: return Material.RED_SHULKER_BOX;
		case BLACK: return Material.BLACK_SHULKER_BOX;
		default: return null;
		}
	}
	
	public static Material fromColor_terracotta(DyeColor color) {
		switch(color) {
		case WHITE: return Material.WHITE_TERRACOTTA; 
		case ORANGE: return Material.ORANGE_TERRACOTTA;
		case MAGENTA: return Material.MAGENTA_TERRACOTTA;
		case LIGHT_BLUE: return Material.LIGHT_BLUE_TERRACOTTA;
		case YELLOW: return Material.YELLOW_TERRACOTTA;
		case LIME: return Material.LIME_TERRACOTTA;
		case PINK: return Material.PINK_TERRACOTTA;
		case GRAY: return Material.GRAY_TERRACOTTA;
		case LIGHT_GRAY: return Material.LIGHT_GRAY_TERRACOTTA;
		case CYAN: return Material.CYAN_TERRACOTTA;
		case PURPLE: return Material.PURPLE_TERRACOTTA;
		case BLUE: return Material.BLUE_TERRACOTTA;
		case BROWN: return Material.BROWN_TERRACOTTA;
		case GREEN: return Material.GREEN_TERRACOTTA;
		case RED: return Material.RED_TERRACOTTA;
		case BLACK: return Material.BLACK_TERRACOTTA;
		default: return null;
		}
	}

	public static Material fromColor_glazed_terracotta(DyeColor color) {
		switch(color) {
		case WHITE: return Material.WHITE_GLAZED_TERRACOTTA; 
		case ORANGE: return Material.ORANGE_GLAZED_TERRACOTTA;
		case MAGENTA: return Material.MAGENTA_GLAZED_TERRACOTTA;
		case LIGHT_BLUE: return Material.LIGHT_BLUE_GLAZED_TERRACOTTA;
		case YELLOW: return Material.YELLOW_GLAZED_TERRACOTTA;
		case LIME: return Material.LIME_GLAZED_TERRACOTTA;
		case PINK: return Material.PINK_GLAZED_TERRACOTTA;
		case GRAY: return Material.GRAY_GLAZED_TERRACOTTA;
		case LIGHT_GRAY: return Material.LIGHT_GRAY_GLAZED_TERRACOTTA;
		case CYAN: return Material.CYAN_GLAZED_TERRACOTTA;
		case PURPLE: return Material.PURPLE_GLAZED_TERRACOTTA;
		case BLUE: return Material.BLUE_GLAZED_TERRACOTTA;
		case BROWN: return Material.BROWN_GLAZED_TERRACOTTA;
		case GREEN: return Material.GREEN_GLAZED_TERRACOTTA;
		case RED: return Material.RED_GLAZED_TERRACOTTA;
		case BLACK: return Material.BLACK_GLAZED_TERRACOTTA;
		default: return null;
		}
	}
	
	public static Material fromColor_dye(DyeColor color) {
		switch(color) {
		case WHITE: return Material.WHITE_DYE; 
		case ORANGE: return Material.ORANGE_DYE;
		case MAGENTA: return Material.MAGENTA_DYE;
		case LIGHT_BLUE: return Material.LIGHT_BLUE_DYE;
		case YELLOW: return Material.YELLOW_DYE;
		case LIME: return Material.LIME_DYE;
		case PINK: return Material.PINK_DYE;
		case GRAY: return Material.GRAY_DYE;
		case LIGHT_GRAY: return Material.LIGHT_GRAY_DYE;
		case CYAN: return Material.CYAN_DYE;
		case PURPLE: return Material.PURPLE_DYE;
		case BLUE: return Material.BLUE_DYE;
		case BROWN: return Material.BROWN_DYE;
		case GREEN: return Material.GREEN_DYE;
		case RED: return Material.RED_DYE;
		case BLACK: return Material.BLACK_DYE;
		default: return null;
		}
	}
}
