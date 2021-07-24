package makamys.dmod.item;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import makamys.dmod.DMod;
import makamys.dmod.DModItems;
import makamys.dmod.future.item.IItemFuture;
import makamys.dmod.future.inventory.SlotFuture;
import makamys.dmod.future.item.ItemFuture;
import makamys.dmod.future.item.ItemStackFuture;
import makamys.dmod.future.nbt.NBTTagListFuture;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

public class ItemBundle extends Item implements IConfigurable, IItemFuture {
	
	public static final int MAX_STORAGE = 64;
	private static final int field_30859 = 4;
	private static final int ITEM_BAR_COLOR = 0x6666FF;
	
	public ItemBundle() {
		setMaxStackSize(1);
		setUnlocalizedName(DMod.MODID + "." + "bundle");
		setCreativeTab(CreativeTabs.tabTools);
		setTextureName("bundle");
	}
	
   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister iconRegister) {
	   super.itemIcon = iconRegister.registerIcon("dmod:bundle");
   }

	@Override
	public boolean isEnabled() {
		return true;
	}
	
	public static float getAmountFilled(ItemStack stack) {
		return (float) getBundleOccupancy(stack) / 64.0F;
	}
	
	@Override
	public boolean onStackClicked(ItemStack stack, Slot slot, int button, EntityPlayer player) {
		if (button != 1) {
			return false;
		} else {
			ItemStack itemStack = slot.getStack();
			if (itemStack == null) {
				ItemStack removed = removeFirstStack(stack);
				if(removed != null) {
					addToBundle(stack, SlotFuture.insertStack(slot, removed));
				}
			} else if (ItemFuture.canBeNested(itemStack.getItem())) {
				int i = (64 - getBundleOccupancy(stack)) / getItemOccupancy(itemStack);
				addToBundle(stack, SlotFuture.takeStackRange(slot, itemStack.stackSize, i, player));
			}

			return true;
		}
	}

	@Override
	public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, int button, EntityPlayer player) {
		if (button == 1 && SlotFuture.canTakePartial(slot, player)) {
			if (otherStack == null) {
				ItemStack var10000 = removeFirstStack(stack);
				if(var10000 != null) {
					player.inventory.setItemStack(var10000);
				}
			} else {
				otherStack.stackSize -= addToBundle(stack, otherStack);
				otherStack = ItemStackFuture.oldify(otherStack);
				if(otherStack == null) {
					player.inventory.setItemStack(null);
				}
			}

			return true;
		} else {
			return false;
		}
	}

	// TODO
	@Override
	public boolean onItemUse(ItemStack p_77648_1_, EntityPlayer p_77648_2_, World p_77648_3_, int p_77648_4_,
			int p_77648_5_, int p_77648_6_, int p_77648_7_, float p_77648_8_, float p_77648_9_, float p_77648_10_) {
		// TODO Auto-generated method stub
		return super.onItemUse(p_77648_1_, p_77648_2_, p_77648_3_, p_77648_4_, p_77648_5_, p_77648_6_, p_77648_7_, p_77648_8_,
				p_77648_9_, p_77648_10_);
	}
	
	@Override
	public ItemStack onItemRightClick(ItemStack p_77659_1_, World p_77659_2_, EntityPlayer p_77659_3_) {
		// TODO Auto-generated method stub
		return super.onItemRightClick(p_77659_1_, p_77659_2_, p_77659_3_);
	}
	
	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
			float hitX, float hitY, float hitZ) {
		// TODO Auto-generated method stub
		return super.onItemUseFirst(stack, player, world, x, y, z, side, hitX, hitY, hitZ);
	}
	/*
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack itemStack = user.getStackInHand(hand);
		if (dropAllBundledItems(itemStack, user)) {
			user.incrementStat(Stats.USED.getOrCreateStat(this));
			return TypedActionResult.success(itemStack, world.isClient());
		} else {
			return TypedActionResult.fail(itemStack);
		}
	}*/

	public boolean isItemBarVisible(ItemStack stack) {
		return getBundleOccupancy(stack) > 0;
	}

	public int getItemBarStep(ItemStack stack) {
		return Math.min(1 + 12 * getBundleOccupancy(stack) / 64, 13);
	}

	public int getItemBarColor(ItemStack stack) {
		return ITEM_BAR_COLOR;
	}

	private static int addToBundle(ItemStack bundle, ItemStack stack) {
		if (stack != null && ItemFuture.canBeNested(stack.getItem())) {
			NBTTagCompound NBTTagCompound = ItemStackFuture.getOrCreateNbt(bundle);
			if (!NBTTagCompound.hasKey("Items")) {
				NBTTagCompound.setTag("Items", new NBTTagList());
			}

			int i = getBundleOccupancy(bundle);
			int j = getItemOccupancy(stack);
			int k = Math.min(stack.stackSize, (64 - i) / j);
			if (k == 0) {
				return 0;
			} else {
				NBTTagList tagList = NBTTagCompound.getTagList("Items", 10);
				Optional<NBTTagCompound> optional = canMergeStack(stack, tagList);
				List<NBTBase> list = NBTTagListFuture.toList(tagList);
				if (optional.isPresent()) {
					NBTTagCompound NBTTagCompound2 = (NBTTagCompound) optional.get();
					ItemStack itemStack = ItemStack.loadItemStackFromNBT(NBTTagCompound2);
					ItemStackFuture.increment(itemStack, k);
					itemStack.writeToNBT(NBTTagCompound2);
					list.remove(NBTTagCompound2);
					NBTTagListFuture.add(tagList, 0, NBTTagCompound2);
				} else {
					ItemStack itemStack2 = stack.copy();
					itemStack2.stackSize = k;
					NBTTagCompound NBTTagCompound3 = new NBTTagCompound();
					itemStack2.writeToNBT(NBTTagCompound3);
					
					NBTTagListFuture.add(tagList, 0, NBTTagCompound3);
				}

				return k;
			}
		} else {
			return 0;
		}
	}

	private static Optional<NBTTagCompound> canMergeStack(ItemStack stack, NBTTagList items) {
		if (stack != null && stack.getItem() == DModItems.bundle) {
			return Optional.empty();
		} else {
			Stream<NBTBase> var10000 = NBTTagListFuture.toList(items).stream();
			Objects.requireNonNull(NBTTagCompound.class);
			var10000 = var10000.filter(NBTTagCompound.class::isInstance);
			Objects.requireNonNull(NBTTagCompound.class);
			return var10000.map(NBTTagCompound.class::cast).filter((item) -> {
				return ItemStackFuture.canCombine(ItemStack.loadItemStackFromNBT(item), stack);
			}).findFirst();
		}
	}

	private static int getItemOccupancy(ItemStack stack) {
		if(stack == null) {
			return 0;
		}
		Item item = stack.getItem();
		if (item == DModItems.bundle) {
			return 4 + getBundleOccupancy(stack);
		} else {
			/*if ((stack.isOf(Items.BEEHIVE) || stack.isOf(Items.BEE_NEST)) && stack.hasNbt()) {
				NBTTagCompound NBTTagCompound = stack.getSubNbt("BlockEntityTag");
				if (NBTTagCompound != null && !NBTTagCompound.getList("Bees", 10).isEmpty()) {
					return 64;
				}
			}*/

			return 64 / stack.getMaxStackSize();
		}
	}

	private static int getBundleOccupancy(ItemStack stack) {
		return getBundledStacks(stack).mapToInt((itemStack) -> {
			return getItemOccupancy(itemStack) * itemStack.stackSize;
		}).sum();
	}

	private static ItemStack removeFirstStack(ItemStack stack) {
		NBTTagCompound NBTTagCompound = ItemStackFuture.getOrCreateNbt(stack);
		if (!NBTTagCompound.hasKey("Items")) {
			return null;
		} else {
			NBTTagList tagList = NBTTagCompound.getTagList("Items", 10);
			List<NBTBase> list = NBTTagListFuture.toList(tagList);
			if (tagList.tagCount() == 0) {
				return null;
			} else {
				//int i = false;
				NBTTagCompound NBTTagCompound2 = tagList.getCompoundTagAt(0);
				ItemStack itemStack = ItemStack.loadItemStackFromNBT(NBTTagCompound2);
				list.remove(0);
				if (tagList.tagCount() == 0) {
					stack.stackTagCompound.removeTag("Items");
				}

				return itemStack;
			}
		}
	}
