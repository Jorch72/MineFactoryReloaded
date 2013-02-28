package powercrystals.minefactoryreloaded.decorative;

import java.util.List;

import powercrystals.minefactoryreloaded.MineFactoryReloadedCore;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPane;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockFactoryGlassPane extends BlockPane
{
	public BlockFactoryGlassPane(int blockId, int whiteTexture, int whiteSideTexture)
	{
		super(blockId, whiteTexture, whiteSideTexture, Material.glass, false);
		this.setCreativeTab(CreativeTabs.tabDecorations);
		setBlockName("factoryGlassPaneBlock");
		setHardness(0.3F);
		setStepSound(soundGlassFootstep);
	}
	
	@Override
	public int getBlockTextureFromSideAndMetadata(int side, int meta)
	{
		return blockIndexInTexture + meta;
	}
	
	public int getRenderBlockPass()
	{
		return 1;
	}
	
	public int getBlockSideTextureFromMetadata(int meta)
	{
		return getSideTextureIndex() + meta;
	}
	
	public boolean canThisFactoryPaneConnectToThisBlockID(int blockId)
	{
		return Block.opaqueCubeLookup[blockId] || blockId == this.blockID || blockId == Block.glass.blockID || blockId == MineFactoryReloadedCore.factoryGlassPaneBlock.blockID ||
				(blockId == Block.thinGlass.blockID && MineFactoryReloadedCore.vanillaOverrideGlassPane.getBoolean(true));
	}
	
	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z)
	{
		float xStart = 0.4375F;
		float zStart = 0.5625F;
		float xStop = 0.4375F;
		float zStop = 0.5625F;
		boolean connectedNorth = this.canThisFactoryPaneConnectToThisBlockID(world.getBlockId(x, y, z - 1));
		boolean connectedSouth = this.canThisFactoryPaneConnectToThisBlockID(world.getBlockId(x, y, z + 1));
		boolean connectedWest = this.canThisFactoryPaneConnectToThisBlockID(world.getBlockId(x - 1, y, z));
		boolean connectedEast = this.canThisFactoryPaneConnectToThisBlockID(world.getBlockId(x + 1, y, z));

		if ((!connectedWest || !connectedEast) && (connectedWest || connectedEast || connectedNorth || connectedSouth))
		{
			if (connectedWest && !connectedEast)
			{
				xStart = 0.0F;
			}
			else if (!connectedWest && connectedEast)
			{
				zStart = 1.0F;
			}
		}
		else
		{
			xStart = 0.0F;
			zStart = 1.0F;
		}

		if ((!connectedNorth || !connectedSouth) && (connectedWest || connectedEast || connectedNorth || connectedSouth))
		{
			if (connectedNorth && !connectedSouth)
			{
				xStop = 0.0F;
			}
			else if (!connectedNorth && connectedSouth)
			{
				zStop = 1.0F;
			}
		}
		else
		{
			xStop = 0.0F;
			zStop = 1.0F;
		}

		this.setBlockBounds(xStart, 0.0F, xStop, zStart, 1.0F, zStop);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public void addCollidingBlockToList(World world, int x, int y, int z, AxisAlignedBB aabb, List blockList, Entity e)
	{
		boolean connectedNorth = this.canThisFactoryPaneConnectToThisBlockID(world.getBlockId(x, y, z - 1));
		boolean connectedSouth = this.canThisFactoryPaneConnectToThisBlockID(world.getBlockId(x, y, z + 1));
		boolean connectedWest = this.canThisFactoryPaneConnectToThisBlockID(world.getBlockId(x - 1, y, z));
		boolean connectedEast = this.canThisFactoryPaneConnectToThisBlockID(world.getBlockId(x + 1, y, z));

		if ((!connectedWest || !connectedEast) && (connectedWest || connectedEast || connectedNorth || connectedSouth))
		{
			if (connectedWest && !connectedEast)
			{
				this.setBlockBounds(0.0F, 0.0F, 0.4375F, 0.5F, 1.0F, 0.5625F);
				addCollidingBlockToList_do(world, x, y, z, aabb, blockList, e);
			}
			else if (!connectedWest && connectedEast)
			{
				this.setBlockBounds(0.5F, 0.0F, 0.4375F, 1.0F, 1.0F, 0.5625F);
				addCollidingBlockToList_do(world, x, y, z, aabb, blockList, e);
			}
		}
		else
		{
			this.setBlockBounds(0.0F, 0.0F, 0.4375F, 1.0F, 1.0F, 0.5625F);
			addCollidingBlockToList_do(world, x, y, z, aabb, blockList, e);
		}

		if ((!connectedNorth || !connectedSouth) && (connectedWest || connectedEast || connectedNorth || connectedSouth))
		{
			if (connectedNorth && !connectedSouth)
			{
				this.setBlockBounds(0.4375F, 0.0F, 0.0F, 0.5625F, 1.0F, 0.5F);
				addCollidingBlockToList_do(world, x, y, z, aabb, blockList, e);
			}
			else if (!connectedNorth && connectedSouth)
			{
				this.setBlockBounds(0.4375F, 0.0F, 0.5F, 0.5625F, 1.0F, 1.0F);
				addCollidingBlockToList_do(world, x, y, z, aabb, blockList, e);
			}
		}
		else
		{
			this.setBlockBounds(0.4375F, 0.0F, 0.0F, 0.5625F, 1.0F, 1.0F);
			addCollidingBlockToList_do(world, x, y, z, aabb, blockList, e);
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void addCollidingBlockToList_do(World world, int x, int y, int z, AxisAlignedBB aabb, List blockList, Entity e)
	{
		AxisAlignedBB newAABB = this.getCollisionBoundingBoxFromPool(world, x, y, z);
	
		if (newAABB != null && aabb.intersectsWith(newAABB))
		{
			blockList.add(newAABB);
		}
	}

	@Override
	public int getRenderType()
	{
		return MineFactoryReloadedCore.renderIdFactoryGlassPane;
	}
	
	@Override
	public String getTextureFile()
	{
		return MineFactoryReloadedCore.terrainTexture;
	}
}
