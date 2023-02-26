package me.medisant.irongolem.mixin;

import com.google.common.hash.Hashing;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.properties.Property;
import me.medisant.irongolem.IronGolem;
import net.minecraft.client.texture.PlayerSkinProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.File;
import java.util.Map;

@Mixin(PlayerSkinProvider.class)
public class MixinPlayerSkinProvider {

    @Shadow
    @Final
    private File skinCacheDir;

    @Inject(
            method = "getTextures",
            at = @At("TAIL"),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void getTextures(GameProfile profile, CallbackInfoReturnable<Map<MinecraftProfileTexture.Type, MinecraftProfileTexture>> cir, Property property) {
        if (!IronGolem.getInstance().getCheckedSkins().containsKey(profile.getId())) {
            MinecraftProfileTexture profileTexture = cir.getReturnValue().get(MinecraftProfileTexture.Type.SKIN);
            if (profileTexture != null) {
                String string = Hashing.sha1().hashUnencodedChars(profileTexture.getHash()).toString();
                File file = new File(skinCacheDir, string.length() > 2 ? string.substring(0, 2) : "xx");
                File file2 = new File(file, string);

                IronGolem.getInstance().getSkinChecker().checkSkinAsynchronous(file2, profile);
            }

        }

    }


}