/*
	private static boolean dropAllBundledItems(ItemStack stack, PlayerEntity player) {
		NBTTagCompound NBTTagCompound = stack.getOrCreateNbt();
		if (!NBTTagCompound.contains("Items")) {
			return false;
		} else {
			if (player instanceof ServerPlayerEntity) {
				NBTTagList NBTTagList = NBTTagCompound.getList("Items", 10);

				for (int i = 0; i < NBTTagList.size(); ++i) {
					NBTTagCompound NBTTagCompound2 = NBTTagList.getCompound(i);
					ItemStack itemStack = ItemStack.fromNbt(NBTTagCompound2);
					player.dropItem(itemStack, true);
				}
			}

			stack.removeSubNbt("Items");
			return true;
		}
	}
*/
	private static Stream<ItemStack> getBundledStacks(ItemStack stack) {
		NBTTagCompound NBTTagCompound = stack.stackTagCompound;
		if (NBTTagCompound == null) {
			return Stream.empty();
		} else {
			NBTTagList NBTTagList = NBTTagCompound.getTagList("Items", 10);
			Stream<NBTBase> var10000 = NBTTagListFuture.toList(NBTTagList).stream();
			Objects.requireNonNull(NBTTagCompound.class);
			return var10000.map(NBTTagCompound.class::cast).map(ItemStack::loadItemStackFromNBT);
		}
	}
/*
	public Optional<TooltipData> getTooltipData(ItemStack stack) {
		DefaultedList<ItemStack> defaultedList = DefaultedList.of();
		Stream var10000 = getBundledStacks(stack);
		Objects.requireNonNull(defaultedList);
		var10000.forEach(defaultedList::add);
		return Optional.of(new BundleTooltipData(defaultedList, getBundleOccupancy(stack)));
	}
*/
	public void appendTooltip(ItemStack stack, World world, List<String> tooltip) {
		tooltip.add(
				//EnumChatFormatting.GRAY + I18n.format("item.minecraft.bundle.fullness", getBundleOccupancy(stack), 64));
				getBundleOccupancy(stack) + " / 64");
	}
/*
	public void onItemEntityDestroyed(ItemEntity entity) {
		ItemUsage.spawnItemContents(entity, getBundledStacks(entity.getStack()));
	}
	*/
}