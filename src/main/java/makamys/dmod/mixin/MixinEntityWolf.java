package makamys.dmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import makamys.dmod.EntityFox;

import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.ai.EntityAITargetNonTamed;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.world.World;

@Mixin(EntityWolf.class)
public abstract class MixinEntityWolf extends EntityTameable {
	
	public MixinEntityWolf(World p_i1604_1_) {
		super(p_i1604_1_);
	}
	
	@Inject(method = "<init>*", at = @At("RETURN"))
	public void postConstructed(CallbackInfo ci) {
		this.targetTasks.addTask(targetTasks.taskEntries.size(), new EntityAITargetNonTamed((EntityWolf)(Object)this, EntityFox.class, 200, false));
	}
}
