package com.festp.misc;

import org.bukkit.Axis;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.FaceAttachable;
import org.bukkit.block.data.FaceAttachable.AttachedFace;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.Rail;
import org.bukkit.block.data.Rail.Shape;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.data.type.Chest;
import org.bukkit.block.data.type.Comparator;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Door.Hinge;
import org.bukkit.block.data.type.Gate;
import org.bukkit.block.data.type.Piston;
import org.bukkit.block.data.type.Repeater;
import org.bukkit.block.data.type.Slab;

import com.festp.utils.Utils;
import com.festp.utils.UtilsType;

import org.bukkit.block.data.type.Slab.Type;
import org.bukkit.inventory.DoubleChestInventory;

public class RotatableBlock {
	
	public static boolean left_click_rotate_attempt(Block b, boolean sneaking) {
		//double chest
		Material blockType = b.getType();
		BlockData data = b.getBlockData();
		if (sneaking) {
			if (blockType == Material.CHEST || blockType == Material.TRAPPED_CHEST)
				return remerge_chest(b, (Chest) data);
			if (data instanceof FaceAttachable)
				rotate_faceAttachable_circle(b);
		}
		return false;
	}
	
	public static boolean rotate_attempt(Block b, boolean sneaking) {
		Material block = b.getType();
		BlockData data = b.getBlockData();
		
		// BOTH SNEAKING AND NOT SNEAKING
		if (UtilsType.isSlab(block)) {
			rotate_bisected(b);
			return true;
		}
		if (data instanceof Orientable && block != Material.NETHER_PORTAL) {//Utils.isLog(block) || block == Material.HAY_BLOCK) {
			rotate_orientable(b);
			return true;
		}
		if (UtilsType.is_glazed_terracotta(block) || block == Material.PUMPKIN || block == Material.CARVED_PUMPKIN || block == Material.JACK_O_LANTERN
				|| block == Material.CAMPFIRE || block == Material.SOUL_CAMPFIRE || block == Material.BEEHIVE || block == Material.BEE_NEST) { // || data instanceof Pumpkin) {
			rotate_directional_4(b);
			return true;
		}
		if (block == Material.OBSERVER) {
			rotate_directional_6(b);
			return true;
		}
		if (block == Material.PISTON || block == Material.STICKY_PISTON) {
			if (((Piston)b.getBlockData()).isExtended()) //(b.isBlockIndirectlyPowered())
				return false;
			rotate_directional_6(b);
			return true;
		}
		if (UtilsType.isSign(block) || UtilsType.is_banner(block) || UtilsType.isHead(block) && data instanceof Rotatable) {
			rotate_rotatable_16(b);
			return true;
		}
		if (block == Material.RAIL) {
			return rail_straight(b);
		}
		
		// SNEAKING
		if (sneaking) {
			if (UtilsType.isStairs(block)) {
				rotate_bisected(b);
				return true;
			}
			if (block == Material.LECTERN || block == Material.BELL || block == Material.STONECUTTER || block == Material.GRINDSTONE) {
				rotate_directional_4(b);
				return true;
			}
			if (UtilsType.isWoodenTrapdoor(block)) {
				rotate_directional_4(b);
				if(((Directional)data).getFacing() == BlockFace.SOUTH)
					rotate_bisected(b);
				return true;
			}
			if (block == Material.CHEST || block == Material.TRAPPED_CHEST) {
				if( !(((org.bukkit.block.Chest)b.getState()).getInventory() instanceof DoubleChestInventory) ) {
					rotate_directional_4(b);
					return true;
				}
			}
			if (block == Material.ENDER_CHEST || block == Material.FURNACE || block == Material.SMOKER || block == Material.BLAST_FURNACE
					|| data instanceof Gate || data instanceof Comparator || data instanceof Repeater
					|| block == Material.ANVIL || block == Material.CHIPPED_ANVIL || block == Material.DAMAGED_ANVIL) {
				rotate_directional_4(b);
				return true;
			}
			if (block == Material.DISPENSER || block == Material.DROPPER) {
				rotate_directional_6(b);
				return true;
			}
			if (data instanceof Door) {
				rotate_door(b);
				return true;
			}
			if (block == Material.HOPPER) {
				rotate_directional_5(b);
				return true;
			}
		}
		
		// NOT SNEAKING
		else {
			if (UtilsType.isStairs(block)) {
				rotate_directional_4(b);
				return true;
			}
		}
		return false;
	}
	
