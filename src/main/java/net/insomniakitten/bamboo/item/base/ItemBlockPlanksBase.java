package net.insomniakitten.bamboo.item.base;

import lombok.experimental.var;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public class ItemBlockPlanksBase extends ItemSubBlockBase {
    public ItemBlockPlanksBase(Block block) {
        super(block);
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        var name = getTranslationKey();
        if (stack.getMetadata() > 0) {
            name += "_vertical";
        }
        return name;
    }
}
