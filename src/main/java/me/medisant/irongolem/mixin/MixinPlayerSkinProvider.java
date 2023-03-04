package me.medisant.irongolem.mixin;

import com.google.common.hash.Hashing;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.properties.Property;
import me.medisant.irongolem.IronGolem;
import me.medisant.irongolem.config.ModConfig;
import me.medisant.irongolem.skinchecker.SkinChecker;
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
        MinecraftProfileTexture profileTexture = cir.getReturnValue().get(MinecraftProfileTexture.Type.SKIN);
        if (profileTexture == null) return;
        String hash = profileTexture.getHash();
        IronGolem ironGolem = IronGolem.getInstance();

        if (!ironGolem.getCheckedHashes().contains(hash)) {
            ironGolem.getCheckedHashes().add(hash);

            String string = Hashing.sha1().hashUnencodedChars(profileTexture.getHash()).toString();
            File file = new File(skinCacheDir, string.length() > 2 ? string.substring(0, 2) : "xx");
            File file2 = new File(file, string);

            SkinChecker skinChecker = ironGolem.getSkinChecker();
            skinChecker.getExecutorService().execute(() -> skinChecker.checkSkinWithModels(
                    file2,
                    profile,
                    result -> {
                        // TODO
                        if (result.hasPositiveMatch() && ModConfig.instance.logMatches) ironGolem.logSkinCheckerResult(result.toLogString());
                    }
            ));

        }

    }


}
