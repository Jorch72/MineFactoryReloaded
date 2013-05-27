package powercrystals.minefactoryreloaded.tile.machine;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Random;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.WeightedRandom;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidDictionary;
import net.minecraftforge.liquids.LiquidStack;
import net.minecraftforge.liquids.LiquidTank;

import powercrystals.core.util.UtilInventory;
import powercrystals.minefactoryreloaded.MFRRegistry;
import powercrystals.minefactoryreloaded.api.IFactoryGrindable;
import powercrystals.minefactoryreloaded.api.MobDrop;
import powercrystals.minefactoryreloaded.core.HarvestAreaManager;
import powercrystals.minefactoryreloaded.core.ITankContainerBucketable;
import powercrystals.minefactoryreloaded.gui.client.GuiFactoryInventory;
import powercrystals.minefactoryreloaded.gui.client.GuiFactoryPowered;
import powercrystals.minefactoryreloaded.gui.container.ContainerFactoryPowered;
import powercrystals.minefactoryreloaded.setup.Machine;
import powercrystals.minefactoryreloaded.tile.base.TileEntityFactoryPowered;
import powercrystals.minefactoryreloaded.world.GrindingWorld;
import powercrystals.minefactoryreloaded.world.GrindingWorldServer;
import powercrystals.minefactoryreloaded.world.IGrindingWorld;

public class TileEntityGrinder extends TileEntityFactoryPowered implements ITankContainerBucketable
{	
	private HarvestAreaManager _areaManager;
	private LiquidTank _tank;
	private Random _rand;
	private IGrindingWorld grindingWorld;

	@Override
	public String getGuiBackground()
	{
		return "grinder.png";
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiFactoryInventory getGui(InventoryPlayer inventoryPlayer)
	{
		return new GuiFactoryPowered(getContainer(inventoryPlayer), this);
	}

	@Override
	public ContainerFactoryPowered getContainer(InventoryPlayer inventoryPlayer)
	{
		return new ContainerFactoryPowered(this, inventoryPlayer);
	}

	public TileEntityGrinder()
	{
		super(Machine.Grinder);
		_areaManager = new HarvestAreaManager(this, 2, 2, 1);
		_tank = new LiquidTank(4 * LiquidContainerRegistry.BUCKET_VOLUME);
		_rand = new Random();
	}

	@Override
    public void setWorldObj(World world)
    {
		super.setWorldObj(world);
        if (grindingWorld != null)
        	grindingWorld.clearReferences();
        if (this.worldObj instanceof WorldServer)
        	grindingWorld = new GrindingWorldServer((WorldServer)this.worldObj, this);
        else
        	grindingWorld = new GrindingWorld(this.worldObj, this);
    }

	public Random getRandom()
	{
		return _rand;
	}

	@Override
	protected boolean shouldPumpLiquid()
	{
		return true;
	}

	@Override
	public int getEnergyStoredMax()
	{
		return 32000;
	}

	@Override
	public int getWorkMax()
	{
		return 1;
	}

	@Override
	public int getIdleTicksMax()
	{
		return 200;
	}

	@Override
	public ILiquidTank getTank()
	{
		return _tank;
	}

	@Override
	public boolean activateMachine()
	{
		grindingWorld.cleanReferences();
		boolean processMob = false;
		List<?> entities = worldObj.getEntitiesWithinAABB(EntityLiving.class, _areaManager.getHarvestArea().toAxisAlignedBB());
		
		entityList: for(Object o : entities)
		{
			if(o instanceof EntityAgeable && ((EntityAgeable)o).getGrowingAge() < 0)
			{
				continue;
			}
			EntityLiving e = (EntityLiving)o;
			if(e.getHealth() <= 0)
			{
				continue;
			}
			processEntity:
			{
				if(MFRRegistry.getGrindables().containsKey(e.getClass()))
				{
					IFactoryGrindable r = MFRRegistry.getGrindables().get(e.getClass());
					List<MobDrop> drops = r.grind(worldObj, e, getRandom());
					if(drops != null && WeightedRandom.getTotalWeight(drops) > 0)
					{
						ItemStack drop = ((MobDrop)WeightedRandom.getRandomItem(_rand, drops)).getStack();
						UtilInventory.dropStack(this, drop, this.getDropDirection());
					}
					if (r.processEntity(e))
					{
						processMob = true;
						break processEntity;
					}
				}
				for (Class<?> t : MFRRegistry.getGrinderBlacklist())
				{
					if (t.isInstance(e))
					{
						continue entityList;
					}
				}
				if (!grindingWorld.addEntityForGrinding(e))
				{
					continue entityList;
				}
			}
			if(processMob && worldObj.getGameRules().getGameRuleBooleanValue("doMobLoot"))
			{
				try
				{
					worldObj.getGameRules().setOrCreateGameRule("doMobLoot", "false");
					e.attackEntityFrom(DamageSource.generic, e.getHealth());
					_tank.fill(LiquidDictionary.getLiquid("mobEssence", 100), true);
				}
				finally
				{
					worldObj.getGameRules().setOrCreateGameRule("doMobLoot", "true");
					setIdleTicks(20);
					e.attackEntityFrom(DamageSource.generic, 5000);
				}
				return true;
			}
			e.attackEntityFrom(DamageSource.generic, e.getHealth());
			_tank.fill(LiquidDictionary.getLiquid("mobEssence", 100), true);
			setIdleTicks(20);
			return true;
		}
		setIdleTicks(getIdleTicksMax());
		return false;
	}

	@Override
	public String getInvName()
	{
		return "Mob Grinder";
	}

	@Override
	public int getSizeInventory()
	{
		return 0;
	}

	@Override
	public int fill(ForgeDirection from, LiquidStack resource, boolean doFill)
	{
		return 0;
	}

	@Override
	public int fill(int tankIndex, LiquidStack resource, boolean doFill)
	{
		return 0;
	}

	@Override
	public boolean allowBucketDrain()
	{
		return true;
	}

	@Override
	public LiquidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		return null;
	}

	@Override
	public LiquidStack drain(int tankIndex, int maxDrain, boolean doDrain)
	{
		return null;
	}

	@Override
	public ILiquidTank[] getTanks(ForgeDirection direction)
	{
		return new ILiquidTank[] { _tank };
	}

	@Override
	public ILiquidTank getTank(ForgeDirection direction, LiquidStack type)
	{
		return _tank;
	}

	@Override
	public boolean manageSolids()
	{
		return true;
	}

	@Override
	public boolean canRotate()
	{
		return true;
	}
}
