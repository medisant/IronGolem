package me.medisant.irongolem.skinchecker;

import com.mojang.authlib.GameProfile;
import me.medisant.irongolem.IronGolem;
import me.medisant.irongolem.opencv.OpenCVUtils;
import net.minecraft.client.MinecraftClient;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SkinChecker {

    private final IronGolem ironGolem;
    private final ExecutorService executorService;

    public SkinChecker(IronGolem ironGolem) {
        this.ironGolem = ironGolem;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * This method should under no circumstances be run synchronous.
     * @param toCheck The file to check
     * @param profile The information about the player
     * @param callback what should happen after the results have been calculated
     */
    public void checkSkinWithModels(File toCheck, GameProfile profile, CheckResultReadyCallback callback) {
        if (profile.getName() == null) MinecraftClient.getInstance().getSessionService().fillProfileProperties(profile, false);

        SkinCheckerResult skinCheckerResult = new SkinCheckerResult(toCheck, profile);

        for (Map.Entry<String, Mat> entry : ironGolem.getModels().entrySet()) {
            skinCheckerResult.getChecks().add(this.checkSkin(skinCheckerResult, entry.getKey(), entry.getValue()));
        }

        callback.onCheckResultReady(skinCheckerResult);
    }

    private SkinCheckerResult.Check checkSkin(SkinCheckerResult skinCheckerResult, String modelPath, Mat model) {
        Mat img = Imgcodecs.imread(skinCheckerResult.getFile().getPath());

        return new SkinCheckerResult.Check(
                new File(modelPath),
                OpenCVUtils.getPSNR(img, model),
                OpenCVUtils.getMSSIM(img, model),
                OpenCVUtils.compareHistograms(img, model)
        );
    }

    public interface CheckResultReadyCallback {
        void onCheckResultReady(SkinCheckerResult result);
    }

}
