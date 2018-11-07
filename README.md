# hodgepodge
Spigot Minecraft Plugin, which adds many unrelated things.

Main features can be divided into:

1) Inventory:  
  • Armor autoequip (while pickup armor with better material)  
  • Autoreplacing broken items (and saving renamed/mending items)  
  • When drinking potions bottle goes into inventory.  
  • Within a second after closing the chest on a swap of hands (F by default), all the contents are thrown in the sight direction.  
2) Decorative:  
  • Cleaning colored blocks in the cauldron.  
  • Turning concrete powder into concrete by cauldron (and by click on it).  
  • Recoloring blocks by right-click on block (with dye in hand).  
  • Rotate(and more) blocks with any hoe.  
3) Vanilla improvements:  
  • More fall protection by jumping potion.  
  • Smelting armor gives a nugget for each ingot in armor. (vanilla crafts are removed)  
  • When you join the game in the portal, you would be teleported from the portal, this allows players to log in using AuthMe.  
  • Boats teleport out of the portals, so boats do not get stuck in it and do not prevent players from entering.  
  • Fireworks fly vertically upwards and do not deflect. (controversial update, for mechanisms with the activation of something above)  
  • Dispensers can fill cauldrons by buckets.  
  • Dispensers can feed animals in the block in front of them.  
  • A player with a saddle on his head (/hat - see “Commands” in ThirdEye(my another plugin)) can sit on his head.  
  • You can leash players, but they may free themselves with sneaking.  
4) Gameplay changes:  
  • Diamond blocks frighten monsters, and blocks spawn of thrm while red-powered.  
  • Pumps like IndustrialCraft and BuildCraft pumps, which are dispensers with a special core inside. To work requires nether fences as a pipe, empty buckets to fill and at least one block of fluid beneath.  
  • Shared enderchests / enderchest groups. One enderchest inventory for several people. There are also channels without administrators and entry restrictions.  
  • Storages - there are 2 types: bottomless (collect items of one type(Material), can grab items drom inventories, have GUI) and multitype (like shulker box that can be opened from inventory by click and can't die or despawn while dropped).
  • Tomes (summon minecarts, boats and horses)  
5) Craft changes: (all changes are added in CraftManager, but it should have its own config files)  
  • Clay craft.  
  • Sandstone into sand, redsand, slabs and stairs into blocks, pocket borsch, chorus from chorus flower and more.  
  • All crafts are added to the craft book.  
6) Commands:  
  • "/ec" or "/enderchest" — interaction with channels (groups) of ender chests (EC), full autocompletion on Tab.  
  • "/item" — simplied /give, can give only tools, weapon and armor(damageable items), supports durability in percents and all the enchantments.  
  • (deprecated) "/boss" — spawning, reconfiguring and removing bosses.  

(rus info: https://vk.com/@mine_surser-plugin-v1)

TO DO:  
  • Bosses will be moved to separate plugin.  
  • Fix leashed.    
  • Fix russian names in shared EC.   
  • Replace russian comments with english. Add different language support to plugin.  
  • Each function must be able to be turned on/off.  
  • Soul stone crafts    
  • Food rebalance?  
  • Increase tripwire length.  
  • com.festp.Utils improvements (God object, which has 1,5k lines of code, but is this normal for Utils?) 
  • ? Coming soon ?