	public static boolean remerge_chest(Block b, Chest chest) {
		//EAST => SOUTH - NORTH - NONE
		BlockFace main_dir = chest.getFacing(), left_dir = Utils.getLeftDirection(main_dir), right_dir = Utils.getRightDirection(main_dir);
		Chest.Type type = chest.getType(), new_type;
		Block second_part, new_part;
		if (type == org.bukkit.block.data.type.Chest.Type.LEFT) {
			second_part = b.getRelative(right_dir);
			new_part = b.getRelative(left_dir);
			type = org.bukkit.block.data.type.Chest.Type.RIGHT;
			new_type = org.bukkit.block.data.type.Chest.Type.LEFT;
		}
		else if (type == org.bukkit.block.data.type.Chest.Type.RIGHT) {
			second_part = b.getRelative(left_dir);
			new_part = null;
			type = org.bukkit.block.data.type.Chest.Type.SINGLE;
			new_type = null;
		}
		else {
			second_part = null;
			new_part = b.getRelative(right_dir);
			type = org.bukkit.block.data.type.Chest.Type.LEFT;
			new_type = org.bukkit.block.data.type.Chest.Type.RIGHT;
			if(new_part.getType() != b.getType() || ((Chest) new_part.getBlockData()).getFacing() != chest.getFacing()) {
				new_part = b.getRelative(left_dir);
				type = org.bukkit.block.data.type.Chest.Type.RIGHT;
				new_type = org.bukkit.block.data.type.Chest.Type.LEFT;
			}
		}
		if (second_part != null) {
			Chest old_part = ((Chest) second_part.getBlockData());
			old_part.setType(org.bukkit.block.data.type.Chest.Type.SINGLE);
			second_part.setBlockData(old_part);
		}
		if (new_part != null && new_part.getType() == b.getType()) {
			Chest new_chest = (Chest) new_part.getBlockData();
			if(new_chest.getFacing() == chest.getFacing() && new_chest.getType() == org.bukkit.block.data.type.Chest.Type.SINGLE) {
				new_chest.setType(new_type);
				new_part.setBlockData(new_chest);
			}
			else type = org.bukkit.block.data.type.Chest.Type.SINGLE;
		}
		else type = org.bukkit.block.data.type.Chest.Type.SINGLE;
		
		if (type == chest.getType())
			return false;
		
		chest.setType(type);
		b.setBlockData(chest);
		//find merged chest and unmerge both (by mutual inventory)
		/*DoubleChest double_chest = (DoubleChest) b.getState();
		((Chest)double_chest.getLeftSide()).setType(org.bukkit.block.data.type.Chest.Type.SINGLE);
		((Chest)double_chest.getRightSide()).setType(org.bukkit.block.data.type.Chest.Type.SINGLE);*/
		return true;
	}
	
	public static void rotate_directional_4(Block b)
	{
		Directional dir = (Directional)b.getBlockData();
		if(dir.getFacing() == BlockFace.EAST) dir.setFacing(BlockFace.NORTH);
		else if(dir.getFacing() == BlockFace.NORTH) dir.setFacing(BlockFace.WEST);
		else if(dir.getFacing() == BlockFace.WEST) dir.setFacing(BlockFace.SOUTH);
		else if(dir.getFacing() == BlockFace.SOUTH) dir.setFacing(BlockFace.EAST);
		b.setBlockData(dir);
	}
	
	/** Rotate hopper */
	public static void rotate_directional_5(Block b)
	{
		Directional dir = (Directional)b.getBlockData();
		if (dir.getFacing() == BlockFace.EAST) dir.setFacing(BlockFace.NORTH);
		else if (dir.getFacing() == BlockFace.NORTH) dir.setFacing(BlockFace.WEST);
		else if (dir.getFacing() == BlockFace.WEST) dir.setFacing(BlockFace.SOUTH);
		else if (dir.getFacing() == BlockFace.SOUTH) dir.setFacing(BlockFace.DOWN);
		else if (dir.getFacing() == BlockFace.DOWN) dir.setFacing(BlockFace.EAST);
		b.setBlockData(dir);
	}
	
	public static void rotate_directional_6(Block b)
	{
		Directional dir = (Directional)b.getBlockData();
		if (dir.getFacing() == BlockFace.EAST) dir.setFacing(BlockFace.NORTH);
		else if (dir.getFacing() == BlockFace.NORTH) dir.setFacing(BlockFace.WEST);
		else if (dir.getFacing() == BlockFace.WEST) dir.setFacing(BlockFace.SOUTH);
		else if (dir.getFacing() == BlockFace.SOUTH) dir.setFacing(BlockFace.UP);
		else if (dir.getFacing() == BlockFace.UP) dir.setFacing(BlockFace.DOWN);
		else if (dir.getFacing() == BlockFace.DOWN) dir.setFacing(BlockFace.EAST);
		b.setBlockData(dir);
	}
	
