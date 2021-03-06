package net.insomniakitten.bamboo.block;

import com.google.common.collect.ImmutableSet;
import net.insomniakitten.bamboo.Bamboozled;
import net.insomniakitten.bamboo.init.BamboozledItems;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;

public final class BlockSaltOre extends Block {
    private static final MethodHandle DONT_SET_BLOCK_FIELD;

    static {
        final Field target = BlockSaltOre.findDontSetBlockField();
        final MethodHandles.Lookup lookup = MethodHandles.lookup();

        target.setAccessible(true);

        try {
            DONT_SET_BLOCK_FIELD = lookup.unreflectGetter(target);
        } catch (final IllegalAccessException exception) {
            throw new RuntimeException("lookup.unreflectGetter(target)", exception);
        }
    }

    public BlockSaltOre() {
        super(Material.ROCK, MapColor.SNOW);
        this.setSoundType(SoundType.STONE);
        this.setHardness(1.5F);
        this.setResistance(17.5F);
        this.setLightOpacity(1);
    }

    private static Field findDontSetBlockField() {
        final Class<EntityFallingBlock> clazz = EntityFallingBlock.class;
        final Set<String> names = ImmutableSet.of("field_145808_f", "dontSetBlock");

        for (final Field field : clazz.getDeclaredFields()) {
            if (names.contains(field.getName())) {
                return field;
            }
        }

        throw new NoSuchElementException("Field " + names + " in " + clazz);
    }

    @Override
    @Deprecated
    public boolean isOpaqueCube(final IBlockState state) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return this.isFancy() ? BlockRenderLayer.TRANSLUCENT : BlockRenderLayer.SOLID;
    }

    @Override
    public void onLanded(final World world, final Entity entity) {
        final double motion = entity.motionY;

        super.onLanded(world, entity);

        if (!entity.onGround || motion >= 0) {
            return;
        }
        if (!(entity instanceof EntityFallingBlock)) {
            return;
        }

        final BlockPos position = new BlockPos(entity);
        final EntityFallingBlock fallingBlock = (EntityFallingBlock) entity;

        if (this.isDontSetBlock(fallingBlock)) {
            return;
        }

        @Nullable final IBlockState state = fallingBlock.getBlock();

        if (state == null) {
            return;
        }

        final Block block = state.getBlock();

        if (Blocks.ANVIL != block) {
            return;
        }

        if (!world.mayPlace(block, position, true, EnumFacing.UP, null)) {
            return;
        }

        world.destroyBlock(position.down(), false);

        final int amount = 4 + world.rand.nextInt(5);
        final ItemStack stack = new ItemStack(BamboozledItems.SALT_PILE, amount);

        Block.spawnAsEntity(world, position.down(), stack);
    }

    @Override
    public boolean doesSideBlockRendering(final IBlockState state, final IBlockAccess access, final BlockPos position, final EnumFacing face) {
        if (!this.isFancy()) {
            return true;
        }

        final BlockPos offset = position.offset(face);
        final IBlockState other = access.getBlockState(offset);

        return this == other.getBlock();
    }

    @Override
    public void getDrops(final NonNullList<ItemStack> drops, final IBlockAccess access, final BlockPos position, final IBlockState state, final int fortune) {
        final Random random = access instanceof World ? ((World) access).rand : new Random();
        final int amount = 4 + random.nextInt(5) * (Math.max(0, random.nextInt(fortune + 2) - 1) + 1);

        drops.add(new ItemStack(BamboozledItems.SALT_PILE, amount));
    }

    private boolean isFancy() {
        return Bamboozled.getClientConfig().forceFancySaltOre || !Blocks.LEAVES.getDefaultState().isOpaqueCube();
    }

    private boolean isDontSetBlock(final EntityFallingBlock entity) {
        try {
            return (boolean) BlockSaltOre.DONT_SET_BLOCK_FIELD.invokeExact(entity);
        } catch (final Throwable throwable) {
            throw new RuntimeException("BlockSaltOre.DONT_SET_BLOCK_FIELD.invokeExact(entity)", throwable);
        }
    }
}
