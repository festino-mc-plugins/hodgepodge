# hodgepodge
Spigot Minecraft Plugin, which adds many unrelated things.

Main features can be divided into:

1) Inventory:  
  • Armor autoequip (while pickup armor with better material)  
  • Autoreplacing broken items (and saving renamed/mending items)  
  • When drinking potions bottle goes into inventory.  
  • Within a second after closing a chest or a shulker box on a swap of hands (F by default), all the contents are thrown in the sight direction.  
  • Items dropped by players can be picked up without delay. (players "pass" items to each other, but it is not always convenient)  
  • Sort hoppers: let us filter several items by renamed hoppers, including unstackable items such as shovels and shulker boxes.*  
  • Experience hoppers: can fill empty bottles with xp.**  
2) Decorative:  
  • Cleaning colored blocks in the cauldron.  
  • Turning concrete powder into concrete by cauldron (and by click on it).  
  • Recoloring blocks by right-click on block (with dye in hand).  
  • Rotate(and more) blocks with any hoe.  
  • All new crafts are added to the craft book.  
3) Vanilla improvements:  
  • More fall protection by jumping potion.  
  • Smelting armor gives a nugget for each ingot in armor. (vanilla crafts are removed)  
  • When you join the game in the nether portal, you would be teleported from the portal, this allows players to log in using AuthMe.  
  • Boats teleport out of the portals, so boats do not get stuck in it and do not prevent players from entering.  
  • Fireworks fly vertically upwards and do not deflect. (controversial update, for mechanisms with the activation of something above)  
  • Dispensers can fill cauldrons by buckets.  
  • Dispensers can feed animals in the block opposite the hole.  
  • A player with a saddle on his head (/hat - see “Commands” in ThirdEye(my another plugin)) can sit on his head.  
  • You can leash players, but they may free themselves with sneaking.  
4) Gameplay changes:  
  • Diamond blocks frighten monsters, and blocks spawn of them while red-powered.  
  • Pumps like IndustrialCraft and BuildCraft pumps, which are dispensers with a special core inside. To work requires nether fences as a pipe, empty buckets to fill and at least one block of fluid beneath.  
  • Shared enderchests / enderchest groups. One enderchest inventory for several people. There are also channels without administrators and entry restrictions.  
  • Storages - there are 2 types: bottomless (collect items of one type(Material), can grab items drom inventories, have GUI) and multitype (like shulker box that can be opened from inventory by click and can't die or despawn while dropped).  
  • Tomes (summon minecarts, boats and horses)  
  • Soul stones: ...  
5) Craft changes: (all changes are added in CraftManager, but it should have its own config files)  
  • Clay craft.  
  • Sandstone into sand, redsand, slabs and stairs into blocks, pocket borsch, chorus from chorus flower and more.  
6) Commands:  
  • "/ec" or "/enderchest" — interaction with channels (groups) of ender chests (EC), full autocompletion on Tab.  
  • "/item" — simplied /give, can give only tools, weapon and armor(damageable items), supports durability in percents and all the enchantments.  
  • (deprecated) "/boss" — spawning, reconfiguring and removing bosses.  

* Also it can detect enchantments of required level(=, < and >) and item names. Syntax: "mc:" or "minecraft:" search as substring of SPIGOT MATERIAL(also exist "armor", "tool" and "all" filters), '&' for intersection, '|' for union of conditions, "n:" or "name:" detects name, "e:" or "ench:" detects enchantment. Example: "iron & shovel & dur=3 | diam & shov" will grab iron shovels with unbreaking 3 and any diamond shovels.  
** Hopper named "xp": when contains empty bottles, collects XP; once stored N xp turns bottle into xp bottle. Grab only bottles(even player can't put anything else). If you pick up all the bottles or destroy the hopper, it throws out the stored experience.

(rus info: https://vk.com/@mine_surser-plugin-v1)

TO DO:  
  • Replace russian comments with english. Add different language support to plugin.  
  • Each function must be able to be turned on/off.  
  • Soul stone crafts    

  • Food rebalance?  
  • Increase tripwire length.  
  • com.festp.Utils improvements (God object, which has 1.7k lines of code, but is this normal for Utils?) 
  • ? Coming soon ?