	public static void rotate_rotatable_16(Block b)
	{
		Rotatable dir = (Rotatable)b.getBlockData();
		if (dir.getRotation() == BlockFace.EAST) dir.setRotation(BlockFace.EAST_NORTH_EAST);
		else if (dir.getRotation() == BlockFace.EAST_NORTH_EAST) dir.setRotation(BlockFace.NORTH_EAST);
		else if (dir.getRotation() == BlockFace.NORTH_EAST) dir.setRotation(BlockFace.NORTH_NORTH_EAST);
		else if (dir.getRotation() == BlockFace.NORTH_NORTH_EAST) dir.setRotation(BlockFace.NORTH);
		else if (dir.getRotation() == BlockFace.NORTH) dir.setRotation(BlockFace.NORTH_NORTH_WEST);
		else if (dir.getRotation() == BlockFace.NORTH_NORTH_WEST) dir.setRotation(BlockFace.NORTH_WEST);
		else if (dir.getRotation() == BlockFace.NORTH_WEST) dir.setRotation(BlockFace.WEST_NORTH_WEST);
		else if (dir.getRotation() == BlockFace.WEST_NORTH_WEST) dir.setRotation(BlockFace.WEST);
		else if (dir.getRotation() == BlockFace.WEST) dir.setRotation(BlockFace.WEST_SOUTH_WEST);
		else if (dir.getRotation() == BlockFace.WEST_SOUTH_WEST) dir.setRotation(BlockFace.SOUTH_WEST);
		else if (dir.getRotation() == BlockFace.SOUTH_WEST) dir.setRotation(BlockFace.SOUTH_SOUTH_WEST);
		else if (dir.getRotation() == BlockFace.SOUTH_SOUTH_WEST) dir.setRotation(BlockFace.SOUTH);
		else if (dir.getRotation() == BlockFace.SOUTH) dir.setRotation(BlockFace.SOUTH_SOUTH_EAST);
		else if (dir.getRotation() == BlockFace.SOUTH_SOUTH_EAST) dir.setRotation(BlockFace.SOUTH_EAST);
		else if (dir.getRotation() == BlockFace.SOUTH_EAST) dir.setRotation(BlockFace.EAST_SOUTH_EAST);
		else if (dir.getRotation() == BlockFace.EAST_SOUTH_EAST) dir.setRotation(BlockFace.EAST);
		b.setBlockData(dir);
	}
	
	public static void rotate_bisected(Block b)
	{
		if (b.getBlockData() instanceof Slab) {
			Slab bis = (Slab)b.getBlockData();
			if (bis.getType() == Type.BOTTOM) bis.setType(Type.TOP);
			else if (bis.getType() == Type.TOP) bis.setType(Type.BOTTOM);
			b.setBlockData(bis);
		}
		else {
			Bisected bis = (Bisected)b.getBlockData();
			if (bis.getHalf() == Half.BOTTOM) bis.setHalf(Half.TOP);
			else bis.setHalf(Half.BOTTOM);
			b.setBlockData(bis);
		}
	}
	
	public static void rotate_orientable(Block b)
	{
		Orientable rot = (Orientable)b.getBlockData();
		if (rot.getAxis() == Axis.X) rot.setAxis(Axis.Y);
		else if (rot.getAxis() == Axis.Y) rot.setAxis(Axis.Z);
		else if (rot.getAxis() == Axis.Z) rot.setAxis(Axis.X);
		b.setBlockData(rot);
	}
	
	public static boolean rail_straight(Block b)
	{
		Rail rail = (Rail)b.getBlockData();
		Shape shape = rail.getShape();
		if (shape == Shape.NORTH_WEST || shape == Shape.NORTH_EAST || shape == Shape.SOUTH_WEST || shape == Shape.SOUTH_EAST || shape == Shape.EAST_WEST) {
			rail.setShape(Shape.NORTH_SOUTH);
			b.setBlockData(rail);
			return true;
		} else if (shape == Shape.NORTH_SOUTH) {
			rail.setShape(Shape.EAST_WEST);
			b.setBlockData(rail);
			return true;
		}
		return false;
	}
	
	public static void rotate_door(Block b)
	{
		rotate_door_part(b);
	}
	public static void rotate_door_part(Block b)
	{
		Door door = (Door)b.getBlockData();
		if (door.getHinge() == Hinge.LEFT) {
			door.setHinge(Hinge.RIGHT);
			b.setBlockData(door);
		}
		else {
			rotate_directional_4(b);
			door = (Door)b.getBlockData();
			door.setHinge(Hinge.LEFT);
			b.setBlockData(door);
		}
	}
	public static void rotate_faceAttachable_circle(Block b)
	{
		FaceAttachable attac = (FaceAttachable)b.getBlockData();
		boolean rot180 = false;
		
		AttachedFace face = attac.getAttachedFace();
		if (face == AttachedFace.FLOOR) {
			face = AttachedFace.WALL;
		} else if (face == AttachedFace.WALL) {
			BlockFace dirFace = ((Directional)b.getBlockData()).getFacing();
			if (dirFace == BlockFace.WEST || dirFace == BlockFace.SOUTH)
				face = AttachedFace.CEILING;
			else
				face = AttachedFace.FLOOR;
			rot180 = true;
		} else if (face == AttachedFace.CEILING) {
			face = AttachedFace.WALL;
		}
		attac.setAttachedFace(face);
		b.setBlockData(attac);
		
		if (rot180)
		{
			rotate_directional_4(b);
			rotate_directional_4(b);
		}
	}
}
